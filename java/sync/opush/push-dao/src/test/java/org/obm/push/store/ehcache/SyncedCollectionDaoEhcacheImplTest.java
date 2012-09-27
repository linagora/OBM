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

import java.util.Collection;
import java.util.List;

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
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.Lists;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncedCollectionDaoEhcacheImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private SyncedCollectionDaoEhcacheImpl syncedCollectionStoreServiceImpl;
	private Credentials credentials;
	private TransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException {
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
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
	}
	
	@Test
	public void putUpdatedCollection() {
		Collection<SyncCollection> cols = buildListCollection(1);
		cols.iterator().next().setCollectionPath("PATH1");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), cols);
		cols.iterator().next().setCollectionPath("PATH1CHANGE");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), cols);
		
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
		Assert.assertEquals("PATH1CHANGE", syncCollection.getCollectionPath());
	}
	
	@Test
	public void putList() {
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1,2,3));
		SyncCollection syncCollection1 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		SyncCollection syncCollection2 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 2);
		SyncCollection syncCollection3 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 3);
		
		Assert.assertNotNull(syncCollection1);
		Assert.assertEquals(new Integer(1), syncCollection1.getCollectionId());
		Assert.assertNotNull(syncCollection2);
		Assert.assertEquals(new Integer(2), syncCollection2.getCollectionId());
		Assert.assertNotNull(syncCollection3);
		Assert.assertEquals(new Integer(3), syncCollection3.getCollectionId());
	}

	private Collection<SyncCollection> buildListCollection(Integer... ids) {
		List<SyncCollection> cols = Lists.newLinkedList();
		for(Integer id : ids){
			SyncCollection col = new SyncCollection();
			col.setCollectionId(id);
			cols.add(col);
		}
		return cols;
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", "DevId", null);
	}
	
}
