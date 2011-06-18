package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.Helper;


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

		CalendarBindingImpl calendarService = new CalendarBindingImpl(null, null, userService, calendarDao, null, rightsHelper);

		try {
			calendarService.createEvent(accessToken, calendar, event, true);
		} catch (ServerFault e) {
			EasyMock.verify(mocks);
			throw e;
		}
	}

}
