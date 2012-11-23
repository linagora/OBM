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
package org.obm.push.calendar;

import java.io.IOException;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.icalendar.Ical4jUser.Factory;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.EventParsingException;
import org.obm.push.store.CalendarDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.client.login.LoginService;

import com.google.common.io.ByteStreams;


public class EventServiceImplTest {
	@Test
	public void testOBMFULL3526() throws EventParsingException, ConversionException, IOException, AuthFault, DaoException {
		
		UserDataRequest udr = new UserDataRequest(
				new Credentials(User.Factory.create().createUser("user@domain", "user@domain", null), "password"), null, null, null);
		
		LoginService loginService = EasyMock.createMock(LoginService.class);
		AccessToken accessToken = new AccessToken(1, "origin");
		EasyMock.expect(loginService.authenticate("user@domain", "password")).andReturn(accessToken);
		loginService.logout(accessToken);
		EasyMock.expectLastCall();
		CalendarDao calendarDao = EasyMock.createMock(CalendarDao.class);
		EasyMock.expect(calendarDao.getMSEventUidFor(EasyMock.anyObject(EventExtId.class), EasyMock.anyObject(Device.class))).andReturn(new MSEventUid("uid"));
		calendarDao.insertExtIdMSEventUidMapping(EasyMock.anyObject(EventExtId.class), EasyMock.anyObject(MSEventUid.class), EasyMock.anyObject(Device.class), EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall();
		Factory factory = EasyMock.createMock(Ical4jUser.Factory.class);
		EasyMock.replay(loginService, calendarDao);
		
		EventConverterImpl eventConverter = new EventConverterImpl(new MSEventToObmEventConverterImpl(), new ObmEventToMSEventConverterImpl());
		
		InputStream icsStream = ClassLoader.getSystemResourceAsStream("icalendar/OBMFULL-3526.ics");
		EventService eventService = new EventServiceImpl(calendarDao, eventConverter, new Ical4jHelper(), factory, loginService);
		eventService.parseEventFromICalendar(udr, new String(ByteStreams.toByteArray(icsStream)));
		
		EasyMock.verify(loginService);
	}

	@Test
	public void testConvertEventToMSEventWithExistingId() throws DaoException, ConversionException {
		EventExtId eventExtId = eventExtId();

		Event event = new Event();
		event.setExtId(eventExtId);

		Device device = device();

		User user = user();
		Credentials credentials = new Credentials(user, "password");

		UserDataRequest udr = EasyMock.createMock(UserDataRequest.class);
		EasyMock.expect(udr.getDevice()).andReturn(device).once();
		EasyMock.expect(udr.getCredentials()).andReturn(credentials).once();

		MSEventUid msEventUid = msEventUid();

		MSEvent expectedMsEvent = new MSEvent();
		expectedMsEvent.setUid(msEventUid);
		expectedMsEvent.setLocation("expected ms event location");

		CalendarDao calendarDao = EasyMock.createMock(CalendarDao.class);
		EasyMock.expect(calendarDao.getMSEventUidFor(event.getExtId(), device))
				.andReturn(msEventUid).once();

		EasyMock.expectLastCall();

		EventConverter converter = EasyMock.createMock(EventConverter.class);
		EasyMock.expect(converter.convert(event, msEventUid, user)).andReturn(expectedMsEvent);

		Object[] mocks = { calendarDao, udr, converter };
		EasyMock.replay(mocks);

		EventService eventService = new EventServiceImpl(calendarDao, converter, null, null, null);

		MSEvent msEvent = eventService.convertEventToMSEvent(udr, event);
		Assertions.assertThat(msEvent).isEqualTo(expectedMsEvent);

		EasyMock.verify(mocks);
	}

	@Test
	public void testConvertEventToMSEventWithNewId() throws DaoException, ConversionException {
		EventExtId eventExtId = eventExtId();

		Event event = new Event();
		event.setExtId(eventExtId);

		Device device = device();

		User user = user();
		Credentials credentials = new Credentials(user, "password");

		UserDataRequest udr = EasyMock.createMock(UserDataRequest.class);
		EasyMock.expect(udr.getDevice()).andReturn(device).once();
		EasyMock.expect(udr.getCredentials()).andReturn(credentials).once();

		MSEventUid msEventUid = msEventUid();

		MSEvent expectedMsEvent = new MSEvent();
		expectedMsEvent.setUid(msEventUid);
		expectedMsEvent.setLocation("expected ms event location");

		byte[] hashedExtId = hashedExtId();

		CalendarDao calendarDao = EasyMock.createMock(CalendarDao.class);
		EasyMock.expect(calendarDao.getMSEventUidFor(event.getExtId(), device)).andReturn(null)
				.once();

		calendarDao.insertExtIdMSEventUidMapping(EasyMock.eq(eventExtId), EasyMock.eq(msEventUid),
				EasyMock.eq(device), EasyMock.aryEq(hashedExtId));
		EasyMock.expectLastCall();

		EventConverter converter = EasyMock.createMock(EventConverter.class);
		EasyMock.expect(converter.convert(event, msEventUid, user)).andReturn(expectedMsEvent);

		Object[] mocks = { calendarDao, udr, converter };
		EasyMock.replay(mocks);

		EventService eventService = new EventServiceImpl(calendarDao, converter, null, null, null);

		MSEvent msEvent = eventService.convertEventToMSEvent(udr, event);
		Assertions.assertThat(msEvent).isEqualTo(expectedMsEvent);

		EasyMock.verify(mocks);
	}

	@Test
	public void testTrackEventExtIdMSEventUidTranslation() throws DaoException {
		CalendarDao calendarDao = EasyMock.createMock(CalendarDao.class);

		EventExtId eventExtId = eventExtId();
		MSEventUid msEventUid = new MSEventUid("ms_event_uid");
		Device device = device();
		byte[] hashedExtId = hashedExtId();

		calendarDao.insertExtIdMSEventUidMapping(EasyMock.eq(eventExtId), EasyMock.eq(msEventUid),
				EasyMock.eq(device), EasyMock.aryEq(hashedExtId));
		EasyMock.expectLastCall();

		Object[] mocks = { calendarDao };
		EasyMock.replay(mocks);

		EventService eventService = new EventServiceImpl(calendarDao, null, null, null, null);
		eventService.trackEventExtIdMSEventUidTranslation(eventExtId, msEventUid, device);
		EasyMock.verify(mocks);
	}

	private EventExtId eventExtId() {
		return new EventExtId("event_ext_id");
	}

	private byte[] hashedExtId() {
		return new byte[] { (byte) 0xb5, (byte) 0x1a, (byte) 0x02, (byte) 0x52,
				(byte) 0x8d, (byte) 0x0b, (byte) 0xf8, (byte) 0xc8, (byte) 0xec, (byte) 0x3f,
				(byte) 0x46, (byte) 0x60, (byte) 0x3a, (byte) 0xfa, (byte) 0xb6, (byte) 0x85,
				(byte) 0xf3, (byte) 0xb4, (byte) 0x42, (byte) 0xa1 };
	}

	private Device device() {
		return new Device(1, "devType", new DeviceId("devId"), null);
	}

	private MSEventUid msEventUid() {
		return new MSEventUid("6576656e745f6578745f6964");
	}

	private User user() {
		return User.Factory.create().createUser("user@obm", "user@obm", "user display name");
	}
}
