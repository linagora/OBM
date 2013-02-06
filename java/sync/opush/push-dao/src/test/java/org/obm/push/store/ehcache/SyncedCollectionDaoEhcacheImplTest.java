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

import java.io.IOException;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncedCollectionDaoEhcacheImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private SyncedCollectionDaoEhcacheImpl syncedCollectionStoreServiceImpl;
	private Credentials credentials;
	private TransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock(), logger);
		this.syncedCollectionStoreServiceImpl = new SyncedCollectionDaoEhcacheImpl(objectStoreManager);
		User user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		this.credentials = new Credentials(user, "password");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		transactionManager.rollback();
		objectStoreManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
	
	@Test
	public void get() {
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNull(syncCollection);
	}
	
	@Test
	public void put() {
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildCollection(1));
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
	}
	
	@Test
	public void putUpdatedCollection() {
		SyncCollection col = buildCollection(1);
		col.setCollectionPath("PATH1");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), col);
		col.setCollectionPath("PATH1CHANGE");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), col);
		
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
		Assert.assertEquals("PATH1CHANGE", syncCollection.getCollectionPath());
	}

	private SyncCollection buildCollection(Integer id) {
		SyncCollection col = new SyncCollection();
		col.setCollectionId(id);
		return col;
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", new DeviceId("DevId"), null, null);
	}
	
}
