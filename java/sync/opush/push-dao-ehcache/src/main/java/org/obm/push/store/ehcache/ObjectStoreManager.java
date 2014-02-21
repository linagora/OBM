/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.store.ehcache;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.statistics.extended.ExtendedStatistics.Operation;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.obm.annotations.transactional.TransactionProvider;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ObjectStoreManager implements StoreManager, EhCacheStores {

	public static final String STORE_NAME = ObjectStoreManager.class.getName();
	
	private final Map<String, Percentage> storesPercentage;
	
	public final static int UNLIMITED_CACHE_MEMORY = 0;
	
	@VisibleForTesting final CacheManager singletonManager;
	private final EhCacheConfiguration ehCacheConfiguration;

	private final Logger configurationLogger;

	@Inject ObjectStoreManager(
			OpushConfiguration opushConfiguration,
			EhCacheConfiguration ehCacheConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger,
			TransactionProvider transactionProvider) {
		this.ehCacheConfiguration = ehCacheConfiguration;
		this.configurationLogger = configurationLogger;
		int transactionTimeoutInSeconds = opushConfiguration.transactionTimeoutInSeconds();
		boolean usePersistentCache = opushConfiguration.usePersistentEhcacheStore();
		String dataDirectory = opushConfiguration.getDataDirectory();
		configurationLogger.info("EhCache transaction timeout in seconds : {}", transactionTimeoutInSeconds);
		configurationLogger.info("EhCache transaction persistent mode : {}", usePersistentCache);
		configurationLogger.info("EhCache data directory : {}", dataDirectory);
		configurationLogger.info("EhCache max local heap in MB: {}", ehCacheConfiguration.maxMemoryInMB());

		forceInitializeTransactionManager(transactionProvider);
		storesPercentage = checkGlobalPercentage(ehCacheConfiguration.percentageAllowedToCaches());
		this.singletonManager = new CacheManager(ehCacheConfiguration(transactionTimeoutInSeconds, usePersistentCache, dataDirectory));
		configureCachesStatistics(singletonManager);
	}

	public static Map<String, Percentage> checkGlobalPercentage(Map<String, Percentage> storesPercentage) {
		int globalPercentage = 0;
		for (Percentage percentage : storesPercentage.values()) {
			if (percentage.isDefined()) {
				globalPercentage += percentage.getIntValue();
			}
		}

		if (globalPercentage != 100) {
			throw new IllegalArgumentException("Global stores percentage must be equal to 100, got : " + globalPercentage);
		}
		return storesPercentage;
	}

	private void configureCachesStatistics(CacheManager singletonManager) {
		for (String cacheName : singletonManager.getCacheNames()) {
			StatisticsGateway stats = singletonManager.getCache(cacheName).getStatistics();
			stats.setStatisticsTimeToDisable(ehCacheConfiguration.statsSamplingTimeStopInMinutes(), TimeUnit.MINUTES);
			configureStatisticsHistory(stats.getExtended().diskGet());
			configureStatisticsHistory(stats.getExtended().diskPut());
			configureStatisticsHistory(stats.getExtended().diskRemove());
		}
	}

	private void forceInitializeTransactionManager(TransactionProvider transactionProvider) {
		transactionProvider.get();
	}
	
	private void configureStatisticsHistory(Operation<?> history) {
		history.setHistory(
				ehCacheConfiguration.statsSampleToRecordCount(), 
				EhCacheConfiguration.STATS_SAMPLING_IN_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(int transactionTimeoutInSeconds, boolean usePersistentCache, String dataDirectory) {
		Configuration configuration = new Configuration()
			.name(STORE_NAME)
			.maxBytesLocalHeap(ehCacheConfiguration.maxMemoryInMB(), MemoryUnit.MEGABYTES)
			.diskStore(new DiskStoreConfiguration().path(dataDirectory))
			.updateCheck(false)
			.defaultTransactionTimeoutInSeconds(transactionTimeoutInSeconds);
		
		for (String name : STORES) {
			configuration.cache(timeToLiveConfiguration(defaultCacheConfiguration(name), usePersistentCache));
		}
		return configuration;
	}

	@SuppressWarnings("deprecation")
	private CacheConfiguration defaultCacheConfiguration(String name) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration()
			.name(name)
			.maxEntriesLocalDisk(UNLIMITED_CACHE_MEMORY)
			.overflowToDisk(true)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
			.transactionalMode(ehCacheConfiguration.transactionalMode());
		
		Percentage percentageAllowedToCache = storesPercentage.get(name);
		if (percentageAllowedToCache.isDefined()) {
			configurationLogger.info(percentageAllowedToCache.get() + " allocated for the cache:" + name);
			cacheConfiguration.setMaxBytesLocalHeap(percentageAllowedToCache.get());
		} else {
			configurationLogger.info("No space allocation is defined for the cache:" + name);
		}
		return cacheConfiguration;
	}

	@SuppressWarnings("deprecation")
	private CacheConfiguration timeToLiveConfiguration(CacheConfiguration configuration, boolean usePersistentCache) {
		return configuration.timeToLiveSeconds(ehCacheConfiguration.timeToLiveInSeconds())
				.diskPersistent(usePersistentCache);
	}
	
	@VisibleForTesting Cache createNewStore(String storeName) {
		Cache store = getStore(storeName);
		if (store == null) {
			store = createStore(storeName);
			this.singletonManager.addCache(store);
		}
		return store;
	}

	private Cache createStore(String storeName) {
		return new Cache(createStoreConfiguration(storeName));
	}

	private CacheConfiguration createStoreConfiguration(String storeName) {
		return new CacheConfiguration().name(storeName);
	}

	@Override
	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}

	public void removeStore(String storeName) {
		this.singletonManager.removeCache(storeName);
	}

	public List<String> listStores() {
		return Arrays.asList(this.singletonManager.getCacheNames());
	}

}
