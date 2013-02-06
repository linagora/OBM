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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncKey;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.bean.SnapshotKey;
import org.obm.push.utils.DateUtils;
import org.slf4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class SnapshotDaoEhcacheImplTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private ObjectStoreManager objectStoreManager;
	private SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl;
	private BitronixTransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		objectStoreManager = new ObjectStoreManager( initConfigurationServiceMock(), logger);
		snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager);
	}
	
	private ConfigurationService initConfigurationServiceMock() throws IOException {
		File dataDir = temporaryFolder.newFolder();
		ConfigurationService configurationService = EasyMock.createMock(ConfigurationService.class);
		EasyMock.expect(configurationService.transactionTimeoutInSeconds()).andReturn(2);
		EasyMock.expect(configurationService.usePersistentCache()).andReturn(true);
		EasyMock.expect(configurationService.getDataDirectory()).andReturn(dataDir.getCanonicalPath()).anyTimes();
		EasyMock.replay(configurationService);
		
		return configurationService;
	}

	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		transactionManager.rollback();
		objectStoreManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
	
	@Test
	public void getNull() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer connectionId = 1;
		
		Snapshot snapshot = snapshotDaoEhcacheImpl.get(deviceId, syncKey, connectionId);
		
		assertThat(snapshot).isNull();
	}
	
	@Test
	public void put() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer collectionId = 1;
		int uidNext = 2;
		Email email = Email.builder()
				.uid(3)
				.read(false)
				.date(DateUtils.getCurrentDate())
				.build();
		
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.uidNext(uidNext)
				.addEmail(email)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
	}
	
	@Test
	public void get() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer collectionId = 1;
		int uidNext = 2;
		Email email = Email.builder()
				.uid(3)
				.read(false)
				.date(DateUtils.getCurrentDate())
				.build();
		
		Snapshot expectedSnapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.uidNext(uidNext)
				.addEmail(email)
				.build();
		
		snapshotDaoEhcacheImpl.put(expectedSnapshot);
		Snapshot snapshot = snapshotDaoEhcacheImpl.get(deviceId, syncKey, collectionId);
		
		assertThat(snapshot).isEqualTo(expectedSnapshot);
	}
	
	@Test
	public void deleteAllNullDeviceId() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.filterType(FilterType.ONE_DAY_BACK)
				.collectionId(collectionId2)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		SnapshotKey expectedSnapshotKey2 = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.deleteAll(null);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey, expectedSnapshotKey2);
	}
	
	@Test
	public void deleteAll() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.deleteAll(deviceId);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey);
	}
	
	@Test
	public void deleteAllWithMultipleSyncKeys() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		SyncKey syncKey3 = new SyncKey("synckey3");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		Snapshot snapshot3 = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey3)
				.collectionId(collectionId2)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.put(snapshot3);
		snapshotDaoEhcacheImpl.deleteAll(deviceId);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey);
	}

	@Test
	public void deleteAllWithMultipleCollectionId() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Integer collectionId3 = 3;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		Snapshot snapshot3 = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId3)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.put(snapshot3);
		snapshotDaoEhcacheImpl.deleteAll(deviceId);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey);
	}
	
	@Test
	public void delete() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.delete(deviceId, syncKey, collectionId);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey);
	}

	@Test
	public void deleteWithMultipleCollectionId() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		Integer collectionId = 1;
		Integer collectionId2 = 2;
		Integer collectionId3 = 3;
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.build();
		Snapshot snapshot2 = Snapshot.builder()
				.deviceId(deviceId2)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		Snapshot snapshot3 = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(syncKey2)
				.collectionId(collectionId3)
				.build();
		
		SnapshotKey expectedSnapshotKey = SnapshotKey.builder()
				.deviceId(deviceId2)
				.syncKey(syncKey2)
				.collectionId(collectionId2)
				.build();
		SnapshotKey expectedSnapshotKey2 = SnapshotKey.builder()
				.deviceId(deviceId)
				.syncKey(syncKey2)
				.collectionId(collectionId3)
				.build();
		
		snapshotDaoEhcacheImpl.put(snapshot);
		snapshotDaoEhcacheImpl.put(snapshot2);
		snapshotDaoEhcacheImpl.put(snapshot3);
		snapshotDaoEhcacheImpl.delete(deviceId, syncKey, collectionId);
		
		List<SnapshotKey> keys = objectStoreManager.getStore(snapshotDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSnapshotKey, expectedSnapshotKey2);
	}
}
