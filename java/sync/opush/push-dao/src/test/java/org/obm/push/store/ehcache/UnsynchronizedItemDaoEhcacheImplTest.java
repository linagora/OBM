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

import java.io.IOException;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class) @Slow
public class UnsynchronizedItemDaoEhcacheImplTest extends StoreManagerConfigurationTest  {

	private ObjectStoreManager objectStoreManager;
	private UnsynchronizedItemDaoEhcacheImpl unSynchronizedItemImpl;
	private TransactionManager transactionManager;
	
	public UnsynchronizedItemDaoEhcacheImplTest() {
		super();
	}
	
	@Before
	public void init() throws NotSupportedException, SystemException, IOException {
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		this.transactionManager.begin();
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock(), logger);
		this.unSynchronizedItemImpl = new UnsynchronizedItemDaoEhcacheImpl(objectStoreManager);
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		this.transactionManager.rollback();
		this.objectStoreManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
	
	@Test
	public void list() {
		assertThat(unSynchronizedItemImpl.listItemsToAdd(new SyncKey("123"))).isNotNull();
	}
	
	@Test
	public void add() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, ImmutableList.of(buildItemChange("test 1")));
		
		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(1)
			.containsOnly(new ItemChange("test 1"));
	}
	
	@Test
	public void addTwoItemsOnTheSameCollection() {
		ItemChange itemChange1 = buildItemChange("1");
		ItemChange itemChange2 = buildItemChange("2");
		ItemChange itemChange3 = buildItemChange("3");
		SyncKey syncKey = new SyncKey("123");
		
		unSynchronizedItemImpl.storeItemsToAdd(
				syncKey, ImmutableList.of(itemChange1, itemChange2));
		
		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(2)
			.doesNotContain(itemChange3)
			.containsOnly(itemChange1, itemChange2);
	}
	
	@Test
	public void addItemsOnTwoCollections() {
		ItemChange itemChange1 = buildItemChange("test 1.1");
		ItemChange itemChange2 = buildItemChange("test 1.2");
		ItemChange itemChange21 = buildItemChange("test 2.1");
		SyncKey syncKey = new SyncKey("123");
		SyncKey syncKey2 = new SyncKey("456");
		
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(itemChange1, itemChange2));
		unSynchronizedItemImpl.storeItemsToAdd(syncKey2, 
				ImmutableList.of(itemChange21));
		
		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(2)
			.containsOnly(itemChange1, itemChange2);

		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey2))
			.isNotNull()
			.hasSize(1)
			.containsOnly(itemChange21);
	}
	
	@Test
	public void addTwoItemsDifferentTypeOnTheSameCollection() {
		ItemChange itemChange1 = buildItemChange("test 1");
		ItemDeletion itemDeletion2 = ItemDeletion.builder().serverId("test 2").build();
		ItemChange itemChange3 = buildItemChange("test 3");
		SyncKey syncKey = new SyncKey("123");
		
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(itemChange1));
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(itemDeletion2));
		
		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(1)
			.doesNotContain(itemChange3)
			.containsOnly(itemChange1);
	}
	
	@Test
	public void clear() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		unSynchronizedItemImpl.clearItemsToAdd(syncKey);
		
		assertThat(unSynchronizedItemImpl.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(0);
	}
	
	@Test
	public void hasAnyItemForNoAdd() {
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(new SyncKey("123"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneAdd() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForThreeAdd() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}

	@Test
	public void hasAnyItemForSyncKeyCollectionAdd() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(new SyncKey("456"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForThreeAddAfterClear() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		unSynchronizedItemImpl.clearItemsToAdd(syncKey);
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isFalse();
	}
	
	@Test
	public void hasAnyItemForNoRemove() {
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(new SyncKey("123"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneRemove() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForThreeRemove() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}

	@Test
	public void hasAnyItemForOtherCollectionRemove() {
		unSynchronizedItemImpl.storeItemsToRemove(new SyncKey("123"), 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(new SyncKey("456"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForThreeRemoveAfterClear() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		unSynchronizedItemImpl.clearItemsToRemove(syncKey);
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneAddAndDeleteButAddCleaned() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		unSynchronizedItemImpl.clearItemsToAdd(syncKey);
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForOneAddAndDeleteButRemoveCleaned() {
		SyncKey syncKey = new SyncKey("123");
		unSynchronizedItemImpl.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		unSynchronizedItemImpl.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		unSynchronizedItemImpl.clearItemsToRemove(syncKey);
		
		assertThat(unSynchronizedItemImpl.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	private ItemChange buildItemChange(String displayName) {
		ItemChange itemChange = new ItemChange();
		itemChange.setServerId(displayName);
		return itemChange;
	}
	
}
