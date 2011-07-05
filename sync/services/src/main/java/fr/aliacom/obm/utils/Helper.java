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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Helper {

	private static final Logger logger = LoggerFactory.getLogger(Helper.class);
	private static final String HEX_DIGITS = "0123456789abcdef";
	private static final String DATE_UTC_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

	private final SimpleDateFormat dateFormatUTC;
	private final ObmHelper obmHelper;

	@Inject
	protected Helper(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
		dateFormatUTC = new SimpleDateFormat(DATE_UTC_PATTERN);
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public String getLoginFromEmail(String email) {
		String username = "";
		if (email != null) {
			Iterable<String> it = Splitter.on('@').omitEmptyStrings()
					.split(email);
			username = Iterables.get(it, 0, "");
		}
		return username;
	}

	public String constructEmailFromList(String listofmail, String domain) {

		String[] lemail = null;
		if (listofmail != null) {
			lemail = listofmail.split("\r\n");
			if (lemail.length > 0) {

				if (lemail[0].contains("@")) {
					return lemail[0];
				}
				return lemail[0] + "@" + domain;
			}
		}
		return "";
	}

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
	 * Returns the given date in utc format.
	 */
	public String getUTCFormat(Date date) {
		String utc = null;
		if (date != null) {
			utc = dateFormatUTC.format(date);
		}
		return utc;
	}

	/**
	 * Returns true if the logged in user can writer on the given user_login's
	 * calendar
	 */
	public boolean canWriteOnCalendar(AccessToken writer, String targetCalendar) {
		// implicit right
		String calendarLogin = getLoginFromEmail(targetCalendar);
		if (checkImplicitRights(writer, calendarLogin)) {
			return true;
		}
		// special account : root account
		if (writer.isRootAccount()) {
			return true;
		}

		boolean ret = false;

		String q =
		// direct rights
		"select entityright_write "
				+ "from EntityRight "
				+ "inner join UserEntity on entityright_consumer_id=userentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userentity_user_id=? and userobm_login=?  "
				+ "and entityright_write=1 and userobm_email is not null "
				+ "and userobm_email != '' and userobm_archive != 1"
				+ " union "
				// public cals
				+ "select entityright_write "
				+ "from EntityRight "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userobm_login=? AND "
				+ // targetCalendar
				"entityright_consumer_id is null and entityright_write=1 and userobm_email is not null and userobm_email != '' "
				+ " and userobm_archive != 1 "

				+ " union "
				// group rights
				+ "select entityright_write "
				+ "from EntityRight "
				+ "inner join GroupEntity on entityright_consumer_id=groupentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = groupentity_group_id "
				+ "where of_usergroup_user_id=? and entityright_write=1  and userobm_login=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, writer.getObmId());
			ps.setString(2, calendarLogin);
			ps.setString(3, calendarLogin);
			ps.setInt(4, writer.getObmId());
			ps.setString(5, calendarLogin);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret = ret || rs.getBoolean(1);
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

	/**
	 * Returns true if the logged in user can writer on the given user_login's
	 * calendar
	 */
	public boolean canReadCalendar(AccessToken writer, String targetCalendar) {
		String calendarLogin = getLoginFromEmail(targetCalendar);
		// implicit right
		if (checkImplicitRights(writer, calendarLogin)) {
			return true;
		}
		// special account : root account
		if (writer.isRootAccount()) {
			return true;
		}

		boolean ret = false;

		String q =
		// direct rights
		"select entityright_read "
				+ "from EntityRight "
				+ "inner join UserEntity on entityright_consumer_id=userentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userentity_user_id=? and userobm_login=?  "
				+ "and entityright_read=1 and userobm_email is not null "
				+ "and userobm_email != '' and userobm_archive != 1"
				+ " union "
				// public cals
				+ "select entityright_read "
				+ "from EntityRight "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userobm_login=? AND "
				+ // targetCalendar
				"entityright_consumer_id is null and entityright_read=1 and userobm_email is not null and userobm_email != '' "
				+ " and userobm_archive != 1 "

				+ " union "
				// group rights
				+ "select entityright_read "
				+ "from EntityRight "
				+ "inner join GroupEntity on entityright_consumer_id=groupentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = groupentity_group_id "
				+ "where of_usergroup_user_id=? and entityright_read=1  and userobm_login=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, writer.getObmId());
			ps.setString(2, calendarLogin);
			ps.setString(3, calendarLogin);
			ps.setInt(4, writer.getObmId());
			ps.setString(5, calendarLogin);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret = ret || rs.getBoolean(1);
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

	private boolean checkImplicitRights(AccessToken writer,
			String targetCalendar) {
		return writer.getUser().equalsIgnoreCase(targetCalendar)
				|| writer.getUserWithDomain().equalsIgnoreCase(targetCalendar);
	}

	/**
	 * Reset status of event
	 */
	public void resetAttendeesStatus(Event event) {
		if (event.getAttendees() != null) {
			for (Attendee att : event.getAttendees()) {
				att.setState(ParticipationState.NEEDSACTION);
			}
		}
	}

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
