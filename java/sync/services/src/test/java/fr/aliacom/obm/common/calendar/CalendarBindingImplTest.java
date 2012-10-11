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

import static fr.aliacom.obm.ToolBox.mockAccessToken;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.after;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import net.fortuna.ical4j.data.ParserException;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.State;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.HelperService;

@RunWith(SlowFilterRunner.class)
public class CalendarBindingImplTest {

	private HelperService mockRightsHelper(String calendar, AccessToken accessToken) {
		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		expect(rightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		return rightsHelper;
	}

	private HelperService mockNoRightsHelper(String calendar, AccessToken accessToken) {
		HelperService noRightsHelper = createMock(HelperService.class);
		expect(noRightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(false).anyTimes();
		expect(noRightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(false).anyTimes();
		return noRightsHelper;
	}
	
	private String stripEmail(String email) {
		String strippedEmail = email.substring(0, email.indexOf('@'));
		return strippedEmail;
	}

	@Test
	public void testGetCalendarMetadata() throws ServerFault, FindException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		CalendarInfo beriaInfo = new CalendarInfo();
		beriaInfo.setUid(defaultUser.getLogin());
		beriaInfo.setFirstname("Lavrenti");
		beriaInfo.setLastname("Beria");
		beriaInfo.setMail(defaultUser.getEmail());
		beriaInfo.setRead(true);
		beriaInfo.setWrite(true);
		
		CalendarInfo hooverInfo = new CalendarInfo();
		hooverInfo.setUid("hoover");
		hooverInfo.setFirstname("John");
		hooverInfo.setLastname("Hoover");
		hooverInfo.setMail("hoover@usa");
		hooverInfo.setRead(true);
		hooverInfo.setWrite(false);
		
		CalendarInfo mccarthyInfo = new CalendarInfo();
		mccarthyInfo.setUid("mccarthy");
		mccarthyInfo.setFirstname("Joseph");
		mccarthyInfo.setLastname("McCarthy");
		mccarthyInfo.setMail("mccarthy@usa");
		mccarthyInfo.setRead(true);
		mccarthyInfo.setWrite(false);
		
		String[] calendarEmails = {
				beriaInfo.getMail(),
				hooverInfo.getMail(),
				mccarthyInfo.getMail()
		};
		
		String[] calendarEmailsWithStrippedEmail = {
				hooverInfo.getMail(),
				stripEmail(hooverInfo.getMail()),
				mccarthyInfo.getMail(),
				stripEmail(mccarthyInfo.getMail()),
		};
		
		CalendarInfo[] expectedCalendarInfos = {
				beriaInfo,
				hooverInfo,
				mccarthyInfo,
		};
		
		CalendarInfo[] calendarInfosFromDao = {
				hooverInfo,
				mccarthyInfo,
		};
			
		AccessToken accessToken = mockAccessToken();
		HelperService rightsHelper = createMock(HelperService.class);
		
		rightsHelper.constructEmailFromList(eq(defaultUser.getEmail()), eq(defaultUser.getDomain().getName()));
		EasyMock.expectLastCall().andReturn(defaultUser.getEmail());
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(defaultUser).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(defaultUser), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
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
		CalendarInfo hooverInfo = new CalendarInfo();
		hooverInfo.setUid("hoover");
		hooverInfo.setFirstname("John");
		hooverInfo.setLastname("Hoover");
		hooverInfo.setMail("hoover@usa");
		hooverInfo.setRead(true);
		hooverInfo.setWrite(false);
		
		CalendarInfo mccarthyInfo = new CalendarInfo();
		mccarthyInfo.setUid("mccarthy");
		mccarthyInfo.setFirstname("Joseph");
		mccarthyInfo.setLastname("McCarthy");
		mccarthyInfo.setMail("mccarthy@usa");
		mccarthyInfo.setRead(true);
		mccarthyInfo.setWrite(false);
		
		String[] calendarEmails = {
				hooverInfo.getMail(),
				mccarthyInfo.getMail()
		};
		
		String[] calendarEmailsWithStrippedEmail = {
				hooverInfo.getMail(),
				stripEmail(hooverInfo.getMail()),
				mccarthyInfo.getMail(),
				stripEmail(mccarthyInfo.getMail())
		};
		
		CalendarInfo[] expectedCalendarInfos = {
				hooverInfo,
				mccarthyInfo,
		};
		
		CalendarInfo[] calendarInfosFromDao = {
				hooverInfo,
				mccarthyInfo,
		};
			
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		AccessToken accessToken = mockAccessToken();
		HelperService rightsHelper = createMock(HelperService.class);
		
		rightsHelper.constructEmailFromList(eq(defaultUser.getLogin()), eq(defaultUser.getDomain().getName()));
		EasyMock.expectLastCall().andReturn(defaultUser.getEmail());
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(defaultUser).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(defaultUser), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		EasyMock.expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);
		
		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};
		
		EasyMock.replay(mocks);
		CalendarInfo[] result = calendarService.getCalendarMetadata(accessToken, calendarEmails);
		assertEquals(new HashSet<CalendarInfo>(Arrays.asList(expectedCalendarInfos)), new HashSet<CalendarInfo>(Arrays.asList(result)));
	}

	@Test
	public void testGetResourceMetadata() throws FindException, ServerFault {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		ResourceInfo resource1 = buildResourceInfo1();
		ResourceInfo resource2 = buildResourceInfo2();
		Collection<ResourceInfo> resourceInfo = Arrays.asList(new ResourceInfo[] { resource1,
				resource2 });

		AccessToken accessToken = EasyMock.createMock(AccessToken.class);
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultUser);

		String[] resources = {"res-1@domain.com", "res-2@domain.com"};

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getResourceMetadata(defaultUser, Arrays.asList(resources))).andReturn(resourceInfo);

		Object[] mocks = { accessToken, calendarDao, userService };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService,
				calendarDao, null, null, null, null);
		Assert.assertArrayEquals(new ResourceInfo[] { resource1, resource2 },
				calendarService.getResourceMetadata(accessToken,
						resources));

		EasyMock.verify(mocks);
	}

	@Test
	public void testGetResourceMetadataWithNoResource() throws ServerFault {
		AccessToken accessToken = EasyMock.createMock(AccessToken.class);

		Object[] mocks = { accessToken };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null,
				null, null, null, null, null);
		Assert.assertArrayEquals(new ResourceInfo[0],
				calendarService.getResourceMetadata(accessToken,
						new String[0]));

		EasyMock.verify(mocks);
	}

	@Test(expected=ServerFault.class)
	public void testCalendarOwnerNotAnAttendee() throws ServerFault, FindException, EventAlreadyExistException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		EventExtId eventExtId = new EventExtId("extid");
		
		AccessToken accessToken = mockAccessToken();
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		expect(rightsHelper.canWriteOnCalendar(accessToken, defaultUser.getEmail())).andReturn(false);
		
		final Event event = createMock(Event.class);
		expect(event.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(event.getObmId()).andReturn(null).atLeastOnce();
		expect(event.isInternalEvent()).andReturn(false).atLeastOnce();
		expect(event.getTitle()).andReturn("title").atLeastOnce();
		expect(event.getAttendees()).andReturn(ImmutableList.of(ToolBox.getFakeAttendee(defaultUser.getEmail()))).atLeastOnce();
		expect(event.getEventsExceptions()).andReturn(ImmutableList.<Event>of());
		
		event.findAttendeeFromEmail(defaultUser.getEmail());
		EasyMock.expectLastCall().andReturn(null).atLeastOnce();
		
		final UserService userService = createMock(UserService.class);
		userService.getUserFromCalendar(eq(calendar), eq(defaultUser.getDomain().getName()));
		EasyMock.expectLastCall().andReturn(defaultUser).atLeastOnce();

		final CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.findEventByExtId(eq(accessToken), eq(defaultUser), eq(eventExtId));
		EasyMock.expectLastCall().andReturn(null).once();

		Object[] mocks = {event, accessToken, userService, calendarDao, rightsHelper};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);

		try {
			calendarService.createEvent(accessToken, calendar, event, true);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			assertThat(e.getMessage()).contains("doesn't involve calendar owner, ignoring creation");
			throw e;
		}
	}
	
	@Test
	public void testImportEventInThePast() throws ImportICalendarException, ServerFault, IOException, ParserException, 
		FindException, SQLException {
		
		Ical4jUser ical4jUser = ServicesToolBox.getIcal4jUser();
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		fakeUserAttendee.setParticipation(Participation.NEEDSACTION);
		
		AccessToken accessToken = mockAccessToken();
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(true).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		expect(calendarFactory.createIcal4jUserFromObmUser(defaultUser)).andReturn(ical4jUser).anyTimes();		
		
		HelperService rightsHelper = mockRightsHelper(defaultUser.getLogin(), accessToken);
		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, defaultUser.getLogin(), defaultUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, defaultUser.getLogin(), defaultUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, calendarDao,
				calendarFactory};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao,
				null, rightsHelper, ical4jHelper, calendarFactory);
		
		try {
			calendarService.importICalendar(accessToken, defaultUser.getLogin(), icsData);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(Participation.ACCEPTED, fakeUserAttendee.getParticipation());
	}
	
	@Test
	public void testPurge() throws FindException, ServerFault, SQLException, NumberFormatException, EventNotFoundException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		EventExtId oldEventNoOtherAttendeesExtId = new EventExtId("oldEventNoOtherAttendeesExtId");
		EventExtId oldEventWithOtherAttendeesExtId = new EventExtId("oldEventWithOtherAttendeesExtId");
		EventObmId oldEventNoOtherAttendeesUid = new EventObmId("1");
		EventObmId oldEventWithOtherAttendeesUid = new EventObmId("2");

		String otherUserEmail = "user2@domain1";
		Attendee userAttendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		Attendee otherAttendee = ToolBox.getFakeAttendee(otherUserEmail);
		userAttendee.setParticipation(Participation.NEEDSACTION);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());

		final Calendar oldEventDate = Calendar.getInstance();
		oldEventDate.add(Calendar.MONTH, -8);

		Event oldEventNoOtherAttendees = new Event();
		oldEventNoOtherAttendees.setExtId(oldEventNoOtherAttendeesExtId);
		oldEventNoOtherAttendees.setUid(oldEventNoOtherAttendeesUid);
		oldEventNoOtherAttendees.setAttendees(ImmutableList.of(userAttendee));
		oldEventNoOtherAttendees.setOwner(defaultUser.getEmail());
		oldEventNoOtherAttendees.setType(EventType.VEVENT);
		oldEventNoOtherAttendees.setInternalEvent(true);

		Event oldEventWithOtherAttendees = new Event();
		oldEventWithOtherAttendees.setExtId(oldEventWithOtherAttendeesExtId);
		oldEventWithOtherAttendees.setUid(oldEventWithOtherAttendeesUid);
		oldEventWithOtherAttendees.setAttendees(ImmutableList.of(userAttendee, otherAttendee));
		oldEventWithOtherAttendees.setOwner(defaultUser.getEmail());
		oldEventWithOtherAttendees.setType(EventType.VEVENT);

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.delete(oldEventNoOtherAttendees, false, accessToken);
		eventChangeHandler.updateParticipation(oldEventWithOtherAttendees, defaultUser,
				Participation.DECLINED, false, accessToken);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(userService.getUserFromLogin(defaultUser.getEmail(), defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.listEventsByIntervalDate(eq(accessToken), eq(defaultUser), isA(Date.class), 
				isA(Date.class), (EventType)isNull())).
			andReturn(ImmutableList.of(oldEventNoOtherAttendees, oldEventWithOtherAttendees)).once();
		expect(calendarDao.findEventById(accessToken, oldEventNoOtherAttendeesUid)).
			andReturn(oldEventNoOtherAttendees).atLeastOnce();
		expect(calendarDao.removeEventById(accessToken, oldEventNoOtherAttendeesUid, 
				oldEventNoOtherAttendees.getType(), oldEventNoOtherAttendees.getSequence()+1)).
				andReturn(oldEventNoOtherAttendees);
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, oldEventWithOtherAttendeesExtId)).
			andReturn(oldEventWithOtherAttendees).atLeastOnce();
		expect(calendarDao.changeParticipation(accessToken, defaultUser,
				oldEventWithOtherAttendees.getExtId(), Participation.DECLINED)).andReturn(true);		

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		Object[] mocks = { accessToken, userService, rightsHelper, calendarDao, eventChangeHandler };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null, null);

		calendarService.purge(accessToken, calendar);

		EasyMock.verify(mocks);
	}

	@Test
	public void testImportEventInTheFuture() throws ImportICalendarException, ServerFault, IOException, 
		ParserException, FindException, SQLException {
		
		Ical4jUser ical4jUser = ServicesToolBox.getIcal4jUser();
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		fakeUserAttendee.setParticipation(Participation.NEEDSACTION);
		
		AccessToken accessToken = mockAccessToken();
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(false).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		HelperService rightsHelper = mockRightsHelper(defaultUser.getLogin(), accessToken);
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		expect(calendarFactory.createIcal4jUserFromObmUser(defaultUser)).andReturn(ical4jUser).anyTimes();

		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, defaultUser.getLogin(), defaultUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, defaultUser.getLogin(), defaultUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, 
				calendarDao, calendarFactory};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null,
				rightsHelper, ical4jHelper, calendarFactory);
		
		try {
			calendarService.importICalendar(accessToken, defaultUser.getLogin(), icsData);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(Participation.NEEDSACTION, fakeUserAttendee.getParticipation());
	}

	@Test
	public void testModifyNullEvent() throws ServerFault {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		EasyMock.replay(accessToken);

		Event modifiedEvent = calendarService.modifyEvent(accessToken, calendar, null, false, false);

		assertThat(modifiedEvent).isNull();
	}

	@Test
	public void testModifyNotExistingEvent() throws FindException, EventNotFoundException, ServerFault {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";
		Event event = new Event();

		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		final UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser)
						.once();
		CalendarDao calendarDao = createMock(CalendarDao.class);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, null, null, null);
		expect(calendarService.loadCurrentEvent(accessToken, defaultObmUser, event)).andReturn(null).once();

		EasyMock.replay(accessToken, userService, calendarDao);

		Event modifiedEvent = calendarService.modifyEvent(accessToken, calendar, event, false, false);
		assertThat(modifiedEvent).isNull();
	}

	@Test
	public void testToModifyEventWithoutWriteRightOnCalendar() throws FindException, EventNotFoundException, ServerFault {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = defaultObmUser.getEmail();
		Event event = new Event();
		event.setOwner("user");

		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		final UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser)
						.once();

		CalendarDao calendarDao = createMock(CalendarDao.class);

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(accessToken, calendar))
				.andReturn(false).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null, null);
		expect(calendarService.loadCurrentEvent(accessToken, defaultObmUser, event)).andReturn(event).once();

		EasyMock.replay(accessToken, userService, calendarDao, rightsHelper);

		Event modifiedEvent = calendarService.modifyEvent(accessToken, calendar, event, false, false);
		assertThat(modifiedEvent).isEqualTo(event);
	}

	@Test
	public void testAttendeeHasRightToWriteOnCalendar() throws FindException, ServerFault, SQLException, EventNotFoundException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		
		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		attendee.setParticipation(Participation.NEEDSACTION);
		
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
		
		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.canWriteOnCalendar(accessToken, defaultUser.getEmail())).andReturn(true).anyTimes();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event, updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		Assert.assertEquals(Participation.ACCEPTED, newEvent.getAttendees().get(0).getParticipation());
		Assert.assertEquals(1, beforeEvent.getAttendees().size());
		Assert.assertEquals(true, beforeEvent.getAttendees().iterator().next().isCanWriteOnCalendar());
		
		Assert.assertEquals(1, event.getAttendees().size());
		Assert.assertEquals(true, event.getAttendees().iterator().next().isCanWriteOnCalendar());
	}

	@Test
	public void testAttendeeHasNoRightToWriteOnCalendar() throws FindException, ServerFault, SQLException, EventNotFoundException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		attendee.setParticipation(Participation.ACCEPTED);

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

		AccessToken accessToken = ToolBox.mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false).anyTimes();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();

		eventChangeHandler.update(beforeEvent, event, notification, accessToken);
		EasyMock.expectLastCall();

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.NEEDSACTION, newEvent.getAttendees().get(0)
				.getParticipation());
	}

	@Test
	public void testAttendeeOfExceptionHasRightToWriteOnCalendar() throws FindException,
			ServerFault, SQLException, EventNotFoundException {
		
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		attendee.setParticipation(Participation.NEEDSACTION);

		Attendee exceptionAttendee = ToolBox.getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setParticipation(Participation.ACCEPTED);

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
		exception.setStartDate(recurrenceId);
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
		dummyException.setStartDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, exceptionAttendee.getEmail()))
				.andReturn(true).atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.NEEDSACTION,
				Iterables.getOnlyElement(newEvent.getAttendees()).getParticipation());
		Event afterException = Iterables.getOnlyElement(newEvent.getRecurrence()
				.getEventExceptions());
		Attendee afterExceptionAttendee = afterException
				.findAttendeeFromEmail(exceptionAttendeeEmail);
		Assert.assertEquals(Participation.ACCEPTED, afterExceptionAttendee.getParticipation());
		Assert.assertEquals(true, afterExceptionAttendee.isCanWriteOnCalendar());
	}

	@Test
	public void testAttendeeOfExceptionHasNoRightToWriteOnCalendar() throws FindException, ServerFault, SQLException, 
	EventNotFoundException {
		
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		attendee.setParticipation(Participation.NEEDSACTION);

		Attendee exceptionAttendee = ToolBox.getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setParticipation(Participation.NEEDSACTION);

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
		exception.setStartDate(recurrenceId);
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
		dummyException.setStartDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, exceptionAttendee.getEmail()))
				.andReturn(false).atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.NEEDSACTION,
				Iterables.getOnlyElement(newEvent.getAttendees()).getParticipation());
		Event afterException = Iterables.getOnlyElement(newEvent.getRecurrence()
				.getEventExceptions());
		Attendee afterExceptionAttendee = afterException
				.findAttendeeFromEmail(exceptionAttendeeEmail);
		Assert.assertEquals(Participation.NEEDSACTION, afterExceptionAttendee.getParticipation());
		Assert.assertEquals(false, afterExceptionAttendee.isCanWriteOnCalendar());
	}
	
	@Test
	public void testCreateAnEventExceptionAndUpdateItsStatusButNotTheParent() throws FindException, SQLException, EventNotFoundException, ServerFault {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		String attendeeEmail = "attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.lookup("daily"));

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmail());
		attendee.setParticipation(Participation.ACCEPTED);
		Attendee attendee2 = ToolBox.getFakeAttendee(attendeeEmail);
		attendee2.setParticipation(Participation.ACCEPTED);
		
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
		
		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(true)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee2.getEmail())).andReturn(false)
		.atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);
		EasyMock.expectLastCall().atLeastOnce();
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.ACCEPTED, newEvent.getAttendees().get(0)
				.getParticipation());		
		Assert.assertEquals(Participation.NEEDSACTION, newEvent.getRecurrence().getEventExceptions().get(0).getAttendees().get(1)
				.getParticipation());
	}
	
	public void testDontSendEmailsAndDontUpdateStatusForUnimportantChanges() throws ServerFault, FindException, SQLException, EventNotFoundException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		String userEmail = "user@domain1";
		String guestAttendee1Email = "guestAttendee1@domain1";
		String guestAttendee2Email = "guestAttendee2@domain1";
		EventExtId eventExtId = new EventExtId("extid");
		EventObmId eventUid = new EventObmId("0");
		int sequence = 2;

		Attendee userAttendee = new Attendee();
		userAttendee.setEmail(userEmail);
		userAttendee.setParticipation(Participation.ACCEPTED);

		Attendee guestAttendee1 = new Attendee();
		guestAttendee1.setEmail(guestAttendee1Email);
		guestAttendee1.setParticipation(Participation.ACCEPTED);

		Attendee guestAttendee2 = new Attendee();
		guestAttendee2.setEmail(guestAttendee2Email);
		guestAttendee2.setParticipation(Participation.NEEDSACTION);

		boolean updateAttendees = true;
		boolean notification = true;

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

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, eventExtId)).andReturn(oldEvent).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, newEvent, updateAttendees, sequence, true)).andReturn(newEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		Object[] mocks = {accessToken, calendarDao, userService, rightsHelper, eventChangeHandler};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null, null);

		calendarService.modifyEvent(accessToken, calendar, newEvent, updateAttendees, notification);

		EasyMock.verify(mocks);

		Assert.assertEquals(Participation.ACCEPTED, userAttendee.getParticipation());
		Assert.assertEquals(Participation.ACCEPTED, guestAttendee1.getParticipation());
		Assert.assertEquals(Participation.NEEDSACTION, guestAttendee2.getParticipation());
	}

	@Test
	public void testListResources() throws FindException, ServerFault {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		ResourceInfo resource1 = buildResourceInfo1();
		ResourceInfo resource2 = buildResourceInfo2();
		Collection<ResourceInfo> resourceInfo = Arrays.asList(new ResourceInfo[] { resource1,
				resource2 });

		AccessToken accessToken = EasyMock.createMock(AccessToken.class);
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultUser);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.listResources(defaultUser)).andReturn(resourceInfo);

		Object[] mocks = { accessToken, calendarDao, userService };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService,
				calendarDao, null, null, null, null);
		Assert.assertArrayEquals(new ResourceInfo[] { resource1, resource2 },
				calendarService.listResources(accessToken));

		EasyMock.verify(mocks);
	}
	
	private Ical4jHelper mockIcal4jHelper(Ical4jUser ical4jUser, String icsData, Event eventWithOwnerAttendee) throws IOException, ParserException{
		Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
		expect(ical4jHelper.parseICS(icsData, ical4jUser)).andReturn(ImmutableList.of(eventWithOwnerAttendee)).once();
		return ical4jHelper;
	}
	
	private UserService mockImportICSUserService(AccessToken accessToken, Attendee fakeUserAttendee, String calendar, ObmUser obmUser) throws FindException{
		UserService userService = createMock(UserService.class);
		String domainName = obmUser.getDomain().getName();
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
		assertThat(result.getObmId()).isNull();
		assertThat(result.getExtId()).isSameAs(extId);
	}
	
	@Test
	public void testParseICSIncludeEventObmId() throws Exception {
		Event eventFromDao = new Event();
		EventObmId eventObmId = new EventObmId(12);
		eventFromDao.setUid(eventObmId);

		EventExtId extId = new EventExtId("extId");
		
		Event result = testParseICS(extId, eventFromDao);
		assertThat(result.getObmId()).isSameAs(eventObmId);
		assertThat(result.getExtId()).isSameAs(extId);
	}
	
	private Event testParseICS(EventExtId extId, Event eventFromDao) throws Exception {
		Ical4jUser ical4jUser = ServicesToolBox.getIcal4jUser();
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "toto";
		String email = calendar + "@" + defaultUser.getDomain().getName();
		String ics = "icsData";
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(email);
		
		Event eventFromIcs = new Event();
		eventFromIcs.setExtId(extId);
		
		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = mockRightsHelper(calendar, accessToken);
		
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();
		
		ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
		expect(calendarFactory.createIcal4jUserFromObmUser(obmUser)).andReturn(ical4jUser).anyTimes();
		
		Ical4jHelper ical4jHelper = mockIcal4jHelper(ical4jUser, ics, eventFromIcs);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, extId)).andReturn(eventFromDao).once();

		Object[] mocks = new Object[] {calendarDao, userService, ical4jHelper, accessToken, helper, calendarFactory};
		
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, helper, ical4jHelper, calendarFactory);
		List<Event> events = calendarService.parseICS(accessToken, ics);
		
		EasyMock.verify(mocks);
		
		assertThat(events).hasSize(1);
		Event result = events.get(0);
		return result;
	}
	
	@Test
	public void testCreateExternalEventCalendarOwnerWithDeclinedPartState() throws FindException, ServerFault, EventAlreadyExistException, SQLException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		
		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean notification = false;
		
		Attendee calOwner = ToolBox.getFakeAttendee(defaultUser.getEmail());
		calOwner.setParticipation(Participation.DECLINED);
		
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
		
		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = mockRightsHelper(calendar, accessToken);
		expect(helper.canWriteOnCalendar(accessToken, defaultUser.getEmail())).andReturn(false);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, event, false)).andReturn(eventCreated).once();
		expect(calendarDao.removeEvent(accessToken, eventCreated, eventCreated.getType(), eventCreated.getSequence())).andReturn(eventCreated).once();
		eventChangeHandler.updateParticipation(eventCreated, defaultUser, calOwner.getParticipation(), notification, accessToken);
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null, null);
		calendarService.createEvent(accessToken, calendar, event, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
	}
	
	@Test
	public void testRecurrenceIdAtTheProperFormatInGetSyncResponse() throws FindException, ServerFault{
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327690144000L);
		EventChanges daoChanges = getFakeEventChanges(RecurrenceKind.none);
		
		EventChanges sortedChanges = mockGetSyncWithSortedChanges(calendar, userName, lastSync, daoChanges);
		
		List<ParticipationChanges> participationUpdated = sortedChanges.getParticipationUpdated();
		Assert.assertEquals(participationUpdated.get(0).getRecurrenceId().serializeToString(), "20120127T160000Z");
	}
	
	@Test
	public void testGetSyncWithRecurrentEventAlwaysInUpdatedTag() throws FindException, ServerFault {
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327680144000L);
		EventChanges daoChanges = getFakeAllRecurrentEventChanges();
		
		EventChanges sortedChanges = mockGetSyncWithSortedChanges(calendar, userName, lastSync, daoChanges);
		
		List<Event> updatedEvents = sortedChanges.getUpdated();
		
		List<Event> updatedRecurrentEvents = Lists.newArrayList(
				getFakeEvent(RecurrenceKind.daily),
				getFakeEvent(RecurrenceKind.monthlybydate),
				getFakeEvent(RecurrenceKind.monthlybyday),
				getFakeEvent(RecurrenceKind.weekly),
				getFakeEvent(RecurrenceKind.yearly));
		
		assertThat(updatedEvents).containsOnly(updatedRecurrentEvents.toArray());
	}

	private EventChanges mockGetSyncWithSortedChanges(String calendar, String userName,
			Date lastSync, EventChanges daoChanges) throws FindException,
			ServerFault {
		
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		AccessToken accessToken = mockAccessToken(userName, defaultUser.getDomain());
		
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		
		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getSync(accessToken, defaultUser, lastSync, null, null, false)).andReturn(daoChanges).once();
		
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

		newEventChanges.setUpdated(Lists.newArrayList(updatedEvent));
		return newEventChanges;
	}

	private EventChanges getFakeAllRecurrentEventChanges() {
		EventChanges newRecurrentEventChanges = new EventChanges();
		List<Event> changedRecurrentEvents = Lists.newArrayList(getFakeEvent(RecurrenceKind.daily),
				getFakeEvent(RecurrenceKind.monthlybydate),
				getFakeEvent(RecurrenceKind.monthlybyday),
				getFakeEvent(RecurrenceKind.weekly),
				getFakeEvent(RecurrenceKind.yearly));

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

		Attendee attendee = ToolBox.getFakeAttendee("user2@domain1");
		attendee.setParticipation(Participation.ACCEPTED);
		
		updatedEvent.addAttendee(attendee);
		return updatedEvent;
	}

	@Test(expected=ServerFault.class)
	public void testCreateNullEvent() throws ServerFault, EventAlreadyExistException {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		try {
			calendarService.createEvent(accessToken, calendar, null, false);
		} catch (ServerFault e) {
			assertThat(e.getMessage()).isEqualTo("event creation without any data");
			throw e;
		}
	}

	@Test(expected=ServerFault.class)
	public void testCreateEventWithObmId() throws ServerFault, EventAlreadyExistException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event event = new Event();
		event.setUid(new EventObmId(42));
		try {
			calendarService.createEvent(accessToken, calendar, event, false);
		} catch (ServerFault e) {
			assertThat(e.getMessage()).isEqualTo("event creation with an event coming from OBM");
			throw e;
		}
	}

	@Test(expected=EventAlreadyExistException.class)
	public void testCreateDuplicateEvent() throws ServerFault, EventAlreadyExistException, FindException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event event = new Event();
		event.setExtId(new EventExtId("123"));
		event.setStartDate(new Date());

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, defaultObmUser, event.getExtId())).andReturn(event).once();

		EasyMock.replay(accessToken, userService, calendarDao);
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, null, null, null);

		calendarService.createEvent(accessToken, calendar, event, false);
	}

	@Test(expected=ServerFault.class)
	public void testCreateUnauthorizedEventOnCalendar() throws ServerFault, EventAlreadyExistException, FindException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event event = new Event();
		event.setExtId(new EventExtId("123"));
		event.setStartDate(new Date());

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, defaultObmUser, event.getExtId())).andReturn(null).once();

		HelperService helperService = mockNoRightsHelper(calendar, accessToken);

		EasyMock.replay(accessToken, userService, calendarDao, helperService);
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, helperService, null, null);

		try {
			calendarService.createEvent(accessToken, calendar, event, false);
		} catch (ServerFault e) {
			assertThat(e.getMessage()).contains("no write right");
			throw e;
		}
	}

	@Test
	public void testCreateInternalEvent() throws EventNotFoundException, ServerFault, FindException, SQLException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event internalEvent = new Event();
		internalEvent.setUid(new EventObmId(1));

		Event expectedEvent = new Event();
		expectedEvent.setUid(new EventObmId(1));

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.createEvent(accessToken, calendar, internalEvent, true)).andReturn(internalEvent).once();
		expect(calendarDao.findEventById(accessToken, internalEvent.getObmId())).andReturn(internalEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.create(internalEvent, false, accessToken);

		EasyMock.replay(calendarDao, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, null, calendarDao, null, null, null, null);
		Event createdEvent = calendarService.createInternalEvent(accessToken, calendar, internalEvent, false);

		EasyMock.verify(calendarDao, eventChangeHandler);

		assertThat(createdEvent).isEqualTo(expectedEvent);
	}

	@Test
	public void testChangePartipationStateNoRightsOnAttendees() {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		HelperService noRightsHelper = createMock(HelperService.class);
		expect(noRightsHelper.canWriteOnCalendar(eq(accessToken), EasyMock.anyObject(String.class))).andReturn(false).anyTimes();

		EasyMock.replay(accessToken, noRightsHelper);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, noRightsHelper, null, null);
		calendarService.assignDelegationRightsOnAttendees(accessToken, after);
		calendarService.applyParticipationModifications(before, after);

		List<Attendee> attendeesToTest = after.getAttendees();
		assertThat(attendeesToTest).hasSize(3);

		Participation needsaction = 
				new Participation(new Comment(""), State.NEEDSACTION);
		for(Attendee attendee: attendeesToTest) {
			assertThat(attendee.getParticipation()).isEqualTo(needsaction);
			assertThat(attendee.isCanWriteOnCalendar()).isEqualTo(false);
		}
	}

	@Test
	public void testChangeParticipationHasRightOnOneAttendee() {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		Attendee beriaAttendee = before.getAttendees().get(0);
		Attendee hooverAttendee = before.getAttendees().get(1);
		Attendee mccarthyAttendee = before.getAttendees().get(2);

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(accessToken, beriaAttendee.getEmail()))
				.andReturn(true).atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(accessToken, hooverAttendee.getEmail()))
				.andReturn(false).atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(accessToken, mccarthyAttendee.getEmail()))
				.andReturn(false).atLeastOnce();

		EasyMock.replay(accessToken, rightsHelper);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, rightsHelper, null, null);
		calendarService.assignDelegationRightsOnAttendees(accessToken, after);
		calendarService.applyParticipationModifications(before, after);

		List<Attendee> attendeesToTest = after.getAttendees();
		assertThat(attendeesToTest).hasSize(3);
		Attendee beria = attendeesToTest.get(0);
		Attendee hoover = attendeesToTest.get(1);
		Attendee mccarthy = attendeesToTest.get(2);

		assertThat(beria.isCanWriteOnCalendar()).isEqualTo(true);
		Assert.assertEquals(beria.getParticipation(), Participation.ACCEPTED);
		assertThat(hoover.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(hoover.getParticipation(), Participation.NEEDSACTION);
		assertThat(mccarthy.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(mccarthy.getParticipation(), Participation.NEEDSACTION);
	}

	@Test
	public void testChangeParticipationRecurrentEventWithException() {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		Event beforeRecurrentEvent = new Event();
		beforeRecurrentEvent.setRecurrence(new EventRecurrence());
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();
		beforeRecurrentEvent.setAttendees(attendees);

		Event occurrence = beforeRecurrentEvent.getOccurrence(new Date());
		beforeRecurrentEvent.addEventException(occurrence);

		Event afterRecurrentEvent = beforeRecurrentEvent.clone();
		afterRecurrentEvent.setLocation("a location");

		Attendee beriaAttendee = beforeRecurrentEvent.getAttendees().get(0);
		Attendee hooverAttendee = beforeRecurrentEvent.getAttendees().get(1);
		Attendee mccarthyAttendee = beforeRecurrentEvent.getAttendees().get(2);

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(accessToken, beriaAttendee.getEmail()))
				.andReturn(true).atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(accessToken, hooverAttendee.getEmail()))
		.andReturn(false).atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(accessToken, mccarthyAttendee.getEmail()))
		.andReturn(false).atLeastOnce();

		EasyMock.replay(accessToken, rightsHelper);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, rightsHelper, null, null);
		calendarService.assignDelegationRightsOnAttendees(accessToken, afterRecurrentEvent);
		calendarService.applyParticipationModifications(beforeRecurrentEvent, afterRecurrentEvent);

		EventRecurrence eventRecurrence = afterRecurrentEvent.getRecurrence();
		Event exception = Iterables.getOnlyElement(eventRecurrence.getEventExceptions());

		List<Attendee> attendeesToTest = exception.getAttendees();
		assertThat(attendeesToTest).hasSize(3);
		Attendee beria = attendeesToTest.get(0);
		Attendee hoover = attendeesToTest.get(1);
		Attendee mccarthy = attendeesToTest.get(2);

		assertThat(beria.isCanWriteOnCalendar()).isEqualTo(true);
		Assert.assertEquals(beria.getParticipation(), Participation.ACCEPTED);
		assertThat(hoover.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(hoover.getParticipation(), Participation.NEEDSACTION);
		assertThat(mccarthy.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(mccarthy.getParticipation(), Participation.NEEDSACTION);
	}

	@Test
	public void testNegativeExceptionChange() throws ServerFault, SQLException, FindException,
			EventNotFoundException {
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();

		Attendee userAttendee = ToolBox.getFakeAttendee(user.getEmail());
		userAttendee.setParticipation(Participation.ACCEPTED);
		Attendee angletonAttendee = ToolBox.getFakeAttendee("james.jesus.angleton");
		angletonAttendee.setParticipation(Participation.ACCEPTED);
		Attendee dullesAttendee = ToolBox.getFakeAttendee("allen.dulles");
		dullesAttendee.setParticipation(Participation.ACCEPTED);

		int previousSequence = 0;
		Date eventDate = after();
		AccessToken token = ToolBox.mockAccessToken();
		Event previousEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, previousSequence,
				userAttendee, angletonAttendee, dullesAttendee);
		previousEvent.setInternalEvent(true);

		int currentSequence = previousSequence + 1;
		Event currentEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, currentSequence,
				userAttendee, angletonAttendee, dullesAttendee);
		Date exceptionDate = new DateTime(eventDate).plusMonths(1).toDate();
		currentEvent.addException(exceptionDate);
		currentEvent.setInternalEvent(true);

		UserService userService = createMock(UserService.class);
		EasyMock.expect(userService.getUserFromCalendar(user.getEmail(), "test.tlse.lng"))
				.andReturn(user).atLeastOnce();

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(token, calendar)).andReturn(true).once();
		expect(rightsHelper.eventBelongsToCalendar(previousEvent, calendar)).andReturn(true).once();
		expect(rightsHelper.canWriteOnCalendar(token, userAttendee.getEmail())).andReturn(true)
				.atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(token, angletonAttendee.getEmail())).andReturn(true)
				.atLeastOnce();
		expect(rightsHelper.canWriteOnCalendar(token, dullesAttendee.getEmail())).andReturn(true)
				.atLeastOnce();

		boolean notification = true;
		EventChangeHandler eventChangeHandler = EasyMock.createMock(EventChangeHandler.class);
		eventChangeHandler.update(previousEvent, currentEvent, notification, token);

		boolean updateAttendees = true;

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(token, user, currentEvent.getExtId())).andReturn(
				previousEvent).once();
		expect(
				calendarDao.modifyEventForcingSequence(token, user.getEmail(), currentEvent,
						updateAttendees, currentEvent.getSequence(), true)).andReturn(currentEvent);

		EasyMock.replay(token, eventChangeHandler, userService, rightsHelper, calendarDao);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, rightsHelper, null, null);
		calendarService.modifyEvent(token, calendar, currentEvent, updateAttendees,
				notification);
	}

	@Test
	public void testRemoveEventByExtIdIsDeclined() throws FindException, ServerFault, SQLException {
		String calendar = "user@test";
		ObmUser user = ToolBox.getDefaultObmUser();
		AccessToken token = ToolBox.mockAccessToken();

		Event daoEvent = new Event();
		daoEvent.setInternalEvent(false);

		Attendee attendee = new Attendee();
		attendee.setParticipation(Participation.ACCEPTED);
		attendee.setEmail(user.getEmail());
		daoEvent.addAttendee(attendee);
		daoEvent.setOwner(calendar);

		UserService userService = createMock(UserService.class);
		EasyMock.expect(userService.getUserFromCalendar(user.getEmail(), "test.tlse.lng"))
				.andReturn(user).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(token, user, daoEvent.getExtId())).andReturn(
				daoEvent).once();

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(token, user.getEmail())).andReturn(true)
				.once();

		EasyMock.expect(userService.getUserFromLogin(daoEvent.getOwner(), "test.tlse.lng"))
		.andReturn(user).once();

		expect(calendarDao.removeEventByExtId(token, user, daoEvent.getExtId(), 1)).andReturn(
				daoEvent).once();

		EventChangeHandler eventChangeHandler = EasyMock.createMock(EventChangeHandler.class);
		eventChangeHandler.updateParticipation(daoEvent, user, Participation.DECLINED, true, token);

		EasyMock.replay(token, userService, rightsHelper, calendarDao,eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, rightsHelper, null, null);
		Event removedEvent = calendarService.removeEventByExtId(token, calendar, daoEvent.getExtId(), 0, true);

		Attendee calendarOwnerAsAttendee = removedEvent.findAttendeeFromEmail(attendee.getEmail());
		assertThat(calendarOwnerAsAttendee.getParticipation()).isEqualTo(Participation.DECLINED);
	}

	@Test
	public void testReadOnlyCalendarWithPrivateEventsIsAnonymized() throws ServerFault,
			FindException {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();
		AccessToken token = ToolBox.mockAccessToken();

		Date timeCreate = new DateTime(1974, Calendar.SEPTEMBER, 4, 14, 0).toDate();
		Date lastSyncDate = new DateTime(1973, Calendar.SEPTEMBER, 4, 14, 0).toDate();
		Date syncDateFromDao = new DateTime(lastSyncDate).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = new DeletedEvent(new EventObmId(1), new EventExtId(
				"deleted event 1"));
		DeletedEvent deletedEvent2 = new DeletedEvent(new EventObmId(2), new EventExtId(
				"deleted event 2"));

		Event publicUpdatedEvent1 = new Event();
		publicUpdatedEvent1.setTitle("publicUpdatedEvent1");
		publicUpdatedEvent1.setTimeCreate(timeCreate);
		Event publicUpdatedEvent2 = new Event();
		publicUpdatedEvent2.setTitle("publicUpdatedEvent2");
		publicUpdatedEvent2.setTimeCreate(timeCreate);
		Event privateUpdatedEvent1 = new Event();
		privateUpdatedEvent1.setTitle("privateUpdatedEvent1");
		privateUpdatedEvent1.setPrivacy(EventPrivacy.PRIVATE);
		privateUpdatedEvent1.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao = new EventChanges();
		eventChangesFromDao.setLastSync(syncDateFromDao);
		eventChangesFromDao.setDeletedEvents(Lists.newArrayList(deletedEvent1, deletedEvent2));
		eventChangesFromDao.setParticipationUpdated(new ArrayList<ParticipationChanges>());
		eventChangesFromDao.setUpdated(Lists.newArrayList(publicUpdatedEvent1, publicUpdatedEvent2,
				privateUpdatedEvent1));

		Event privateUpdatedAndAnonymizedEvent1 = new Event();
		privateUpdatedAndAnonymizedEvent1.setTitle(null);
		privateUpdatedAndAnonymizedEvent1.setPrivacy(EventPrivacy.PRIVATE);
		privateUpdatedAndAnonymizedEvent1.setTimeCreate(timeCreate);

		EventChanges anonymizedEventChanges = new EventChanges();
		anonymizedEventChanges.setLastSync(syncDateFromDao);
		anonymizedEventChanges.setDeletedEvents(Lists.newArrayList(deletedEvent1, deletedEvent2));
		anonymizedEventChanges.setParticipationUpdated(new ArrayList<ParticipationChanges>());
		anonymizedEventChanges.setUpdated(Lists.newArrayList(publicUpdatedEvent1,
				publicUpdatedEvent2, privateUpdatedAndAnonymizedEvent1));

		UserService userService = createMock(UserService.class);
		EasyMock.expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user)
				.atLeastOnce();

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canReadCalendar(token, calendar)).andReturn(true).once();
		expect(rightsHelper.canWriteOnCalendar(token, calendar)).andReturn(false).once();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getSync(token, user, lastSyncDate, null, null, false)).andReturn(
				eventChangesFromDao);

		Object[] mocks = { token, userService, calendarDao, rightsHelper };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService,
				calendarDao, null, rightsHelper, null, null);

		EventChanges actualChanges = calendarService.getSyncWithSortedChanges(token, calendar,
				lastSyncDate);
		EasyMock.verify(mocks);
		assertThat(actualChanges).isEqualTo(anonymizedEventChanges);
	}
	
	@Test
	public void testEventCanBeModifiedWhenCannotWriteOnCalendar() {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();
		Event eventToModify = new Event();
		
		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false);
		EasyMock.replay(helperService);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, helperService, null, null);
		
		boolean eventCanBeModified = calendarService.eventCanBeModified(token, calendar, eventToModify);
		
		assertThat(eventCanBeModified).isFalse();
	}
	
	@Test
	public void testEventCanBeModifiedWhenEventDoesNotBelongToCalendar() {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();
		Event eventToModify = new Event();
		
		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(false);
		EasyMock.replay(helperService, token);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, helperService, null, null);
		
		boolean eventCanBeModified = calendarService.eventCanBeModified(token, calendar, eventToModify);
		
		assertThat(eventCanBeModified).isFalse();
	}
	
	@Test
	public void testEventCanBeModifiedWhenRequirementsAreOK() {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();
		Event eventToModify = new Event();
		
		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(true);
		EasyMock.replay(helperService);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, helperService, null, null);
		
		boolean eventCanBeModified = calendarService.eventCanBeModified(token, calendar, eventToModify);
		
		assertThat(eventCanBeModified).isTrue();
	}
	
	@Test
	public void testEventCanBeModifiedWhenEventBelongsToEditorInAnotherCalendar() {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();
		Event eventToModify = new Event();
		
		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(false);
		EasyMock.replay(helperService, token);
		
		eventToModify.setOwnerEmail(token.getUserEmail());
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, helperService, null, null);
		
		boolean eventCanBeModified = calendarService.eventCanBeModified(token, calendar, eventToModify);
		
		assertThat(eventCanBeModified).isTrue();
	}
	
	@Test
		public void testInheritsParticipationForSpecificAttendee() {
			Attendee expectedAttendee = Attendee.builder()
				.email("attendee@test.lng")
				.participation(Participation.NEEDSACTION)
				.build();
			Event event = createEvent(Arrays.asList(expectedAttendee));

			CalendarBindingImpl calendarService =
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);

			Attendee attendee = Attendee.builder()
				.email("attendee@test.lng")
				.participation(Participation.ACCEPTED)
				.build();
			calendarService.inheritsParticipationForSpecificAttendee(event, attendee);
			assertThat(attendee).isEqualTo(expectedAttendee);
		}
	
	@Test
		public void testInheritsParticipationFromExistingEventEmptyAttendees() {
			Event before = new Event();
			Event after = new Event();

			CalendarBindingImpl calendarService =
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);

			calendarService.inheritsParticipationFromExistingEvent(before, after);
			assertThat(after.getAttendees()).isEmpty();
		}

	@Test
		public void testInheritsParticipationFromExistingEventOneHandEmptyAttendees() {
			Event before = new Event();

			List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.ACCEPTED);
			Event after = createEvent(expectedAttendees);

			CalendarBindingImpl calendarService =
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);

			calendarService.inheritsParticipationFromExistingEvent(before, after);
			assertThat(after.getAttendees()).isEqualTo(expectedAttendees);
		}

	@Test
		public void testInheritsParticipationFromExistingEventThOtherHandEmptyAttendees() {
			List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.ACCEPTED);
			Event before = createEvent(expectedAttendees);

			Event after = new Event();

			CalendarBindingImpl calendarService =
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);

			calendarService.inheritsParticipationFromExistingEvent(before, after);
			assertThat(after.getAttendees()).isEmpty();
		}

	@Test
		public void testInheritsParticipationFromExistingEvent() {
			List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
			Event before = createEvent(expectedAttendees);

			Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));

			CalendarBindingImpl calendarService =
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);

			calendarService.inheritsParticipationFromExistingEvent(before, after);
			assertThat(after.getAttendees()).isEqualTo(expectedAttendees);
		}

	@Test
	public void testInheritsParticipationOnEmptyExceptions() {
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
		Event before = createEvent(expectedAttendees);
		
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationOnExceptions(before, after);
		assertThat(after.getEventsExceptions()).isEmpty();
	}

	@Test
		public void testInheritsParticipationOnExceptions() {
			List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.DECLINED);
			Event beforeException = createEvent(expectedAttendeesException);
			
			Event before = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
			before.addEventException(beforeException);
			
			Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
			Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
			after.addEventException(afterException);
			
			CalendarBindingImpl calendarService = 
					new CalendarBindingImpl(null, null, null, null, null, null, null, null);
			
			calendarService.inheritsParticipationOnExceptions(before, after);
			assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
		}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEvent() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.DECLINED);
		Event beforeException = createEvent(expectedAttendeesException);
		
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		
		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		after.addEventException(afterException);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventEmptyExceptions() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.DECLINED);
		Event beforeException = createEvent(expectedAttendeesException);
		
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);
		
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getEventsExceptions()).isEmpty();
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventComparator() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.DECLINED);
		Event beforeException = createEvent(expectedAttendeesException);
		
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);
		
		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		afterException.setExtId(new EventExtId(expectedExtId));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		after.addEventException(afterException);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventMultipleExceptions() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.DECLINED);
		Event beforeException = createEvent(expectedAttendeesException);
		
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.NEEDSACTION);
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);
		
		Event firstException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		firstException.setExtId(new EventExtId("012"));
		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		afterException.setExtId(new EventExtId(expectedExtId));
		Event thirdException = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		thirdException.setExtId(new EventExtId("234"));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.ACCEPTED));
		after.addEventException(thirdException);
		after.addEventException(firstException);
		after.addEventException(afterException);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventNotFoundExceptions() {
		Event beforeException = createEvent(createOrganiserAndContactAttendees(Participation.DECLINED));
		
		Event before = createEvent(createOrganiserAndContactAttendees(Participation.NEEDSACTION));
		before.addEventException(beforeException);
		beforeException.setExtId(new EventExtId("123"));
		
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.ACCEPTED);
		Event firstException = createEvent(expectedAttendees);
		firstException.setExtId(new EventExtId("012"));
		Event afterException = createEvent(expectedAttendees);
		afterException.setExtId(new EventExtId("123"));
		Event thirdException = createEvent(expectedAttendees);
		thirdException.setExtId(new EventExtId("234"));
		Event after = createEvent(expectedAttendees);
		after.addEventException(thirdException);
		after.addEventException(firstException);
		after.addEventException(afterException);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendees);
	}
	
	@Test
	public void testBuildTreeMapEmptyList() {
		ImmutableList<Event> events = ImmutableList.<Event> of();
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		TreeMap<Event, Event> treeMap = calendarService.buildTreeMap(events);
		assertThat(treeMap).isEmpty();
	}
	
	@Test
	public void testBuildTreeMapNullList() {
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		TreeMap<Event, Event> treeMap = calendarService.buildTreeMap(null);
		assertThat(treeMap).isEmpty();
	}
	
	@Test
	public void testBuildTreeMap() {
		List<Attendee> attendees = createOrganiserAndContactAttendees(Participation.ACCEPTED);
		Event firstException = createEvent(attendees);
		firstException.setExtId(new EventExtId("012"));
		Event secondException = createEvent(attendees);
		secondException.setExtId(new EventExtId("123"));
		Event thirdException = createEvent(attendees);
		thirdException.setExtId(new EventExtId("234"));
		Event[] expectedEvents = new Event[] { firstException, secondException, thirdException };
		
		ImmutableList<Event> events = ImmutableList.<Event> of(thirdException, secondException, firstException);
		
		CalendarBindingImpl calendarService = 
				new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		TreeMap<Event, Event> treeMap = calendarService.buildTreeMap(events);
		Event[] eventsArray = Iterables.toArray(treeMap.keySet(), Event.class);
		assertThat(eventsArray).isEqualTo(expectedEvents);
	}

    @Test
	public void testGetResourceEvents() throws ServerFault, FindException {
		String resourceEmail = "resource@domain";
		Event mockEvent1 = createMock(Event.class);
		Event mockEvent2 = createMock(Event.class);
		Collection<Event> expectedEvents = Lists.newArrayList(mockEvent1, mockEvent2);

		Date date = new Date();
		Date threeMonthsBefore = new org.joda.time.DateTime(date).minus(Months.THREE).toDate();
		Date sixMonthsAfter = new org.joda.time.DateTime(date).plus(Months.SIX).toDate();
		SyncRange syncRange = new SyncRange(sixMonthsAfter, threeMonthsBefore);
		CalendarDao mockDao = createMock(CalendarDao.class);
		ResourceInfo mockResource = createMock(ResourceInfo.class);
		expect(mockDao.getResource(resourceEmail)).andReturn(mockResource);
		expect(mockDao.getResourceEvents(mockResource, syncRange)).andReturn(expectedEvents);

		Object[] mocks = { mockEvent1, mockEvent2, mockDao, mockResource };
		replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, mockDao,
				null, null, null, null);
		Collection<Event> events = calendarService.getResourceEvents(resourceEmail, date);
		assertThat(events).isEqualTo(expectedEvents);

		verify(mocks);
    }

	@Test(expected=ResourceNotFoundException.class)
	public void testGetResourceEventsWithMissingResource() throws ServerFault, FindException {
		String resourceEmail = "resource@domain";

		CalendarDao mockDao = createMock(CalendarDao.class);
		expect(mockDao.getResource(resourceEmail)).andReturn(null);

		Object[] mocks = { mockDao };
		replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, mockDao,
				null, null, null, null);
		try {
			calendarService.getResourceEvents(resourceEmail, new Date());
		}
		finally {
			verify(mocks);
		}
	}
	
	@Test
	public void testApplyParticipationStateModificationsWithoutDelegations() {
		Attendee organizer = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1 = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.NEEDSACTION).build();
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.applyParticipationModifications(before, event);
		
		assertThat(att1.getParticipation()).isEqualTo(Participation.NEEDSACTION);
	}
	
	@Test
	public void testApplyParticipationStateModificationsWithDelegations() {
		Attendee organizer = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1 = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.NEEDSACTION).build();
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		att1.setCanWriteOnCalendar(true);
		calendarService.applyParticipationModifications(before, event);
		
		assertThat(att1.getParticipation()).isEqualTo(Participation.ACCEPTED);
	}
	
	@Test
	public void testApplyParticipationStateModificationsWithDelegationsWithExceptions() {
		Attendee organizer = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee organizerForExc = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1 = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.NEEDSACTION).build();
		Attendee att1ForExc = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.NEEDSACTION).build();
		Event exception = createEvent(Arrays.asList(organizerForExc, att1ForExc));
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		att1.setCanWriteOnCalendar(true);
		att1ForExc.setCanWriteOnCalendar(true);
		event.addEventException(exception);
		calendarService.applyParticipationModifications(before, event);
		
		assertThat(att1.getParticipation()).isEqualTo(Participation.ACCEPTED);
		assertThat(att1ForExc.getParticipation()).isEqualTo(Participation.ACCEPTED);
	}
	
	@Test
	public void testInitDefaultParticipationState() {
		Attendee organizer = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1 = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.ACCEPTED).build();
		Event event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		calendarService.initDefaultParticipation(event);
		
		assertThat(organizer.getParticipation()).isEqualTo(Participation.NEEDSACTION);
		assertThat(att1.getParticipation()).isEqualTo(Participation.NEEDSACTION);
	}
	
	@Test
	public void testInitDefaultParticipationStateWithExceptions() {
		Attendee organizer = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee organizerForExc = Attendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1 = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.ACCEPTED).build();
		Attendee att1ForExc = Attendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.ACCEPTED).build();
		Event event = createEvent(Arrays.asList(organizer, att1));
		Event exception = createEvent(Arrays.asList(organizerForExc, att1ForExc));
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, null, null, null, null, null, null);
		
		event.addEventException(exception);
		calendarService.initDefaultParticipation(event);
		
		assertThat(organizer.getParticipation()).isEqualTo(Participation.NEEDSACTION);
		assertThat(att1.getParticipation()).isEqualTo(Participation.NEEDSACTION);
		assertThat(organizerForExc.getParticipation()).isEqualTo(Participation.NEEDSACTION);
		assertThat(att1ForExc.getParticipation()).isEqualTo(Participation.NEEDSACTION);
	}

	private Event createEvent(List<Attendee> expectedAttendees) {
		Event event = new Event();
		event.addAttendees(expectedAttendees);
		
		return event;
	}

	private List<Attendee> createOrganiserAndContactAttendees(Participation contactState) {
		return Arrays.asList(
				Attendee.builder()
				.asOrganizer()
				.participation(Participation.ACCEPTED)
				.email("organiser@test.lng").build(),
				Attendee.builder()
				.asContact()
				.participation(contactState)
				.email("attendee@test.lng").build());
	}

	private ResourceInfo buildResourceInfo1() {
		return ResourceInfo.builder().id(1).name("resource1").mail("res-1@domain.com").read(true).write(true).domainName("domain").build();
	}

	private ResourceInfo buildResourceInfo2() {
		return ResourceInfo.builder().id(1).name("resource2").mail("res-2@domain.com").read(true).write(false).domainName("domain").build();
	}
}
