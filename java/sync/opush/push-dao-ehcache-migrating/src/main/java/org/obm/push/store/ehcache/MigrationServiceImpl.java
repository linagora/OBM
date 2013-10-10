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

import org.obm.annotations.transactional.Transactional;
import org.obm.push.bean.SyncKeysKey;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl.Key;
import org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl.Key_2_4_2_4;
import org.obm.push.store.ehcache.WindowingDaoEhcacheImpl.ChunkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
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

	@Inject
	@VisibleForTesting MigrationServiceImpl(
			MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl, MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl,
			SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl, SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl,
			SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl, SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl,
			SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl, SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl,
			UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl, UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl,
			WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl, WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl, WindowingDaoEhcacheImpl windowingDaoEhcacheImpl) {
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
	}
	
	@Override
	@Transactional
	public void migrate() {
		logger.warn("Starting EhCache migration");
		migrateMonitoredCollection();
		migrateSnashot();
		migrateSyncedCollection();
		migrateSyncKeys();
		migrateUnsynchronizedItem();
		migrateWindowingChunk();
		migrateWindowingIndex();
		logger.warn("End of EhCache migration");
	}

	@VisibleForTesting void migrateMonitoredCollection() {
		List<Object> keys = monitoredCollectionDaoEhcacheMigrationImpl.getKeys();
		logStart("MonitoredCollection", keys.size());
		
		for (Object keyObject : keys) {
			Key key = (Key) keyObject;
			Serializable value = monitoredCollectionDaoEhcacheMigrationImpl.get(key).getValue();
			monitoredCollectionDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
			
			monitoredCollectionDaoEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("MonitoredCollection");
	}

	@VisibleForTesting void migrateSnashot() {
		List<Object> keys = snapshotDaoEhcacheMigrationImpl.getKeys();
		logStart("Snashot", keys.size());
		
		for (Object keyObject : keys) {
			SnapshotKey key = (SnapshotKey) keyObject;
			Serializable value = snapshotDaoEhcacheMigrationImpl.get(key).getValue();
			snapshotDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
			
			snapshotDaoEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("Snashot");
	}

	@VisibleForTesting void migrateSyncedCollection() {
		List<Object> keys = syncedCollectionDaoEhcacheMigrationImpl.getKeys();
		logStart("SyncedCollection", keys.size());
		
		for (Object keyObject : keys) {
			SyncedCollectionDaoEhcacheImpl.Key key = (SyncedCollectionDaoEhcacheImpl.Key) keyObject;
			Serializable value = syncedCollectionDaoEhcacheMigrationImpl.get(key).getValue();
			syncedCollectionDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
			
			syncedCollectionDaoEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("SyncedCollection");
	}

	@VisibleForTesting void migrateSyncKeys() {
		List<Object> keys = syncKeysDaoEhcacheMigrationImpl.getKeys();
		logStart("SyncKeys", keys.size());
		
		for (Object keyObject : keys) {
			SyncKeysKey key = (SyncKeysKey) keyObject;
			Serializable value = syncKeysDaoEhcacheMigrationImpl.get(key).getValue();
			syncKeysDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
			
			syncKeysDaoEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("SyncKeys");
	}

	@VisibleForTesting void migrateUnsynchronizedItem() {
		List<Object> keys = unsynchronizedItemDaoEhcacheMigrationImpl.getKeys();
		logStart("UnsynchronizedItem", keys.size());
		
		for (Object keyObject : keys) {
			Key_2_4_2_4 key = (Key_2_4_2_4) keyObject;
			Serializable value = unsynchronizedItemDaoEhcacheMigrationImpl.get(key).getValue();
			unsynchronizedItemDaoEhcacheImpl.getStore().put(new net.sf.ehcache.Element(key, value));
			
			unsynchronizedItemDaoEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("UnsynchronizedItem");
	}

	@VisibleForTesting void migrateWindowingChunk() {
		List<Object> keys = windowingDaoChunkEhcacheMigrationImpl.getKeys();
		logStart("WindowingChunk", keys.size());
		
		for (Object keyObject : keys) {
			ChunkKey key = (ChunkKey) keyObject;
			Serializable value = windowingDaoChunkEhcacheMigrationImpl.get(key).getValue();
			windowingDaoEhcacheImpl.getChunksStore().put(new net.sf.ehcache.Element(key, value));
			
			windowingDaoChunkEhcacheMigrationImpl.remove(key);
		}
		
		logEnd("WindowingChunk");
	}

	@VisibleForTesting void migrateWindowingIndex() {
		List<Object> keys = windowingDaoIndexEhcacheMigrationImpl.getKeys();
		logStart("WindowingIndex", keys.size());
		
		for (Object keyObject : keys) {
			WindowingIndexKey key = (WindowingIndexKey) keyObject;
			Serializable value = windowingDaoIndexEhcacheMigrationImpl.get(key).getValue();
			windowingDaoEhcacheImpl.getIndexStore().put(new net.sf.ehcache.Element(key, value));
			
			windowingDaoIndexEhcacheMigrationImpl.remove(key);
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
