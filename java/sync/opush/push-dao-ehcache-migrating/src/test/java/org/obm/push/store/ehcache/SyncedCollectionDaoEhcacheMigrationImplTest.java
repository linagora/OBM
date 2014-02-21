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
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl.Key;
import org.slf4j.Logger;

public class SyncedCollectionDaoEhcacheMigrationImplTest extends StoreManagerConfigurationTest {

	private MigrationSourceObjectStoreManager objectStoreManagerMigration;
	private SyncedCollectionDaoEhcacheMigrationImpl syncedCollectionDaoEhcacheMigrationImpl;
	private Credentials credentials;
	
	@Before
	public void init() throws IOException {
		Logger logger = EasyMock.createNiceMock(Logger.class);
		this.objectStoreManagerMigration = new MigrationSourceObjectStoreManager( super.initOpushConfigurationMock(), logger);
		this.syncedCollectionDaoEhcacheMigrationImpl = new SyncedCollectionDaoEhcacheMigrationImpl(objectStoreManagerMigration);
		User user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		this.credentials = new Credentials(user, "password");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException {
		objectStoreManagerMigration.shutdown();
	}
	
	@Test
	public void testGetKeys() {
		Key key = new Key(credentials, getFakeDeviceId());
		Key key2 = new Key(credentials, getFakeDeviceId());
		syncedCollectionDaoEhcacheMigrationImpl.store.put(new Element(
				key, 
				AnalysedSyncCollection.builder()
						.collectionId(1)
						.syncKey(new SyncKey("123"))
						.build()));
		syncedCollectionDaoEhcacheMigrationImpl.store.put(new Element(
				key2, 
				AnalysedSyncCollection.builder()
						.collectionId(2)
						.syncKey(new SyncKey("456"))
						.build()));
		
		List<Object> keys = syncedCollectionDaoEhcacheMigrationImpl.getKeys();
		assertThat(keys).containsOnly(key, key2);
	}
	
	@Test
	public void testGet() {
		Element element = new Element(
				new Key(credentials, getFakeDeviceId()), 
				AnalysedSyncCollection.builder()
					.collectionId(1)
					.syncKey(new SyncKey("123"))
					.build());
		syncedCollectionDaoEhcacheMigrationImpl.store.put(element);
		
		Element value = syncedCollectionDaoEhcacheMigrationImpl.get(new Key(credentials, getFakeDeviceId()));
		assertThat(value).isEqualTo(element);
	}
	
	@Test
	public void testRemove() {
		Key key = new Key(credentials, getFakeDeviceId());
		syncedCollectionDaoEhcacheMigrationImpl.store.put(new Element(
				key, 
				AnalysedSyncCollection.builder()
						.collectionId(1)
						.syncKey(new SyncKey("123"))
						.build()));
		
		syncedCollectionDaoEhcacheMigrationImpl.remove(key);
		
		Element value = syncedCollectionDaoEhcacheMigrationImpl.get(key);
		assertThat(value).isNull();
	}
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", new DeviceId("DevId"), null, ProtocolVersion.V121);
	}
}
