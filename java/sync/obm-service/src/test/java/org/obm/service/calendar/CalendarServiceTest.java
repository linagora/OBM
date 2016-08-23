/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service.calendar;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.reportMatcher;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.easymock.IArgumentMatcher;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.domain.dao.CalendarDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.service.user.UserService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.date.DateProvider;
import org.obm.sync.services.AttendeeService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;


@GuiceModule(CalendarServiceTest.Env.class)
@RunWith(GuiceRunner.class)
public class CalendarServiceTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(CalendarDao.class);
			bindWithMock(UserService.class);
			bindWithMock(AttendeeService.class);
			bindWithMock(DateProvider.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Inject private IMocksControl mocksControl;
	@Inject private CalendarDao calendarDao;
	@Inject private UserService userService;
	@Inject private AttendeeService attendeeService;
	@Inject private CalendarService testee;

	private AccessToken token;
	private ObmUser user;
	private String domainName;
	private String calendar;

	@Before
	public void setUp() {
		token = ToolBox.mockAccessToken(mocksControl);
		user = ToolBox.getDefaultObmUser();
		domainName = user.getDomain().getName();
		calendar = user.getLogin();
	}


	@Test
	public void testImportEventInThePast() throws Exception {
		testImportEvent("ics/inPastEvent.ics", ImmutableMap.of(
			"organizer@test.tlse.lng", Participation.State.ACCEPTED,
			"user@test.tlse.lng", Participation.State.ACCEPTED)
		);
	}

	@Test
	public void testImportEventInTheFuture() throws Exception {
		testImportEvent("ics/inFutureEvent.ics", ImmutableMap.of(
			"organizer@test.tlse.lng", Participation.State.ACCEPTED,
			"user@test.tlse.lng", Participation.State.NEEDSACTION)
		);
	}

	private void testImportEvent(String icsName, Map<String, Participation.State> expectedAttendeesParticipation) throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(icsName));
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").participation(Participation.needsAction()).build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).participation(Participation.needsAction()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(eq(organizer), eq(domainName))).andReturn(null);
		expect(userService.getUserFromAttendee(eq(attendee), eq(domainName))).andReturn(user);
		expect(attendeeService.findAttendee(null, attendee.getEmail(), true, user.getDomain(), user.getUid())).andReturn(attendee);
		expect(attendeeService.findAttendee(null, organizer.getEmail(), true, user.getDomain(), user.getUid())).andReturn(organizer);
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null);
		expect(calendarDao.createEvent(eq(token), eq(calendar), eventWithAttendeeParticipationState(expectedAttendeesParticipation))).andReturn(null);

		mocksControl.replay();
		testee.importICalendar(token, user.getLogin(), ics);
		mocksControl.verify();
	}
	
	@Test
	public void testImportICSWithoutOrganizerNorAttendees() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/eventWithoutOrganizerNorAttendees.ics"));
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(user);
		expect(attendeeService.findUserAttendee(null, user.getLogin(), user.getDomain())).andReturn(attendee);
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null);
		expect(calendarDao.createEvent(eq(token), eq(calendar), eventWithSingleAttendeeAsOrganizer())).andReturn(null);

		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	@Test
	public void testImportICSWithoutOrganizerNorAttendeesSetsOwner() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/eventWithoutOrganizerNorAttendees.ics"));
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(user);
		expect(attendeeService.findUserAttendee(null, user.getLogin(), user.getDomain())).andReturn(attendee);
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null);
		expect(calendarDao.createEvent(eq(token), eq(calendar), eventWithDefinedOwner())).andReturn(null);
		
		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	@Test
	public void testImportICSPerformsOnlyOneCalendarOwnerLookup() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/4Events.ics"));
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user).once();
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(user).anyTimes();
		expect(attendeeService.findAttendee(null, attendee.getEmail(), true, user.getDomain(), user.getUid())).andReturn(attendee);
		expect(attendeeService.findAttendee(null, organizer.getEmail(), true, user.getDomain(), user.getUid())).andReturn(organizer);
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null).times(4);
		expect(calendarDao.createEvent(eq(token), eq(calendar), isA(Event.class))).andReturn(null).times(4);
		
		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	@Test
	public void testImportICSPerformsOnlyOneLookupPerAttendee() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/4Events.ics"));
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(attendee, domainName)).andReturn(user).once();
		expect(userService.getUserFromAttendee(organizer, domainName)).andReturn(user).once();
		expect(attendeeService.findAttendee(null, attendee.getEmail(), true, user.getDomain(), user.getUid())).andReturn(attendee);
		expect(attendeeService.findAttendee(null, organizer.getEmail(), true, user.getDomain(), user.getUid())).andReturn(organizer);
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null).times(4);
		expect(calendarDao.createEvent(eq(token), eq(calendar), isA(Event.class))).andReturn(null).times(4);
		
		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}
	
	@Test
	public void testImportICSCachesAttendeeLookups() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/4Events.ics"));
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(attendee, domainName)).andReturn(user);
		expect(userService.getUserFromAttendee(organizer, domainName)).andReturn(user);
		expect(attendeeService.findAttendee(null, "user@test.tlse.lng", true, user.getDomain(), user.getUid())).andReturn(attendee).once();
		expect(attendeeService.findAttendee(null, "organizer@test.tlse.lng", true, user.getDomain(), user.getUid())).andReturn(organizer).once();
		expect(calendarDao.findEventByExtId(eq(token), eq(user), isA(EventExtId.class))).andReturn(null).times(4);
		expect(calendarDao.createEvent(eq(token), eq(calendar), isA(Event.class))).andReturn(null).times(4);

		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	@Test
	public void testImportICSShouldNotUpdateTheEventWhenExtIdIsAlreadyKnownAndSameSequence() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/inFutureEvent.ics"));
		int icsSequence = 2;
		Event alreadyInDbEvent = ToolBox.getFakeEvent(5);
		alreadyInDbEvent.setExtId(new EventExtId("TheUid"));
		alreadyInDbEvent.setUid(new EventObmId(5));
		alreadyInDbEvent.setSequence(icsSequence);
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(user).anyTimes();
		expect(attendeeService.findAttendee(null, attendee.getEmail(), true, user.getDomain(), user.getUid())).andReturn(attendee);
		expect(attendeeService.findAttendee(null, organizer.getEmail(), true, user.getDomain(), user.getUid())).andReturn(organizer);
		expect(calendarDao.findEventByExtId(token, user, alreadyInDbEvent.getExtId())).andReturn(alreadyInDbEvent);
		
		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	@Test
	public void testImportICSShouldUpdateTheEventWhenExtIdIsAlreadyKnownAndHigherSequence() throws Exception {
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/inFutureEvent.ics"));
		int icsSequence = 2;
		Event alreadyInDbEvent = ToolBox.getFakeEvent(5);
		alreadyInDbEvent.setExtId(new EventExtId("TheUid"));
		alreadyInDbEvent.setUid(new EventObmId(5));
		alreadyInDbEvent.setSequence(icsSequence - 1);
		UserAttendee organizer = UserAttendee.builder().email("organizer@test.tlse.lng").build();
		UserAttendee attendee = UserAttendee.builder().email(user.getLoginAtDomain()).build();

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(user);
		expect(userService.getUserFromAccessToken(token)).andReturn(user);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(user).anyTimes();
		expect(attendeeService.findAttendee(null, attendee.getEmail(), true, user.getDomain(), user.getUid())).andReturn(attendee);
		expect(attendeeService.findAttendee(null, organizer.getEmail(), true, user.getDomain(), user.getUid())).andReturn(organizer);
		expect(calendarDao.findEventByExtId(token, user, alreadyInDbEvent.getExtId())).andReturn(alreadyInDbEvent);
		expect(calendarDao.modifyEvent(eq(token), eq(calendar), isA(Event.class), eq(true))).andReturn(alreadyInDbEvent);
		
		mocksControl.replay();
		testee.importICalendar(token, calendar, ics);
		mocksControl.verify();
	}

	private static Event eventWithAttendeeParticipationState(final Map<String, Participation.State> expectedAttendeesParticipation) {
		reportMatcher(new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				if (!(argument instanceof Event)) {
					return false;
				}

				Event event = (Event) argument;
				for (Entry<String, Participation.State> expecting : expectedAttendeesParticipation.entrySet()) {
					if (event.findAttendeeFromEmail(expecting.getKey()).getParticipation().getState() != expecting.getValue()) {
						return false;
					}
				}

				return true;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("event with expected attendees participation");
			}
		});

		return null;
	}

	private static Event eventWithDefinedOwner() {
		reportMatcher(new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				if (!(argument instanceof Event)) {
					return false;
				}

				Event event = (Event) argument;

				return event.getOwnerEmail() != null || event.getOwner() != null;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("event with a defined 'owner' or 'ownerEmail'");
			}
		});

		return null;
	}

	private static Event eventWithSingleAttendeeAsOrganizer() {
		reportMatcher(new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				if (!(argument instanceof Event)) {
					return false;
				}

				Event event = (Event) argument;

				if (Iterables.size(event.getAttendees()) != 1) {
					return false;
				}

				return Iterables.getFirst(event.getAttendees(), null).isOrganizer();
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("event with a single organizer attendee");
			}
		});

		return null;
	}
}
