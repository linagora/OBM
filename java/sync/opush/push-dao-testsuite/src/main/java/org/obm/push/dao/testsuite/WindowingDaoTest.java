/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.mail.EmailChanges;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.WindowingDao;

import com.google.common.base.Function;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

@RunWith(GuiceRunner.class)
public abstract class WindowingDaoTest {

	protected WindowingDao windowingDao;

	private User user;
	private Device device;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testGetWindowingSyncKeyOnEmptyStore() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey syncKey = windowingDao.getWindowingSyncKey(WindowingIndexKey);
		assertThat(syncKey).isNull();
	}
	
	@Test
	public void testGetWindowingSyncKey() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey expectedSyncKey = new SyncKey("123");
		windowingDao.pushPendingElements(WindowingIndexKey, expectedSyncKey, EmailChanges.builder().build());
		SyncKey syncKey = windowingDao.getWindowingSyncKey(WindowingIndexKey);
		assertThat(syncKey).isEqualTo(expectedSyncKey);
	}
	
	@Test
	public void testGetWindowingSyncKeyBadKey() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		windowingDao.pushPendingElements(WindowingIndexKey, new SyncKey("123"), EmailChanges.builder().build());
		SyncKey syncKey = windowingDao.getWindowingSyncKey(new WindowingIndexKey(user, device.getDevId(), 2));
		assertThat(syncKey).isNull();
	}
	
	@Test
	public void testPushNextAndConsume() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey syncKey = new SyncKey("123");
		
		EmailChanges firstEmails = generateEmails(25);
		EmailChanges secondEmails = generateEmails(25);
		windowingDao.pushPendingElements(WindowingIndexKey, syncKey, firstEmails);
		windowingDao.pushNextRequestPendingElements(WindowingIndexKey, syncKey, secondEmails);
		
		Iterable<EmailChanges> emailChanges = windowingDao.consumingChunksIterable(WindowingIndexKey);
		assertThat(emailChanges).containsOnly(firstEmails, secondEmails);
	}
	
	@Test
	public void testPushNextAndConsumeWithDataRemaining() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey syncKey = new SyncKey("123");
		EmailChanges generateEmails = generateEmails(25);
		windowingDao.pushPendingElements(WindowingIndexKey, syncKey, generateEmails);
		windowingDao.pushNextRequestPendingElements(WindowingIndexKey, syncKey, EmailChanges.builder().build());
		
		Iterable<EmailChanges> emailChanges = windowingDao.consumingChunksIterable(WindowingIndexKey);
		assertThat(emailChanges).containsOnly(generateEmails);
	}
	
	@Test
	public void testRemovePreviousCollectionWindowing() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey expectedSyncKey = new SyncKey("123");
		windowingDao.pushPendingElements(WindowingIndexKey, expectedSyncKey, EmailChanges.builder().build());
		windowingDao.removePreviousCollectionWindowing(WindowingIndexKey);
		SyncKey syncKey = windowingDao.getWindowingSyncKey(WindowingIndexKey);
		assertThat(syncKey).isNull();
	}
	
	@Test
	public void testConsumingChunksIterableCleansStore() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey syncKey = new SyncKey("123");
		EmailChanges generateEmails = generateEmails(25);
		windowingDao.pushPendingElements(WindowingIndexKey, syncKey, generateEmails);
		
		Iterable<EmailChanges> emailChanges = windowingDao.consumingChunksIterable(WindowingIndexKey);
		assertThat(emailChanges).containsOnly(generateEmails);
		
		Iterable<EmailChanges> emailChangesSecondTime = windowingDao.consumingChunksIterable(WindowingIndexKey);
		assertThat(emailChangesSecondTime).isEmpty();
	}
	
	@Test
	public void testConsumingChunksIterableWithIndex() {
		WindowingIndexKey WindowingIndexKey = new WindowingIndexKey(user, device.getDevId(), 1);
		SyncKey syncKey = new SyncKey("123");
		EmailChanges firstEmails = generateEmails(25);
		windowingDao.pushPendingElements(WindowingIndexKey, syncKey, firstEmails);
		SyncKey secondSyncKey = new SyncKey("456");
		EmailChanges secondEmails = generateEmails(25, 30);
		windowingDao.pushPendingElements(WindowingIndexKey, secondSyncKey, secondEmails);
		
		Iterable<EmailChanges> emailChanges = windowingDao.consumingChunksIterable(WindowingIndexKey);
		assertThat(emailChanges).containsOnly(firstEmails, secondEmails);
	}

	private EmailChanges generateEmails(long number) {
		return generateEmails(0, number);
	}
	
	private EmailChanges generateEmails(long start, long number) {
		return EmailChanges.builder()
				.additions(
					FluentIterable.from(ContiguousSet.create(Range.closedOpen(start, start + number), DiscreteDomain.longs()))
						.transform(new Function<Long, Email>() {
							@Override
							public Email apply(Long uid) {
								return Email.builder().uid(uid).build();
							}
						}).toSet())
				.build();
	}
}
