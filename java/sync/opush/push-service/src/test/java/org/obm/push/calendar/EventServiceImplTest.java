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
import org.junit.Test;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.icalendar.Ical4jUser.Factory;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.User;
import org.obm.push.calendar.EventConverterImpl;
import org.obm.push.calendar.EventServiceImpl;
import org.obm.push.calendar.MSEventToObmEventConverterImpl;
import org.obm.push.calendar.ObmEventToMSEventConverterImpl;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.EventParsingException;
import org.obm.push.store.CalendarDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.client.login.LoginService;

import com.google.common.io.ByteStreams;


public class EventServiceImplTest {
	@Test
	public void testOBMFULL3526() throws EventParsingException, ConversionException, IOException, AuthFault, DaoException {
		
		BackendSession bs = new BackendSession(
				new Credentials(User.Factory.create().createUser("user@domain", "user@domain", null), "password"), null, null, null);
		
		LoginService loginService = EasyMock.createMock(LoginService.class);
		AccessToken accessToken = new AccessToken(1, "origin");
		EasyMock.expect(loginService.authenticate("user@domain", "password")).andReturn(accessToken);
		loginService.logout(accessToken);
		EasyMock.expectLastCall();
		CalendarDao calendarDao = EasyMock.createMock(CalendarDao.class);
		EasyMock.expect(calendarDao.getMSEventUidFor(EasyMock.anyObject(EventExtId.class), EasyMock.anyObject(Device.class))).andReturn(new MSEventUid("uid"));
		calendarDao.insertExtIdMSEventUidMapping(EasyMock.anyObject(EventExtId.class), EasyMock.anyObject(MSEventUid.class), EasyMock.anyObject(Device.class));
		EasyMock.expectLastCall();
		Factory factory = EasyMock.createMock(Ical4jUser.Factory.class);
		EasyMock.replay(loginService, calendarDao);
		
		EventConverterImpl eventConverter = new EventConverterImpl(new MSEventToObmEventConverterImpl(), new ObmEventToMSEventConverterImpl());
		
		InputStream icsStream = ClassLoader.getSystemResourceAsStream("icalendar/OBMFULL-3526.ics");
		EventService eventService = new EventServiceImpl(calendarDao, eventConverter, new Ical4jHelper(), factory, loginService);
		eventService.parseEventFromICalendar(bs, new String(ByteStreams.toByteArray(icsStream)));
		
		EasyMock.verify(loginService);
	}
	
}
