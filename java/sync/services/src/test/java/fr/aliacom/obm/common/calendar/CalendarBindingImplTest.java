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
package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.easymock.EasyMock;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.HelperService;

public class CalendarBindingImplTest {

	private class ColdWarFixtures {
		private ObmDomain domain;
		private ObmUser user;

		private String userEmail = "beria@ussr";
		private String userEmailWithoutDomain = "beria";
		private String domainName = "ussr";

		private CalendarInfo beriaInfo;
		private CalendarInfo hooverInfo;
		private CalendarInfo mccarthyInfo;

		private ColdWarFixtures() {
			beriaInfo = new CalendarInfo();
			beriaInfo.setUid("beria");
			beriaInfo.setFirstname("Lavrenti");
			beriaInfo.setLastname("Beria");
			beriaInfo.setMail("beria@ussr");
			beriaInfo.setRead(true);
			beriaInfo.setWrite(true);
			
			hooverInfo = new CalendarInfo();
			hooverInfo.setUid("hoover");
			hooverInfo.setFirstname("John");
			hooverInfo.setLastname("Hoover");
			hooverInfo.setMail("hoover@usa");
			hooverInfo.setRead(true);
			hooverInfo.setWrite(false);
			
			mccarthyInfo = new CalendarInfo();
			mccarthyInfo.setUid("mccarthy");
			mccarthyInfo.setFirstname("Joseph");
			mccarthyInfo.setLastname("McCarthy");
			mccarthyInfo.setMail("mccarthy@usa");
			mccarthyInfo.setRead(true);
			mccarthyInfo.setWrite(false);
			
			domain = new ObmDomain();
			domain.setName(domainName);
			
			user = new ObmUser();
			user.setLogin(userEmailWithoutDomain);
			user.setEmail(userEmailWithoutDomain);
			user.setDomain(domain);
		}
	}
	
	private HelperService mockRightsHelper(String calendar, AccessToken accessToken) {
		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		expect(rightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		return rightsHelper;
	}

	private AccessToken mockAccessToken(String userName, ObmDomain domain) {
		AccessToken accessToken = createMock(AccessToken.class);
		expect(accessToken.getDomain()).andReturn(domain).atLeastOnce();
		expect(accessToken.getUserLogin()).andReturn(userName).anyTimes();
		expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		return accessToken;
	}

	private ObmUser mockObmUser(String userEmail, ObmDomain domain) {
		ObmUser user = createMock(ObmUser.class);
		expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
		expect(user.getDomain()).andReturn(domain).anyTimes();
		return user;
	}
	
	private String stripEmail(String email) {
		String strippedEmail = email.substring(0, email.indexOf('@'));
		return strippedEmail;
	}
	
	@Test
	public void testGetCalendarMetadata() throws ServerFault, FindException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String[] calendarEmails = {
				fixtures.beriaInfo.getMail(),
				fixtures.hooverInfo.getMail(),
				fixtures.mccarthyInfo.getMail()
		};
		
		String[] calendarEmailsWithStrippedEmail = {
				fixtures.hooverInfo.getMail(),
				stripEmail(fixtures.hooverInfo.getMail()),
				fixtures.mccarthyInfo.getMail(),
				stripEmail(fixtures.mccarthyInfo.getMail()),
		};
		
		CalendarInfo[] expectedCalendarInfos = {
				fixtures.beriaInfo,
				fixtures.hooverInfo,
				fixtures.mccarthyInfo,
		};
		
		CalendarInfo[] calendarInfosFromDao = {
				fixtures.hooverInfo,
				fixtures.mccarthyInfo,
		};
			
		AccessToken accessToken = mockAccessToken(fixtures.userEmail, fixtures.domain);
		HelperService rightsHelper = createMock(HelperService.class);
		
		rightsHelper.constructEmailFromList(eq(fixtures.userEmail), eq(fixtures.domainName));
		EasyMock.expectLastCall().andReturn(fixtures.userEmail);
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(fixtures.user).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(fixtures.user), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		EasyMock.expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);
		
		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};
		
		EasyMock.replay(mocks);
		CalendarInfo[] result = calendarService.getCalendarMetadata(accessToken, calendarEmails);
		assertEquals(new HashSet<CalendarInfo>(Arrays.asList(expectedCalendarInfos)), new HashSet<CalendarInfo>(Arrays.asList(result)));
	}
	
	@Test
	public void testGetCalendarMetadataExceptCurrentUser() throws ServerFault, FindException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String[] calendarEmails = {
				fixtures.hooverInfo.getMail(),
				fixtures.mccarthyInfo.getMail()
		};
		
		String[] calendarEmailsWithStrippedEmail = {
				fixtures.hooverInfo.getMail(),
				stripEmail(fixtures.hooverInfo.getMail()),
				fixtures.mccarthyInfo.getMail(),
				stripEmail(fixtures.mccarthyInfo.getMail())
		};
		
		CalendarInfo[] expectedCalendarInfos = {
				fixtures.hooverInfo,
				fixtures.mccarthyInfo,
		};
		
		CalendarInfo[] calendarInfosFromDao = {
				fixtures.hooverInfo,
				fixtures.mccarthyInfo,
		};
			
		AccessToken accessToken = mockAccessToken(fixtures.userEmail, fixtures.domain);
		HelperService rightsHelper = createMock(HelperService.class);
		
		rightsHelper.constructEmailFromList(eq(fixtures.userEmailWithoutDomain), eq(fixtures.domainName));
		EasyMock.expectLastCall().andReturn(fixtures.userEmail);
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(fixtures.user).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(fixtures.user), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		EasyMock.expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);
		
		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};
		
		EasyMock.replay(mocks);
		CalendarInfo[] result = calendarService.getCalendarMetadata(accessToken, calendarEmails);
		assertEquals(new HashSet<CalendarInfo>(Arrays.asList(expectedCalendarInfos)), new HashSet<CalendarInfo>(Arrays.asList(result)));
	}
	
	@Test(expected=ServerFault.class)
	public void testCalendarOwnerNotAnAttendee() throws ServerFault, FindException, EventAlreadyExistException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userName = "user";
		EventExtId eventExtId = new EventExtId("extid");
		String userEmail = "user@domain1";
		String mccarthyEmail = fixtures.mccarthyInfo.getMail();
		
		ObmUser user = mockObmUser(userEmail, fixtures.domain);
		
		AccessToken accessToken = mockAccessToken(userName, fixtures.domain);
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		expect(rightsHelper.canWriteOnCalendar(accessToken, mccarthyEmail)).andReturn(false);
		
		final Event event = createMock(Event.class);
		expect(event.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(event.getObmId()).andReturn(null).atLeastOnce();
		expect(event.isInternalEvent()).andReturn(false).atLeastOnce();
		expect(event.getTitle()).andReturn("title").atLeastOnce();
		expect(event.getAttendees()).andReturn(ImmutableList.of(getFakeAttendee(mccarthyEmail))).atLeastOnce();
		expect(event.getEventsExceptions()).andReturn(ImmutableList.<Event>of());
		
		event.findAttendeeFromEmail(userEmail);
		EasyMock.expectLastCall().andReturn(null).atLeastOnce();
		
		final UserService userService = createMock(UserService.class);
		userService.getUserFromCalendar(eq(calendar), eq(fixtures.domainName));
		EasyMock.expectLastCall().andReturn(user).atLeastOnce();

		final CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.findEventByExtId(eq(accessToken), eq(user), eq(eventExtId));
		EasyMock.expectLastCall().andReturn(null).once();

		Object[] mocks = {event, accessToken, userService, calendarDao, rightsHelper, user};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);

		try {
			calendarService.createEvent(accessToken, calendar, event, true);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
	}
	
	@Test
	public void testImportEventInThePast() 
		throws ImportICalendarException, ServerFault, IOException, ParserException, FindException, SQLException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = getFakeAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail, fixtures.domain);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(true).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		Ical4jUser ical4jUser = Ical4jUser.Factory.create().createIcal4jUser(userEmail, fixtures.domain);
		expect(calendarFactory.createIcal4jUserFromObmUser(obmUser)).andReturn(ical4jUser).anyTimes();		
		
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, calendar, fixtures.domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, obmUser, calendarDao,
				calendarFactory};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao,
				null, rightsHelper, ical4jHelper, calendarFactory);
		
		try {
			calendarService.importICalendar(accessToken, calendar, icsData);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(ParticipationState.ACCEPTED, fakeUserAttendee.getState());
	}
	
	@Test
	public void testPurge() throws FindException, ServerFault, SQLException, NumberFormatException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		EventExtId oldEventNoOtherAttendeesExtId = new EventExtId("oldEventNoOtherAttendeesExtId");
		EventExtId oldEventWithOtherAttendeesExtId = new EventExtId("oldEventWithOtherAttendeesExtId");
		EventObmId oldEventNoOtherAttendeesUid = new EventObmId("1");
		EventObmId oldEventWithOtherAttendeesUid = new EventObmId("2");

		String otherUserEmail = "user2@domain1";
		Attendee userAttendee = getFakeAttendee(userEmail);
		Attendee otherAttendee = getFakeAttendee(otherUserEmail);
		userAttendee.setState(ParticipationState.NEEDSACTION);
		final ObmUser obmUser = mockObmUser(userEmail, fixtures.domain);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);

		final Calendar oldEventDate = Calendar.getInstance();
		oldEventDate.add(Calendar.MONTH, -8);

		Event oldEventNoOtherAttendees = new Event();
		oldEventNoOtherAttendees.setExtId(oldEventNoOtherAttendeesExtId);
		oldEventNoOtherAttendees.setUid(oldEventNoOtherAttendeesUid);
		oldEventNoOtherAttendees.setAttendees(ImmutableList.of(userAttendee));
		oldEventNoOtherAttendees.setOwner(userEmail);
		oldEventNoOtherAttendees.setType(EventType.VEVENT);
		oldEventNoOtherAttendees.setInternalEvent(true);

		Event oldEventWithOtherAttendees = new Event();
		oldEventWithOtherAttendees.setExtId(oldEventWithOtherAttendeesExtId);
		oldEventWithOtherAttendees.setUid(oldEventWithOtherAttendeesUid);
		oldEventWithOtherAttendees.setAttendees(ImmutableList.of(userAttendee, otherAttendee));
		oldEventWithOtherAttendees.setOwner(userEmail);
		oldEventWithOtherAttendees.setType(EventType.VEVENT);

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.delete(obmUser, oldEventNoOtherAttendees, false, accessToken);
		eventChangeHandler.updateParticipationState(oldEventWithOtherAttendees, obmUser, 
				ParticipationState.DECLINED, false, accessToken);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser).atLeastOnce();
		expect(userService.getUserFromLogin(userEmail, fixtures.domainName)).andReturn(obmUser).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.listEventsByIntervalDate(eq(accessToken), eq(obmUser), isA(Date.class), 
				isA(Date.class), (EventType)isNull())).
			andReturn(ImmutableList.of(oldEventNoOtherAttendees, oldEventWithOtherAttendees)).once();
		expect(calendarDao.findEventById(accessToken, oldEventNoOtherAttendeesUid)).
			andReturn(oldEventNoOtherAttendees).atLeastOnce();
		expect(calendarDao.removeEventById(accessToken, oldEventNoOtherAttendeesUid, 
				oldEventNoOtherAttendees.getType(), oldEventNoOtherAttendees.getSequence()+1)).
				andReturn(oldEventNoOtherAttendees);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, oldEventWithOtherAttendeesExtId)).
			andReturn(oldEventWithOtherAttendees).atLeastOnce();
		expect(calendarDao.changeParticipationState(accessToken, obmUser, 
				oldEventWithOtherAttendees.getExtId(), ParticipationState.DECLINED)).andReturn(true);		

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		Object[] mocks = { accessToken, userService, rightsHelper, obmUser, calendarDao, eventChangeHandler };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null, null);

		calendarService.purge(accessToken, calendar);

		EasyMock.verify(mocks);
	}

	@Test
	public void testImportEventInTheFuture() 
		throws ImportICalendarException, ServerFault, IOException, ParserException, FindException, SQLException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = getFakeAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail, fixtures.domain);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(false).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		Ical4jUser ical4jUser = Ical4jUser.Factory.create().createIcal4jUser(userEmail, fixtures.domain);
		expect(calendarFactory.createIcal4jUserFromObmUser(obmUser)).andReturn(ical4jUser).anyTimes();

		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, calendar, fixtures.domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, 
				obmUser, calendarDao, calendarFactory};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null,
				rightsHelper, ical4jHelper, calendarFactory);
		
		try {
			calendarService.importICalendar(accessToken, calendar, icsData);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(ParticipationState.NEEDSACTION, fakeUserAttendee.getState());
	}

	@Test
	public void testAttendeeHasRightToWriteOnCalendar() throws FindException, ServerFault, SQLException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		
		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.NEEDSACTION);
		
		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);
		
		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setLocation("aLocation");
		event.setSequence(1);
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);
		
		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(true).atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event, updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, beforeEvent, event, notification, true, accessToken);
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		Assert.assertEquals(ParticipationState.ACCEPTED, newEvent.getAttendees().get(0).getState());
		Assert.assertEquals(1, beforeEvent.getAttendees().size());
		Assert.assertEquals(true, beforeEvent.getAttendees().iterator().next().isCanWriteOnCalendar());
		
		Assert.assertEquals(1, event.getAttendees().size());
		Assert.assertEquals(true, event.getAttendees().iterator().next().isCanWriteOnCalendar());
	}

	@Test
	public void testAttendeeOfExceptionHasRightToWriteOnCalendar() throws FindException,
			ServerFault, SQLException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.NEEDSACTION);

		Attendee exceptionAttendee = getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setState(ParticipationState.ACCEPTED);

		Date recurrenceId = new Date();

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);

		EventRecurrence beforeRecurrence = new EventRecurrence();
		beforeRecurrence.setKind(RecurrenceKind.daily);
		beforeEvent.setRecurrence(beforeRecurrence);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setTitle("firstTitle");
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setSequence(1);
		Event exception = new Event();
		exception.setType(EventType.VEVENT);
		exception.setTitle("firstTitle");
		exception.setInternalEvent(true);
		exception.setExtId(extId);
		exception.addAttendee(attendee);
		exception.addAttendee(exceptionAttendee);
		exception.setSequence(1);
		exception.setDate(recurrenceId);
		exception.setRecurrenceId(recurrenceId);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Lists.newArrayList(exception));
		event.setRecurrence(recurrence);

		Event dummyException = new Event();
		dummyException.setType(EventType.VEVENT);
		dummyException.setTitle("firstTitle");
		dummyException.setInternalEvent(true);
		dummyException.setExtId(extId);
		dummyException.addAttendee(attendee);
		dummyException.setSequence(0);
		dummyException.setDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, exceptionAttendee.getEmail()))
				.andReturn(true).atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, dummyException, exception, notification, true,
				accessToken);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(ParticipationState.NEEDSACTION,
				Iterables.getOnlyElement(newEvent.getAttendees()).getState());
		Event afterException = Iterables.getOnlyElement(newEvent.getRecurrence()
				.getEventExceptions());
		Attendee afterExceptionAttendee = afterException
				.findAttendeeFromEmail(exceptionAttendeeEmail);
		Assert.assertEquals(ParticipationState.ACCEPTED, afterExceptionAttendee.getState());
		Assert.assertEquals(true, afterExceptionAttendee.isCanWriteOnCalendar());
	}

	@Test
	public void testAttendeeOfExceptionHasNoRightToWriteOnCalendar() throws FindException,
			ServerFault, SQLException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.NEEDSACTION);

		Attendee exceptionAttendee = getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setState(ParticipationState.NEEDSACTION);

		Date recurrenceId = new Date();

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);

		EventRecurrence beforeRecurrence = new EventRecurrence();
		beforeRecurrence.setKind(RecurrenceKind.daily);
		beforeEvent.setRecurrence(beforeRecurrence);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setTitle("firstTitle");
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setSequence(1);
		Event exception = new Event();
		exception.setType(EventType.VEVENT);
		exception.setTitle("firstTitle");
		exception.setInternalEvent(true);
		exception.setExtId(extId);
		exception.addAttendee(attendee);
		exception.addAttendee(exceptionAttendee);
		exception.setSequence(1);
		exception.setDate(recurrenceId);
		exception.setRecurrenceId(recurrenceId);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Lists.newArrayList(exception));
		event.setRecurrence(recurrence);

		Event dummyException = new Event();
		dummyException.setType(EventType.VEVENT);
		dummyException.setTitle("firstTitle");
		dummyException.setInternalEvent(true);
		dummyException.setExtId(extId);
		dummyException.addAttendee(attendee);
		dummyException.setSequence(0);
		dummyException.setDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, exceptionAttendee.getEmail()))
				.andReturn(false).atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, dummyException, exception, notification, true,
				accessToken);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(ParticipationState.NEEDSACTION,
				Iterables.getOnlyElement(newEvent.getAttendees()).getState());
		Event afterException = Iterables.getOnlyElement(newEvent.getRecurrence()
				.getEventExceptions());
		Attendee afterExceptionAttendee = afterException
				.findAttendeeFromEmail(exceptionAttendeeEmail);
		Assert.assertEquals(ParticipationState.NEEDSACTION, afterExceptionAttendee.getState());
		Assert.assertEquals(false, afterExceptionAttendee.isCanWriteOnCalendar());
	}

	@Test
	public void testAttendeeHasNoRightToWriteOnCalendar() throws FindException, ServerFault,
			SQLException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.ACCEPTED);

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.setSequence(0);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setLocation("aLocation");
		event.setSequence(1);

		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, beforeEvent, event, notification, true, accessToken);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(ParticipationState.NEEDSACTION, newEvent.getAttendees().get(0)
				.getState());
	}

	
	@Test
	public void testCreateAnEventExceptionAndUpdateItsStatusButNotTheParent() throws FindException, SQLException, EventNotFoundException, ServerFault {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String attendeeEmail = "attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.lookup("daily"));

		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.ACCEPTED);
		Attendee attendee2 = getFakeAttendee(attendeeEmail);
		attendee2.setState(ParticipationState.ACCEPTED);
		
		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.addAttendee(attendee2);
		beforeEvent.setRecurrence(recurrence);
		beforeEvent.setSequence(0);
		
		Event eventException = beforeEvent.clone();
		EventRecurrence recurrence2 = new EventRecurrence();
		recurrence2.setKind(RecurrenceKind.lookup("none"));
		eventException.setRecurrence(recurrence2);
		eventException.setRecurrenceId(new Date());
		beforeEvent.getRecurrence().addEventException(eventException);
		
		Event event = beforeEvent.clone();
		event.setSequence(1);
		event.getRecurrence().getEventExceptions().get(0).setLocation("aLocation");
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(true)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee2.getEmail())).andReturn(false)
		.atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		
		eventChangeHandler.update(obmUser, beforeEvent.getRecurrence().getEventExceptionWithRecurrenceId(eventException.getRecurrenceId()), 
				event.getRecurrence().getEventExceptionWithRecurrenceId(eventException.getRecurrenceId()), notification, true, accessToken);
		EasyMock.expectLastCall().atLeastOnce();
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(ParticipationState.ACCEPTED, newEvent.getAttendees().get(0)
				.getState());		
		Assert.assertEquals(ParticipationState.NEEDSACTION, newEvent.getRecurrence().getEventExceptions().get(0).getAttendees().get(1)
				.getState());
	}
	
	public void testDontSendEmailsAndDontUpdateStatusForUnimportantChanges() throws ServerFault, FindException, SQLException, EventNotFoundException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String guestAttendee1Email = "guestAttendee1@domain1";
		String guestAttendee2Email = "guestAttendee2@domain1";
		EventExtId eventExtId = new EventExtId("extid");
		EventObmId eventUid = new EventObmId("0");
		int sequence = 2;

		Attendee userAttendee = new Attendee();
		userAttendee.setEmail(userEmail);
		userAttendee.setState(ParticipationState.ACCEPTED);

		Attendee guestAttendee1 = new Attendee();
		guestAttendee1.setEmail(guestAttendee1Email);
		guestAttendee1.setState(ParticipationState.ACCEPTED);

		Attendee guestAttendee2 = new Attendee();
		guestAttendee2.setEmail(guestAttendee2Email);
		guestAttendee2.setState(ParticipationState.NEEDSACTION);

		boolean updateAttendees = true;
		boolean notification = true;

		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		List<Attendee> oldAttendees = Arrays.asList(userAttendee, guestAttendee1);
		List<Attendee> newAttendees = Arrays.asList(userAttendee, guestAttendee1, guestAttendee2);

		Event oldEvent = new Event();
		oldEvent.setExtId(eventExtId);
		oldEvent.setUid(eventUid);
		oldEvent.setInternalEvent(true);
		oldEvent.setAttendees(oldAttendees);
		oldEvent.setSequence(sequence);

		Event newEvent = new Event();
		newEvent.setExtId(eventExtId);
		newEvent.setAttendees(newAttendees);
		newEvent.setSequence(sequence);

		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser).atLeastOnce();

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, eventExtId)).andReturn(oldEvent).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, newEvent, updateAttendees, sequence, true)).andReturn(newEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		Object[] mocks = {accessToken, calendarDao, userService, rightsHelper, eventChangeHandler};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null, null);

		calendarService.modifyEvent(accessToken, calendar, newEvent, updateAttendees, notification);

		EasyMock.verify(mocks);

		Assert.assertEquals(ParticipationState.ACCEPTED, userAttendee.getState());
		Assert.assertEquals(ParticipationState.ACCEPTED, guestAttendee1.getState());
		Assert.assertEquals(ParticipationState.NEEDSACTION, guestAttendee2.getState());
	}

	private Attendee getFakeAttendee(String userEmail) {
		Attendee att = new Attendee();
		att.setEmail(userEmail);
		return att;
	}
	
	private Ical4jHelper mockIcal4jHelper(Ical4jUser ical4jUser, String icsData, Event eventWithOwnerAttendee) throws IOException, ParserException{
		Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
		expect(ical4jHelper.parseICSEvent(icsData, ical4jUser)).andReturn(ImmutableList.of(eventWithOwnerAttendee)).once();
		return ical4jHelper;
	}
	
	private UserService mockImportICSUserService(AccessToken accessToken, Attendee fakeUserAttendee, String calendar, String domainName, ObmUser obmUser) throws FindException{
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();
		expect(userService.getUserFromAttendee(fakeUserAttendee, domainName)).andReturn(obmUser);
		expect(userService.getUserFromAttendee(fakeUserAttendee, domainName)).andReturn(obmUser);
		return userService;
	}
	
	private CalendarDao mockImportICalendarCalendarDao(AccessToken accessToken, String calendar, ObmUser obmUser, EventExtId eventExtId, Event eventWithOwnerAttendee) throws FindException, SQLException, ServerFault{
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(eq(accessToken), eq(obmUser), eq(eventExtId))).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, eventWithOwnerAttendee, false)).andReturn(eventWithOwnerAttendee).once();
		return calendarDao;
	}
	
	@Test
	public void testParseICSEventNotInternal() throws Exception {
		EventExtId extId = new EventExtId("extId1");
		
		Event result = testParseICS(extId, null);
		Assertions.assertThat(result.getObmId()).isNull();
		Assertions.assertThat(result.getExtId()).isSameAs(extId);
	}
	
	@Test
	public void testParseICSIncludeEventObmId() throws Exception {
		Event eventFromDao = new Event();
		EventObmId eventObmId = new EventObmId(12);
		eventFromDao.setUid(eventObmId);

		EventExtId extId = new EventExtId("extId");
		
		Event result = testParseICS(extId, eventFromDao);
		Assertions.assertThat(result.getObmId()).isSameAs(eventObmId);
		Assertions.assertThat(result.getExtId()).isSameAs(extId);
	}
	
	private Event testParseICS(EventExtId extId, Event eventFromDao) throws Exception {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "toto";
		String email = calendar + "@" + fixtures.domainName;
		String ics = "icsData";
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(email);
		
		Event eventFromIcs = new Event();
		eventFromIcs.setExtId(extId);
		
		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		Ical4jUser ical4jUser = Ical4jUser.Factory.create().createIcal4jUser(email, fixtures.domain);
		expect(calendarFactory.createIcal4jUserFromObmUser(obmUser)).andReturn(ical4jUser).anyTimes();
		
		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, ics, eventFromIcs);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, extId)).andReturn(eventFromDao).once();

		Object[] mocks = new Object[] {calendarDao, userService, ical4jHelper, accessToken, helper, calendarFactory};
		
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, helper, ical4jHelper, calendarFactory);
		List<Event> events = calendarService.parseICS(accessToken, ics);
		
		EasyMock.verify(mocks);
		
		Assertions.assertThat(events).hasSize(1);
		Event result = events.get(0);
		return result;
	}
	
	@Test
	public void testCreateExternalEventCalendarOwnerWithDeclinedPartState() throws FindException, ServerFault, EventAlreadyExistException, SQLException {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userEmail = "user@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean notification = false;
		
		Attendee calOwner = getFakeAttendee(userEmail);
		calOwner.setState(ParticipationState.DECLINED);
		
		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(false);
		event.setExtId(extId);
		event.setSequence(0);
		event.addAttendee(calOwner);
		
		
		Event eventCreated = new Event();
		eventCreated.setType(EventType.VEVENT);
		eventCreated.setInternalEvent(false);
		eventCreated.setExtId(extId);
		eventCreated.setSequence(0);
		eventCreated.addAttendee(calOwner);
		EventObmId obmId = new EventObmId(1);
		eventCreated.setUid(obmId);
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);
		
		AccessToken accessToken = mockAccessToken(calendar, fixtures.domain);
		HelperService helper = mockRightsHelper(calendar, accessToken);
		expect(helper.canWriteOnCalendar(accessToken, userEmail)).andReturn(false);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(obmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, event, false)).andReturn(eventCreated).once();
		expect(calendarDao.removeEvent(accessToken, eventCreated, eventCreated.getType(), eventCreated.getSequence())).andReturn(eventCreated).once();
		eventChangeHandler.updateParticipationState(eventCreated, obmUser, calOwner.getState(), notification, accessToken);
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null, null);
		calendarService.createEvent(accessToken, calendar, event, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
	}
	
	@Test
	public void testRecurrenceIdAtTheProperFormatInGetSyncResponse() throws FindException, ServerFault{
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327680144000L);
		EventChanges daoChanges = getFakeEventChanges(RecurrenceKind.none);
		
		EventChanges sortedChanges = mockGetSyncWithSortedChanges(fixtures,
				calendar, userName, lastSync, daoChanges);
		
		ParticipationChanges[] participationUpdated = sortedChanges.getParticipationUpdated();
		Assert.assertEquals(participationUpdated[0].getRecurrenceId().serializeToString(), "20120127T160000Z");
	}
	
	@Test
	public void testGetSyncWithRecurrentEventAlwaysInUpdatedTag() throws FindException, ServerFault {
		ColdWarFixtures fixtures = new ColdWarFixtures();
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327680144000L);
		EventChanges daoChanges = getFakeAllRecurrentEventChanges();
		
		EventChanges sortedChanges = mockGetSyncWithSortedChanges(fixtures,
				calendar, userName, lastSync, daoChanges);
		
		Event[] updatedEvents = sortedChanges.getUpdated();
		
		List<Event> updatedRecurrentEvents = Lists.newArrayList(
				getFakeEvent(RecurrenceKind.daily),
				getFakeEvent(RecurrenceKind.monthlybydate),
				getFakeEvent(RecurrenceKind.monthlybyday),
				getFakeEvent(RecurrenceKind.weekly),
				getFakeEvent(RecurrenceKind.yearly));
		
		Assertions.assertThat(updatedEvents).containsOnly(updatedRecurrentEvents.toArray());
	}

	private EventChanges mockGetSyncWithSortedChanges(
			ColdWarFixtures fixtures, String calendar, String userName,
			Date lastSync, EventChanges daoChanges) throws FindException,
			ServerFault {
		ObmUser user = new ObmUser();

		AccessToken accessToken = mockAccessToken(userName, fixtures.domain);
		
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, fixtures.domainName)).andReturn(user).atLeastOnce();
		
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getSync(accessToken, user, lastSync, null, null, false)).andReturn(daoChanges).once();
		
		Object[] mocks = {calendarDao, accessToken, userService, rightsHelper};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);

		EventChanges sortedChanges = calendarService.getSyncWithSortedChanges(accessToken, calendar, lastSync);
		EasyMock.verify(mocks);
		return sortedChanges;
	}
	
	private EventChanges getFakeEventChanges(RecurrenceKind recurrenceKind) {
		EventChanges newEventChanges = new EventChanges();
		Event updatedEvent = getFakeEvent(recurrenceKind);

		newEventChanges.setUpdated(new Event[]{updatedEvent});
		return newEventChanges;
	}

	private EventChanges getFakeAllRecurrentEventChanges() {
		EventChanges newRecurrentEventChanges = new EventChanges();
		Event[] changedRecurrentEvents = { getFakeEvent(RecurrenceKind.daily),
				getFakeEvent(RecurrenceKind.monthlybydate),
				getFakeEvent(RecurrenceKind.monthlybyday),
				getFakeEvent(RecurrenceKind.weekly),
				getFakeEvent(RecurrenceKind.yearly) };

		newRecurrentEventChanges.setUpdated(changedRecurrentEvents);
		return newRecurrentEventChanges;
	}
	
	private Event getFakeEvent(RecurrenceKind recurrenceKind) {
		Event updatedEvent = new Event();    
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(recurrenceKind);
		updatedEvent.setRecurrence(eventRecurrence);
		updatedEvent.setTimeCreate(new Date(1327680143000L));
		updatedEvent.setTimeUpdate(new Date(1327680144000L));
		updatedEvent.setRecurrenceId(new Date(1327680000000L)); // Fri, 27 Jan 2012 16:00:00 GMT <=> 20120127T160000Z

		Attendee attendee = getFakeAttendee("user2@domain1");
		attendee.setState(ParticipationState.ACCEPTED);
		
		updatedEvent.addAttendee(attendee);
		return updatedEvent;
	}
}
