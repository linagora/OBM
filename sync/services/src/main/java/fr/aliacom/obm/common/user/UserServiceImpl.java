package fr.aliacom.obm.common.user;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class UserServiceImpl implements UserService {

	private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
	private final DomainService domainService;
	private final UserDao userDao;

	@Inject
	private UserServiceImpl(DomainService domainService, UserDao userDao) {
		this.domainService = domainService;
		this.userDao = userDao;
	}
	
	@Override
	public ObmUser getUserFromAccessToken(AccessToken token) {
		ObmDomain obmDomain = domainService.findDomainByName(token.getDomain());
		return userDao.findUserById(token.getObmId(), obmDomain);
	}

	@Override
	public ObmUser getUserFromLogin(String login, String domain) {
		try {
			return getUserFromCalendar(login, domain);
		} catch (FindException e) {
			return null;
		}
	}

	@Override
	public ObmUser getUserFromAttendee(Attendee attendee, String domain) {
		return getUserFromLogin(attendee.getEmail(), domain);
	}

	@Override
	public ObmUser getUserFromCalendar(String calendar, String domainName) throws FindException {
		String username = getUsernameFromString(calendar);
		if (username == null) {
			throw new FindException("invalid calendar string : " + calendar);
		}
		ObmDomain domain = domainService.findDomainByName(domainName);
		if(domain == null){
			logger.info("domain :" + domainName
					+ " not found");
			throw new FindException("Domain["+domainName+"] not exist or not valid");
		}
		ObmUser user = userDao.findUserByLogin(username, domain);
		if (user == null || StringUtils.isEmpty(user.getEmail())) {
			logger.info("user :" + calendar	+ " not found, archived or have no email");
			throw new FindException("Calendar not exist or not valid");
		}
		return user;
	}

	private String getUsernameFromString(String calendar) {
		String username = Iterables.getFirst(
				Splitter.on('@').omitEmptyStrings().split(calendar), null);
		return username;
	}

}
