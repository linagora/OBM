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

import static org.obm.push.store.ehcache.ObjectStoreManager.UNLIMITED_CACHE_MEMORY;
import static org.obm.push.store.ehcache.ObjectStoreManager.checkGlobalPercentage;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.obm.configuration.module.LoggerModule;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class MigrationTargetObjectStoreManager implements StoreManager, EhCacheStores {

	public static final String STORE_NAME = MigrationTargetObjectStoreManager.class.getName();

	private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;

	private final CacheManager singletonManager;
	private final EhCacheConfiguration ehCacheConfiguration;
	private final Map<String, Percentage> storesPercentage;
	private final Logger configurationLogger;

	@Inject MigrationTargetObjectStoreManager(
			OpushConfiguration opushConfiguration,
			EhCacheConfiguration ehCacheConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		this.ehCacheConfiguration = ehCacheConfiguration;
		this.configurationLogger = configurationLogger;
		String dataDirectory = opushConfiguration.getDataDirectory();
		configurationLogger.info("EhCache transaction mode : {}", TRANSACTIONAL_MODE);
		configurationLogger.info("EhCache data directory : {}", dataDirectory);
		configurationLogger.info("EhCache migration version in use");
		configurationLogger.info("EhCache maxBytesLocalHeap in MB : {}", ehCacheConfiguration.maxMemoryInMB());

		storesPercentage = checkGlobalPercentage(ehCacheConfiguration.percentageAllowedToCaches());
		this.singletonManager = new CacheManager(ehCacheConfiguration(dataDirectory));
	}

	@Override
	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(String dataDirectory) {
		Configuration configuration = new Configuration()
			.name(STORE_NAME)
			.maxBytesLocalHeap(ehCacheConfiguration.maxMemoryInMB(), MemoryUnit.MEGABYTES)
			.diskStore(new DiskStoreConfiguration().path(dataDirectory))
			.updateCheck(false);
		
		for (String name : STORES) {
			configuration.cache(cacheConfiguration(name));
		}
		return configuration;
	}

	@SuppressWarnings("deprecation")
	private CacheConfiguration cacheConfiguration(String name) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration()
			.name(name)
			.maxEntriesLocalDisk(UNLIMITED_CACHE_MEMORY)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
			.transactionalMode(TRANSACTIONAL_MODE)
			.overflowToDisk(true)
			.diskPersistent(true)
			.eternal(true);

		Percentage percentageAllowedToCache = storesPercentage.get(name);
		if (percentageAllowedToCache.isDefined()) {
			configurationLogger.info(percentageAllowedToCache.get() + " allocated for the cache:" + name);
			cacheConfiguration.setMaxBytesLocalHeap(percentageAllowedToCache.get());
		} else {
			configurationLogger.info("No space allocation is defined for the cache:" + name);
		}
		return cacheConfiguration;
	}

	@Override
	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}
}
