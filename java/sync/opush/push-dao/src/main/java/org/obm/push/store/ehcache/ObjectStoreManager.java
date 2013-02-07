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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.module.LoggerModule;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ObjectStoreManager {

	public static final String MONITORED_COLLECTION_STORE = "monitoredCollectionService";
	public static final String SYNCED_COLLECTION_STORE = "syncedCollectionStoreService";
	public static final String UNSYNCHRONIZED_ITEM_STORE = "unsynchronizedItemService";
	public static final String MAIL_SNAPSHOT_STORE = "mailSnapshotStore";
	public static final String MAIL_WINDOWING_INDEX_STORE = "mailWindowingIndexStore";
	public static final String MAIL_WINDOWING_CHUNKS_STORE = "mailWindowingChunksStore";
	public static final String SYNC_KEYS_STORE = "syncKeysStore";
	public static final String PENDING_CONTINUATIONS = "pendingContinuation";
	
	private final static int UNLIMITED_CACHE_MEMORY = 0;
	private final static int MAX_ELEMENT_IN_MEMORY = 5000;
	private final CacheManager singletonManager;

	@Inject ObjectStoreManager(ConfigurationService configurationService,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		int transactionTimeoutInSeconds = configurationService.transactionTimeoutInSeconds();
		boolean usePersistentCache = configurationService.usePersistentCache();
		String dataDirectory = configurationService.getDataDirectory();
		configurationLogger.info("EhCache transaction timeout in seconds : {}", transactionTimeoutInSeconds);
		configurationLogger.info("EhCache transaction persistent mode : {}", usePersistentCache);
		configurationLogger.info("EhCache data directory : {}", dataDirectory);
		this.singletonManager = new CacheManager(ehCacheConfiguration(transactionTimeoutInSeconds, usePersistentCache, dataDirectory));
	}

	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(int transactionTimeoutInSeconds, boolean usePersistentCache, String dataDirectory) {
		return new Configuration()
			.diskStore(new DiskStoreConfiguration().path(dataDirectory))
			.updateCheck(false)
			.cache(eternal(defaultCacheConfiguration().name(UNSYNCHRONIZED_ITEM_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(SYNCED_COLLECTION_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(MONITORED_COLLECTION_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(MAIL_SNAPSHOT_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(MAIL_WINDOWING_CHUNKS_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(MAIL_WINDOWING_INDEX_STORE), usePersistentCache))
			.cache(eternal(defaultCacheConfiguration().name(SYNC_KEYS_STORE), usePersistentCache))
			.cache(pendingContinuationConfiguration().name(PENDING_CONTINUATIONS))
			.defaultTransactionTimeoutInSeconds(transactionTimeoutInSeconds);
	}
	
	private CacheConfiguration pendingContinuationConfiguration() {
		return new CacheConfiguration()
			.maxElementsInMemory(UNLIMITED_CACHE_MEMORY)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false);
	}
	
	private CacheConfiguration defaultCacheConfiguration() {
		return new CacheConfiguration()
			.maxElementsInMemory(1000)
			.maxElementsOnDisk(100000)
			.overflowToDisk(true)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.XA);
	}
	
	private CacheConfiguration eternal(CacheConfiguration configuration, boolean eternal) {
		return configuration.eternal(eternal).diskPersistent(eternal);
	}
	
	public Cache createNewStore(String storeName) {
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
		return new CacheConfiguration(storeName, MAX_ELEMENT_IN_MEMORY);
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
