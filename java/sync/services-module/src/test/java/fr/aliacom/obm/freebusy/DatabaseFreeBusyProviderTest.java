package fr.aliacom.obm.freebusy;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.domain.dao.CalendarDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.icalendar.Ical4jHelper;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.exception.ObmUserNotFoundException;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserService;

@GuiceModule(DatabaseFreeBusyProviderTest.Env.class)
@RunWith(GuiceRunner.class)
public class DatabaseFreeBusyProviderTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(Ical4jHelper.class);
			bindWithMock(UserService.class);
			bindWithMock(CalendarDao.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private DatabaseFreeBusyProvider databaseFreebusyProvider;
	@Inject
	private Ical4jHelper ical4jHelper;
	@Inject
	private UserService userService;
	@Inject
	private CalendarDao calendarDao;
	
	private final static String DOMAIN = "domain";
	private final static String ICS = "ics";
	private final static UserLogin OWNER_LOGIN = UserLogin.valueOf("owner");
	private final static String OWNER_EMAIL = "owner@domain";
	private final static String ATTENDEE_EMAIL = "attendee@domain";

	private ObmDomain domain;
	private ObmUser user;
	
	@Before
	public void setUp() {
		domain = ObmDomain.builder().name(DOMAIN).build();
		user = ObmUser.builder().uid(1).login(OWNER_LOGIN).domain(domain).publicFreeBusy(true).build();
	}
	
	@Test
	public void testFindFreeBusyIcs() throws FreeBusyException, ObmUserNotFoundException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		FreeBusy freeBusy = buildFakeFreeBusyResponse();
		
		List<FreeBusy> freeBusyList = Lists.newArrayList();
		freeBusyList.add(freeBusy);
		
		expect(userService.getUserFromEmail(ATTENDEE_EMAIL)).andReturn(user);
		expect(calendarDao.getFreeBusy(domain, fbr)).andReturn(freeBusyList);
		expect(ical4jHelper.parseFreeBusy(freeBusy)).andReturn(ICS);
		
		mocksControl.replay();
		
		String ics = databaseFreebusyProvider.findFreeBusyIcs(fbr);
		
		mocksControl.verify();
		
		assertThat(ics).isEqualTo(ICS);
	}
	
	@Test(expected=FreeBusyException.class)
	public void testFindFreeBusyIcsThrowPrivateFreebusyException() throws FreeBusyException, ObmUserNotFoundException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		ObmUser user = ObmUser.builder().uid(1).login(OWNER_LOGIN).domain(domain).publicFreeBusy(false).build();
		
		expect(userService.getUserFromEmail(ATTENDEE_EMAIL)).andReturn(user);
		
		mocksControl.replay();
		
		try {
			databaseFreebusyProvider.findFreeBusyIcs(fbr);
		} catch (FreeBusyException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}
	
	@Test(expected=ObmUserNotFoundException.class)
	public void testFindFreeBusyIcsThrowObmUserNotFoundException() throws FreeBusyException, ObmUserNotFoundException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		expect(userService.getUserFromEmail(ATTENDEE_EMAIL)).andReturn(null);
		
		mocksControl.replay();
		
		try {
			databaseFreebusyProvider.findFreeBusyIcs(fbr);
		} catch (ObmUserNotFoundException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}
	
	@Test
	public void testFindFreeBusyIcsReturnNullOnNullIcal4jHelper() throws FreeBusyException, ObmUserNotFoundException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		FreeBusy freeBusy = buildFakeFreeBusyResponse();
		
		List<FreeBusy> freeBusyList = Lists.newArrayList();
		freeBusyList.add(freeBusy);
		
		expect(userService.getUserFromEmail(ATTENDEE_EMAIL)).andReturn(user);
		expect(calendarDao.getFreeBusy(domain, fbr)).andReturn(freeBusyList);
		expect(ical4jHelper.parseFreeBusy(freeBusy)).andReturn(null);
		
		mocksControl.replay();
		
		String ics = databaseFreebusyProvider.findFreeBusyIcs(fbr);
		
		mocksControl.verify();
		
		assertThat(ics).isNull();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFindFreeBusyIcsThrowExceptionOnEmptyAttendees()
			throws ObmUserNotFoundException, FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		fbr.getAttendees().clear();
		
		mocksControl.replay();
		
		try {
			databaseFreebusyProvider.findFreeBusyIcs(fbr);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFindFreeBusyIcsThrowExceptionOnTwoAttendees()
			throws ObmUserNotFoundException, FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		fbr.addAttendee(UserAttendee.builder().email(ATTENDEE_EMAIL).build());
		
		mocksControl.replay();
		
		try {
			databaseFreebusyProvider.findFreeBusyIcs(fbr);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}

	private FreeBusyRequest buildFakeFreeBusyRequest() {
		FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
		freeBusyRequest.setStart(DateUtils.date("2013-01-01T14:00:00"));
		freeBusyRequest.setEnd(DateUtils.date("2013-02-01T14:00:00"));
		freeBusyRequest.setOwner(OWNER_EMAIL);
		freeBusyRequest.setUid("1");
		freeBusyRequest.addAttendee(UserAttendee.builder().email(ATTENDEE_EMAIL).build());
		return freeBusyRequest;
	}
	
	private FreeBusy buildFakeFreeBusyResponse() {
		FreeBusyInterval freeBusyInterval = new FreeBusyInterval();
		freeBusyInterval.setAllDay(false);
		freeBusyInterval.setDuration(3600);
		freeBusyInterval.setStart(DateUtils.date("2013-01-10T14:00:00"));
		
		FreeBusyInterval freeBusyInterval2 = new FreeBusyInterval();
		freeBusyInterval.setAllDay(false);
		freeBusyInterval.setDuration(7200);
		freeBusyInterval.setStart(DateUtils.date("2013-01-20T07:00:00"));
		
		FreeBusy freeBusy = new FreeBusy();
		freeBusy.setAtt(UserAttendee.builder().email(ATTENDEE_EMAIL).build());
		freeBusy.setStart(DateUtils.date("2013-01-01T14:00:00"));
		freeBusy.setEnd(DateUtils.date("2013-02-01T14:00:00"));
		freeBusy.setOwner(OWNER_EMAIL);
		freeBusy.setUid("1");
		freeBusy.addFreeBusyInterval(freeBusyInterval);
		freeBusy.addFreeBusyInterval(freeBusyInterval2);
		return freeBusy;
	}
}
