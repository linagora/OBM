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
import java.util.List;

import net.sf.ehcache.migrating.Element;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl.Key_2_4_2_4;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class) @Slow
public class UnsynchronizedItemDaoEhcacheMigrationImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManagerMigration objectStoreManagerMigration;
	private UnsynchronizedItemDaoEhcacheMigrationImpl unsynchronizedItemDaoEhcacheMigrationImpl;
	
	@Before
	public void init() throws IOException {
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManagerMigration = new ObjectStoreManagerMigration( super.initConfigurationServiceMock(), logger);
		this.unsynchronizedItemDaoEhcacheMigrationImpl = new UnsynchronizedItemDaoEhcacheMigrationImpl(objectStoreManagerMigration);
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException {
		objectStoreManagerMigration.shutdown();
	}
	
	@Test
	public void testGetKeys() {
		Key_2_4_2_4 key = UnsynchronizedItemDaoEhcacheImpl.key(new SyncKey("123"), UnsynchronizedItemType.ADD);
		Key_2_4_2_4 key2 = UnsynchronizedItemDaoEhcacheImpl.key(new SyncKey("456"), UnsynchronizedItemType.DELETE);
		unsynchronizedItemDaoEhcacheMigrationImpl.store.put(new Element(
				key, 
				ImmutableSet.of(new ItemChange("1"))));
		unsynchronizedItemDaoEhcacheMigrationImpl.store.put(new Element(
				key2, 
				ImmutableSet.of(new ItemChange("2"))));
		
		List<Object> keys = unsynchronizedItemDaoEhcacheMigrationImpl.getKeys();
		assertThat(keys).containsOnly(key, key2);
	}
	
	@Test
	public void testGet() {
		Element element = new Element(
				UnsynchronizedItemDaoEhcacheImpl.key(new SyncKey("123"), UnsynchronizedItemType.ADD), 
				ImmutableSet.of(new ItemChange("1")));
		unsynchronizedItemDaoEhcacheMigrationImpl.store.put(element);
		
		Element value = unsynchronizedItemDaoEhcacheMigrationImpl.get(UnsynchronizedItemDaoEhcacheImpl.key(new SyncKey("123"), UnsynchronizedItemType.ADD));
		assertThat(value).isEqualTo(element);
	}
	
	@Test
	public void testRemove() {
		Key_2_4_2_4 key = UnsynchronizedItemDaoEhcacheImpl.key(new SyncKey("123"), UnsynchronizedItemType.ADD);
		unsynchronizedItemDaoEhcacheMigrationImpl.store.put(new Element(
				key, 
				ImmutableSet.of(new ItemChange("1"))));
		
		unsynchronizedItemDaoEhcacheMigrationImpl.remove(key);
		
		Element value = unsynchronizedItemDaoEhcacheMigrationImpl.get(key);
		assertThat(value).isNull();
	}
}
