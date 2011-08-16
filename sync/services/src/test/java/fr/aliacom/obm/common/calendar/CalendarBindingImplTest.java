package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import net.fortuna.ical4j.data.ParserException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
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

	private AccessToken mockAccessToken(String domainName) {
		AccessToken accessToken = createMock(AccessToken.class);
		expect(accessToken.getDomain()).andReturn(domainName).atLeastOnce();
		expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		return accessToken;
	}

	private ObmUser mockObmUser(String userEmail) {
		ObmUser user = createMock(ObmUser.class);
		expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
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
			
		AccessToken accessToken = mockAccessToken(fixtures.domainName);
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
			
		AccessToken accessToken = mockAccessToken(fixtures.domainName);
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
		String eventExtId = "extid";
		String userEmail = "user@domain1";
		
		ObmUser user = mockObmUser(userEmail);
		
		AccessToken accessToken = mockAccessToken(domainName);
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		
		final Event event = createMock(Event.class);
		expect(event.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(event.getUid()).andReturn(null).atLeastOnce();
		expect(event.isInternalEvent()).andReturn(false).atLeastOnce();
		expect(event.getTitle()).andReturn("title").atLeastOnce();
		event.findAttendeeForUser(userEmail);
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
		String eventExtId = "extid";
		Attendee fakeUserAttendee = getFakeOwnerAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(domainName);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(true).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		Ical4jHelper ical4jHelper = mockImportICalendarIcal4jHelper(obmUser, eventWithOwnerAttendee);
		UserService userService = mockImportICalendarUserService(accessToken, fakeUserAttendee, calendar, domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, obmUser, calendarDao};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, ical4jHelper);
		
		try {
			calendarService.importICalendar(accessToken, calendar, "");
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(ParticipationState.ACCEPTED, fakeUserAttendee.getState());
	}
	
	@Test
	public void testPurge() throws FindException, ServerFault, SQLException {
		String calendar = "cal1";
		String domainName = "domain1";
		String userEmail = "user@domain1";
		String oldEventNoOtherAttendeesExtId = "oldEventNoOtherAttendeesExtId";
		String oldEventWithOtherAttendeesExtId = "oldEventWithOtherAttendeesExtId";
		String oldEventNoOtherAttendeesUid = "1";
		String oldEventWithOtherAttendeesUid = "2";

		String otherUserEmail = "user2@domain1";
		Attendee userAttendee = getFakeOwnerAttendee(userEmail);
		Attendee otherAttendee = getFakeOwnerAttendee(otherUserEmail);
		userAttendee.setState(ParticipationState.NEEDSACTION);
		final ObmUser obmUser = mockObmUser(userEmail);

		AccessToken accessToken = mockAccessToken(domainName);

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
		expect(calendarDao.findEvent(accessToken, Integer.parseInt(oldEventNoOtherAttendeesUid))).
			andReturn(oldEventNoOtherAttendees).atLeastOnce();
		expect(calendarDao.removeEvent(accessToken, Integer.parseInt(oldEventNoOtherAttendeesUid), 
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
		String eventExtId = "extid";
		Attendee fakeUserAttendee = getFakeOwnerAttendee(userEmail);
		fakeUserAttendee.setState(ParticipationState.NEEDSACTION);
		
		final ObmUser obmUser = mockObmUser(userEmail);
		expect(obmUser.getLogin()).andReturn(calendar).atLeastOnce();
		
		AccessToken accessToken = mockAccessToken(domainName);
		
		Event eventWithOwnerAttendee = createMock(Event.class);
		expect(eventWithOwnerAttendee.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(eventWithOwnerAttendee.isEventInThePast()).andReturn(false).once();
		expect(eventWithOwnerAttendee.getAttendees()).andReturn(ImmutableList.of(fakeUserAttendee)).atLeastOnce();
		eventWithOwnerAttendee.setAttendees(Arrays.asList(fakeUserAttendee));
		EasyMock.expectLastCall().once();
		
		Helper rightsHelper = mockRightsHelper(calendar, accessToken);
		Ical4jHelper ical4jHelper = mockImportICalendarIcal4jHelper(obmUser, eventWithOwnerAttendee);
		UserService userService = mockImportICalendarUserService(accessToken, fakeUserAttendee, calendar, domainName, obmUser);
		CalendarDao calendarDao = mockImportICalendarCalendarDao(accessToken, calendar, obmUser, eventExtId, eventWithOwnerAttendee);
		
		Object[] mocks = {accessToken, userService, rightsHelper, eventWithOwnerAttendee, ical4jHelper, obmUser, calendarDao};
		EasyMock.replay(mocks);
		
		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper, ical4jHelper);
		
		try {
			calendarService.importICalendar(accessToken, calendar, "");
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
		
		Assert.assertEquals(ParticipationState.NEEDSACTION, fakeUserAttendee.getState());
	}

	private Attendee getFakeOwnerAttendee(String userEmail) {
		Attendee att = new Attendee();
		att.setEmail(userEmail);
		return att;
	}
	
	private Ical4jHelper mockImportICalendarIcal4jHelper(ObmUser obmUser, Event eventWithOwnerAttendee) throws IOException, ParserException{
		Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
		expect(ical4jHelper.parseICSEvent("", obmUser)).andReturn(ImmutableList.of(eventWithOwnerAttendee)).once();
		return ical4jHelper;
	}
	
	private UserService mockImportICalendarUserService(AccessToken accessToken, Attendee fakeUserAttendee, String calendar, String domainName, ObmUser obmUser) throws FindException{
		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();
		expect(userService.getUserFromAttendee(fakeUserAttendee, domainName)).andReturn(obmUser);
		expect(userService.getUserFromAttendee(fakeUserAttendee, domainName)).andReturn(obmUser);
		return userService;
	}
	
	private CalendarDao mockImportICalendarCalendarDao(AccessToken accessToken, String calendar, ObmUser obmUser, String eventExtId, Event eventWithOwnerAttendee) throws FindException, SQLException{
		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(eq(accessToken), eq(obmUser), eq(eventExtId))).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, eventWithOwnerAttendee, false)).andReturn(eventWithOwnerAttendee).once();
		return calendarDao;
	}
}
