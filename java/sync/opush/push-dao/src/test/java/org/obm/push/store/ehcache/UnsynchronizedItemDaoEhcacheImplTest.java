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
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.change.item.ItemChange;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class UnsynchronizedItemDaoEhcacheImplTest extends StoreManagerConfigurationTest  {

	private ObjectStoreManager objectStoreManager;
	private UnsynchronizedItemDaoEhcacheImpl unSynchronizedItemImpl;
	private Credentials credentials;
	private TransactionManager transactionManager;
	
	public UnsynchronizedItemDaoEhcacheImplTest() {
		super();
	}
	
	@Before
	public void init() throws NotSupportedException, SystemException {
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		this.transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock(), logger);
		this.unSynchronizedItemImpl = new UnsynchronizedItemDaoEhcacheImpl(objectStoreManager);
		User user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		this.credentials = new Credentials(user, "password");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		this.transactionManager.rollback();
		this.objectStoreManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
	
	@Test
	public void list() {
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(itemChanges);
	}
	
	@Test
	public void add() {
		unSynchronizedItemImpl.storeItemsToAdd(credentials, getFakeDeviceId(), 1, ImmutableList.of(buildItemChange("test 1")));
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(1, itemChanges.size());
		Assert.assertEquals("test 1", itemChanges.iterator().next().getServerId());
	}
	
	@Test
	public void addTwoItemsOnTheSameCollection() {
		ItemChange itemChange1 = buildItemChange("1");
		ItemChange itemChange2 = buildItemChange("2");
		ItemChange itemChange3 = buildItemChange("3");
		
		unSynchronizedItemImpl.storeItemsToAdd(
				credentials, getFakeDeviceId(), 1, ImmutableList.of(itemChange1, itemChange2));
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(2, itemChanges.size());
		
		Assert.assertTrue( contains(itemChanges, itemChange1) );
		Assert.assertTrue( contains(itemChanges, itemChange2) );
		Assert.assertFalse( contains(itemChanges, itemChange3) );
	}
	
	@Test
	public void addItemsOnTwoCollections() {
		ItemChange itemChange1 = buildItemChange("test 1.1");
		ItemChange itemChange2 = buildItemChange("test 1.2");
		ItemChange itemChange21 = buildItemChange("test 2.1");
		
		unSynchronizedItemImpl.storeItemsToAdd(credentials, getFakeDeviceId(), 1, 
				ImmutableList.of(itemChange1, itemChange2));
		unSynchronizedItemImpl.storeItemsToAdd(credentials, getFakeDeviceId(), 2, 
				ImmutableList.of(itemChange21));
		Set<ItemChange> itemChangesOneCollection = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Set<ItemChange> itemChangesTwoCollection = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 2);
		
		Assert.assertNotNull(itemChangesOneCollection);
		Assert.assertEquals(2, itemChangesOneCollection.size());
		Assert.assertTrue( contains(itemChangesOneCollection, itemChange1) );
		Assert.assertTrue( contains(itemChangesOneCollection, itemChange2) );
		
		Assert.assertNotNull(itemChangesTwoCollection);
		Assert.assertEquals(1, itemChangesTwoCollection.size());
		Assert.assertTrue( contains(itemChangesTwoCollection, itemChange21) );	
	}
	
	@Test @Slow
	public void addTwoItemsDifferentTypeOnTheSameCollection() {
		ItemChange itemChange1 = buildItemChange("test 1");
		ItemChange itemChange2 = buildItemChange("test 2");
		ItemChange itemChange3 = buildItemChange("test 3");
		
		unSynchronizedItemImpl.storeItemsToAdd(credentials, getFakeDeviceId(), 1, 
				ImmutableList.of(itemChange1));
		unSynchronizedItemImpl.storeItemsToRemove(credentials, getFakeDeviceId(), 1, 
				ImmutableList.of(itemChange2));
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(1, itemChanges.size());
		
		Assert.assertTrue( contains(itemChanges, itemChange1) );
		Assert.assertFalse( contains(itemChanges, itemChange2) );
		Assert.assertFalse( contains(itemChanges, itemChange3) );
	}
	
	@Test @Slow
	public void clear() {
		unSynchronizedItemImpl.storeItemsToAdd(credentials, getFakeDeviceId(), 1, 
				ImmutableList.of(buildItemChange("test 1")));
		unSynchronizedItemImpl.clearItemsToAdd(credentials, getFakeDeviceId(), 1);		
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemsToAdd(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(0, itemChanges.size());
	}
	
	private ItemChange buildItemChange(String displayName) {
		ItemChange itemChange = new ItemChange();
		itemChange.setServerId(displayName);
		return itemChange;
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", "DevId", null);
	}
	
	private boolean contains(Set<ItemChange> expected, ItemChange actual) {
		for (ItemChange itemChange: expected) {
			if (itemChange.getServerId().equals(actual.getServerId())) {
				return true;
			}
		}
		return false;
	}
	
}
