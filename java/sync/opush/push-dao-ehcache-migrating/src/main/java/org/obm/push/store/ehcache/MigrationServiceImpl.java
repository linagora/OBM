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

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MigrationServiceImpl implements MigrationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final StoreManager objectStoreManager;
	private final MigrationSourceObjectStoreManager objectStoreManagerMigration;
	private final ImmutableMap<AbstractEhcacheDaoMigration, Cache> migrationCaches;

	@Inject
	@VisibleForTesting MigrationServiceImpl(
			StoreManager objectStoreManager,
			MigrationSourceObjectStoreManager objectStoreManagerMigration,
			MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl, MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl,
			SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl, SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl,
			SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl, SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl,
			SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl, SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl,
			UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl, UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl,
			WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl, WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl, WindowingDaoEhcacheImpl windowingDaoEhcacheImpl) {
		this.objectStoreManager = objectStoreManager;
		this.objectStoreManagerMigration = objectStoreManagerMigration;
		
		migrationCaches = ImmutableMap.<AbstractEhcacheDaoMigration, Cache>builder()
				.put(monitoredCollectionDaoEhcacheMigrationImpl, monitoredCollectionDaoEhcacheImpl.getStore())
				.put(snapshotDaoEhcacheMigrationImpl, snapshotDaoEhcacheImpl.getStore())
				.put(syncedCollectionDaoEhcacheMigrationImpl, syncedCollectionDaoEhcacheImpl.getStore())
				.put(syncKeysDaoEhcacheMigrationImpl, syncKeysDaoEhcacheImpl.getStore())
				.put(unsynchronizedItemDaoEhcacheMigrationImpl, unsynchronizedItemDaoEhcacheImpl.getStore())
				.put(windowingDaoChunkEhcacheMigrationImpl, windowingDaoEhcacheImpl.getChunksStore())
				.put(windowingDaoIndexEhcacheMigrationImpl, windowingDaoEhcacheImpl.getIndexStore())
				.build();
	}
	
	@Override
	public void migrate() {
		if (needMigration()) {
			logger.warn("EHCACHE MIGRATION - START");
			migrateCaches();
			cleanOriginalCaches();
			logger.warn("EHCACHE MIGRATION - END SUCCESSFULY");
		} else {
			logger.warn("EHCACHE MIGRATION - NO MIGRATION");
		}
	}

	private boolean needMigration() {
		return Iterables.any(migrationCaches.keySet(), new Predicate<AbstractEhcacheDaoMigration>() {

				@Override
				public boolean apply(AbstractEhcacheDaoMigration input) {
					return input.hasElementToMigrate();
				}
		});
	}

	private void migrateCaches() {
		logger.warn("EHCACHE MIGRATION - START MIGRATION");
		for(Entry<AbstractEhcacheDaoMigration, Cache> caches : migrationCaches.entrySet()) {
			migrateCache(caches.getKey(), caches.getValue());
			caches.getValue().dispose();
		}
		logger.warn("EHCACHE MIGRATION - SHUTDOWN CACHE MANAGER");
		objectStoreManager.shutdown();
		logger.warn("EHCACHE MIGRATION - END MIGRATION");
	}

	protected List<Object> getKeys(AbstractEhcacheDaoMigration cacheToReadFrom) {
		return cacheToReadFrom.getKeys();
	}

	private void cleanOriginalCaches() {
		logger.warn("EHCACHE MIGRATION - START REMOVING ORGINAL CACHES");
		logger.warn("EHCACHE MIGRATION - SHUTDOWN MIGRATION CACHE MANAGER");
		objectStoreManagerMigration.shutdown();
		for (AbstractEhcacheDaoMigration migrationCache : migrationCaches.keySet()) {
			logger.warn("EHCACHE MIGRATION - Deleting migration data : " + migrationCache.destroyMigrationData());
		}
		logger.warn("EHCACHE MIGRATION - END REMOVING ORGINAL CACHES");
	}

	@VisibleForTesting void migrateCache(AbstractEhcacheDaoMigration cacheToReadFrom, Cache cacheToWriteTo) {
		List<Object> keys = getKeys(cacheToReadFrom);
		logStart(cacheToReadFrom.getStoreName(), keys.size());
		migrateCacheWithKeys(cacheToReadFrom, cacheToWriteTo, keys);
		assertMigrationHasSucceedOrDie(cacheToWriteTo, keys);
		logEnd(cacheToWriteTo.getName());
	}

	private void assertMigrationHasSucceedOrDie(Cache cacheToWriteTo, List<Object> keys) {
		if (cacheToWriteTo.getKeys().size() != keys.size()) {
			logger.error(
					"EHCACHE MIGRATION - Failed for the cache [{}], keys to migrate [{}] done [{}]. " +
					"Try to allow more memory to the cache into the configuration file {}", 
					cacheToWriteTo.getName(), keys.size(), cacheToWriteTo.getKeys().size(), 
					EhCacheConfigurationFileImpl.CONFIG_FILE_PATH);
			throw new IllegalStateException("Error during migration");
		}
	}

	private void migrateCacheWithKeys(AbstractEhcacheDaoMigration cacheToReadFrom, Cache cacheToWriteTo,
			List<Object> keys) {
		
		CacheEventListenerAdapterExtension listener = new CacheEventListenerAdapterExtension();
		cacheToWriteTo.getCacheEventNotificationService().registerListener(listener);
		
		for (Object key : keys) {
			Serializable value = cacheToReadFrom.get(key).getValue();
			cacheToWriteTo.put(new net.sf.ehcache.Element(key, value));
		}
		
		logger.warn("{}: items {} put {} evicted {} expired {} update {}", cacheToWriteTo.getName(), 
				keys.size(), listener.put, listener.evicted, listener.expired, listener.update);
	}
	
	private void logStart(String cacheName, int size) {
		logger.warn("EHCACHE MIGRATION - Starting {}, number of keys: {}", cacheName, size);
	}
	
	private void logEnd(String cacheName) {
		logger.warn("EHCACHE MIGRATION - Done {}", cacheName);
	}

	private final class CacheEventListenerAdapterExtension extends CacheEventListenerAdapter {
		
		public int put;
		public int update;
		public int evicted;
		public int expired;
		
		@Override
		public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
			put++;
		}
		
		@Override
		public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
			update++;
		}
		
		@Override
		public void notifyElementEvicted(Ehcache cache, Element element) {
			evicted++;
		}
		
		@Override
		public void notifyElementExpired(Ehcache cache, Element element) {
			expired++;
		}
	}
}
