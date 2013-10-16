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

import static org.easymock.EasyMock.createMock;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.slf4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncedCollectionDaoEhcacheImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private SyncedCollectionDaoEhcacheImpl syncedCollectionStoreServiceImpl;
	private Credentials credentials;
	private BitronixTransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		Logger logger = EasyMock.createNiceMock(Logger.class);
		EhCacheConfiguration config = new TestingEhCacheConfiguration();
		this.objectStoreManager = new ObjectStoreManager(super.mockConfigurationService(), config, logger);
		CacheEvictionListener cacheEvictionListener = createMock(CacheEvictionListener.class);
		this.syncedCollectionStoreServiceImpl = new SyncedCollectionDaoEhcacheImpl(objectStoreManager, cacheEvictionListener);
		User user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		this.credentials = new Credentials(user, "password");
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
	}
	
	@After
	public void cleanup() throws Exception {
		transactionManager.commit();
		transactionManager.shutdown();
		objectStoreManager.shutdown();
	}
	
	@Test
	public void get() {
		AnalysedSyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		assertThat(syncCollection).isNull();
	}
	
	@Test
	public void put() {
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildCollection(1, SyncKey.INITIAL_FOLDER_SYNC_KEY));
		AnalysedSyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		assertThat(syncCollection).isNotNull();
		assertThat(syncCollection.getCollectionId()).isEqualTo(1);
	}
	
	@Test
	public void putUpdatedCollection() {
		SyncKey expectedSyncKey = new SyncKey("123");
		AnalysedSyncCollection col = buildCollection(1, SyncKey.INITIAL_FOLDER_SYNC_KEY);
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), col);
		col = buildCollection(1, expectedSyncKey);
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), col);
		
		AnalysedSyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		assertThat(syncCollection).isNotNull();
		assertThat(syncCollection.getCollectionId()).isEqualTo(1);
		assertThat(syncCollection.getSyncKey()).isEqualTo(expectedSyncKey);
	}

	private AnalysedSyncCollection buildCollection(Integer id, SyncKey syncKey) {
		return AnalysedSyncCollection.builder()
				.collectionId(id)
				.syncKey(syncKey)
				.build();
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", new DeviceId("DevId"), null, null);
	}
	
}
