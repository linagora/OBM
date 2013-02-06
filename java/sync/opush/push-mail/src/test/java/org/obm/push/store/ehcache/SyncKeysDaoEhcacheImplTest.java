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
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncKeysKey;
import org.slf4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncKeysDaoEhcacheImplTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private ObjectStoreManager objectStoreManager;
	private SyncKeysDaoEhcacheImpl syncKeysDaoEhcacheImpl;
	private BitronixTransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		objectStoreManager = new ObjectStoreManager(initConfigurationServiceMock(), logger);
		syncKeysDaoEhcacheImpl = new SyncKeysDaoEhcacheImpl(objectStoreManager);
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
		DeviceId deviceId = new DeviceId("deviceId");
		int collectionId = 1;
		
		List<SyncKey> syncKeys = syncKeysDaoEhcacheImpl.get(deviceId, collectionId);
		
		assertThat(syncKeys).isNull();
	}
	
	@Test
	public void put() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		int collectionId = 1;
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
	}
	
	@Test
	public void get() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		int collectionId = 1;
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		List<SyncKey> syncKeys = syncKeysDaoEhcacheImpl.get(deviceId, collectionId);
		
		assertThat(syncKeys).containsOnly(syncKey);
	}
	
	@Test
	public void putMultipleDifferentKeys() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		int collectionId = 1;
		int collectionId2 = 2;
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		syncKeysDaoEhcacheImpl.put(deviceId2, collectionId2, syncKey2);
		List<SyncKey> syncKeys = syncKeysDaoEhcacheImpl.get(deviceId, collectionId);
		List<SyncKey> syncKeys2 = syncKeysDaoEhcacheImpl.get(deviceId2, collectionId2);
		
		assertThat(syncKeys).containsOnly(syncKey);
		assertThat(syncKeys2).containsOnly(syncKey2);
	}
	
	@Test
	public void putMultipleSameKey() {
		DeviceId deviceId = new DeviceId("deviceId");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		int collectionId = 1;
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey2);
		List<SyncKey> syncKeys = syncKeysDaoEhcacheImpl.get(deviceId, collectionId);
		
		assertThat(syncKeys).containsOnly(syncKey, syncKey2);
	}
	
	@Test
	public void delete() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		int collectionId = 1;
		int collectionId2 = 2;
		
		SyncKeysKey expectedSyncKeysKey = SyncKeysKey.builder()
				.deviceId(deviceId2)
				.collectionId(collectionId2)
				.build();
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		syncKeysDaoEhcacheImpl.put(deviceId2, collectionId2, syncKey2);
		syncKeysDaoEhcacheImpl.delete(deviceId, collectionId);
		
		List<SyncKeysKey> keys = objectStoreManager.getStore(syncKeysDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSyncKeysKey);
	}
	
	@Test
	public void deleteWithMultipleSyncKeys() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		SyncKey syncKey3 = new SyncKey("synckey3");
		int collectionId = 1;
		int collectionId2 = 2;
		
		SyncKeysKey expectedSyncKeysKey = SyncKeysKey.builder()
				.deviceId(deviceId2)
				.collectionId(collectionId2)
				.build();
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		syncKeysDaoEhcacheImpl.put(deviceId2, collectionId2, syncKey2);
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey3);
		syncKeysDaoEhcacheImpl.delete(deviceId, collectionId);
		
		List<SyncKeysKey> keys = objectStoreManager.getStore(syncKeysDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSyncKeysKey);
	}

	@Test
	public void deleteWithMultipleCollectionId() {
		DeviceId deviceId = new DeviceId("deviceId");
		DeviceId deviceId2 = new DeviceId("deviceId2");
		SyncKey syncKey = new SyncKey("synckey");
		SyncKey syncKey2 = new SyncKey("synckey2");
		int collectionId = 1;
		int collectionId2 = 2;
		int collectionId3 = 3;
		
		SyncKeysKey expectedSyncKeysKey = SyncKeysKey.builder()
				.deviceId(deviceId2)
				.collectionId(collectionId2)
				.build();
		SyncKeysKey expectedSyncKeysKey2 = SyncKeysKey.builder()
				.deviceId(deviceId)
				.collectionId(collectionId3)
				.build();
		
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId, syncKey);
		syncKeysDaoEhcacheImpl.put(deviceId2, collectionId2, syncKey2);
		syncKeysDaoEhcacheImpl.put(deviceId, collectionId3, syncKey2);
		syncKeysDaoEhcacheImpl.delete(deviceId, collectionId);
		
		List<SyncKeysKey> keys = objectStoreManager.getStore(syncKeysDaoEhcacheImpl.getStoreName()).getKeys();
		assertThat(keys).containsOnly(expectedSyncKeysKey, expectedSyncKeysKey2);
	}
}
