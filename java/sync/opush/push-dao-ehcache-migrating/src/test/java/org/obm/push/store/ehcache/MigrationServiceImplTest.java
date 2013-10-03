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
import static org.fest.assertions.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(SlowFilterRunner.class) @Slow
public class MigrationServiceImplTest extends StoreManagerConfigurationTest {

	private Logger logger;
	private MigrationSourceObjectStoreManager objectStoreManagerMigration;
	private ObjectStoreManager objectStoreManager;
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
		EhCacheConfiguration config = buildNonTransactionalConfig();
		
		IMocksControl control = createControl();
		TransactionProvider transactionProvider = control.createMock(TransactionProvider.class);
		expect(transactionProvider.get()).andReturn(null).anyTimes();
		control.replay();
		
		copyCacheFilesInTemporaryFolder();
		objectStoreManagerMigration = new MigrationSourceObjectStoreManager(configurationService, logger);
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

		migrationServiceImpl = new MigrationServiceImpl(logger,
				objectStoreManager, objectStoreManagerMigration,
				monitoredCollectionDaoEhcacheMigrationImpl, monitoredCollectionDaoEhcacheImpl,
				snapshotDaoEhcacheMigrationImpl, snapshotDaoEhcacheImpl,
				syncedCollectionDaoEhcacheMigrationImpl, syncedCollectionDaoEhcacheImpl,
				syncKeysDaoEhcacheMigrationImpl, syncKeysDaoEhcacheImpl,
				unsynchronizedItemDaoEhcacheMigrationImpl, unsynchronizedItemDaoEhcacheImpl,
				windowingDaoChunkEhcacheMigrationImpl, windowingDaoIndexEhcacheMigrationImpl, windowingDaoEhcacheImpl);
	}
	
	private EhCacheConfiguration buildNonTransactionalConfig() {
		return new TestingEhCacheConfiguration().withTransactionMode(TransactionalMode.OFF);
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
	public void shutdown() {
		objectStoreManagerMigration.shutdown();
		objectStoreManager.shutdown();
	}

	@Test
	public void testMigrateMonitoredCollection() {
		int expectedSize = monitoredCollectionDaoEhcacheMigrationImpl.getKeys().size();
		
		migrationServiceImpl.migrateCache(
				monitoredCollectionDaoEhcacheMigrationImpl, monitoredCollectionDaoEhcacheImpl.getStore());
		
		assertThat(monitoredCollectionDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(monitoredCollectionDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateSnapshot() {
		int expectedSize = snapshotDaoEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				snapshotDaoEhcacheMigrationImpl, snapshotDaoEhcacheImpl.getStore());
		
		assertThat(snapshotDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(snapshotDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateSyncedCollection() {
		int expectedSize = syncedCollectionDaoEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				syncedCollectionDaoEhcacheMigrationImpl, syncedCollectionDaoEhcacheImpl.getStore());
		
		assertThat(syncedCollectionDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(syncedCollectionDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateSyncKeys() {
		int expectedSize = syncKeysDaoEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				syncKeysDaoEhcacheMigrationImpl, syncKeysDaoEhcacheImpl.getStore());
		
		assertThat(syncKeysDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(syncKeysDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateUnsynchronizedItem() {
		int expectedSize = unsynchronizedItemDaoEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				unsynchronizedItemDaoEhcacheMigrationImpl, unsynchronizedItemDaoEhcacheImpl.getStore());
		
		assertThat(unsynchronizedItemDaoEhcacheImpl.getStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(unsynchronizedItemDaoEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateWindowingChunk() {
		int expectedSize = windowingDaoChunkEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				windowingDaoChunkEhcacheMigrationImpl, windowingDaoEhcacheImpl.getChunksStore());
		
		assertThat(windowingDaoEhcacheImpl.getChunksStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(windowingDaoChunkEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}

	@Test
	public void testMigrateWindowingIndex() {
		int expectedSize = windowingDaoIndexEhcacheMigrationImpl.getKeys().size();

		migrationServiceImpl.migrateCache(
				windowingDaoIndexEhcacheMigrationImpl, windowingDaoEhcacheImpl.getIndexStore());
		
		assertThat(windowingDaoEhcacheImpl.getIndexStore().getKeys().size()).isGreaterThan(0).isEqualTo(expectedSize);
		assertThat(windowingDaoIndexEhcacheMigrationImpl.getKeys().size()).isEqualTo(expectedSize);
	}
	
	@Test
	public void testCheckMigrationFilesDeletion() {
		File[] files = { new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_SNAPSHOT_STORE + ".data"), 
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_SNAPSHOT_STORE + ".index"), 
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_WINDOWING_CHUNKS_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_WINDOWING_CHUNKS_STORE + ".index"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_WINDOWING_INDEX_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MAIL_WINDOWING_INDEX_STORE + ".index"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MONITORED_COLLECTION_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.MONITORED_COLLECTION_STORE + ".index"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.SYNCED_COLLECTION_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.SYNCED_COLLECTION_STORE + ".index"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.SYNC_KEYS_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.SYNC_KEYS_STORE + ".index"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.UNSYNCHRONIZED_ITEM_STORE + ".data"),
				new File(dataDir + File.separator + MigrationSourceObjectStoreManager.UNSYNCHRONIZED_ITEM_STORE + ".index") };
		
		assertThat(dataDir.listFiles()).contains(files);
		migrationServiceImpl.migrate();
		assertThat(dataDir.listFiles()).doesNotContain(files);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testAssertMigrationHasSucceedOrDieWhenMoreKeys() {
		String targetName = "store name";
		List<Object> sourceKeyList = windowingDaoIndexEhcacheMigrationImpl.getKeys();
		List<Object> targetKeyList = ImmutableList.copyOf(Iterables.concat(
				windowingDaoIndexEhcacheMigrationImpl.getKeys(), Lists.newArrayList(new Object())));

		migrationServiceImpl.assertMigrationHasSucceedOrDie(targetName, targetKeyList, sourceKeyList);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testAssertMigrationHasSucceedOrDieWhenLessKeys() {
		String targetName = "store name";
		List<Object> sourceKeyList = windowingDaoIndexEhcacheMigrationImpl.getKeys();
		List<Object> targetKeyList = ImmutableList.copyOf(Iterables.skip(
				windowingDaoIndexEhcacheMigrationImpl.getKeys(), 1));
		
		migrationServiceImpl.assertMigrationHasSucceedOrDie(targetName, targetKeyList, sourceKeyList);
	}
	
	@Test
	public void testAssertMigrationHasSucceedOrDieWhenSameKeys() {
		String targetName = "store name";
		List<Object> sourceKeyList = windowingDaoIndexEhcacheMigrationImpl.getKeys();
		List<Object> targetKeyList = ImmutableList.copyOf(windowingDaoIndexEhcacheMigrationImpl.getKeys());
		
		try {
			migrationServiceImpl.assertMigrationHasSucceedOrDie(targetName, targetKeyList, sourceKeyList);
		} catch (Exception e) {
			fail("no exception expected", e);
		}
	}
}
