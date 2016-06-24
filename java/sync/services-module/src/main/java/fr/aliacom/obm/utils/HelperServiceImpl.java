/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package fr.aliacom.obm.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.service.user.UserService;
import org.obm.sync.Right;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.UserPassword;

@Singleton
public class HelperServiceImpl implements HelperService {

	private static final Logger logger = LoggerFactory.getLogger(HelperServiceImpl.class);
	private static final String HEX_DIGITS = "0123456789abcdef";

	private final HelperDao helperDao;
	private final UserService userService;
	
	@Inject
	protected HelperServiceImpl(HelperDao helperDao, UserService userService) {
		this.helperDao = helperDao;
		this.userService = userService;
	}

	@Override
	public String getMD5Diggest(UserPassword password) {
		try {
			MessageDigest mg = MessageDigest.getInstance("MD5");
			mg.update(password.getStringValue().getBytes(Charsets.UTF_8));
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
	public boolean canWriteOnCalendar(AccessToken accessToken, String email) {
		return listRightsOnCalendars(accessToken, ImmutableList.of(email))
				.getRights(email).or(EnumSet.noneOf(Right.class))
				.contains(Right.WRITE);
	}

	@Override
	public CalendarRights listRightsOnCalendars(AccessToken accessToken,
			Iterable<String> emails) {
		Set<String> emailSet = ImmutableSet.copyOf(emails);
		Map<String, String> loginToEmail = buildLoginToEmailMap(emailSet);
		Set<String> loginsOnCurrentDomain = loginToEmail.keySet();
		Set<String> loginsWithImplicitRights = findEmailsWithImplicitRights(accessToken,
				loginsOnCurrentDomain);
		Set<String> loginsWithUnknownRights = Sets.difference(loginsOnCurrentDomain,
				loginsWithImplicitRights);
		Map<String, EnumSet<Right>> otherLoginsToRights = null;
		try {
			otherLoginsToRights = helperDao.listRightsOnCalendars(accessToken,
					loginsWithUnknownRights);
		} catch (SQLException e) {
			Throwables.propagate(e);
		}

		Map<String, EnumSet<Right>> calsWithImplicitRightsToRights = buildCalsWithImplicitRightsToRights(
				loginsToEmails(loginsWithImplicitRights, loginToEmail));
		Map<String, EnumSet<Right>> otherCalsToRights = loginMapToEmailMap(otherLoginsToRights,
				loginToEmail);
		Map<String, EnumSet<Right>> almostCompleteResults = Maps.newHashMap();
		almostCompleteResults.putAll(calsWithImplicitRightsToRights);
		almostCompleteResults.putAll(otherCalsToRights);

		CalendarRights results;
		if (almostCompleteResults.size() == emailSet.size()) {
			results = CalendarRights.builder().fromMap(almostCompleteResults).build();
		}
		else {
			// Some target calendars have no rights at all and do not appear in
			// otherCalsToRights
			results = appendCalendarsWithNoRights(emailSet, almostCompleteResults);
		}

		return results;
	}

	private static <T> Map<String, T> loginMapToEmailMap(Map<String, T> loginToValue,
			final Map<String, String> loginToEmail) {
		ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
		for (Map.Entry<String, T> entry : loginToValue.entrySet()) {
			builder.put(loginToEmail.get(entry.getKey()), entry.getValue());
		}
		return builder.build();
	}

	private static Set<String> loginsToEmails(Set<String> logins,
			final Map<String, String> loginToEmail) {
		return ImmutableSet.copyOf(Iterables.transform(logins, new Function<String, String>() {

			@Override
			public String apply(String login) {
				return loginToEmail.get(login);
			}

		}));
	}

	private static CalendarRights appendCalendarsWithNoRights(Set<String> allEmails,
			Map<String, EnumSet<Right>> emailToRights) {
		CalendarRights.Builder builder = CalendarRights.builder();
		for (String email : allEmails) {
			EnumSet<Right> rights = Optional.fromNullable(emailToRights.get(email)).or(EnumSet.noneOf(Right.class));
			builder.addRights(email, rights);
		}
		return builder.build();
	}

	private Map<String, String> buildLoginToEmailMap(Set<String> emails) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (String email : emails) {
			builder.put(this.extractLogin(email), email);
		}
		return builder.build();
	}

	private static Map<String, EnumSet<Right>> buildCalsWithImplicitRightsToRights(
			Set<String> emails) {
		return Maps.asMap(emails, new Function<String, EnumSet<Right>>() {

			@Override
			public EnumSet<Right> apply(String input) {
				return EnumSet.of(Right.ACCESS, Right.READ, Right.WRITE);
			}

		});
	}

	private Set<String> findEmailsWithImplicitRights(final AccessToken token, Set<String> emails) {
		return ImmutableSet.copyOf(Iterables.filter(emails, new Predicate<String>() {

			@Override
			public boolean apply(String email) {
				return checkImplicitRights(token, email);
			}

		}));
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
		try {
			Map<String, EnumSet<Right>> mailToRights = helperDao.listRightsOnCalendars(accessToken, ImmutableList.of(login));
			EnumSet<Right> rights = mailToRights.get(login);
			return rights != null && rights.contains(Right.READ);
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
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
		if (   accessToken.getUserLogin().equalsIgnoreCase(loginOrEmailWithoutDomain) || 
			   accessToken.getUserWithDomain().equalsIgnoreCase(loginOrEmailWithoutDomain)  ) {
			return true;
		}
		return accessToken.isRootAccount();
	}
	
	@Override
	public boolean attendeesContainsUser(List<Attendee> attendees,
			AccessToken token) {
		final String email = token.getUserEmail();
		return Iterables.any(attendees, new Predicate<Attendee>() {
			@Override
			public boolean apply(Attendee attendee) {
				return attendee.getEmail().equalsIgnoreCase(email);
			}
		});
	}

	@Override
	public boolean eventBelongsToCalendar(Event event, String calendar) {
		Preconditions.checkNotNull(event);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(event.getOwnerEmail()));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(calendar));
		
		return calendar.equalsIgnoreCase(event.getOwnerEmail());
	}
	
}
