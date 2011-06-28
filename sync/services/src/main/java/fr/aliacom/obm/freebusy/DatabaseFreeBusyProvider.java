package fr.aliacom.obm.freebusy;

import java.util.List;

import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.domain.DomainDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.utils.Ical4jHelper;

/**
 * Retrieves freebusy data from the local database.
 */
public class DatabaseFreeBusyProvider implements LocalFreeBusyProvider {
	private Ical4jHelper ical4jHelper;
	private DomainDao domainDao;
	private UserDao userDao;
	private CalendarDao calendarDao;

	@Inject
	/* package */DatabaseFreeBusyProvider(Ical4jHelper ical4jHelper, DomainDao domainDao,
			UserDao userDao, CalendarDao calendarDao) {
		this.ical4jHelper = ical4jHelper;
		this.domainDao = domainDao;
		this.userDao = userDao;
		this.calendarDao = calendarDao;
	}

	@Override
	public String findFreeBusyIcs(FreeBusyRequest fbr) throws FreeBusyException {
		String ics = null;

		FreeBusy freeBusy = findFreeBusy(fbr);
		if (freeBusy != null) {
			ics = ical4jHelper.parseFreeBusy(freeBusy);
		}
		return ics;
	}

	private FreeBusy findFreeBusy(FreeBusyRequest fbr) throws PrivateFreeBusyException {
		String email = fbr.getOwner();
		String domainName = findDomainName(email);
		ObmUser user = findObmUser(email, domainName);
		if (user == null) {
			return null;
		}

		if (!user.isPublicFreeBusy()) {
			throw new PrivateFreeBusyException();
		}

		FreeBusy freeBusy = null;
		List<FreeBusy> freeBusyList = calendarDao.getFreeBusy(user.getDomain(), fbr);
		if (!freeBusyList.isEmpty()) {
			freeBusy = freeBusyList.get(0);
		}
		return freeBusy;
	}

	private ObmUser findObmUser(String email, String domainName) {
		ObmDomain domain = null;
		ObmUser user = null;
		if (!Strings.isNullOrEmpty(domainName)) {
			domain = domainDao.findDomainByName(domainName);
		}
		if (domain != null) {
			user = userDao.findUser(email, domain);
		}
		return user;
	}

	private String findDomainName(String email) {
		String[] parts = email.split("@");
		String domain = null;
		if (parts.length > 1) {
			domain = parts[1];
		}
		return domain;
	}
}
