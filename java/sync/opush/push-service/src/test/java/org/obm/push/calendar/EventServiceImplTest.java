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
