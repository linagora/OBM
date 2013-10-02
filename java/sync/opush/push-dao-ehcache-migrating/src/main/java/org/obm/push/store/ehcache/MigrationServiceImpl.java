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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MigrationServiceImpl implements MigrationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl;
	private final MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl;
	private final SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl;
	private final SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl;
	private final SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl;
	private final SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl;
	private final SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl;
	private final SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl;
	private final UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl;
	private final UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl;
	private final WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl;
	private final WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl;
	private final WindowingDaoEhcacheImpl windowingDaoEhcacheImpl;

	private final StoreManager objectStoreManager;
	private final ObjectStoreManagerMigration objectStoreManagerMigration;

	private final ImmutableList<AbstractEhcacheDaoMigration> migrationCaches;

	@Inject
	@VisibleForTesting MigrationServiceImpl(
			StoreManager objectStoreManager,
			ObjectStoreManagerMigration objectStoreManagerMigration,
			MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl, MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl,
			SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl, SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl,
			SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl, SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl,
			SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl, SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl,
			UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl, UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl,
			WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl, WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl, WindowingDaoEhcacheImpl windowingDaoEhcacheImpl) {
		this.objectStoreManager = objectStoreManager;
		this.objectStoreManagerMigration = objectStoreManagerMigration;
		this.monitoredCollectionDaoEhcacheMigrationImpl = monitoredCollectionDaoEhcacheMigrationImpl;
		this.monitoredCollectionDaoEhcacheImpl = monitoredCollectionDaoEhcacheImpl;
		this.snapshotDaoEhcacheMigrationImpl = snapshotDaoEhcacheMigrationImpl;
		this.snapshotDaoEhcacheImpl = snapshotDaoEhcacheImpl;
		this.syncedCollectionDaoEhcacheMigrationImpl = syncedCollectionDaoEhcacheMigrationImpl;
		this.syncedCollectionDaoEhcacheImpl = syncedCollectionDaoEhcacheImpl;
		this.syncKeysDaoEhcacheMigrationImpl = syncKeysDaoEhcacheMigrationImpl;
		this.syncKeysDaoEhcacheImpl = syncKeysDaoEhcacheImpl;
		this.unsynchronizedItemDaoEhcacheMigrationImpl = unsynchronizedItemDaoEhcacheMigrationImpl;
		this.unsynchronizedItemDaoEhcacheImpl = unsynchronizedItemDaoEhcacheImpl;
		this.windowingDaoChunkEhcacheMigrationImpl = windowingDaoChunkEhcacheMigrationImpl;
		this.windowingDaoIndexEhcacheMigrationImpl = windowingDaoIndexEhcacheMigrationImpl;
		this.windowingDaoEhcacheImpl = windowingDaoEhcacheImpl;
		
		migrationCaches = ImmutableList.of(
				monitoredCollectionDaoEhcacheMigrationImpl,
				snapshotDaoEhcacheMigrationImpl,
				syncedCollectionDaoEhcacheMigrationImpl,
				syncKeysDaoEhcacheMigrationImpl,
				unsynchronizedItemDaoEhcacheMigrationImpl,
				windowingDaoChunkEhcacheMigrationImpl,
				windowingDaoIndexEhcacheMigrationImpl);
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
		return Iterables.any(migrationCaches, new Predicate<AbstractEhcacheDaoMigration>() {

				@Override
				public boolean apply(AbstractEhcacheDaoMigration input) {
					return input.hasElementToMigrate();
				}
		});
	}

	private void migrateCaches() {
		logger.warn("EHCACHE MIGRATION - START MIGRATION");
		migrateMonitoredCollection();
		migrateSnashot();
		migrateSyncedCollection();
		migrateSyncKeys();
		migrateUnsynchronizedItem();
		migrateWindowingChunk();
		migrateWindowingIndex();
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
		for (AbstractEhcacheDaoMigration migrationCache : migrationCaches) {
			logger.warn("EHCACHE MIGRATION - Deleting migration data : " + migrationCache.destroyMigrationData());
		}
		logger.warn("EHCACHE MIGRATION - END REMOVING ORGINAL CACHES");
	}

	@VisibleForTesting void migrateMonitoredCollection() {
		List<Object> keys = getKeys(monitoredCollectionDaoEhcacheMigrationImpl);
		logStart("MonitoredCollection", keys.size());
		
		for (Object key : keys) {
			Serializable value = monitoredCollectionDaoEhcacheMigrationImpl.get(key).getValue();
			monitoredCollectionDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("MonitoredCollection");
	}

	@VisibleForTesting void migrateSnashot() {
		List<Object> keys = getKeys(snapshotDaoEhcacheMigrationImpl);
		logStart("Snashot", keys.size());

		for (Object key : keys) {
			Serializable value = snapshotDaoEhcacheMigrationImpl.get(key).getValue();
			snapshotDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("Snapshot");
	}

	@VisibleForTesting void migrateSyncedCollection() {
		List<Object> keys = getKeys(syncedCollectionDaoEhcacheMigrationImpl);
		logStart("SyncedCollection", keys.size());

		for (Object key : keys) {
			Serializable value = syncedCollectionDaoEhcacheMigrationImpl.get(key).getValue();
			syncedCollectionDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("SyncedCollection");
	}

	@VisibleForTesting void migrateSyncKeys() {
		List<Object> keys = getKeys(syncKeysDaoEhcacheMigrationImpl);
		logStart("SyncKeys", keys.size());

		for (Object key : keys) {
			Serializable value = syncKeysDaoEhcacheMigrationImpl.get(key).getValue();
			syncKeysDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("SyncKeys");
	}

	@VisibleForTesting void migrateUnsynchronizedItem() {
		List<Object> keys = getKeys(unsynchronizedItemDaoEhcacheMigrationImpl);
		logStart("UnsynchronizedItem", keys.size());

		for (Object key : keys) {
			Serializable value = unsynchronizedItemDaoEhcacheMigrationImpl.get(key).getValue();
			unsynchronizedItemDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("UnsynchronizedItem");
	}

	@VisibleForTesting void migrateWindowingChunk() {
		List<Object> keys = getKeys(windowingDaoChunkEhcacheMigrationImpl);
		logStart("WindowingChunk", keys.size());

		for (Object key : keys) {
			Serializable value = windowingDaoChunkEhcacheMigrationImpl.get(key).getValue();
			windowingDaoEhcacheImpl.getChunksStore().put(new net.sf.ehcache.Element(key, value));
		}

		logEnd("WindowingChunk");
	}

	@VisibleForTesting void migrateWindowingIndex() {
		List<Object> keys = getKeys(windowingDaoIndexEhcacheMigrationImpl);
		logStart("WindowingIndex", keys.size());

		for (Object key : keys) {
			Serializable value = windowingDaoIndexEhcacheMigrationImpl.get(key).getValue();
			windowingDaoEhcacheImpl.getIndexStore().put(new net.sf.ehcache.Element(key, value));
		}
		
		logEnd("WindowingIndex");
	}
	
	private void logStart(String cacheName, int size) {
		logger.warn("Starting migration of {}, number of keys: {}", cacheName, size);
	}
	
	private void logEnd(String cacheName) {
		logger.warn("Migration of {} done", cacheName);
	}
}
