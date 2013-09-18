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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.apache.commons.io.IOUtils;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.TransactionProvider;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class MigrationServiceImplTest extends StoreManagerConfigurationTest {

	private Logger logger;
	private ObjectStoreManagerMigration objectStoreManagerMigration;
	private ObjectStoreManager objectStoreManager;
	private BitronixTransactionManager transactionManager;
	private MigrationServiceImpl migrationServiceImpl;
	private MonitoredCollectionDaoEhcacheMigrationImpl monitoredCollectionDaoEhcacheMigrationImpl;
	private MonitoredCollectionDaoEhcacheImpl monitoredCollectionDaoEhcacheImpl;
	private SnapshotDaoEhcacheMigrationImpl snapshotDaoEhcacheMigrationImpl;
	private SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl;
	private SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl;
	private SyncedCollectionDaoEhcacheImpl syncedCollectionDaoEhcacheImpl;
	private SyncKeysDaoEhcacheMigrationImpl syncKeysDaoEhcacheMigrationImpl;
	private SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl;
	private UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl;
	private UnsynchronizedItemDaoEhcacheImpl unsynchronizedItemDaoEhcacheImpl;
	private WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl;
	private WindowingDaoIndexEhcacheMigrationImpl windowingDaoIndexEhcacheMigrationImpl;
	private WindowingDaoEhcacheImpl windowingDaoEhcacheImpl;

	public MigrationServiceImplTest() {
		super();
	}
	
	@Before
	public void init() throws Exception {
		logger = LoggerFactory.getLogger(getClass());
		ConfigurationService configurationService = initConfigurationServiceMock();
		EhCacheConfiguration config = buildConfig();
		
		IMocksControl control = createControl();
		TransactionProvider transactionProvider = control.createMock(TransactionProvider.class);
		transactionManager = TransactionManagerServices.getTransactionManager();
		expect(transactionProvider.get()).andReturn(transactionManager).anyTimes();
		control.replay();
		
		copyCacheFilesInTemporaryFolder();
		objectStoreManagerMigration = new ObjectStoreManagerMigration(configurationService, logger);
		objectStoreManager = new ObjectStoreManager(configurationService, config, logger, transactionProvider);
		
		monitoredCollectionDaoEhcacheMigrationImpl = new MonitoredCollectionDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		monitoredCollectionDaoEhcacheImpl = new MonitoredCollectionDaoEhcacheImpl(objectStoreManager);
		snapshotDaoEhcacheMigrationImpl = new SnapshotDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager);
		syncedCollectionDaoEhcacheMigrationImpl = new SyncedCollectionDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		syncedCollectionDaoEhcacheImpl = new SyncedCollectionDaoEhcacheImpl(objectStoreManager);
		syncKeysDaoEhcacheMigrationImpl = new SyncKeysDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		syncKeysDaoEhcacheImpl = new SyncKeysDaoEhcacheImpl(objectStoreManager);
		unsynchronizedItemDaoEhcacheMigrationImpl = new UnsynchronizedItemDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		unsynchronizedItemDaoEhcacheImpl = new UnsynchronizedItemDaoEhcacheImpl(objectStoreManager);
		windowingDaoChunkEhcacheMigrationImpl = new WindowingDaoChunkEhcacheMigrationImpl(objectStoreManagerMigration);
		windowingDaoIndexEhcacheMigrationImpl = new WindowingDaoIndexEhcacheMigrationImpl(objectStoreManagerMigration);
		windowingDaoEhcacheImpl = new WindowingDaoEhcacheImpl(objectStoreManager);
		transactionManager.begin();
		
		migrationServiceImpl = new MigrationServiceImpl(monitoredCollectionDaoEhcacheMigrationImpl, monitoredCollectionDaoEhcacheImpl,
				snapshotDaoEhcacheMigrationImpl, snapshotDaoEhcacheImpl,
				syncedCollectionDaoEhcacheMigrationImpl, syncedCollectionDaoEhcacheImpl,
				syncKeysDaoEhcacheMigrationImpl, syncKeysDaoEhcacheImpl,
				unsynchronizedItemDaoEhcacheMigrationImpl, unsynchronizedItemDaoEhcacheImpl,
				windowingDaoChunkEhcacheMigrationImpl, windowingDaoIndexEhcacheMigrationImpl, windowingDaoEhcacheImpl);
	}
	
	private EhCacheConfiguration buildConfig() {
		return new EhCacheConfiguration() {

			@Override
			public int maxMemoryInMB() {
				return 10;
			}
	
			@Override
			public Percentage percentageAllowedToCache(String cacheName) {
				return Percentage.UNDEFINED;
			}
	
			@Override
			public long timeToLiveInSeconds() {
				return 60;
			}

			@Override
			public TransactionalMode transactionalMode() {
				return TransactionalMode.XA;
			}
		};
	}
	
	private void copyCacheFilesInTemporaryFolder() throws Exception {
		copyFileInTemporaryFolder("mailSnapshotStore.data");
		copyFileInTemporaryFolder("mailSnapshotStore.index");
		copyFileInTemporaryFolder("mailWindowingChunksStore.data");
		copyFileInTemporaryFolder("mailWindowingChunksStore.index");
		copyFileInTemporaryFolder("mailWindowingIndexStore.data");
		copyFileInTemporaryFolder("mailWindowingIndexStore.index");
		copyFileInTemporaryFolder("monitoredCollectionService.data");
		copyFileInTemporaryFolder("monitoredCollectionService.index");
		copyFileInTemporaryFolder("syncedCollectionStoreService.data");
		copyFileInTemporaryFolder("syncedCollectionStoreService.index");
		copyFileInTemporaryFolder("syncKeysStore.data");
		copyFileInTemporaryFolder("syncKeysStore.index");
		copyFileInTemporaryFolder("unsynchronizedItemService.data");
		copyFileInTemporaryFolder("unsynchronizedItemService.index");
	}

	private void copyFileInTemporaryFolder(String fileName) throws IOException, FileNotFoundException {
		IOUtils.copy(ClassLoader.getSystemResourceAsStream(fileName), 
				new FileOutputStream(new File(dataDir.getCanonicalPath() + File.separatorChar + fileName)));
	}

	@After
	public void shutdown() throws Exception {
		transactionManager.rollback();
		transactionManager.shutdown();
		objectStoreManagerMigration.shutdown();
		objectStoreManager.shutdown();
	}

	@Test
	public void testMigrateMonitoredCollection() {
		int expectedSize = monitoredCollectionDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateMonitoredCollection();
		
		assertThat(monitoredCollectionDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(monitoredCollectionDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateSnapshot() {
		int expectedSize = snapshotDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateSnashot();
		
		assertThat(snapshotDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(snapshotDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateSyncedCollection() {
		int expectedSize = syncedCollectionDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateSyncedCollection();
		
		assertThat(syncedCollectionDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(syncedCollectionDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateSyncKeys() {
		int expectedSize = syncKeysDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateSyncKeys();
		
		assertThat(syncKeysDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(syncKeysDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateUnsynchronizedItem() {
		int expectedSize = unsynchronizedItemDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateUnsynchronizedItem();
		
		assertThat(unsynchronizedItemDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(unsynchronizedItemDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateWindowingChunk() {
		int expectedSize = windowingDaoChunkEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateWindowingChunk();
		
		assertThat(windowingDaoEhcacheImpl.getChunksStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(windowingDaoChunkEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}

	@Test
	public void testMigrateWindowingIndex() {
		int expectedSize = windowingDaoIndexEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateWindowingIndex();
		
		assertThat(windowingDaoEhcacheImpl.getIndexStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(windowingDaoIndexEhcacheMigrationImpl.getKeys().size()).isEqualTo(0);
	}
}
