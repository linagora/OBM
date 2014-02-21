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

import java.io.File;
import java.io.FilenameFilter;

import net.sf.ehcache.migrating.Cache;
import net.sf.ehcache.migrating.CacheManager;
import net.sf.ehcache.migrating.config.CacheConfiguration;
import net.sf.ehcache.migrating.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.migrating.config.Configuration;
import net.sf.ehcache.migrating.config.DiskStoreConfiguration;
import net.sf.ehcache.migrating.store.MemoryStoreEvictionPolicy;

import org.obm.push.configuration.LoggerModule;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.sync.LifecycleListener;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class MigrationSourceObjectStoreManager implements LifecycleListener {

	private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;
	
	public static final String MONITORED_COLLECTION_STORE = "monitoredCollectionService";
	public static final String SYNCED_COLLECTION_STORE = "syncedCollectionStoreService";
	public static final String UNSYNCHRONIZED_ITEM_STORE = "unsynchronizedItemService";
	public static final String MAIL_SNAPSHOT_STORE = "mailSnapshotStore";
	public static final String MAIL_WINDOWING_INDEX_STORE = "mailWindowingIndexStore";
	public static final String MAIL_WINDOWING_CHUNKS_STORE = "mailWindowingChunksStore";
	public static final String SYNC_KEYS_STORE = "syncKeysStore";

	private static final int MAX_ENTRIES_IN_MEMORY = 1000;
	private final CacheManager singletonManager;

	@Inject MigrationSourceObjectStoreManager(OpushConfiguration opushConfiguration,
			@Named(LoggerModule.MIGRATION)Logger configurationLogger) {
		String dataDirectory = opushConfiguration.getDataDirectory();
		configurationLogger.info("EhCache migration transaction mode : {}", TRANSACTIONAL_MODE);
		configurationLogger.info("EhCache migration data directory : {}", dataDirectory);
		configurationLogger.info("EhCache migration unlimited version in use");
		
		touchIndexFiles(dataDirectory, configurationLogger);
		this.singletonManager = new CacheManager(ehCacheConfiguration(dataDirectory));
	}

	private void touchIndexFiles(String dataDirectory, Logger configurationLogger) {
		String[] files = new File(dataDirectory).list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".index");
			}
		});
		
		long now = System.currentTimeMillis();
		for (String fileName : files) {
			String fullName = dataDirectory + File.separator + fileName;
			configurationLogger.info("touch {}", fullName);
			if (!new File(fullName).setLastModified(now)) {
				configurationLogger.error("Couldn't touch {}", fullName);
			}
		}
	}

	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(String dataDirectory) {
		return new Configuration()
			.diskStore(new DiskStoreConfiguration().path(dataDirectory))
			.updateCheck(false)
			.cache(cacheConfiguration().name(UNSYNCHRONIZED_ITEM_STORE))
			.cache(cacheConfiguration().name(SYNCED_COLLECTION_STORE))
			.cache(cacheConfiguration().name(MONITORED_COLLECTION_STORE))
			.cache(cacheConfiguration().name(MAIL_SNAPSHOT_STORE))
			.cache(cacheConfiguration().name(MAIL_WINDOWING_CHUNKS_STORE))
			.cache(cacheConfiguration().name(MAIL_WINDOWING_INDEX_STORE))
			.cache(cacheConfiguration().name(SYNC_KEYS_STORE));
	}
	
	private CacheConfiguration cacheConfiguration() {
		return new CacheConfiguration()
			.maxElementsInMemory(MAX_ENTRIES_IN_MEMORY)
			.maxElementsOnDisk(ObjectStoreManager.UNLIMITED_CACHE_MEMORY)
			.overflowToDisk(true)
			.diskPersistent(true)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF);
	}
	
	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}
}
