/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import net.sf.ehcache.migrating.Element;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.mail.EmailChanges;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.ehcache.WindowingDaoEhcacheImpl.ChunkKey;
import org.slf4j.Logger;

public class WindowingDaoChunkEhcacheMigrationImplTest extends StoreManagerConfigurationTest {

	private MigrationSourceObjectStoreManager objectStoreManagerMigration;
	private WindowingDaoChunkEhcacheMigrationImpl windowingDaoChunkEhcacheMigrationImpl;
	private User user;
	private DeviceId deviceId;
	
	@Before
	public void init() throws IOException {
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManagerMigration = new MigrationSourceObjectStoreManager( super.initOpushConfigurationMock(), logger);
		this.windowingDaoChunkEhcacheMigrationImpl = new WindowingDaoChunkEhcacheMigrationImpl(objectStoreManagerMigration);
		this.user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		this.deviceId = new DeviceId("DevId");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException {
		objectStoreManagerMigration.shutdown();
	}
	
	@Test
	public void testGetKeys() {
		ChunkKey key = new ChunkKey(new WindowingIndexKey(user, deviceId, 1), 1);
		ChunkKey key2 = new ChunkKey(new WindowingIndexKey(user, deviceId, 1), 2);
		windowingDaoChunkEhcacheMigrationImpl.store.put(new Element(
				key, 
				EmailChanges.builder()
					.build()));
		windowingDaoChunkEhcacheMigrationImpl.store.put(new Element(
				key2, 
				EmailChanges.builder()
					.build()));
		
		List<Object> keys = windowingDaoChunkEhcacheMigrationImpl.getKeys();
		assertThat(keys).containsOnly(key, key2);
	}
	
	@Test
	public void testGet() {
		Element element = new Element(
				new ChunkKey(new WindowingIndexKey(user, deviceId, 1), 1), 
				EmailChanges.builder()
					.build());
		windowingDaoChunkEhcacheMigrationImpl.store.put(element);
		
		Element value = windowingDaoChunkEhcacheMigrationImpl.get(new ChunkKey(new WindowingIndexKey(user, deviceId, 1), 1));
		assertThat(value).isEqualTo(element);
	}
	
	@Test
	public void testRemove() {
		ChunkKey key = new ChunkKey(new WindowingIndexKey(user, deviceId, 1), 1);
		windowingDaoChunkEhcacheMigrationImpl.store.put(new Element(
				key, 
				EmailChanges.builder()
					.build()));
		
		windowingDaoChunkEhcacheMigrationImpl.remove(key);
		
		Element value = windowingDaoChunkEhcacheMigrationImpl.get(key);
		assertThat(value).isNull();
	}
}
