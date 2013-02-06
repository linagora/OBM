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
package org.obm.push.mail;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.WindowingDao;

import com.google.common.collect.ImmutableList;


public class WindowingServiceImplTest {

	private WindowingDao windowingDao;
	private WindowingServiceImpl testee;
	private IMocksControl control;
	private int collectionId;
	private DeviceId deviceId;
	private User user;

	@Before
	public void setup() {
		collectionId = 5;
		deviceId = new DeviceId("ab123");
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");

		control = createControl();
		windowingDao = control.createMock(WindowingDao.class);
		testee = new WindowingServiceImpl(windowingDao);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void popNextPendingElementsNullKey() {
		WindowingIndexKey key = null;
		int expectedSize = 12;

		testee.popNextPendingElements(key, expectedSize);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void popNextPendingElementsZeroExpectedSize() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		int expectedSize = 0;
		testee.popNextPendingElements(key, expectedSize);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void popNextPendingElementsNegativeExpectedSize() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		int expectedSize = -5;
		testee.popNextPendingElements(key, expectedSize);
	}
	
	@Test
	public void popNextPendingElementsEmpty() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		int expectedSize = 12;

		expect(windowingDao.consumingChunksIterable(key))
			.andReturn(ImmutableList.<EmailChanges>of()).once();

		control.replay();
		EmailChanges elements = testee.popNextPendingElements(key, expectedSize);
		control.verify();

		assertThat(elements.sumOfChanges()).isEqualTo(0);
	}
	
	@Test
	public void popNextPendingElementsFewElements() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		int expectedSize = 12;

		expect(windowingDao.consumingChunksIterable(key))
			.andReturn(ImmutableList.<EmailChanges>of(
					EmailChanges.builder().addition(Email.builder().uid(1).build()).build(),
					EmailChanges.builder().addition(Email.builder().uid(2).build()).build()))
			.once();
	
		control.replay();
		EmailChanges elements = testee.popNextPendingElements(key, expectedSize);
		control.verify();

		assertThat(elements.sumOfChanges()).isEqualTo(2);
	}
	
	@Test
	public void popNextPendingElementsEnoughElements() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		int expectedSize = 2;

		expect(windowingDao.consumingChunksIterable(key))
			.andReturn(ImmutableList.<EmailChanges>of(
					EmailChanges.builder().addition(
							Email.builder().uid(1).build(),
							Email.builder().uid(2).build(),
							Email.builder().uid(3).build(),
							Email.builder().uid(4).build())
						.build(),
					EmailChanges.builder().addition(Email.builder().uid(5).build()).build()))
			.once();

		windowingDao.pushPendingElements(key, EmailChanges.builder()
				.addition(
					Email.builder().uid(2).build(),
					Email.builder().uid(1).build())
				.build());
		expectLastCall();
		
		control.replay();
		EmailChanges elements = testee.popNextPendingElements(key, expectedSize);
		control.verify();
		
		assertThat(elements.sumOfChanges()).isEqualTo(2);
	}

	@Test
	public void hasPendingElementsNoIndex() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		SyncKey syncKey = new SyncKey("123");
		
		expect(windowingDao.getWindowingSyncKey(key)).andReturn(null);
		
		control.replay();
		boolean hasPendingElements = testee.hasPendingElements(key, syncKey);
		control.verify();
		
		assertThat(hasPendingElements).isFalse();
	}

	@Test
	public void hasPendingElementsSameIndexSyncKey() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		SyncKey requestSyncKey = new SyncKey("123");
		
		expect(windowingDao.getWindowingSyncKey(key)).andReturn(requestSyncKey); 
		
		control.replay();
		boolean hasPendingElements = testee.hasPendingElements(key, requestSyncKey);
		control.verify();
		
		assertThat(hasPendingElements).isTrue();
	}
	
	@Test
	public void hasPendingElementsDifferentIndexSyncKey() {
		WindowingIndexKey key = new WindowingIndexKey(user, deviceId, collectionId);
		SyncKey requestSyncKey = new SyncKey("123");
		SyncKey storedIndexSyncKey = new SyncKey("456");
		
		expect(windowingDao.getWindowingSyncKey(key)).andReturn(storedIndexSyncKey);
		windowingDao.removePreviousCollectionWindowing(key);
		expectLastCall();
		
		control.replay();
		boolean hasPendingElements = testee.hasPendingElements(key, requestSyncKey);
		control.verify();
		
		assertThat(hasPendingElements).isFalse();
	}
}
