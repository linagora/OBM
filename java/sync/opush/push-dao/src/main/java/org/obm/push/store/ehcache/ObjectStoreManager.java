/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ObjectStoreManager {

	public static final String STORE_NAME = ObjectStoreManager.class.getName();
	
	public static final String MONITORED_COLLECTION_STORE = "monitoredCollectionService";
	public static final String SYNCED_COLLECTION_STORE = "syncedCollectionStoreService";
	public static final String UNSYNCHRONIZED_ITEM_STORE = "unsynchronizedItemService";
	public static final String MAIL_SNAPSHOT_STORE = "mailSnapshotStore";
	public static final String MAIL_WINDOWING_INDEX_STORE = "mailWindowingIndexStore";
	public static final String MAIL_WINDOWING_CHUNKS_STORE = "mailWindowingChunksStore";
	public static final String SYNC_KEYS_STORE = "syncKeysStore";
	public static final String PENDING_CONTINUATIONS = "pendingContinuation";
	
	private final static int UNLIMITED_CACHE_MEMORY = 0;
	
	@VisibleForTesting final CacheManager singletonManager;
	private final EhCacheConfiguration ehCacheConfiguration;

	private final Logger configurationLogger;

	@Inject ObjectStoreManager(
			ConfigurationService configurationService,
			EhCacheConfiguration ehCacheConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		this.ehCacheConfiguration = ehCacheConfiguration;
		this.configurationLogger = configurationLogger;
		int transactionTimeoutInSeconds = configurationService.transactionTimeoutInSeconds();
		boolean usePersistentCache = configurationService.usePersistentCache();
		String dataDirectory = configurationService.getDataDirectory();
		configurationLogger.info("EhCache transaction timeout in seconds : {}", transactionTimeoutInSeconds);
		configurationLogger.info("EhCache transaction persistent mode : {}", usePersistentCache);
		configurationLogger.info("EhCache data directory : {}", dataDirectory);
		configurationLogger.info("EhCache maxBytesLocalHeap : {}", ehCacheConfiguration.maxMemoryInMB());
		this.singletonManager = new CacheManager(ehCacheConfiguration(transactionTimeoutInSeconds, usePersistentCache, dataDirectory));
		configureCachesStatistics(singletonManager);
	}

	private void configureCachesStatistics(CacheManager singletonManager2) {
		for (String cacheName : singletonManager2.getCacheNames()) {
			StatisticsGateway stats = singletonManager2.getCache(cacheName).getStatistics();
			stats.setStatisticsTimeToDisable(ehCacheConfiguration.statsSamplingTimeStopInMinutes(), TimeUnit.MINUTES);
			stats.getExtended().diskGet().setHistory(
					ehCacheConfiguration.statsSampleToRecordCount(), 
					EhCacheConfiguration.STATS_SAMPLING_IN_SECONDS, TimeUnit.SECONDS);
		}
	}

	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(int transactionTimeoutInSeconds, boolean usePersistentCache, String dataDirectory) {
		return new Configuration()
			.name(STORE_NAME)
			.maxBytesLocalHeap(ehCacheConfiguration.maxMemoryInMB(), MemoryUnit.MEGABYTES)
			.diskStore(new DiskStoreConfiguration().path(dataDirectory))
			.updateCheck(false)
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(UNSYNCHRONIZED_ITEM_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(SYNCED_COLLECTION_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(MONITORED_COLLECTION_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(MAIL_SNAPSHOT_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(MAIL_WINDOWING_CHUNKS_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(MAIL_WINDOWING_INDEX_STORE), usePersistentCache))
			.cache(timeToLiveConfiguration(defaultCacheConfiguration(SYNC_KEYS_STORE), usePersistentCache))
			.cache(pendingContinuationConfiguration(PENDING_CONTINUATIONS))
			.defaultTransactionTimeoutInSeconds(transactionTimeoutInSeconds);
	}
	
	private CacheConfiguration pendingContinuationConfiguration(String name) {
		return new CacheConfiguration()
			.name(name)
			.maxEntriesLocalHeap(UNLIMITED_CACHE_MEMORY)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false);
	}

	@SuppressWarnings("deprecation")
	private CacheConfiguration defaultCacheConfiguration(String name) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration()
			.name(name)
			.maxEntriesLocalDisk(UNLIMITED_CACHE_MEMORY)
			.overflowToDisk(true)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(ehCacheConfiguration.transactionalMode());
		
		Percentage percentageAllowedToCache = ehCacheConfiguration.percentageAllowedToCache(name);
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
