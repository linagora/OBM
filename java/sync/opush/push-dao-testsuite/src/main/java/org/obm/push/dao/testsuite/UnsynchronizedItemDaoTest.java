/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.dao.testsuite;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;

import com.google.common.collect.ImmutableList;

@RunWith(GuiceRunner.class)
public abstract class UnsynchronizedItemDaoTest {

	protected UnsynchronizedItemDao unsynchronizedItemDao;
	
	@Test
	public void list() {
		assertThat(unsynchronizedItemDao.listItemsToAdd(new SyncKey("123"))).isNotNull();
	}
	
	@Test
	public void add() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, ImmutableList.of(buildItemChange("test 1")));
		
		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey))
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
		
		unsynchronizedItemDao.storeItemsToAdd(
				syncKey, ImmutableList.of(itemChange1, itemChange2));
		
		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey))
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
		
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(itemChange1, itemChange2));
		unsynchronizedItemDao.storeItemsToAdd(syncKey2, 
				ImmutableList.of(itemChange21));
		
		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(2)
			.containsOnly(itemChange1, itemChange2);

		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey2))
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
		
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(itemChange1));
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(itemDeletion2));
		
		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(1)
			.doesNotContain(itemChange3)
			.containsOnly(itemChange1);
	}
	
	@Test
	public void clear() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		
		assertThat(unsynchronizedItemDao.listItemsToAdd(syncKey))
			.isNotNull()
			.hasSize(0);
	}
	
	@Test
	public void hasAnyItemForNoAdd() {
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(new SyncKey("123"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneAdd() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForThreeAdd() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}

	@Test
	public void hasAnyItemForSyncKeyCollectionAdd() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(new SyncKey("456"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForThreeAddAfterClear() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1"), buildItemChange("test 2"), buildItemChange("test 3")));
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isFalse();
	}
	
	@Test
	public void hasAnyItemForNoRemove() {
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(new SyncKey("123"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneRemove() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForThreeRemove() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}

	@Test
	public void hasAnyItemForOtherCollectionRemove() {
		unsynchronizedItemDao.storeItemsToRemove(new SyncKey("123"), 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(new SyncKey("456"))).isFalse();
	}
	
	@Test
	public void hasAnyItemForThreeRemoveAfterClear() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build(),
						ItemDeletion.builder().serverId("test 2").build(),
						ItemDeletion.builder().serverId("test 3").build()));
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isFalse();
	}
	
	@Test
	public void hasAnyItemForOneAddAndDeleteButAddCleaned() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	@Test
	public void hasAnyItemForOneAddAndDeleteButRemoveCleaned() {
		SyncKey syncKey = new SyncKey("123");
		unsynchronizedItemDao.storeItemsToAdd(syncKey, 
				ImmutableList.of(buildItemChange("test 1")));
		
		unsynchronizedItemDao.storeItemsToRemove(syncKey, 
				ImmutableList.of(
						ItemDeletion.builder().serverId("test 1").build()));
		
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		
		assertThat(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).isTrue();
	}
	
	private ItemChange buildItemChange(String displayName) {
		ItemChange itemChange = new ItemChange();
		itemChange.setServerId(displayName);
		return itemChange;
	}
}
