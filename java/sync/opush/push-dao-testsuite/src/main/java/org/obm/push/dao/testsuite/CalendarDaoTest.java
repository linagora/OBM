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
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.store.CalendarDao;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.calendar.EventExtId;

import com.google.common.hash.Hashing;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class CalendarDaoTest {

	@Inject CalendarDao calendarDao;
	
	protected EventExtId eventExtId;
	protected MSEventUid msEventUid;
	protected byte[] hashedExtId;
	protected User user;
	protected Device device;
	
	@Before
	public void setUp() {
		eventExtId = new EventExtId("123");
		msEventUid = new MSEventUid("456");
		hashedExtId = Hashing.sha1().hashUnencodedChars(eventExtId.getExtId()).asBytes();
		user = Factory.create().createUser("user@domain.org", "user@domain.org", "displayName");
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testInsertThenRightGets() throws Exception {
		calendarDao.insertExtIdMSEventUidMapping(eventExtId, msEventUid, device, hashedExtId);
		assertThat(calendarDao.getEventExtIdFor(msEventUid, device)).isEqualTo(eventExtId);
		assertThat(calendarDao.getMSEventUidFor(eventExtId, device)).isEqualTo(msEventUid);
	}
	
	@Test
	public void testInsertThenGetsUsingOtherExtId() throws Exception {
		calendarDao.insertExtIdMSEventUidMapping(eventExtId, msEventUid, device, hashedExtId);
		assertThat(calendarDao.getMSEventUidFor(new EventExtId("other"), device)).isNull();
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testInsertThenGetsUsingOtherMSUid() throws Exception {
		calendarDao.insertExtIdMSEventUidMapping(eventExtId, msEventUid, device, hashedExtId);
		calendarDao.getEventExtIdFor(new MSEventUid("other"), device);
	}
	
	@Test
	public void testInsertThenGetsUsingOtherDeviceByExtId() throws Exception {
		calendarDao.insertExtIdMSEventUidMapping(eventExtId, msEventUid, device, hashedExtId);
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		assertThat(calendarDao.getMSEventUidFor(eventExtId, otherDevice)).isNull();
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testInsertThenGetsUsingOtherDeviceByMSUid() throws Exception {
		calendarDao.insertExtIdMSEventUidMapping(eventExtId, msEventUid, device, hashedExtId);
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		calendarDao.getEventExtIdFor(msEventUid, otherDevice);
	}
}