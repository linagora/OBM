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
import java.util.Collection;
import java.util.Set;

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

import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class MonitoredCollectionDaoEhcacheImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private MonitoredCollectionDaoEhcacheImpl monitoredCollectionStoreServiceImpl;
	private Credentials credentials;
	private TransactionManager transactionManager;
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock(), logger);
		this.monitoredCollectionStoreServiceImpl = new MonitoredCollectionDaoEhcacheImpl(objectStoreManager);
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
	public void testList() {
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
	}
	
	@Test @Slow
	public void testSimplePut() {
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(1, syncCollections.size());
		containsCollectionWithId(syncCollections, 1);
	}
	
	@Test @Slow
	public void testPutNewItems() {
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(2, 3));

		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(2, syncCollections.size());
		containsCollectionWithId(syncCollections, 2);
		containsCollectionWithId(syncCollections, 3);
		
	}
	
	private void containsCollectionWithId(
			Collection<SyncCollection> syncCollections, Integer id) {
		boolean find = false;
		for(SyncCollection col : syncCollections){
			if(col.getCollectionId().equals(id)){
				find = true;
			}
		}
		Assert.assertTrue(find);
	}

	private Set<SyncCollection> buildListCollection(Integer... ids) {
		Set<SyncCollection> cols = Sets.newHashSet();
		for(Integer id : ids){
			SyncCollection col = new SyncCollection();
			col.setCollectionId(id);
			cols.add(col);
		}
		return cols;
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", new DeviceId("DevId"), null, null);
	}
}
