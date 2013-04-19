package fr.aliacom.obm.freebusy;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.icalendar.Ical4jHelper;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.UserAttendee;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.domain.DomainDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;

@RunWith(SlowFilterRunner.class)
public class DatabaseFreeBusyProviderTest {

	private static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(Ical4jHelper.class);
			bindWithMock(DomainDao.class);
			bindWithMock(UserDao.class);
			bindWithMock(CalendarDao.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private DatabaseFreeBusyProvider databaseFreebusyProvider;
	@Inject
	private Ical4jHelper ical4jHelper;
	@Inject
	private DomainDao domainDao;
	@Inject
	private UserDao userDao;
	@Inject
	private CalendarDao calendarDao;
	
	private final static String DOMAIN = "domain";
	private final static String ICS = "ics";
	
	@Test
	public void testFindFreeBusyIcs() throws FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		ObmDomain domain = ObmDomain.builder().name(DOMAIN).build();
		ObmUser user = ObmUser.builder().uid(1).login("owner").domain(domain).publicFreeBusy(true).build();
		
		FreeBusy freeBusy = buildFakeFreeBusyResponse();
		
		List<FreeBusy> freeBusyList = Lists.newArrayList();
		freeBusyList.add(freeBusy);
		
		expect(domainDao.findDomainByName(DOMAIN)).andReturn(domain);
		expect(userDao.findUser("owner@domain", domain)).andReturn(user);
		expect(calendarDao.getFreeBusy(domain, fbr)).andReturn(freeBusyList);
		expect(ical4jHelper.parseFreeBusy(freeBusy)).andReturn(ICS);
		
		mocksControl.replay();
		
		String ics = databaseFreebusyProvider.findFreeBusyIcs(fbr);
		
		mocksControl.verify();
		
		assertThat(ics).isEqualTo(ICS);
	}
	
	@Test(expected=FreeBusyException.class)
	public void testFindFreeBusyIcsThrowPrivateFreebusyException() throws FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		ObmDomain domain = ObmDomain.builder().name(DOMAIN).build();
		ObmUser user = ObmUser.builder().uid(1).login("owner").domain(domain).publicFreeBusy(false).build();
		
		expect(domainDao.findDomainByName(DOMAIN)).andReturn(domain);
		expect(userDao.findUser("owner@domain", domain)).andReturn(user);
		
		mocksControl.replay();
		
		try {
			databaseFreebusyProvider.findFreeBusyIcs(fbr);
		} catch (FreeBusyException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}
	
	@Test
	public void testFindFreeBusyIcsReturnNullOnNullObmUser() throws FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		ObmDomain domain = ObmDomain.builder().name(DOMAIN).build();
		
		expect(domainDao.findDomainByName(DOMAIN)).andReturn(domain);
		expect(userDao.findUser("owner@domain", domain)).andReturn(null);
		
		mocksControl.replay();
		
		String ics = databaseFreebusyProvider.findFreeBusyIcs(fbr);
		
		mocksControl.verify();
		
		assertThat(ics).isNull();
	}
	
	@Test
	public void testFindFreeBusyIcsReturnNullOnNullIcal4jHelper() throws FreeBusyException {
		FreeBusyRequest fbr = buildFakeFreeBusyRequest();
		
		ObmDomain domain = ObmDomain.builder().name(DOMAIN).build();
		ObmUser user = ObmUser.builder().uid(1).login("owner").domain(domain).publicFreeBusy(true).build();
		
		FreeBusy freeBusy = buildFakeFreeBusyResponse();
		
		List<FreeBusy> freeBusyList = Lists.newArrayList();
		freeBusyList.add(freeBusy);
		
		expect(domainDao.findDomainByName(DOMAIN)).andReturn(domain);
		expect(userDao.findUser("owner@domain", domain)).andReturn(user);
		expect(calendarDao.getFreeBusy(domain, fbr)).andReturn(freeBusyList);
		expect(ical4jHelper.parseFreeBusy(freeBusy)).andReturn(null);
		
		mocksControl.replay();
		
		String ics = databaseFreebusyProvider.findFreeBusyIcs(fbr);
		
		mocksControl.verify();
		
		assertThat(ics).isNull();
	}

	private FreeBusyRequest buildFakeFreeBusyRequest() {
		FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
		freeBusyRequest.setStart(DateUtils.date("2013-01-01T14:00:00"));
		freeBusyRequest.setEnd(DateUtils.date("2013-02-01T14:00:00"));
		freeBusyRequest.setOwner("owner@domain");
		freeBusyRequest.setUid("1");
		freeBusyRequest.addAttendee(UserAttendee.builder().email("attendee@domain").build());
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
		freeBusy.setAtt(UserAttendee.builder().email("attendee@domain").build());
		freeBusy.setStart(DateUtils.date("2013-01-01T14:00:00"));
		freeBusy.setEnd(DateUtils.date("2013-02-01T14:00:00"));
		freeBusy.setOwner("owner@domain");
		freeBusy.setUid("1");
		freeBusy.addFreeBusyInterval(freeBusyInterval);
		freeBusy.addFreeBusyInterval(freeBusyInterval2);
		return freeBusy;
	}
}
