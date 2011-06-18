package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.Ical4jHelper;


public class CalendarBindingImplTest {

	private Helper mockRightsHelper(String calendar, AccessToken accessToken) {
		Helper rightsHelper = createMock(Helper.class);
		rightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar));
		EasyMock.expectLastCall().andReturn(true);
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
	
	@Test(expected=ServerFault.class)
	public void testCalendarOwnerNotAnAttendee() throws AuthFault, ServerFault, FindException {
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
		throws ImportICalendarException, AuthFault, ServerFault, IOException, ParserException, FindException, SQLException {
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
		eventWithOwnerAttendee.setAttendees(EasyMock.anyObject(List.class));
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
	public void testImportEventInTheFuture() 
		throws ImportICalendarException, AuthFault, ServerFault, IOException, ParserException, FindException, SQLException {
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
		eventWithOwnerAttendee.setAttendees(EasyMock.anyObject(List.class));
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
