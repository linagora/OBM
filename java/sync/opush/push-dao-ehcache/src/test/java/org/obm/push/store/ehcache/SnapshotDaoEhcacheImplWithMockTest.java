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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.RegisteredEventListeners;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncKey;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.utils.DateUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Cache.class, ObjectStoreManager.class, RegisteredEventListeners.class})
public class SnapshotDaoEhcacheImplWithMockTest {

	@Test
	public void getNull() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer collectionId = 1;
		SnapshotKey buildKey = SnapshotKey.builder()
				.syncKey(syncKey)
				.deviceId(deviceId)
				.collectionId(collectionId)
				.build();
		ObjectStoreManager objectStoreManager = createObjectStoreManager();
		
		Cache cache = createCache();
		RegisteredEventListeners registeredEventListeners = createMock(RegisteredEventListeners.class);
		expect(registeredEventListeners.registerListener(anyObject(CacheEventListener.class)))
			.andReturn(true);
		expect(cache.getCacheEventNotificationService())
			.andReturn(registeredEventListeners).anyTimes();
		
		expect(cache.get(buildKey))
			.andReturn(null);
		
		expect(objectStoreManager.getStore(ObjectStoreManager.MAIL_SNAPSHOT_STORE))
			.andReturn(cache).anyTimes();

		replayAll();
		
		SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager, null);
		Snapshot snapshot = snapshotDaoEhcacheImpl.get(deviceId, syncKey, collectionId);
		
		verifyAll();
		assertThat(snapshot).isNull();
	}
	
	@Test
	public void get() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer collectionId = 2;
		SnapshotKey snapshotKey = SnapshotKey.builder()
				.syncKey(syncKey)
				.deviceId(deviceId)
				.collectionId(collectionId)
				.build();
		ObjectStoreManager objectStoreManager = createObjectStoreManager();
		
		Cache cache = createCache();
		RegisteredEventListeners registeredEventListeners = createMock(RegisteredEventListeners.class);
		expect(registeredEventListeners.registerListener(anyObject(CacheEventListener.class)))
			.andReturn(true);
		expect(cache.getCacheEventNotificationService())
			.andReturn(registeredEventListeners).anyTimes();
		
		Email email = Email.builder()
				.uid(1)
				.read(false)
				.date(DateUtils.getCurrentDate())
				.build();
		
		Snapshot expectedSnapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.uidNext(3)
				.addEmail(email)
				.build();
		
		Element expectedElement = new Element(snapshotKey, expectedSnapshot);
		expect(cache.get(snapshotKey))
			.andReturn(expectedElement).once();
		
		expect(objectStoreManager.getStore(ObjectStoreManager.MAIL_SNAPSHOT_STORE))
			.andReturn(cache).anyTimes();

		replayAll();
		
		SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager, null);
		Snapshot snapshot = snapshotDaoEhcacheImpl.get(deviceId, syncKey, collectionId);
		
		verifyAll();
		assertThat(snapshot).isEqualTo(expectedSnapshot);
	}
	
	@Test
	public void put() {
		SyncKey syncKey = new SyncKey("synckey");
		DeviceId deviceId = new DeviceId("deviceId");
		Integer collectionId = 2;
		SnapshotKey snapshotKey = SnapshotKey.builder()
				.syncKey(syncKey)
				.deviceId(deviceId)
				.collectionId(collectionId)
				.build();
		ObjectStoreManager objectStoreManager = createObjectStoreManager();
		
		Cache cache = createCache();
		RegisteredEventListeners registeredEventListeners = createMock(RegisteredEventListeners.class);
		expect(registeredEventListeners.registerListener(anyObject(CacheEventListener.class)))
			.andReturn(true);
		expect(cache.getCacheEventNotificationService())
			.andReturn(registeredEventListeners).anyTimes();
		
		Email email = Email.builder()
				.uid(1)
				.read(false)
				.date(DateUtils.getCurrentDate())
				.build();
		
		Snapshot snapshot = Snapshot.builder()
				.deviceId(deviceId)
				.filterType(FilterType.THREE_DAYS_BACK)
				.syncKey(syncKey)
				.collectionId(collectionId)
				.uidNext(3)
				.addEmail(email)
				.build();
		
		Element element = new Element(snapshotKey, snapshot);
		cache.put(element);
		expectLastCall().once();
		
		expect(objectStoreManager.getStore(ObjectStoreManager.MAIL_SNAPSHOT_STORE))
			.andReturn(cache).anyTimes();

		replayAll();
		
		SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager, null);
		snapshotDaoEhcacheImpl.put(snapshot);
		
		verifyAll();
	}
	
	@Test
	public void deleteAll() {
		DeviceId deviceId = new DeviceId("deviceId");
		
		ObjectStoreManager objectStoreManager = createObjectStoreManager();
		
		Cache cache = createCache();
		RegisteredEventListeners registeredEventListeners = createMock(RegisteredEventListeners.class);
		expect(registeredEventListeners.registerListener(anyObject(CacheEventListener.class)))
			.andReturn(true);
		expect(cache.getCacheEventNotificationService())
			.andReturn(registeredEventListeners).anyTimes();
		
		SnapshotKey snapshotKey = SnapshotKey.builder()
					.deviceId(deviceId)
					.syncKey(new SyncKey("syncKey"))
					.collectionId(2)
					.build();
		expect(cache.getKeys())
			.andReturn(ImmutableList.of(snapshotKey)).once();
		expect(cache.remove(snapshotKey))
			.andReturn(true).once();
		
		expect(objectStoreManager.getStore(ObjectStoreManager.MAIL_SNAPSHOT_STORE))
			.andReturn(cache).anyTimes();

		replayAll();
		
		SnapshotDaoEhcacheImpl snapshotDaoEhcacheImpl = new SnapshotDaoEhcacheImpl(objectStoreManager, null);
		snapshotDaoEhcacheImpl.deleteAll(deviceId);
		
		verifyAll();
	}

	private ObjectStoreManager createObjectStoreManager() {
		ObjectStoreManager objectStoreManager = createMock(ObjectStoreManager.class);
		return objectStoreManager;
	}
	
	private Cache createCache() {
		return createMock(Cache.class);
	}
}
