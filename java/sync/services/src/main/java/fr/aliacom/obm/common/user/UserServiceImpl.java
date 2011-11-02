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
package fr.aliacom.obm.common.user;

import org.apache.commons.lang.StringUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory
			.getLogger(UserServiceImpl.class);
	private final DomainService domainService;
	private final UserDao userDao;

	@Inject
	/* package */ UserServiceImpl(DomainService domainService, UserDao userDao) {
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
		// Lowercase the username, we're going to attempt to match it against the
		// login, and all logins in the DB are lowercase, while usernames might not be so,
		// especially if provisioning from LDAP is involved (OBMFULL-2553)
		String lcUsername = username.toLowerCase();
		ObmUser user = userDao.findUserByLogin(lcUsername, domain);
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
	
	@Override
	public String getLoginFromEmail(String email) {
		ObmUser obmUser = findUserFromEmail(email);
		if (obmUser != null && obmUser.getLogin() != null) {
			return obmUser.getLogin();
		}
		return null;
	}

	private ObmUser findUserFromEmail(String email) {
		String domainName = findDomainNameFromEmail(email);
		if (domainName != null) {
			ObmDomain obmDomain = domainService.findDomainByName(domainName);
			if (obmDomain != null) {
				return userDao.findUser(email, obmDomain);
			}	
		}
		return null;
	}
	
	private String findDomainNameFromEmail(String email) {
		String[] parts = email.split("@");
		String domain = null;
		if (parts.length > 1) {
			domain = parts[1];
		}
		return domain;
	}

}
