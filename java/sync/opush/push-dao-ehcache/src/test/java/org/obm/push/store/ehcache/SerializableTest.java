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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl.Key;

import com.google.common.testing.SerializableTester;

public class SerializableTest {
	
	private ObjectOutputStream objectOutputStream;

	@Before
	public void buildOutputStream() throws IOException {
		objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());	
	}
	
	@Test
	public void unsynchronizedItemDaoEhcacheImplKey() {
		UnsynchronizedItemDaoEhcacheImpl.Key_2_4_2_4 key = new UnsynchronizedItemDaoEhcacheImpl.Key_2_4_2_4(
				new SyncKey("132"), UnsynchronizedItemType.ADD);
		SerializableTester.reserializeAndAssert(key);
	}

	@Test
	public void monitoredCollectionDaoEhcacheImplKey() {
		Key key = new MonitoredCollectionDaoEhcacheImpl.Key(
				new Credentials(User.Factory.create().createUser("email@domain", "email@domain", "User"), "password"),
				new Device(1, "devType", new DeviceId("deviceId"), new Properties(), ProtocolVersion.V120));
		
		SerializableTester.reserializeAndAssert(key);
	}
	
	@Test
	public void testWindowingIndex() {
		SerializableTester.reserializeAndAssert(new WindowingDaoEhcacheImpl.WindowingIndex(5, new SyncKey("123")));
	}

	@Test
	public void testChunkKey() {
		User user = User.Factory.create().createUser("user@email.org", "user@email.org", "display name");
		WindowingIndexKey windowingIndexKey = new WindowingIndexKey(user, new DeviceId("564"), 456);
		SerializableTester.reserializeAndAssert(new WindowingDaoEhcacheImpl.ChunkKey(windowingIndexKey, 5));
	}
	
	@Test
	public void testSnapshotKey() throws IOException {
		SnapshotKey snapshotKey = SnapshotKey.builder()
				.deviceId(new DeviceId("deviceId"))
				.syncKey(new SyncKey("syncKey"))
				.collectionId(1)
				.build();
		objectOutputStream.writeObject(snapshotKey);
	}

}
