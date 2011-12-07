/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.calendar.CalendarDaoJdbcImpl;
import fr.aliacom.obm.common.user.UserService;

@Singleton
public class HelperServiceImpl implements HelperService {

	private static final Logger logger = LoggerFactory.getLogger(HelperServiceImpl.class);
	private static final String HEX_DIGITS = "0123456789abcdef";

	private final HelperDao helperDao;
	private final CalendarDaoJdbcImpl calendarDaoJdbcImpl;
	private final UserService userService;
	
	@Inject
	protected HelperServiceImpl(HelperDao helperDao, CalendarDaoJdbcImpl calendarDaoJdbcImpl, UserService userService) {
		this.helperDao = helperDao;
		this.calendarDaoJdbcImpl = calendarDaoJdbcImpl;
		this.userService = userService;
	}

	@Override
	public String constructEmailFromList(String listofmail, String domain) {
		return calendarDaoJdbcImpl.constructEmailFromList(listofmail, domain);
	}

	@Override
	public String getMD5Diggest(String plaintext) {
		try {
			MessageDigest mg = MessageDigest.getInstance("MD5");
			mg.update(plaintext.getBytes());
			return toHexString(mg.digest());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String toHexString(byte[] param) {
		StringBuffer sb = new StringBuffer(param.length * 2);
		for (int i = 0; i < param.length; i++) {
			int b = param[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4)).append(
					HEX_DIGITS.charAt(b & 0xF));
		}
		return sb.toString();
	}

	/**
	 * Returns true if the logged in user can writer on the given user_login's
	 * calendar
	 */
	@Override
	public boolean canWriteOnCalendar(AccessToken accessToken, String loginOrEmail) {
		String login = extractLogin(loginOrEmail);
		if (checkImplicitRights(accessToken, login)) {
			return true;
		}
		return helperDao.canWriteOnCalendar(accessToken, login);
	}

	/**
	 * Returns true if the logged in user can writer on the given user_login's
	 * calendar
	 */
	@Override
	public boolean canReadCalendar(AccessToken accessToken, String loginOrEmail) {
		String login = extractLogin(loginOrEmail);
		if (checkImplicitRights(accessToken, login)) {
			return true;
		}
		return helperDao.canReadCalendar(accessToken, login);
	}

	private String extractLogin(String loginOrEmail) {
		if (isEmail(loginOrEmail)) {
			String login = userService.getLoginFromEmail(loginOrEmail);
			if (login != null) {
				return login;
			} else {
				return getEmailWithoutDomain(loginOrEmail);
			}
		} else {
			return loginOrEmail;
		}
	}

	private String getEmailWithoutDomain(String email) {
		if (email != null) {
			Iterable<String> it = Splitter.on('@').omitEmptyStrings().split(email);
			return Iterables.get(it, 0, "");
		}
		return email;
	}
	
	private boolean isEmail(String str) {
		if (str.contains("@")) {
			String[] parts = str.split("@");
			return parts.length == 2;
		} 
		return false;
	}
	
	private boolean checkImplicitRights(AccessToken accessToken, String loginOrEmailWithoutDomain) {
		if (   accessToken.getUser().equalsIgnoreCase(loginOrEmailWithoutDomain) || 
			   accessToken.getUserWithDomain().equalsIgnoreCase(loginOrEmailWithoutDomain)  ) {
			return true;
		}
		return accessToken.isRootAccount();
	}
	
	@Override
	public boolean attendeesContainsUser(List<Attendee> attendees,
			AccessToken token) {
		final String email = token.getEmail();
		return Iterables.any(attendees, new Predicate<Attendee>() {
			@Override
			public boolean apply(Attendee attendee) {
				return attendee.getEmail().equalsIgnoreCase(email);
			}
		});
	}
	
}
