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
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.Ical4jHelper;

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
	
	private Helper mockRightsHelper(String calendar, AccessToken accessToken) {
		Helper rightsHelper = createMock(Helper.class);
		expect(rightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		expect(rightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		return rightsHelper;
	}

	private AccessToken mockAccessToken(String userName, String domainName) {
		AccessToken accessToken = createMock(AccessToken.class);
		expect(accessToken.getDomain()).andReturn(domainName).atLeastOnce();
		expect(accessToken.getUser()).andReturn(userName).anyTimes();
		expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		return accessToken;
	}

	private ObmUser mockObmUser(String userEmail, String domain) {
		ObmUser user = createMock(ObmUser.class);
		expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName(domain);
		expect(user.getDomain()).andReturn(obmDomain).anyTimes();
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
			
		AccessToken accessToken = mockAccessToken(fixtures.userEmail, fixtures.domainName);
		Helper rightsHelper = createMock(Helper.class);
		
		rightsHelper.constructEmailFromList(eq(fixtures.userEmail), eq(fixtures.domainName));
		EasyMock.expectLastCall().andReturn(fixtures.userEmail);
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(fixtures.user).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(fixtures.user), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		EasyMock.expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null);
		
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
			
		AccessToken accessToken = mockAccessToken(fixtures.userEmail, fixtures.domainName);
		Helper rightsHelper = createMock(Helper.class);
		
		rightsHelper.constructEmailFromList(eq(fixtures.userEmailWithoutDomain), eq(fixtures.domainName));
		EasyMock.expectLastCall().andReturn(fixtures.userEmail);
		
		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		EasyMock.expectLastCall().andReturn(fixtures.user).once();
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(fixtures.user), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		EasyMock.expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null);
		
		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};
		
		EasyMock.replay(mocks);
		CalendarInfo[] result = calendarService.getCalendarMetadata(accessToken, calendarEmails);
		assertEquals(new HashSet<CalendarInfo>(Arrays.asList(expectedCalendarInfos)), new HashSet<CalendarInfo>(Arrays.asList(result)));
	}
	
	@Test(expected=ServerFault.class)
	public void testCalendarOwnerNotAnAttendee() throws ServerFault, FindException, EventAlreadyExistException {
		String calendar = "cal1";
		String domainName = "domain1";
		String userName = "user";
		EventExtId eventExtId = new EventExtId("extid");
		String userEmail = "user@domain1";
		
		ObmUser user = mockObmUser(userEmail, domainName);
		
		AccessToken accessToken = mockAccessToken(userName, domainName);
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		
		final Event event = createMock(Event.class);
		expect(event.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(event.getObmId()).andReturn(null).atLeastOnce();
		expect(event.isInternalEvent()).andReturn(false).atLeastOnce();
		expect(event.getTitle()).andReturn("title").atLeastOnce();
		event.findAttendeeFromEmail(userEmail);
		EasyMock.expectLastCall().andReturn(null).atLeastOnce();
		
		final UserService userService = createMock(UserService.class);
		userService.getUserFromCalendar(eq(calendar), eq(domainName));
		EasyMock.expectLastCall().andReturn(user).atLeastOnce();

		final CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.findEventByExtId(eq(accessToken), eq(user), eq(eventExtId));
		EasyMock.expectLastCall().andReturn(null).once();

		Object[] mocks = {event, accessToken, userService, calendarDao, rightsHelper, user};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, null);

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
		String calendar = "cal1";
		String domainName = "domain1";
		String userEmail = "user@domain1";
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = getFakeAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail, domainName);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(calendar, domainName);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(true).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		Ical4jHelper ical4jHelper = mockIcal4jHelper(obmUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, calendar, domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, obmUser, calendarDao};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, ical4jHelper);
		
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
		String calendar = "cal1";
		String domainName = "domain1";
		String userEmail = "user@domain1";
		EventExtId oldEventNoOtherAttendeesExtId = new EventExtId("oldEventNoOtherAttendeesExtId");
		EventExtId oldEventWithOtherAttendeesExtId = new EventExtId("oldEventWithOtherAttendeesExtId");
		EventObmId oldEventNoOtherAttendeesUid = new EventObmId("1");
		EventObmId oldEventWithOtherAttendeesUid = new EventObmId("2");

		String otherUserEmail = "user2@domain1";
		Attendee userAttendee = getFakeAttendee(userEmail);
		Attendee otherAttendee = getFakeAttendee(otherUserEmail);
		userAttendee.setState(ParticipationState.NEEDSACTION);
		final ObmUser obmUser = mockObmUser(userEmail, domainName);

		AccessToken accessToken = mockAccessToken(calendar, domainName);

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
		eventChangeHandler.delete(obmUser, oldEventNoOtherAttendees, false);
		eventChangeHandler.updateParticipationState(oldEventWithOtherAttendees, obmUser, 
				ParticipationState.DECLINED, false);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).atLeastOnce();
		expect(userService.getUserFromLogin(userEmail, domainName)).andReturn(obmUser).atLeastOnce();
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

		Helper rightsHelper = mockRightsHelper(calendar, accessToken);

		Object[] mocks = { accessToken, userService, rightsHelper, obmUser, calendarDao, eventChangeHandler };
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null);

		calendarService.purge(accessToken, calendar);

		EasyMock.verify(mocks);
	}

	@Test
	public void testImportEventInTheFuture() 
		throws ImportICalendarException, ServerFault, IOException, ParserException, FindException, SQLException {
		String calendar = "cal1";
		String domainName = "domain1";
		String userEmail = "user@domain1";
		String icsData = "icsData";
		EventExtId eventExtId = new EventExtId("extid");
		Attendee fakeUserAttendee = getFakeAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail, domainName);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(calendar, domainName);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(false).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		Ical4jHelper ical4jHelper = mockIcal4jHelper(obmUser, icsData, eventWithOwnerAttendee);
		UserService userService = mockImportICSUserService(accessToken, fakeUserAttendee, calendar, domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, obmUser, calendarDao};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, ical4jHelper);
		
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
		String calendar = "cal1";
		String domainName = "domain1";
		String userEmail = "user@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		
		Attendee attendee = getFakeAttendee(userEmail);
		attendee.setState(ParticipationState.NEEDSACTION);
		
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
		event.setSequence(1);
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);
		
		AccessToken accessToken = mockAccessToken(calendar, domainName);
		Helper helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(true).atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event, updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, beforeEvent, event, notification, true);
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		Assert.assertEquals(ParticipationState.ACCEPTED, newEvent.getAttendees().get(0).getState());
	}
	
	@Test
	public void testAttendeeHasNoRightToWriteOnCalendar() throws FindException, ServerFault,
			SQLException, EventNotFoundException {
		String calendar = "cal1";
		String domainName = "domain1";
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
		event.setSequence(1);

		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		AccessToken accessToken = mockAccessToken(calendar, domainName);
		Helper helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(
				calendarDao.modifyEventForcingSequence(accessToken, calendar, event,
						updateAttendee, 1, true)).andReturn(event).atLeastOnce();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).atLeastOnce();
		eventChangeHandler.update(obmUser, beforeEvent, event, notification, true);

		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, null, helper, null);
		Event newEvent = calendarService.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(ParticipationState.NEEDSACTION, newEvent.getAttendees().get(0)
				.getState());
	}

	public void testDontSendEmailsAndDontUpdateStatusForUnimportantChanges() throws ServerFault, FindException, SQLException, EventNotFoundException {
		String calendar = "cal1";
		String domainName = "domain1";
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

		AccessToken accessToken = mockAccessToken(calendar, domainName);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).atLeastOnce();

		Helper rightsHelper = mockRightsHelper(calendar, accessToken);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, eventExtId)).andReturn(oldEvent).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, newEvent, updateAttendees, sequence, true)).andReturn(newEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		Object[] mocks = {accessToken, calendarDao, userService, rightsHelper, eventChangeHandler};
		EasyMock.replay(mocks);

		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, rightsHelper, null);

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
	
	private Ical4jHelper mockIcal4jHelper(ObmUser obmUser, String icsData, Event eventWithOwnerAttendee) throws IOException, ParserException{
		Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
		expect(ical4jHelper.parseICSEvent(icsData, obmUser)).andReturn(ImmutableList.of(eventWithOwnerAttendee)).once();
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
	
	private CalendarDao mockImportICalendarCalendarDao(AccessToken accessToken, String calendar, ObmUser obmUser, EventExtId eventExtId, Event eventWithOwnerAttendee) throws FindException, SQLException{
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
		String domainName = "myDomain";
		String calendar = "toto";
		String email = calendar + "@" + domainName;
		String ics = "icsData";
		
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(email);
		
		Event eventFromIcs = new Event();
		eventFromIcs.setExtId(extId);
		
		AccessToken accessToken = mockAccessToken(calendar, domainName);
		Helper helper = mockRightsHelper(calendar, accessToken);
		
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();
		Ical4jHelper ical4jHelper = mockIcal4jHelper(obmUser, ics, eventFromIcs);
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, extId)).andReturn(eventFromDao).once();

		Object[] mocks = new Object[] {calendarDao, userService, ical4jHelper, accessToken, helper};
		
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, helper, ical4jHelper);
		List<Event> events = calendarService.parseICS(accessToken, ics);
		
		EasyMock.verify(mocks);
		
		Assertions.assertThat(events).hasSize(1);
		Event result = events.get(0);
		return result;
	}
	
	@Test
	public void testCreateExternalEventCalendarOwnerWithDeclinedPartState() throws FindException, ServerFault, EventAlreadyExistException, SQLException {
		String calendar = "cal1";
		String domainName = "domain1";
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
		
		AccessToken accessToken = mockAccessToken(calendar, domainName);
		Helper helper = mockRightsHelper(calendar, accessToken);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, obmUser, event.getExtId())).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, event, false)).andReturn(eventCreated).once();
		expect(calendarDao.removeEvent(accessToken, eventCreated, eventCreated.getType(), eventCreated.getSequence())).andReturn(eventCreated).once();
		eventChangeHandler.updateParticipationState(eventCreated, obmUser, calOwner.getState(), notification);
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, null, helper, null);
		calendarService.createEvent(accessToken, calendar, event, notification);
		
		EasyMock.verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
		
	}
}
