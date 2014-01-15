/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package fr.aliacom.obm.common.calendar.loader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.obm.push.utils.JDBCUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.RecurrenceDaysParser;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.utils.DisplayNameUtils;
import org.obm.sync.utils.MailUtils;

public class EventBuilder {
	private Calendar cal;
	
	public EventBuilder(Calendar cal) {
		this.cal = cal;
	}

	public Event buildFromResultSet(ResultSet rs) throws SQLException {
		Event e = new Event();
		int id = rs.getInt("event_id");
		e.setUid(new EventObmId(id));
		e.setTimeUpdate(JDBCUtils.getDate(rs, "event_timeupdate"));
		e.setTimeCreate(JDBCUtils.getDate(rs, "event_timecreate"));
		e.setType(EventType.valueOf(rs.getString("event_type")));
		e.setExtId(new EventExtId(rs.getString("event_ext_id")));
		e.setOpacity(EventOpacity.getValueOf(rs.getString("event_opacity")));
		e.setCategory(rs.getString("eventcategory1_label"));
		e.setTitle(rs.getString("event_title"));
		e.setLocation(rs.getString("event_location"));
		cal.setTimeInMillis(rs.getTimestamp("event_date").getTime());
		e.setStartDate(cal.getTime());
		e.setTimezoneName(rs.getString("event_timezone"));
		e.setDuration(JDBCUtils.convertNegativeIntegerToZero(rs, "event_duration"));
		e.setPriority(rs.getInt("event_priority"));
		e.setPrivacy(EventPrivacy.valueOf(rs.getInt("event_privacy")));
		e.setAllday(rs.getBoolean("event_allday"));
		e.setDescription(rs.getString("event_description"));
		e.setSequence(rs.getInt("event_sequence"));

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.valueOf(rs.getString("event_repeatkind")));
		er.setDays(new RecurrenceDaysParser().parse(rs.getString("event_repeatdays")));
		er.setFrequence(rs.getInt("event_repeatfrequence"));
		if (rs.getTimestamp("event_endrepeat") != null) {
			cal.setTimeInMillis(rs.getTimestamp("event_endrepeat").getTime());
			er.setEnd(cal.getTime());
		}
		e.setRecurrence(er);

		e.setOwner(rs.getString("owner"));
		e.setOwnerEmail(getUserObmEmail(rs, rs.getString("domain_name")));
		e.setOwnerDisplayName(getOwnerDisplayName(rs));
		e.setCreatorEmail(getCreatorObmEmail(rs, rs.getString("domain_name")));
		e.setCreatorDisplayName(getCreatorDisplayName(rs));
		if (rs.getTimestamp("recurrence_id") != null) {
			cal.setTimeInMillis(rs.getTimestamp("recurrence_id").getTime());
			e.setRecurrenceId(cal.getTime());
		}
		return e;
	}
	

	private String getUserObmEmail(ResultSet rs, String domainName) throws SQLException {
		String firstEmail = null;
		String userEmailString = rs.getString("userobm_email");
		if (userEmailString != null) {
			String[] alias = userEmailString.split("\r\n");
			if (alias[0].contains("@")) {
				firstEmail = alias[0];
			} else {
				firstEmail = alias[0] + "@" + domainName;
			}
		}
		return firstEmail;
	}
	
	private String getOwnerDisplayName(ResultSet evrs) throws SQLException {
		String first = evrs.getString("ownerFirstName");
		String last = evrs.getString("ownerLastName");
		String common = evrs.getString("ownerCommonName");
		return getDisplayName(first, last, common);
	}

	private String getCreatorDisplayName(ResultSet evrs) throws SQLException {
		String first = evrs.getString("creatorFirstName");
		String last = evrs.getString("creatorLastName");
		String common = evrs.getString("creatorCommonName");
		return getDisplayName(first, last, common);
	}

	private String getDisplayName(String firstName, String lastName,
			String commonName) {
		return DisplayNameUtils.getDisplayName(commonName, firstName, lastName);
	}

	private String getCreatorObmEmail(ResultSet rs, String domainName) throws SQLException {
		return MailUtils.extractFirstEmail(rs.getString("creatorEmail"), domainName);
	}
}
