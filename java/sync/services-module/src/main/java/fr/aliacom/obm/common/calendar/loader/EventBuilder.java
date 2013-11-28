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
	private final Calendar cal;
	
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
