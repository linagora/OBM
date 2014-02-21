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

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.obm.push.configuration.LoggerModule;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class MigrationServiceImpl implements MigrationService {

	private final Logger logger;
	
	private final StoreManager objectStoreManager;
	private final MigrationSourceObjectStoreManager objectStoreManagerMigration;
	private final ImmutableMap<AbstractEhcacheDaoMigration, Cache> migrationCaches;

	@Inject
	@VisibleForTesting MigrationServiceImpl(
			@Named(LoggerModule.MIGRATION)Logger logger,
			StoreManager objectStoreManager,
			MigrationSourceObjectStoreManager objectStoreManagerMigration,
			MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl, MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl,
			SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl, SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl,
			SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl, SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl,
			SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl, SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl,
			UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl, UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl,
			WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl, WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl, WindowingDaoEhcacheImpl windowingDaoEhcacheImpl) {
		this.logger = logger;
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
			logger.warn("=========================================");
			logger.warn("================== START ================");
			logger.warn("=========================================");
			migrateCaches();
			cleanOriginalCaches();
			logger.warn("=========================================");
			logger.warn("============= END SUCCESSFULLY ==========");
			logger.warn("=========================================");
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
		try {
			for(Entry<AbstractEhcacheDaoMigration, Cache> caches : migrationCaches.entrySet()) {
				migrateCache(caches.getKey(), caches.getValue());
				caches.getValue().dispose();
			}
			logger.warn("Target cache manager shutdown");
			objectStoreManager.shutdown();
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	protected List<Object> getKeys(AbstractEhcacheDaoMigration cacheToReadFrom) {
		return cacheToReadFrom.getKeys();
	}

	private void cleanOriginalCaches() {
		logger.warn("Source cache manager shutdown");
		objectStoreManagerMigration.shutdown();
		logger.warn("Removing source migration files");
		for (AbstractEhcacheDaoMigration migrationCache : migrationCaches.keySet()) {
			logger.warn("Removing {}", migrationCache.destroyMigrationData());
		}
		logger.warn("Removing source migration files - DONE");
	}

	@VisibleForTesting void migrateCache(AbstractEhcacheDaoMigration cacheToReadFrom, Cache cacheToWriteTo) {
		List<Object> keys = getKeys(cacheToReadFrom);
		logStart(cacheToReadFrom.getStoreName(), keys.size());
		migrateCacheWithKeys(cacheToReadFrom, cacheToWriteTo, keys);
		assertMigrationHasSucceedOrDie(cacheToWriteTo, keys);
		logEnd(cacheToWriteTo.getName());
	}

	private void assertMigrationHasSucceedOrDie(Cache cacheToWriteTo, List<Object> keys) {
		assertMigrationHasSucceedOrDie(cacheToWriteTo.getName(), cacheToWriteTo.getKeys(), keys);
	}

	@VisibleForTesting void assertMigrationHasSucceedOrDie(String targetName, List<Object> targetKeyList, List<Object> sourceKeyList) {
		if (targetKeyList.size() != sourceKeyList.size()) {
			logger.error(
					"Migration failed for the cache [{}], keys to migrate [{}] done [{}]. " +
					"See the documentation at http://obm.org/wiki/migration for details", 
					targetName, sourceKeyList.size(), targetKeyList.size());
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
		logger.warn("Starting {}, number of keys: {}", cacheName, size);
	}
	
	private void logEnd(String cacheName) {
		logger.warn("Done {}", cacheName);
	}

	private final static class CacheEventListenerAdapterExtension extends CacheEventListenerAdapter {
		
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
