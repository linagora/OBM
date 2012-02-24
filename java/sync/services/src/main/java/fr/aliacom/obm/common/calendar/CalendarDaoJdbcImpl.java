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
package fr.aliacom.obm.common.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;

import org.obm.icalendar.Ical4jHelper;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.jdbc.AbstractSQLCollectionHelper;
import org.obm.push.utils.jdbc.IntegerIndexedSQLCollectionHelper;
import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.AttendeeAlert;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.solr.SolrHelper;
import org.obm.sync.solr.SolrHelper.Factory;
import org.obm.sync.utils.DisplayNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.contact.ContactDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.utils.LinkedEntity;
import fr.aliacom.obm.utils.LogUtils;
import fr.aliacom.obm.utils.ObmHelper;
import fr.aliacom.obm.utils.RFC2445;

/**
 * Calendar data access functions
 */
@Singleton
public class CalendarDaoJdbcImpl implements CalendarDao {

	private static final Logger logger = LoggerFactory
			.getLogger(CalendarDaoJdbcImpl.class);

	// event_ext_id | character varying(255) | default ''::character varying
	// event_owner | integer |
	// event_timezone | character varying(255) | default 'GMT'::character
	// varying
	// event_opacity | event_opacity | default 'OPAQUE'::event_opacity
	// event_title | character varying(255) | default NULL::character varying
	// event_location | character varying(100) | default NULL::character varying
	// event_category1_id | integer |
	// event_priority | integer |
	// event_privacy | integer |
	// event_date | timestamp without time zone | not null
	// event_duration | integer | not null default 0
	// event_allday | boolean | default false
	// event_repeatkind | character varying(20) | default NULL::character
	// varying
	// event_repeatfrequence | integer |
	// event_repeatdays | character varying(7) | default NULL::character varying
	// event_endrepeat | timestamp without time zone |
	// event_color | character varying(7) |
	// event_completed | timestamp without time zone | not null
	// event_url | text |
	// event_description | text |
	// event_properties | text |

	private static final String EVENT_SELECT_FIELDS = "event_id, "
			+ "event_timeupdate, "
			+ "event_timecreate, "
			+ "event_type, "
			+ "event_ext_id, "
			+ "event_timezone, "
			+ "event_opacity, "
			+ "event_title, "
			+ "event_location, "
			+ "eventcategory1_label, "
			+ "event_priority, "
			+ "event_privacy, "
			+ "event_date, "
			+ "event_duration, "
			+ "event_allday, "
			+ "event_repeatkind, "
			+ "event_repeatfrequence, "
			+ "event_repeatdays, "
			+ "event_endrepeat, "
			+ "event_color, "
			+ "event_completed, "
			+ "event_url, "
			+ "event_description, now() as last_sync, event_domain_id, evententity_entity_id, "
			+ "event_sequence, "
			+ "o.userobm_login as owner, domain_name, o.userobm_firstname as ownerFirstName, "
			+ "o.userobm_lastname as ownerLastName,  o.userobm_commonname as ownerCommonName, " 
			+ "o.userobm_email,"
			+ "c.userobm_login as creator, "
			+ "c.userobm_firstname as creatorFirstName, "
			+ "c.userobm_lastname as creatorLastName,"
			+ "c.userobm_commonname as creatorCommonName, "
		    + "c.userobm_email as creatorEmail";

	private static final String EVENT_INSERT_FIELDS = "event_owner, "
			+ "event_ext_id, "
			+ "event_timezone, "
			+ "event_opacity, "
			+ "event_title, "
			+ "event_location, "
			+ "event_category1_id, "
			+ "event_priority, "
			+ "event_privacy, "
			+ "event_date, "
			+ "event_duration, "
			+ "event_allday, "
			+ "event_repeatkind, "
			+ "event_repeatfrequence, "
			+ "event_repeatdays, "
			+ "event_endrepeat, "
			+ "event_color, "
			+ "event_completed, "
			+ "event_url, "
			+ "event_description, event_domain_id, event_usercreate, event_origin, event_type, event_timecreate,"
			+ "event_sequence";

	private static final String ATT_AND_ALERT_FIELDS = "eventlink_event_id, "
			+ "eventlink_state, "
			+ "eventlink_required, "
			+ "eventlink_percent, "
			+ "eventlink_is_organizer, "
			+ "eventalert_duration, userobm_email, userobm_firstname, userobm_lastname, userobm_commonname, userentity_user_id ";

	private static final String CONTACT_AND_ALERT_FIELDS = "eventlink_event_id, "
			+ "eventlink_state, "
			+ "eventlink_required, "
			+ "eventlink_percent, "
			+ "eventlink_is_organizer, "
			+ "0 as eventalert_duration, email_address as userobm_email, contact_firstname as userobm_firstname, "
			+ "contact_lastname as userobm_lastname, contact_commonname as userobm_commonname, contactentity_contact_id as userentity_user_id ";

	private static final String ATT_INSERT_FIELDS = "eventlink_event_id, eventlink_entity_id, "
			+ "eventlink_state, eventlink_required, eventlink_percent, eventlink_usercreate, eventlink_is_organizer";

	private static final String EXCEPS_FIELDS = "event_id, eventexception_date";

	private final UserDao userDao;
	private final ContactDao contactDao;
	private final ObmHelper obmHelper;

	private final Ical4jHelper ical4jHelper;

	private final Factory solrHelperFactory;

	@Inject
	private CalendarDaoJdbcImpl(UserDao userDao, SolrHelper.Factory solrHelperFactory, ContactDao contactDao,
			ObmHelper obmHelper, Ical4jHelper ical4jHelper) {
		this.userDao = userDao;
		this.solrHelperFactory = solrHelperFactory;
		this.contactDao = contactDao;
		this.obmHelper = obmHelper;
		this.ical4jHelper = ical4jHelper;
	}
	
	private Integer catIdFromString(Connection con, String category,
			int domainId) throws SQLException {
		if (category == null) {
			return null;
		}

		Integer ret = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement("select eventcategory1_id from EventCategory1 where eventcategory1_label=? "
					+ "and eventcategory1_domain_id=?");
			st.setString(1, category);
			st.setInt(2, domainId);
			rs = st.executeQuery();
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
		return ret;
	}

	@Override
	public Event createEvent(AccessToken editor, String calendar, Event ev, Boolean useObmUser)throws FindException, SQLException, ServerFault {
		logger.info("create with token " + editor.getSessionId() + " from "
				+ editor.getOrigin() + " for " + editor.getUserEmail());
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return createEvent(con, editor, calendar, ev, useObmUser);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}

	@Override
	public Event createEvent(Connection con, AccessToken editor, String calendar, Event ev,
			Boolean useObmUser) throws SQLException, FindException, ServerFault {
		Integer ownerId = null;
		logger.info("try to create with calendar owner:"
				+ calendar);
		ownerId = userDao.userIdFromEmail(con, calendar, editor.getDomain().getId());
		if(ownerId == null){
			throw new FindException("Error finding user["+calendar+"]");
		}
		
		String evQ = "INSERT INTO Event (" + EVENT_INSERT_FIELDS
				+ ") values ( " + ownerId + ",  " +
				// event_owner
				"?, " + // event_ext_id
				"?, " + // event_timezone
				"?, " + // event_opacity
				"?, " + // event_title
				"?, " + // event_location
				"?, " + // event_category1_id
				"?, " + // event_priority
				"?, " + // event_privacy
				"?, " + // event_date
				"?, " + // event_duration
				"?, " + // event_allday
				"?, " + // event_repeatking
				"?, " + // event_repeatfrequence
				"?, " + // event_repeatdays
				"?, " + // event_endrepeat
				"?, " + // event_color
				"?, " + // event_completed
				"?, " + // event_url
				"?," + // event_description
				"?," + // event_domain_id
				editor.getObmId() + // event_usercreate
				",?," + // origin
				"?," + // type
				"now()," + // event_timecreate
				"?" + // event_sequence
				")";

		PreparedStatement ps = con.prepareStatement(evQ);
		fillEventStatement(ps, ev, editor, 1);
		ps.executeUpdate();
		EventObmId id = new EventObmId(obmHelper.lastInsertId(con));
		ev.setUid(id);

		ps.close();

		LinkedEntity le = obmHelper.linkEntity(con, "EventEntity", "event_id",
				id.getObmId());
		ev.setEntityId(le.getEntityId());

		insertAttendees(editor, calendar, ev, con, ev.getAttendees(), useObmUser);

		insertExceptions(editor, ev, con, id);
		if (ev.isRecurrent()) {
			insertEventExceptions(editor, calendar, ev.getRecurrence()
					.getEventExceptions(), con, id, useObmUser);
		}
		Integer a = ev.getAlert();
		if (a != null && a >= 0) {
			ps = con.prepareStatement("insert into EventAlert "
					+ "(eventalert_event_id, eventalert_user_id, eventalert_duration, eventalert_usercreate) "
					+ "values (?, ?, ?, ?)");
			ps.setInt(1, id.getObmId());
			ps.setInt(2, editor.getObmId());
			ps.setInt(3, a);
			ps.setInt(4, editor.getObmId());
			ps.executeUpdate();
		}
		ps.close();
		
		indexEvent(editor, ev);	
		
		return ev;
	}

	private void indexEvent(AccessToken editor, Event ev) {
		try {
			solrHelperFactory.createClient(editor).createOrUpdate(ev);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	private List<Event> insertEventExceptions(AccessToken editor, String calendar,
			List<Event> eventException, Connection con, EventObmId id, Boolean useObmUser)
			throws SQLException, FindException, ServerFault {
		List<Event> newEvExcepts = new LinkedList<Event>();
		Event created = null;
		Map<EventObmId, Date> eventsEx = new HashMap<EventObmId, Date>();

		for (Event evExcept : eventException) {
			created = createEvent(con, editor, calendar, evExcept, useObmUser);
			newEvExcepts.add(created);
			eventsEx.put(created.getObmId(),
					evExcept.getRecurrenceId());
		}

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("insert into EventException "
					+ "(eventexception_parent_id, eventexception_child_id, eventexception_date, eventexception_usercreate) "
					+ "values (?, ?, ?, " + editor.getObmId() + ")");

			for (Entry<EventObmId, Date> entry: eventsEx.entrySet()) {
				ps.setInt(1, id.getObmId());
				ps.setInt(2, entry.getKey().getObmId());
				ps.setTimestamp(3, new Timestamp(entry.getValue().getTime()));
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
		return newEvExcepts;
	}

	private void insertExceptions(AccessToken editor, Event ev, Connection con,
			EventObmId id) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("insert into EventException "
					+ "(eventexception_parent_id, eventexception_date, eventexception_usercreate) "
					+ "values (?, ?, " + editor.getObmId() + ")");
			for (Date exd : ev.getRecurrence().getExceptions()) {
				ps.setInt(1, id.getObmId());
				ps.setTimestamp(2, new Timestamp(exd.getTime()));
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private Event eventFromCursor(Calendar cal, ResultSet evrs)
			throws SQLException {
		Event e = new Event();
		int id = evrs.getInt("event_id");
		e.setUid(new EventObmId(id));
		e.setTimeUpdate(evrs.getTimestamp("event_timeupdate"));
		e.setTimeCreate(evrs.getTimestamp("event_timecreate"));
		e.setType(EventType.valueOf(evrs.getString("event_type")));
		e.setExtId(new EventExtId(evrs.getString("event_ext_id")));
		e.setOpacity(EventOpacity.getValueOf(evrs.getString("event_opacity")));
		e.setCategory(evrs.getString("eventcategory1_label")); // cat as string
		// in
		// sync ??
		e.setTitle(evrs.getString("event_title"));
		e.setLocation(evrs.getString("event_location"));
		cal.setTimeInMillis(evrs.getTimestamp("event_date").getTime());
		e.setDate(cal.getTime());
		e.setDuration(evrs.getInt("event_duration"));
		e.setPriority(evrs.getInt("event_priority"));
		e.setPrivacy(evrs.getInt("event_privacy"));
		e.setAllday(evrs.getBoolean("event_allday"));
		e.setDescription(evrs.getString("event_description"));
		e.setSequence(evrs.getInt("event_sequence"));

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.valueOf(evrs.getString("event_repeatkind")));
		er.setDays(evrs.getString("event_repeatdays"));
		er.setFrequence(evrs.getInt("event_repeatfrequence"));
		if (evrs.getTimestamp("event_endrepeat") != null) {
			cal.setTimeInMillis(evrs.getTimestamp("event_endrepeat").getTime());
			er.setEnd(cal.getTime());			
		}
		e.setRecurrence(er);

		e.setEntityId(evrs.getInt("evententity_entity_id"));
		e.setOwner(evrs.getString("owner"));
		e.setOwnerEmail( getUserObmEmail(evrs, evrs.getString("domain_name")) );
		e.setOwnerDisplayName(getOwnerDisplayName(evrs));
		e.setCreator(evrs.getString("creator"));
		e.setCreatorEmail(getCreatorObmEmail(evrs, evrs.getString("domain_name")));
		e.setCreatorDisplayName(getCreatorDisplayName(evrs));
		if (evrs.getTimestamp("recurrence_id") != null) {
			cal.setTimeInMillis(evrs.getTimestamp("recurrence_id").getTime());
			e.setRecurrenceId(cal.getTime());
		}
		return e;
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

	private int fillEventStatement(PreparedStatement ps, Event ev,
			AccessToken at, int i) throws SQLException {
		int idx = i;
		ps.setString(idx++, ev.getExtId().getExtId());
		ps.setString(idx++, ev.getTimezoneName());
		ps.setObject(idx++, ev.getOpacity().getJdbcObject(obmHelper.getType()));
		ps.setString(idx++, ev.getTitle());
		ps.setString(idx++, ev.getLocation());
		Integer cat = catIdFromString(ps.getConnection(), ev.getCategory(),
				at.getDomain().getId());
		if (cat != null) {
			ps.setInt(idx++, cat);
		} else {
			ps.setNull(idx++, Types.INTEGER);
		}
		ps.setInt(idx++, RFC2445.getPriorityOrDefault(ev.getPriority()));
		ps.setInt(idx++, ev.getPrivacy());
		if (ev.getDate() != null) {
			ps.setTimestamp(idx++, new Timestamp(ev.getDate().getTime()));
		} else {
			ps.setNull(idx++, Types.DATE);
		}
		ps.setInt(idx++, ev.getDuration());
		ps.setBoolean(idx++, ev.isAllday());
		EventRecurrence r = ev.getRecurrence();
		ps.setString(idx++, r.getKind().toString());
		ps.setInt(idx++, r.getFrequence());
		ps.setString(idx++, r.getDays());
		if (r.getEnd() != null) {
			ps.setTimestamp(idx++, new Timestamp(r.getEnd().getTime()));
		} else {
			ps.setNull(idx++, Types.DATE);
		}
		ps.setNull(idx++, Types.VARCHAR); // color
		ps.setNull(idx++, Types.DATE); // FIXME completed
		ps.setNull(idx++, Types.VARCHAR); // FIXME url
		ps.setString(idx++, ev.getDescription());
		ps.setInt(idx++, at.getDomain().getId());
		ps.setString(idx++, at.getOrigin());
		ps.setObject(idx++, ev.getType().getJdbcObject(obmHelper.getType()));
		ps.setInt(idx++, ev.getSequence());
		return idx;
	}
	
    private List<DeletedEvent> findDeletedEvents(ObmUser calendarUser, Date d,
                    EventType et, List<DeletedEvent> declined) {

            List<DeletedEvent> result = new LinkedList<DeletedEvent>();
            result.addAll(declined);
            Connection con = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String q = "SELECT deletedevent_event_id, deletedevent_event_ext_id FROM DeletedEvent "
                               + "WHERE deletedevent_user_id=? AND deletedevent_type=? ";
            if (d != null) {
                    q += "AND deletedevent_timestamp >= ?";
            }
            try {
                    con = obmHelper.getConnection();
                    ps = con.prepareStatement(q);
                    ps.setInt(1, calendarUser.getUid());
                    ps.setObject(2, et.getJdbcObject(obmHelper.getType()));
                    if (d != null) {
                            ps.setTimestamp(3, new Timestamp(d.getTime()));
                    }

                    rs = ps.executeQuery();
                    while (rs.next()) {
                            result.add(new DeletedEvent(
                                            new EventObmId(rs.getInt(1)),
                                            new EventExtId(rs.getString(2))));
                    }
            } catch (SQLException se) {
                    logger.error(se.getMessage(), se);
            } finally {
                    obmHelper.cleanup(con, ps, rs);
            }

            return result;
    }
	

	@Override
	public Event findEventById(AccessToken token, EventObmId uid) throws EventNotFoundException, ServerFault {
		String ev = "SELECT "
				+ EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ " FROM Event e "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
				+ "INNER JOIN Domain ON event_domain_id=domain_id "
				+ "INNER JOIN EventEntity ON evententity_event_id=event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "WHERE event_id=? ";

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			
			evps = con.prepareStatement(ev);
			evps.setInt(1, uid.getObmId());
			evrs = evps.executeQuery();

			if (evrs.next()) {
				Calendar cal = DateUtils.getCurrentGMTCalendar();
				Event event = eventFromCursor(cal, evrs);
				String domainName = evrs.getString("domain_name");
				
				Map<EventObmId, Event> eventById = new HashMap<EventObmId, Event>();
				eventById.put(event.getObmId(), event);
			
				IntegerIndexedSQLCollectionHelper eventIdSQLCollectionHelper = new IntegerIndexedSQLCollectionHelper(ImmutableList.of(event.getObmId()));
				loadAttendeesAndAlerts(con, token, eventById, eventIdSQLCollectionHelper, domainName);
				loadExceptions(con, cal, eventById, eventIdSQLCollectionHelper);
				loadEventExceptions(con, token, eventById, eventIdSQLCollectionHelper);
				
				return event;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}
		throw new EventNotFoundException(uid);
	}

	@Override
	public List<String> findEventTwinKeys(String calendar, Event event,
			ObmDomain domain) {

		String squery = "SELECT DISTINCT event_id from Event "
				+ "INNER JOIN EventLink ON eventlink_event_id=event_id "
				+ "INNER JOIN UserEntity ON userentity_entity_id=eventlink_entity_id "
				+ "INNER JOIN UserObm ON userobm_id=userentity_user_id "
				+ "WHERE event_title=? AND userobm_login=? ";
		if (event.isAllday()) {
			squery += "AND event_date >= ? AND event_date <= ? AND (event_duration >= 86400 OR event_allday) ";
		} else {
			squery += "AND event_date=? AND event_duration=? ";
		}
		squery += "AND userobm_domain_id=?";

		PreparedStatement query = null;
		Connection con = null;
		ResultSet rs = null;
		List<String> ret = new LinkedList<String>();

		try {
			con = obmHelper.getConnection();
			query = con.prepareStatement(squery);
			int index = 1;
			query.setString(index++, event.getTitle());
			query.setString(index++, calendar);
			if (event.isAllday()) {

				java.util.Calendar dayAfter = java.util.Calendar.getInstance();
				dayAfter.setTimeZone(TimeZone.getTimeZone("GMT"));
				dayAfter.setTimeInMillis(event.getDate().getTime());
				dayAfter.add(java.util.Calendar.HOUR_OF_DAY, 11);

				java.util.Calendar dayBefore = java.util.Calendar.getInstance();
				dayBefore.setTimeZone(TimeZone.getTimeZone("GMT"));
				dayBefore.setTimeInMillis(event.getDate().getTime());
				dayBefore.add(java.util.Calendar.HOUR_OF_DAY, -11);

				query.setTimestamp(index++,
						new java.sql.Timestamp(dayBefore.getTimeInMillis()));

				query.setTimestamp(index++,
						new java.sql.Timestamp(dayAfter.getTimeInMillis()));
			} else {
				query.setTimestamp(index++, new java.sql.Timestamp(event
						.getDate().getTime()));
				query.setInt(index++, event.getDuration());
			}

			if (domain != null) {
				query.setInt(index++, domain.getId());
			}
			rs = query.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString(1));
			}
			logger.info("Found " + ret.size() + " results with title "
					+ event.getTitle() + " date: " + event.getDate()
					+ " duration: " + event.getDuration() + " domain_id: "
					+ (domain != null ? domain.getId() : "null"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, query, rs);
		}
		return ret;
	}

	@Override
	public List<String> findRefusedEventsKeys(ObmUser calendarUser, Date d) {
		List<Integer> result = new LinkedList<Integer>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String q = "SELECT eventlink_event_id FROM EventLink, UserEntity WHERE "
				+ "eventlink_state='DECLINED' AND eventlink_entity_id=userentity_entity_id AND userentity_user_id=? ";
		if (d != null) {
			q += "AND eventlink_timeupdate >= ?";
		}
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, calendarUser.getUid());
			if (d != null) {
				ps.setTimestamp(2, new Timestamp(d.getTime()));
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt(1));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		List<String> ret = new ArrayList<String>(result.size());
		for (Integer eid : result) {
			ret.add(eid.toString());
		}
		return ret;
	}

	private String getAttendeeDisplayName(ResultSet rs) throws SQLException {
		String first = rs.getString("userobm_firstname");
		String last = rs.getString("userobm_lastname");
		String common = rs.getString("userobm_commonname");
		return getDisplayName(first, last, common);
	}

	private String getUserObmEmail(ResultSet rs, String domainName)
			throws SQLException {
		String firstEmail = null;
		String userEmailString = rs.getString("userobm_email");
		if (userEmailString != null) {
			String[] alias = userEmailString.split("\r\n");
			if (alias[0].contains("@")) {
				firstEmail = alias[0];
			}
			else {
				firstEmail = alias[0] + "@" + domainName;
			}
		}
		return firstEmail;
	}

	private String getCreatorObmEmail(ResultSet rs, String domainName)
			throws SQLException {
		String firstEmail = null;
		String creatorEmailString = rs.getString("creatorEmail");
		if (creatorEmailString != null) {
			String[] alias = creatorEmailString.split("\r\n");
			if (alias[0].contains("@")) {
				firstEmail = alias[0];
			}
			else {
				firstEmail = alias[0] + "@" + domainName;
			}
		}
		return firstEmail;
	}

	private ParticipationRole getAttendeeRequired(ResultSet rs)
			throws SQLException {
		return ParticipationRole.valueOf(rs.getString("eventlink_required"));
	}

	private ParticipationState getAttendeeState(ResultSet rs)
			throws SQLException {
		return ParticipationState.getValueOf(rs.getString("eventlink_state"));
	}

	private int getAttendeePercent(ResultSet rs) throws SQLException {
		return rs.getInt("eventlink_percent");
	}
	
	private boolean getAttendeeOrganizer(ResultSet rs) throws SQLException {
		return rs.getBoolean("eventlink_is_organizer");
	}
	
	@Override
	public List<FreeBusy> getFreeBusy(ObmDomain domain, FreeBusyRequest fbr) {

		String fb = "SELECT e.event_id, e.event_date, e.event_duration, event_allday"
				+ ", e.event_repeatkind, e.event_repeatdays, e.event_repeatfrequence, e.event_endrepeat"
				+ " FROM Event e "
				+ "INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id "
				+ "INNER JOIN EventEntity ON e.event_id=evententity_event_id "
				+ "INNER JOIN UserObm ON ue.userentity_user_id=userobm_id "
				+ "INNER JOIN Domain ON e.event_domain_id=domain_id "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "WHERE "
				+ "userentity_user_id = ? AND event_type=? "
				+ "AND ((event_repeatkind != 'none' AND (event_endrepeat IS NULL OR event_endrepeat >= ?)) OR "
				+ "(event_date >= ? AND event_date <= ?) ) "
                + "AND eventlink_state!=? AND event_opacity=?";


		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		List<FreeBusy> ret = new LinkedList<FreeBusy>();
		try {
			con = obmHelper.getConnection();
			for (Attendee att : fbr.getAttendees()) {

				logger.info("freebusy " + att.getEmail() + " dstart: "
						+ fbr.getStart() + " dend: " + fbr.getEnd());

				ObmUser u = userDao.findUser(att.getEmail(), domain);

				Calendar cal = getGMTCalendar();
				if (u != null) {
					ps = null;
					rs = null;
					try {

						ps = con.prepareStatement(fb);

						int idx = 1;
						ps.setInt(idx++, u.getUid());
						ps.setObject(idx++, EventType.VEVENT.getJdbcObject(obmHelper.getType()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getStart()
								.getTime()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getStart()
								.getTime()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getEnd()
								.getTime()));
						ps.setObject(idx++, ParticipationState.DECLINED
								.getJdbcObject(obmHelper.getType()));
						ps.setObject(idx++, EventOpacity.OPAQUE
								.getJdbcObject(obmHelper.getType()));
						rs = ps.executeQuery();
						FreeBusy freebusy = new FreeBusy();
						freebusy.setStart(fbr.getStart());
						freebusy.setEnd(fbr.getEnd());
						freebusy.setOwner(fbr.getOwner());
						freebusy.setUid(fbr.getUid());
						freebusy.setAtt(att);
						while (rs.next()) {
							EventRecurrence er = eventRecurrenceFromCursor(cal, rs);
							cal.setTimeInMillis(rs.getTimestamp("event_date").getTime());
							
							if (er == null || RecurrenceKind.none.equals(er.getKind())) {
								cal.setTimeInMillis(rs.getTimestamp("event_date").getTime());
								freebusy.addFreeBusyInterval(getFreeBusyInterval(cal.getTime(), rs));
							} else {
								Set<Date> extDate = getAllDateEventException(con, new EventObmId(rs.getInt("event_id")));
								List<Date> recurDates = ical4jHelper
										.dateInInterval(er, cal.getTime(),
												fbr.getStart(), fbr.getEnd(),
												extDate);
								for (Date rd : recurDates) {
									FreeBusyInterval line = new FreeBusyInterval();
									cal.setTimeInMillis(rd.getTime());
									line.setStart(cal.getTime());
									line.setAllDay(rs.getBoolean("event_allday"));
									line.setDuration(rs.getInt("event_duration"));
									freebusy.addFreeBusyInterval(line);
								}
							}
						}

						ret.add(freebusy);

						logger.info("freebusy found "
								+ freebusy.getFreeBusyIntervals().size()
								+ " events.");

					} catch (SQLException e) {
						logger.error(e.getMessage(), e);
					} finally {
						obmHelper.cleanup(null, ps, rs);
					}
				} else {
					logger.info("User " + att.getEmail() + " doesn't exist");
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return ret;
	}

	private FreeBusyInterval getFreeBusyInterval(Date start, ResultSet rs)
			throws SQLException {
		FreeBusyInterval line = new FreeBusyInterval();
		line.setStart(start);
		line.setAllDay(rs.getBoolean("event_allday"));
		line.setDuration(rs.getInt("event_duration"));
		return line;
	}

	private Set<Date> getAllDateEventException(Connection con, EventObmId id) {
		Set<Date> ret = new HashSet<Date>();

		String q = "SELECT eventexception_date FROM EventException "
				+ " INNER JOIN Event ON eventexception_parent_id = event_id"
				+ " WHERE eventexception_parent_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(q);
			ps.setInt(1, id.getObmId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Timestamp ts = rs.getTimestamp("eventexception_date");
				Calendar cal = Calendar.getInstance();
				cal.setTime(ts);
				cal.set(Calendar.MILLISECOND, 0);
				ret.add(cal.getTime());
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return ret;
	}

	@Override
	public EventChanges getSync(AccessToken token, ObmUser calendarUser,
			Date lastSync, SyncRange syncRange, EventType typeFilter, boolean onEventDate) {
		EventChanges ret = new EventChanges();

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;
		Calendar cal = getGMTCalendar();
		StringBuilder fetchIds = new StringBuilder();
		fetchIds.append("SELECT e.event_id, att.eventlink_state, e.event_ext_id, ex.eventexception_parent_id ");
		fetchIds.append(" FROM Event e ");
		fetchIds.append("INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id ");
		fetchIds.append("INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id ");
		fetchIds.append("INNER JOIN EventLink attupd ON attupd.eventlink_event_id=e.event_id ");
		fetchIds.append("LEFT JOIN EventException ex ON ex.eventexception_child_id = e.event_id ");
		fetchIds.append("WHERE e.event_type=? AND ue.userentity_user_id=? ");

		// dirty hack to disable need-action to opush & tbird
		if (token.getOrigin().contains("push")) {
			fetchIds.append(" AND att.eventlink_state != 'NEEDS-ACTION' ");
		}

		if (lastSync != null) {
			fetchIds.append(" AND (e.event_timecreate >= ? OR e.event_timeupdate >= ? OR attupd.eventlink_timeupdate >= ?");
			if (onEventDate) {
				fetchIds.append(" OR e.event_date >= ? OR e.event_repeatkind != 'none'");
			}
			fetchIds.append(")");
		}
		
		if(syncRange != null){
			fetchIds.append("AND (");
			fetchIds.append("e.event_repeatkind != 'none' OR ");
			fetchIds.append("(e.event_date >= ? ");
			if(syncRange.getBefore() != null){
				fetchIds.append("AND e.event_date <= ? ");
			}
			fetchIds.append(") )");
			logger.info(token.getUserLogin() + " will use the sync range [ "
					+ syncRange.getAfter() + " - " + syncRange.getBefore() + " ]");
		}

		fetchIds.append(" GROUP BY e.event_id, att.eventlink_state, e.event_ext_id, ex.eventexception_parent_id");

		List<DeletedEvent> declined = new LinkedList<DeletedEvent>();
		Set<Event> parentOfDeclinedRecurrentEvent = Sets.newHashSet();
		
		StringBuilder fetched = new StringBuilder();
		fetched.append("(0");
		boolean fetchedData = false;
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(fetchIds.toString());
			int idx = 1;
			evps.setObject(idx++, typeFilter.getJdbcObject(obmHelper.getType()));
			evps.setObject(idx++, calendarUser.getUid());
			if (lastSync != null) {
				evps.setTimestamp(idx++, new Timestamp(lastSync.getTime()));
				evps.setTimestamp(idx++, new Timestamp(lastSync.getTime()));
				evps.setTimestamp(idx++, new Timestamp(lastSync.getTime()));
				if (onEventDate) {
					evps.setTimestamp(idx++, new Timestamp(lastSync.getTime()));
				}
			}
			if (syncRange != null) {
				evps.setTimestamp(idx++, new Timestamp(syncRange.getAfter().getTime()));
				if(syncRange.getBefore() != null){
					evps.setTimestamp(idx++, new Timestamp(syncRange.getBefore().getTime()));
				}
			}

			
			evrs = evps.executeQuery();
			while (evrs.next()) {
				int recurentParentId = evrs.getInt(4);
				ParticipationState state = ParticipationState.getValueOf(evrs
						.getString(2));
				Integer eventId = evrs.getInt(1);
				if (state == ParticipationState.DECLINED) {
					if (recurentParentId == 0) {
						declined.add(new DeletedEvent(
							new EventObmId(eventId), 
							new EventExtId(evrs.getString(3))));
					} else {					
						Event e = findEventById(token, new EventObmId(recurentParentId));
						parentOfDeclinedRecurrentEvent.add(e);
					}
				} else {
					fetchedData = true;
					if(recurentParentId > 0){
						fetched.append(",");
						fetched.append(recurentParentId);
					}
					fetched.append(",");
					fetched.append(eventId);
				}
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}
		fetched.append(")");

		List<Event> changedEvent = new LinkedList<Event>();
		Map<EventObmId, Event> eventById = new HashMap<EventObmId, Event>();

		evps = null;
		evrs = null;
		con = null;

		if (fetchedData) {
			String ev = "SELECT "
					+ EVENT_SELECT_FIELDS
					+ ", eventexception_date as recurrence_id "
					+ " FROM Event e "
					+ "INNER JOIN EventEntity ON e.event_id=evententity_event_id "
					+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
					+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
					+ "INNER JOIN Domain ON e.event_domain_id=domain_id "
					+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
					+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
					+ "WHERE e.event_id IN " + fetched.toString();
			
			try {
				con = obmHelper.getConnection();
				evps = con.prepareStatement(ev);
				evrs = evps.executeQuery();
				boolean lastSyncSet = false;
				while (evrs.next()) {
					if (!lastSyncSet) {
						ret.setLastSync(evrs.getTimestamp("last_sync"));
						lastSyncSet = true;
					}

					Event e = eventFromCursor(cal, evrs);
					eventById.put(e.getObmId(), e);
					changedEvent.add(e);
				}
			} catch (SQLException e) {
				logger.error("error getting events", e);
			} finally {
				obmHelper.cleanup(con, evps, evrs);
			}
		}

		if (ret.getLastSync() == null) {
			if (lastSync != null) {
				Connection conDate = null;
				try {
					conDate = obmHelper.getConnection();
					Date newLastSync = obmHelper.selectNow(conDate);
					if (newLastSync != null) {
						ret.setLastSync(newLastSync);
					} else {
						ret.setLastSync(lastSync);
					}
				} catch (SQLException e) {
					logger.error("error updating lastsync field", e);
				} finally {
					obmHelper.cleanup(conDate, null, null);
				}

			} else {
				Calendar ls = Calendar.getInstance();
				ls.set(Calendar.YEAR, 1970);
				ret.setLastSync(ls.getTime());
			}
		}

		Connection conComp = null;
		Date touchDateForFakeExDates;
		try {
			conComp = obmHelper.getConnection();
			touchDateForFakeExDates = obmHelper.selectNow(conComp);
			if (!changedEvent.isEmpty()) {
				IntegerIndexedSQLCollectionHelper changedIds = new IntegerIndexedSQLCollectionHelper(changedEvent);
				loadAttendeesAndAlerts(conComp, token, eventById, changedIds, calendarUser
						.getDomain().getName());
				loadExceptions(conComp, cal, eventById, changedIds);
				loadEventExceptions(conComp, token, eventById, changedIds);
			}

			touchParentOfDeclinedRecurrentEvents(
					parentOfDeclinedRecurrentEvent, changedEvent,
					touchDateForFakeExDates);

			if (!changedEvent.isEmpty()) {
				replaceDeclinedEventExceptionByException(token, changedEvent);
			}
		} catch (SQLException e) {
			logger.error("error loading attendees, alerts, exceptions, eventException", e);
		} finally {
			obmHelper.cleanup(conComp, null, null);
		}
		ret.setUpdated(changedEvent.toArray(new Event[0]));
		ret.setDeletions(findDeletedEvents(calendarUser, lastSync, typeFilter,
				declined));
		
		return ret;
	}

	private void touchParentOfDeclinedRecurrentEvents(
			Set<Event> parentOfDeclinedRecurrentEvent,
			List<Event> changedEvents, Date touchDateForFakeExDates) {
		for (Event parentEvent : parentOfDeclinedRecurrentEvent) {
			int i = changedEvents.indexOf(parentEvent);
			boolean isPresent = i > -1;
			Event event;
			if (!isPresent) {
				changedEvents.add(parentEvent);
				event = parentEvent;
			} else {
				event = changedEvents.get(i);
			}
			// Do a 'touch' on the event since we added a "fake"
			// exdate (to force the client to remove declined
			// exceptions)
			event.setTimeUpdate(touchDateForFakeExDates);
		}
	}

	private void replaceDeclinedEventExceptionByException(AccessToken token,
			List<Event> changedEvent) {
		for(Event event: changedEvent) {
			if(event.isRecurrent()) {
				event.getRecurrence().replaceDeclinedEventExceptionByException(token.getUserEmail());
			}
		}
	}

	private Set<CalendarInfo> listCalendarRights(ObmUser user, Collection<String> calendarEmails)
			throws FindException {
		Set<CalendarInfo> rights = new HashSet<CalendarInfo>();

		StringSQLCollectionHelper collectionHelper = new StringSQLCollectionHelper(calendarEmails);

		String calendarEmailsPlaceHolders = collectionHelper.asPlaceHolders();

		String directRightsQuery =
		"SELECT u.userobm_login, u.userobm_firstname, u.userobm_lastname, u.userobm_email, er.entityright_read, er.entityright_write "
	    + "FROM UserObm u "
	    + "JOIN CalendarEntity ce ON u.userobm_id=ce.calendarentity_calendar_id "
	    + "LEFT JOIN EntityRight er ON ce.calendarentity_entity_id=er.entityright_entity_id "
	    + "LEFT JOIN UserEntity ue ON er.entityright_consumer_id=ue.userentity_entity_id " 	    
		+ "WHERE (ue.userentity_user_id=? OR ue.userentity_user_id IS NULL) "
		+ "AND u.userobm_email IN (" + calendarEmailsPlaceHolders + ") "
		+ "AND u.userobm_archive != 1 "
		+ "AND u.userobm_domain_id=?";

		String publicCalsQuery =
		// public cals
		"SELECT u.userobm_login, u.userobm_firstname, u.userobm_lastname, u.userobm_email, er.entityright_read, er.entityright_write "
		+ "FROM UserObm u "
		+ "JOIN CalendarEntity ce ON u.userobm_id=ce.calendarentity_calendar_id "
		+ "JOIN EntityRight er ON ce.calendarentity_entity_id=er.entityright_entity_id "
		+ "WHERE er.entityright_consumer_id IS NULL "
		+ "AND u.userobm_domain_id=? "
		+ "AND u.userobm_email in (" + calendarEmailsPlaceHolders + ") "
		+ "AND u.userobm_archive != 1";

		String query = directRightsQuery + " UNION " + publicCalsQuery;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			int parameterCount = 1;

			ps.setInt(parameterCount++, user.getUid());
			parameterCount = collectionHelper.insertValues(ps, parameterCount);
			ps.setInt(parameterCount++, user.getDomain().getId());

			ps.setInt(parameterCount++, user.getDomain().getId());
			parameterCount = collectionHelper.insertValues(ps, parameterCount);

			rs = ps.executeQuery();
			while (rs.next()) {
				processRightsRow(rs, rights, user);
			}
		} catch (SQLException e) {
			logger.error("Error finding user rights", e);
			throw new FindException(e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}
		return rights;
	}

	private Set<CalendarInfo> listUserRights(ObmUser user) throws FindException {
		String query =
		// direct rights
		"select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join UserEntity u1 on entityright_consumer_id=u1.userentity_entity_id "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "where u1.userentity_user_id=? and (entityright_read=1 or entityright_write=1) and userobm_email is not null "
				+ "and userobm_email != '' and userobm_archive != 1"
				+ " union "
				// public cals
				+ "select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "where entityright_consumer_id is null and (entityright_read=1 or entityright_write=1) and userobm_email is not null and userobm_email != '' "
				+ " and userobm_domain_id="
				+ user.getDomain().getId()
				+ " and userobm_archive != 1";

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Set<CalendarInfo> calendarInfos = new HashSet<CalendarInfo>();

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, user.getUid());

			rs = ps.executeQuery();
			while (rs.next()) {
				processRightsRow(rs, calendarInfos, user);
			}
		} catch (SQLException e) {
			logger.error("Error finding user rights", e);
			throw new FindException(e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}
		return calendarInfos;
	}

	private Set<CalendarInfo> listGroupRights(ObmUser user) throws FindException {
		String query = "select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join GroupEntity u1 on entityright_consumer_id=u1.groupentity_entity_id "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = u1.groupentity_group_id "
				+ "where of_usergroup_user_id=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1";

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Set<CalendarInfo> calendarInfos = new HashSet<CalendarInfo>();

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, user.getUid());

			rs = ps.executeQuery();
			while (rs.next()) {
				processRightsRow(rs, calendarInfos, user);
			}
		} catch (SQLException e) {
			logger.error("Error finding user rights", e);
			throw new FindException(e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}

		return calendarInfos;
	}

	@Override
	public Collection<CalendarInfo> listCalendars(ObmUser user) throws FindException {
		Set<CalendarInfo> calendarUserRights = listUserRights(user);
		Set<CalendarInfo> calendarGroupRights = listGroupRights(user);

		Set<CalendarInfo> calendarRights = new HashSet<CalendarInfo>();
		calendarRights.addAll(calendarUserRights);
		calendarRights.addAll(calendarGroupRights);
		return calendarRights;
	}

	@Override
	public Collection<CalendarInfo> getCalendarMetadata(ObmUser user, Collection<String> calendarEmails)
			throws FindException {
		Set<CalendarInfo> rights = this.listCalendarRights(user, calendarEmails);
		return rights;
	}

	private void processRightsRow(ResultSet rs, Set<CalendarInfo> dbResults, ObmUser user)
			throws SQLException {
		CalendarInfo newInfo = makeCalendarInfo(rs, user.getDomain().getName());
		// FIXME this is an ugly hack to give the user the maximum read/write rights
		// Should be fixed by changing the SQL query
		boolean wasAdded = dbResults.add(newInfo);
		if (!wasAdded) {
			for (CalendarInfo oldInfo : dbResults) {
				if (oldInfo.equals(newInfo)) {
					oldInfo.setRead(oldInfo.isRead() || newInfo.isRead());
					oldInfo.setWrite(oldInfo.isWrite() || newInfo.isWrite());
					break;
				}
			}
		}
	}

	private CalendarInfo makeCalendarInfo(ResultSet rs, String domainName) throws SQLException {
		String email = constructEmailFromList(rs.getString(4), domainName);

		CalendarInfo info = new CalendarInfo();
		info.setUid(rs.getString(1));
		info.setFirstname(rs.getString(2));
		info.setLastname(rs.getString(3));
		info.setMail(email);
		info.setRead(rs.getBoolean(5));
		info.setWrite(rs.getBoolean(6));
		if (info.isWrite()) {
			info.setRead(true);
		}
		return info;
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
	
	private void loadAttendeesAndAlerts(Connection con, AccessToken token, Map<EventObmId, Event> eventById,
			AbstractSQLCollectionHelper<?> eventIds, String domainName) throws SQLException {
		if (eventById.isEmpty()) {
			return;
		}
		String attUserAlerts = "SELECT "
				+ ATT_AND_ALERT_FIELDS
				+ " FROM EventLink att "
				+ "LEFT JOIN EventAlert ON eventlink_event_id=eventalert_event_id "
				+ "INNER JOIN UserEntity ON att.eventlink_entity_id=userentity_entity_id "
				+ "INNER JOIN UserObm ON userobm_id=userentity_user_id "
				+ "WHERE eventlink_event_id IN (" + eventIds.asPlaceHolders() + ")";

		String attContactAlerts = "SELECT "
				+ CONTACT_AND_ALERT_FIELDS
				+ " FROM EventLink att "
				+ "INNER JOIN ContactEntity ON att.eventlink_entity_id=contactentity_entity_id "
				+ "INNER JOIN Contact ON contact_id=contactentity_contact_id "
				+ "INNER JOIN Email ON email_entity_id=contactentity_entity_id "
				+ "WHERE eventlink_event_id IN ("
				+ eventIds.asPlaceHolders()
				+ ") AND email_label='INTERNET;X-OBM-Ref1' ";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(attUserAlerts);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			Multimap<EventObmId, AttendeeAlert> attsUsersByEvent = getUserAttendeesByEventIdFromCursor(rs, domainName);
			appendAttendeeToEvent(eventById, attsUsersByEvent);
			appendEventToAlert(token, eventById, attsUsersByEvent);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		try {
			//contact			
			ps = con.prepareStatement(attContactAlerts);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			Multimap<EventObmId, AttendeeAlert> attsContactsByEvent = getContactAttendeesByEventIdFromCursor(rs, domainName);
			appendAttendeeToEvent(eventById, attsContactsByEvent);
			
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		defineEventsInternalStatus(eventById.values());
	}
	
	private void defineEventsInternalStatus(Collection<Event> events) {
		for (Event evt: events) {
			evt.setInternalEvent(EventUtils.isInternalEvent(evt));
		}
	}

	
	private void appendEventToAlert(AccessToken token, Map<EventObmId, Event> eventById, Multimap<EventObmId, AttendeeAlert> userAttendeesByEventId){
		if (token != null) {
			for(Event event : eventById.values()){
				int alert = 0;
				for(AttendeeAlert att : userAttendeesByEventId.get(event.getObmId())){
					if (token.getObmId() == att.getEntityId()) {
						if (att.getAlert() >= 0) {
							alert = att.getAlert();
						}
					}
				}
				event.setAlert(alert);
			}
		}
	}
	
	private Multimap<EventObmId, AttendeeAlert> getUserAttendeesByEventIdFromCursor(ResultSet rs, String domainName) throws SQLException{
		return getAttendeesByEventIdFromCursor(rs, domainName, true);
	}
	
	private Multimap<EventObmId, AttendeeAlert> getContactAttendeesByEventIdFromCursor(ResultSet rs, String domainName) throws SQLException{
		return getAttendeesByEventIdFromCursor(rs, domainName, false);
	}
	
	private Multimap<EventObmId, AttendeeAlert> getAttendeesByEventIdFromCursor(ResultSet rs, String domainName, boolean isObmUser) throws SQLException{
		Multimap<EventObmId, AttendeeAlert> attsByEvent = ArrayListMultimap.create();
		while (rs.next()) {
			EventObmId eventId = new EventObmId(rs.getInt(1));
			AttendeeAlert att = new AttendeeAlert();
			att.setObmUser(isObmUser);
			att.setDisplayName(getAttendeeDisplayName(rs));
			att.setEmail(getUserObmEmail(rs, domainName));
			att.setState(getAttendeeState(rs));
			att.setRequired(getAttendeeRequired(rs));
			att.setPercent(getAttendeePercent(rs));
			att.setOrganizer(getAttendeeOrganizer(rs));
			att.setEntityId(rs.getInt("userentity_user_id"));
			att.setAlert(rs.getInt("eventalert_duration"));
			attsByEvent.put(eventId, att);
		}
		return attsByEvent;
	}

	private void appendAttendeeToEvent(Map<EventObmId, Event> eventById, Multimap<EventObmId, AttendeeAlert> attendeesByEventId){
		for(Event event : eventById.values()){
			Collection<AttendeeAlert> atts = attendeesByEventId.get(event.getObmId());
			event.addAttendees(atts);
		}
	}

	private void loadEventExceptions(Connection con, AccessToken token,
			Map<EventObmId, Event> eventById, AbstractSQLCollectionHelper<?> eventIds) throws SQLException {
		String query = "SELECT "
				+ EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ ", eventexception_parent_id as parent_id FROM Event e "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "INNER JOIN Domain ON event_domain_id=domain_id "
				+ "INNER JOIN EventEntity ON evententity_event_id=event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "INNER JOIN EventException ON e.event_id = eventexception_child_id "
				+ "WHERE eventexception_parent_id IN (" + eventIds.asPlaceHolders() + ") "
				+ "OR eventexception_child_id IN (" + eventIds.asPlaceHolders() + ") ";

		PreparedStatement ps = null;
		ResultSet rs = null;

		String domainName = null;

		Map<EventObmId, Event> evenExcepttById = new HashMap<EventObmId, Event>();
		List<Event> changedEvent = new LinkedList<Event>();
		Calendar cal = getGMTCalendar();
		try {

			ps = con.prepareStatement(query);
			int nextId = eventIds.insertValues(ps, 1);
			eventIds.insertValues(ps, nextId);
			rs = ps.executeQuery();

			while (rs.next()) {
				Event eventExcept = eventFromCursor(cal, rs);
				cal.setTimeInMillis(rs.getTimestamp("recurrence_id").getTime());
				eventExcept.setRecurrenceId(cal.getTime());
				domainName = rs.getString("domain_name");
				EventObmId eventId = new EventObmId(rs.getInt("parent_id"));
				Event event = eventById.get(eventId);
				if (event != null) {
					event.getRecurrence().addEventException(eventExcept);
					changedEvent.add(eventExcept);
					evenExcepttById.put(eventExcept.getObmId(),
							eventExcept);
				}
			}
			IntegerIndexedSQLCollectionHelper changedIds = new IntegerIndexedSQLCollectionHelper(changedEvent);
			loadAttendeesAndAlerts(con, token, evenExcepttById, changedIds,
					domainName);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private void loadExceptions(Connection con, Calendar cal, Map<EventObmId, Event> eventById,
			AbstractSQLCollectionHelper<?> eventIds) throws SQLException {
		if (eventById.isEmpty()) {
			return;
		}
		String exceps = "SELECT "
				+ EXCEPS_FIELDS
				+ " FROM Event e LEFT JOIN EventException on event_id=eventexception_parent_id "
				+ "WHERE event_id IN (" + eventIds.asPlaceHolders()
				+ ") AND eventexception_child_id IS NULL";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(exceps);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				EventObmId eventId = new EventObmId(rs.getInt(1));
				Event e = eventById.get(eventId);
				EventRecurrence er = e.getRecurrence();
				Timestamp t = rs.getTimestamp(2);
				if (t != null) {
					cal.setTimeInMillis(t.getTime());
					er.addException(cal.getTime());
				}
			}
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private EventObmId markUpdated(Connection con, EventObmId eventObmId) throws SQLException {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement("UPDATE Event SET event_timeupdate=? WHERE event_id=?");
			st.setTimestamp(1, new Timestamp(obmHelper.selectNow(con).getTime()));
			st.setInt(2, eventObmId.getObmId());
			st.execute();
		} finally {
			obmHelper.cleanup(null, st, null);
		}
		return eventObmId;
	}

	@Override
	public Event modifyEventForcingSequence(AccessToken at, String calendar, Event ev,
			boolean updateAttendees, int sequence, Boolean useObmUser) throws SQLException, FindException, ServerFault, EventNotFoundException {

		logger.info("should modify event with title " + ev.getTitle()
				+ " date: " + ev.getDate() + " id: " + ev.getObmId());
		
		Connection con = null;

		try {
			con = obmHelper.getConnection();
			modifyEventForcingSequence(con, at, calendar, ev, updateAttendees, sequence, useObmUser);
		} finally {
			obmHelper.cleanup(con, null, null);
		}

		return findEventById(at, ev.getObmId());
	}
	
	@Override
	public Event modifyEvent(AccessToken at, String calendar, Event ev,
			boolean updateAttendees, Boolean useObmUser) throws SQLException, FindException, EventNotFoundException, ServerFault {

		logger.info("should modify event with title " + ev.getTitle()
				+ " date: " + ev.getDate() + " id: " + ev.getObmId());
		
		Connection con = null;

		try {
			con = obmHelper.getConnection();
			modifyEvent(con, at, calendar, ev, updateAttendees, useObmUser);
		} finally {
			obmHelper.cleanup(con, null, null);
		}

		return findEventById(at, ev.getObmId());
	}

	@Override
	public void modifyEvent(Connection con, AccessToken editor, String calendar, Event ev, boolean updateAttendees, 
			Boolean useObmUser) throws SQLException, FindException, ServerFault, EventNotFoundException {
		
		modifyEventForcingSequence(con, editor, calendar, ev, updateAttendees, ev.getSequence(), useObmUser);
	}
	
	@Override
	public void modifyEventForcingSequence(Connection con, AccessToken editor, String calendar, Event ev, 
			boolean updateAttendees, int sequence, Boolean useObmUser)
			throws SQLException, FindException, ServerFault, EventNotFoundException {
		
		Event old = findEventById(editor, ev.getObmId());
		List<Attendee> attendeetoRemove = Lists.newArrayList(old.getAttendees());
		attendeetoRemove.removeAll(ev.getAttendees());

		old.getRecurrence().getEventExceptions()
				.removeAll(ev.getRecurrence().getEventExceptions());

		PreparedStatement ps = null;

		try {

			ps = createEventUpdateStatement(con, editor, ev, sequence);
			ps.executeUpdate();
			ps.close();
			ps = null;
			if (updateAttendees) {
				removeAttendees(editor, con, attendeetoRemove, ev);
			}

			if (updateAttendees) {
				updateAttendees(editor, con, calendar, ev, useObmUser);
				markUpdated(con, ev.getObmId());
			}
			updateAlerts(editor, con, ev);

			removeAllException(con, ev);

			insertExceptions(editor, ev, con, ev.getObmId());
			if (ev.isRecurrent()) {
				insertEventExceptions(editor, calendar, ev.getRecurrence()
						.getEventExceptions(), con, ev.getObmId(), useObmUser);
			}
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
		
		indexEvent(editor, ev);
	}

	private PreparedStatement createEventUpdateStatement(Connection con,
			AccessToken at, Event ev, int sequence) throws SQLException {
		PreparedStatement ps;
		String upQ = "UPDATE Event SET event_userupdate=?, "
				+ "event_type=?, event_timezone=?, event_opacity=?, "
				+ "event_title=?, event_location=?, "
				+ "event_category1_id=?, event_priority=?, "
				+ "event_privacy=?, event_date=?, event_duration=?, "
				+ "event_allday=?, event_repeatkind=?, "
				+ "event_repeatfrequence=?, event_repeatdays=?, "
				+ "event_endrepeat=?, event_completed=?, "
				+ "event_url=?, event_description=?, event_origin=?, "
				+ "event_sequence=? "
				+ "WHERE event_id=?";

		ps = con.prepareStatement(upQ);
		ps.setInt(1, at.getObmId());
		ps.setObject(2, ev.getType().getJdbcObject(obmHelper.getType()));
		ps.setString(3, ev.getTimezoneName() != null ? ev.getTimezoneName()
				: "Europe/Paris");
		ps.setObject(4, ev.getOpacity().getJdbcObject(obmHelper.getType()));
		ps.setString(5, ev.getTitle());
		ps.setString(6, ev.getLocation());
		Integer cat = catIdFromString(con, ev.getCategory(), at.getDomain().getId());
		if (cat != null) {
			ps.setInt(7, cat);
		} else {
			ps.setNull(7, Types.INTEGER);
		}
		ps.setInt(8, ev.getPriority());
		// do not allow making a private event become public from sync
		// ps.setInt(9, old.getPrivacy() != 1 ? ev.getPrivacy() : old
		// .getPrivacy());
		ps.setInt(9, ev.getPrivacy());
		ps.setTimestamp(10, new Timestamp(ev.getDate().getTime()));
		ps.setInt(11, ev.getDuration());
		ps.setBoolean(12, ev.isAllday());
		EventRecurrence er = ev.getRecurrence();
		ps.setString(13, er.getKind().toString());
		ps.setInt(14, er.getFrequence());
		ps.setString(15, er.getDays());
		if (er.getEnd() != null) {
			ps.setTimestamp(16, new Timestamp(er.getEnd().getTime()));
		} else {
			ps.setNull(16, Types.TIMESTAMP);
		}
		ps.setNull(17, Types.TIMESTAMP);
		ps.setNull(18, Types.VARCHAR);
		ps.setString(19, ev.getDescription());
		ps.setString(20, at.getOrigin());
		ps.setInt(21, sequence);
		ps.setInt(22, ev.getObmId().getObmId());
		return ps;
	}

	private void removeEventExceptions(Connection con, EventObmId idEventParent)
			throws SQLException {
		logger.info("event update will remove all eventException for event "
				+ idEventParent + ".");

		List<EventObmId> toDel = new LinkedList<EventObmId>();
		PreparedStatement idsFetch = null;
		ResultSet rs = null;
		try {
			idsFetch = con
					.prepareStatement("SELECT eventexception_child_id FROM EventException WHERE eventexception_parent_id=?");
			idsFetch.setInt(1, idEventParent.getObmId());
			rs = idsFetch.executeQuery();
			while (rs.next()) {
				toDel.add(new EventObmId(rs.getInt(1)));
			}
		} finally {
			obmHelper.cleanup(null, idsFetch, rs);
		}

		IntegerIndexedSQLCollectionHelper toDeleteIds = new IntegerIndexedSQLCollectionHelper(toDel);

		PreparedStatement dev = null;
		try {
			dev = con.prepareStatement("DELETE FROM Event WHERE event_id IN ("
					+ toDeleteIds.asPlaceHolders() + ") ");
			toDeleteIds.insertValues(dev, 1);
			dev.executeUpdate();
		} finally {
			obmHelper.cleanup(null, dev, null);
		}

		String q = "DELETE FROM EventException "
				+ "WHERE eventexception_parent_id=? AND eventexception_child_id IS NOT NULL";
		// FIXME need to delete also event in mysql ?
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(q);
			ps.setInt(1, idEventParent.getObmId());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void removeAttendees(AccessToken editor, Connection con, List<Attendee> toRemove,
			Event ev) throws SQLException {
		logger.info("event update will remove " + toRemove.size()
				+ " attendees.");
		String q = "delete from EventLink where eventlink_event_id=? and eventlink_entity_id=? ";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(q);
			for (Attendee at : toRemove) {
				Integer id = userDao.userEntityIdFromEmail(con,
						at.getEmail(), editor.getDomain().getId());
				if (id == null) {
					id = userDao
							.contactEntityFromEmailQuery(con, at.getEmail());
				}
				ps.setInt(1, ev.getObmId().getObmId());
				ps.setInt(2, id);
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void removeAllException(Connection con, Event eventParent)
			throws SQLException {
		removeEventExceptions(con, eventParent.getObmId());
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("delete from EventException where eventexception_parent_id=?");
			ps.setInt(1, eventParent.getObmId().getObmId());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	@Override
	public Event removeEvent(AccessToken token, Event event, EventType eventType, int sequence) throws SQLException {
		Event deletedEvent = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			deletedEvent = removeEvent(con, token, eventType, event);
		} finally {
			obmHelper.cleanup(con, null, null);
		}

		deletedEvent.setSequence(sequence);
		return deletedEvent;
	}
	
	@Override
	public Event removeEventById(AccessToken token, EventObmId eventId, EventType eventType, int sequence) throws SQLException, EventNotFoundException, ServerFault {
		Event event = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			event = removeEventById(con, token, eventId, eventType, sequence);
		} finally {
			obmHelper.cleanup(con, null, null);
		}

		return event;
	}

	// FIXME: event type should come from database
	@Override
	public Event removeEventById(Connection con, AccessToken token, EventObmId uid, EventType et, int sequence) 
			throws EventNotFoundException, ServerFault {
		
		Event ev = findEventById(token, uid);
		Event deleted = removeEvent(con, token, et, ev);
		deleted.setSequence(sequence);
		return deleted;
	}

	private Event removeEvent(Connection con, AccessToken token, EventType et, Event ev) {
		PreparedStatement dev = null;
		try {
			dev = con
					.prepareStatement("INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, "
							+ "deletedevent_origin, deletedevent_type, deletedevent_timestamp, deletedevent_event_ext_id) "
							+ "VALUES (?, ?, ?, ?, now(), ?)");
			EventObmId databaseId = ev.getObmId();
			for (Attendee at : ev.getAttendees()) {
				Integer userId = userDao.userIdFromEmail(con,
						at.getEmail(), token.getDomain().getId());
				if (userId != null) {
					dev.setInt(1, databaseId.getObmId());
					dev.setInt(2, userId);
					dev.setString(3, token.getOrigin());
					dev.setObject(4, et.getJdbcObject(obmHelper.getType()));
					dev.setString(5, ev.getExtId().getExtId());
					dev.addBatch();
				}
			}
			dev.executeBatch();
			dev.close();

			removeEventExceptions(con, databaseId);

			dev = con.prepareStatement("DELETE FROM Event WHERE event_id=?");
			dev.setInt(1, databaseId.getObmId());
			dev.executeUpdate();

		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, dev, null);
		}

		removeEventFromSolr(token, ev);
		
		return ev;
	}

	private void removeEventFromSolr(AccessToken token, Event ev) {
		try {
			solrHelperFactory.createClient(token).delete(ev);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	private void insertAttendees(AccessToken editor, String calendar, Event ev, Connection con,
			List<Attendee> attendees, boolean useObmUser) throws SQLException, ServerFault {
		String attQ = "INSERT INTO EventLink (" + ATT_INSERT_FIELDS
				+ ") VALUES (" + "?, " + // event_id
				"?, " + // entity_id
				"?, " + // state
				"?, " + // required
				"?," + // percent
				"?," + // user_create
				"?" + // is_organizer
				")";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(attQ);
			Integer userEntityCalender = userDao.userEntityIdFromEmail(con, calendar, editor.getDomain().getId());
			
			final Set<Attendee> listAttendee = removeDuplicateAttendee(attendees);			
			for (final Attendee at : listAttendee) {
				Integer userEntity = getUserEntityOrContactEntity(editor, con, userEntityCalender, at.getEmail(), useObmUser);
				
				if (userEntity == null) {
					logger.info("Attendee " + at.getEmail()
							+ " not found in OBM, will create a contact");
					Contact c = new Contact();
					c.setLastname(at.getDisplayName());
					c.setFirstname("");
					c.addEmail("INTERNET;X-OBM-Ref1", new Email(at.getEmail()));
					c.setCollected(true);
					c = contactDao.createContact(editor, con, c);
					userEntity = c.getEntityId();
				}
				ps.setInt(1, ev.getObmId().getObmId());
				ps.setInt(2, userEntity);
				ps.setObject(3, getJdbcObjectParticipationState(at));
				ps.setObject(4, getJdbcObjectParticipationRole(at));
				ps.setInt(5, at.getPercent());
				ps.setInt(6, editor.getObmId());
				ps.setBoolean(7, at.isOrganizer());
				ps.addBatch();
				logger.info(LogUtils.prefix(editor) + "Adding " + at.getEmail() + ( at.isOrganizer() ? " as organizer" : " as attendee"));
			}
			
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private Object getJdbcObjectParticipationState(final Attendee at) throws SQLException {
		final ParticipationState pStat = RFC2445.getParticipationStateOrDefault(at.getState());
		return pStat.getJdbcObject(obmHelper.getType());
	}

	private Object getJdbcObjectParticipationRole(final Attendee at) throws SQLException {
		final ParticipationRole pRole = RFC2445.getParticipationRoleOrDefault(at.getRequired());
		return pRole.getJdbcObject(obmHelper.getType());
	}
	
	private Set<Attendee> removeDuplicateAttendee(List<Attendee> attendees) {
		return ImmutableSet.copyOf(attendees);
	}

	private Integer getUserEntityOrContactEntity(AccessToken editor, Connection con, Integer userEntityCalendar, String email, boolean useObmUser) throws SQLException {
		Integer userEntity  = userDao.userEntityIdFromEmail(con,
				email, editor.getDomain().getId());
		if(!useObmUser && !userEntityCalendar.equals(userEntity)){
			userEntity = null;
			logger.info("user with email " + email
					+ " not found. Checking contacts.");
		}
		if (userEntity == null) {
			userEntity = userDao.contactEntityFromEmailQuery(con,
					email);
		}
		return userEntity;
	}

	private void updateAttendees(AccessToken updater, Connection con, String calendar, Event ev,
			Boolean useObmUser) throws SQLException, ServerFault {
		String q = "update EventLink set eventlink_state=?, eventlink_required=?, eventlink_userupdate=?, eventlink_percent=? "
				+ "where eventlink_event_id=? AND "
				+ "eventlink_entity_id IN "
				+ "(select userentity_entity_id from UserEntity where userentity_entity_id=?  "
				+ "UNION SELECT contactentity_entity_id from ContactEntity where contactentity_entity_id=?)";
		PreparedStatement ps = null;
		int[] updatedAttendees;
		List<Attendee> mightInsert = new LinkedList<Attendee>();
		List<Attendee> toInsert = new LinkedList<Attendee>();

		try {
			ps = con.prepareStatement(q);
			Integer userEntityCalendar = userDao.userEntityIdFromEmail(con, calendar, updater.getDomain().getId());
			for (Attendee at : ev.getAttendees()) {
				Integer userEntity = getUserEntityOrContactEntity(updater, con, userEntityCalendar, at.getEmail(), useObmUser);
				if (userEntity == null) {
					logger.info("skipping attendee update for email "
							+ at.getEmail() + ". Will add as contact");
					toInsert.add(at);
					continue;
				}

				int idx = 1;
				ps.setObject(idx++,
						at.getState().getJdbcObject(obmHelper.getType()));
				ps.setObject(idx++,
						at.getRequired().getJdbcObject(obmHelper.getType()));
				ps.setInt(idx++, updater.getObmId());
				ps.setInt(idx++, at.getPercent());
				ps.setInt(idx++, ev.getObmId().getObmId());
				ps.setInt(idx++, userEntity);
				ps.setInt(idx++, userEntity);
				ps.addBatch();
				mightInsert.add(at);
			}
			updatedAttendees = ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

		for (int i = 0; i < updatedAttendees.length; i++) {
			if (updatedAttendees[i] == 0) {
				Attendee at = mightInsert.get(i);
				toInsert.add(at);
			}
		}
		
		logger.info("event modification needs to add " + toInsert.size() + " attendees.");
		insertAttendees(updater, calendar, ev, con, toInsert, useObmUser);

		Statement st = null;
		try {
			st = con.createStatement();
			if (ev.getAlert() == null || ev.getAlert() < 0) {
				st.executeUpdate("delete from EventAlert where eventalert_user_id="
						+ updater.getObmId()
						+ " AND eventalert_event_id="
						+ ev.getObmId().getObmId());
			} else {
				int upd = st
						.executeUpdate("update EventAlert set eventalert_duration="
								+ ev.getAlert()
								+ ", eventalert_userupdate="
								+ updater.getObmId()
								+ " where eventalert_user_id="
								+ updater.getObmId()
								+ " AND eventalert_event_id="
								+ ev.getObmId().getObmId());
				if (upd <= 0) {
					st.executeUpdate("insert into EventAlert (eventalert_duration, eventalert_event_id, eventalert_usercreate, eventalert_user_id)"
							+ " values ("
							+ ev.getAlert()
							+ ","
							+ ev.getObmId().getObmId()
							+ ","
							+ updater.getObmId()
							+ "," + updater.getObmId() + " )");
				}
			}
		} finally {
			obmHelper.cleanup(null, st, null);
		}

	}

	private void updateAlerts(AccessToken updater, Connection con, Event ev)
			throws SQLException {

		Statement st = null;
		try {
			st = con.createStatement();
			if (ev.getAlert() == null || ev.getAlert() < 0) {
				st.executeUpdate("delete from EventAlert where eventalert_user_id="
						+ updater.getObmId()
						+ " AND eventalert_event_id="
						+ ev.getObmId().getObmId());
			} else {
				int upd = st
						.executeUpdate("update EventAlert set eventalert_duration="
								+ ev.getAlert()
								+ ", eventalert_userupdate="
								+ updater.getObmId()
								+ " where eventalert_user_id="
								+ updater.getObmId()
								+ " AND eventalert_event_id="
								+ ev.getObmId().getObmId());
				if (upd <= 0) {
					st.executeUpdate("insert into EventAlert (eventalert_duration, eventalert_event_id, eventalert_usercreate, eventalert_user_id)"
							+ " values ("
							+ ev.getAlert()
							+ ","
							+ ev.getObmId().getObmId()
							+ ","
							+ updater.getObmId()
							+ "," + updater.getObmId() + " )");
				}
			}
		} finally {
			obmHelper.cleanup(null, st, null);
		}

	}

	@Override
	public Event findEventByExtId(AccessToken token, ObmUser calendar,
			EventExtId extId) {
		String ev = "SELECT "
				+ EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ " FROM Event e "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
				+ "INNER JOIN Domain ON event_domain_id=domain_id "
				+ "INNER JOIN EventEntity ON evententity_event_id=event_id "
				+ "INNER JOIN EventLink link ON link.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ON userentity_entity_id=eventlink_entity_id "
				+ "INNER JOIN UserObm u ON u.userobm_id=userentity_user_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "WHERE e.event_ext_id=? " 
				+ "AND u.userobm_login=?";

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;

		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(ev);
			evps.setString(1, extId.getExtId());
			evps.setString(2, calendar.getLogin());
			evrs = evps.executeQuery();
			if (evrs.next()) {
				Calendar cal = getGMTCalendar();
				Event ret = eventFromCursor(cal, evrs);
				String domainName = evrs.getString("domain_name");
				Map<EventObmId, Event> eventById = ImmutableMap.of(ret.getObmId(), ret);
				IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(ImmutableList.of(ret));
				loadAttendeesAndAlerts(con, token, eventById, eventIds, domainName);
				loadExceptions(con, cal, eventById, eventIds);
				loadEventExceptions(con, token, eventById, eventIds);
				return ret;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}
			
		return null;
	}

	public Event findEventByExtIdAndRecurrenceId(AccessToken token, ObmUser calendar, EventExtId extId, RecurrenceId recurrenceId) throws ParseException {
		
		String ev = "SELECT "
				+ EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ " FROM Event e "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "LEFT JOIN EventException eexp ON e.event_id = eventexception_child_id "
				+ "INNER JOIN Domain ON event_domain_id=domain_id "
				+ "INNER JOIN EventEntity ON evententity_event_id=event_id "
				+ "INNER JOIN EventLink link ON link.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ON userentity_entity_id=eventlink_entity_id "
				+ "INNER JOIN UserObm u ON u.userobm_id=userentity_user_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "WHERE e.event_ext_id=? " 
				+ "AND eexp.eventexception_date=?"
				+ "AND u.userobm_login=?";
		
		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;

		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(ev);
			evps.setString(1, extId.getExtId());
			
			Date recId = new DateTime(recurrenceId.getRecurrenceId());
			evps.setTimestamp(2, new Timestamp(recId.getTime()));
			evps.setString(3, calendar.getLogin());

			evrs = evps.executeQuery();
			if (evrs.next()) {
				Calendar cal = getGMTCalendar();
				Event ret = eventFromCursor(cal, evrs);
				String domainName = evrs.getString("domain_name");
				Map<EventObmId, Event> eventById = ImmutableMap.of(ret.getObmId(), ret);
				IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(ImmutableList.of(ret));
				loadAttendeesAndAlerts(con, token, eventById, eventIds, domainName);
				return ret;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			throw e; 
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}	
		return null;
	}
	
	@Override
	public List<Event> listEventsByIntervalDate(final AccessToken token, final ObmUser obmUser, 
			final Date startDate, final Date endDate, final EventType typeFilter) {
		
		final String sql = "SELECT " + EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ " FROM Event e "
				+ "INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id "
				+ "INNER JOIN EventEntity ON e.event_id=evententity_event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "INNER JOIN Domain ON e.event_domain_id=domain_id "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
				+ "WHERE e.event_type=? AND ue.userentity_user_id=? "
				+ "AND ((event_repeatkind != 'none' AND event_endrepeat <= ?) OR "
				+ "(event_repeatkind = 'none' AND event_date >= ? AND event_date <= ?) )";
		
		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;
		
		final List<Event> changedEvent = new LinkedList<Event>();
		final Map<EventObmId, Event> eventById = new HashMap<EventObmId, Event>();
		final List<Event> ret = new LinkedList<Event>();
		final Calendar cal = getGMTCalendar();
		
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(sql);
			int idx = 1;
			evps.setObject(idx++, typeFilter.getJdbcObject(obmHelper.getType()));
			evps.setObject(idx++, obmUser.getUid());
			evps.setTimestamp(idx++, new Timestamp(endDate.getTime()));
			evps.setTimestamp(idx++, new Timestamp(startDate.getTime()));
			evps.setTimestamp(idx++, new Timestamp(endDate.getTime()));
			evrs = evps.executeQuery();
			while (evrs.next()) {
				Event event = eventFromCursor(cal, evrs);
				Set<Date> extDate = getAllDateEventException(con, event.getObmId());
				Date recurDate = ical4jHelper.isInIntervalDate(event, startDate, endDate, extDate);
				if (recurDate != null) {
					eventById.put(event.getObmId(), event);
					changedEvent.add(event);
					ret.add(event);
				}
			}
			
			IntegerIndexedSQLCollectionHelper changedIds = new IntegerIndexedSQLCollectionHelper(changedEvent);
			loadAttendeesAndAlerts(con, token, eventById, changedIds, obmUser.getDomain().getName());
			loadExceptions(con, cal, eventById, changedIds);
			loadEventExceptions(con, token, eventById, changedIds);
			
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}

		return ret;
	}

	@Override
	public List<Event> findAllEvents(AccessToken token, ObmUser calendarUser,
			EventType typeFilter) {
		String ev = "SELECT "
				+ EVENT_SELECT_FIELDS
				+ ", eventexception_date as recurrence_id "
				+ " FROM Event e "
				+ "INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id "
				+ "INNER JOIN EventEntity ON e.event_id=evententity_event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "INNER JOIN Domain ON e.event_domain_id=domain_id "
				+ "LEFT JOIN EventCategory1 ON e.event_category1_id=eventcategory1_id "
				+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
				+ "WHERE ue.userentity_user_id=? and e.event_type=? ";

		StringBuilder sb = new StringBuilder(ev);

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;

		Map<EventObmId, Event> eventById = new HashMap<EventObmId, Event>();
		List<Event> ret = new LinkedList<Event>();
		Calendar cal = getGMTCalendar();
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(sb.toString());
			int idx = 1;
			evps.setObject(idx++, calendarUser.getUid());
			evps.setObject(idx++, typeFilter.getJdbcObject(obmHelper.getType()));
			evrs = evps.executeQuery();

			while (evrs.next()) {
				Event event = eventFromCursor(cal, evrs);
				eventById.put(event.getObmId(), event);
				ret.add(event);
			}
			
			IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(ret);
			loadAttendeesAndAlerts(con, token, eventById, eventIds, calendarUser.getDomain().getName());
			loadExceptions(con, cal, eventById, eventIds);
			loadEventExceptions(con, token, eventById, eventIds);
			
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}

		
		return ret;
	}

	@Override
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter) {
		String ev = "SELECT e.event_id, e.event_title, att.eventlink_state, e.event_date, e.event_repeatkind, e.event_repeatdays, e.event_repeatfrequence, e.event_endrepeat, al.eventalert_duration"
				+ " FROM Event e "
				+ "INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id "
				+ "LEFT JOIN EventAlert al ON eventlink_event_id=eventalert_event_id "
				+ "INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id "
				+ "INNER JOIN UserObm ON e.event_owner=userobm_id "
				+ "INNER JOIN Domain ON e.event_domain_id=domain_id "
				+ "WHERE e.event_type=? AND ue.userentity_user_id=? "
				+ "AND al.eventalert_duration != 0 "
				+ "AND ((event_repeatkind != 'none' AND (event_endrepeat IS NULL OR event_endrepeat >= ?)) OR "
				+ "(event_date >= ? AND event_date <= ?) )";

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;

		List<EventParticipationState> ret = new LinkedList<EventParticipationState>();
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(ev);
			int idx = 1;
			evps.setObject(idx++, typeFilter.getJdbcObject(obmHelper.getType()));
			evps.setObject(idx++, calendarUser.getUid());
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			evps.setTimestamp(idx++, new Timestamp(end.getTime()));

			evrs = evps.executeQuery();
			while (evrs.next()) {
				EventParticipationState psEvent = participationStateEventFromCursor(evrs);
				Calendar cal = getGMTCalendar();
				EventRecurrence er = eventRecurrenceFromCursor(cal, evrs);

				cal.setTimeInMillis(evrs.getTimestamp("event_date").getTime());
				Set<Date> extDate = getAllDateEventException(con,
						new EventObmId(evrs.getInt("event_id")));
				Date recurDate = ical4jHelper.isInIntervalDate(er,
						cal.getTime(), start, end, extDate);
				if (recurDate != null) {
					psEvent.setDate(recurDate);
					ret.add(psEvent);
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}
		return ret;
	}

	private Calendar getGMTCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	private EventParticipationState participationStateEventFromCursor(
			ResultSet evrs) throws SQLException {
		EventParticipationState e = new EventParticipationState();
		int id = evrs.getInt("event_id");
		e.setUid("" + id);
		e.setTitle(evrs.getString("event_title"));
		e.setState(getAttendeeState(evrs));
		e.setAlert(evrs.getInt("eventalert_duration"));
		return e;
	}

	@Override
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter) {

		String ev = "SELECT event_id, event_ext_id, event_timecreate, event_timeupdate, e.event_date, e.event_repeatkind, e.event_repeatdays, e.event_repeatfrequence, e.event_endrepeat, eventexception_date as recurrenceId"
				+ " FROM Event e "
				+ "INNER JOIN EventLink att ON att.eventlink_event_id=e.event_id "
				+ "INNER JOIN UserEntity ue ON att.eventlink_entity_id=ue.userentity_entity_id "
				+ "LEFT JOIN EventException ON e.event_id = eventexception_child_id "
				+ "WHERE ue.userentity_user_id=? and e.event_type=? "
				+ "AND eventlink_state != ? "
				+ "AND ((event_repeatkind != 'none' AND (event_endrepeat IS NULL OR event_endrepeat >= ?)) OR "
				+ "(event_date >= ? ";
		if (end != null) {
			ev += "  AND event_date <= ?";
		}
		ev += ") )";

		StringBuilder sb = new StringBuilder(ev);
		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;
		List<EventTimeUpdate> ret = new LinkedList<EventTimeUpdate>();
		Calendar cal = getGMTCalendar();
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(sb.toString());
			int idx = 1;
			evps.setObject(idx++, calendarUser.getUid());
			evps.setObject(idx++, typeFilter.getJdbcObject(obmHelper.getType()));
			evps.setObject(idx++, ParticipationState.DECLINED
					.getJdbcObject(obmHelper.getType()));
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			if (end != null) {
				evps.setTimestamp(idx++, new Timestamp(end.getTime()));
			}
			evrs = evps.executeQuery();

			while (evrs.next()) {
				EventTimeUpdate event = eventTimeUpdateFromCursor(cal, evrs);
				EventRecurrence er = eventRecurrenceFromCursor(cal, evrs);
				Set<Date> extDate = getAllDateEventException(con,
						new EventObmId(evrs.getInt("event_id")));
				Date recurDate = ical4jHelper.isInIntervalDate(er,
						event.getDate(), start, end, extDate);
				if (recurDate != null) {
					ret.add(event);
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}
		return ret;
	}

	@Override
	public Date findLastUpdate(AccessToken token, String calendar) {
		String ev = "SELECT MAX(event_timeupdate), MAX(event_timecreate) "
				+ " FROM Event e "
				+ "INNER JOIN UserObm ON e.event_owner=userobm_id "
				+ "WHERE userobm_login=?";

		PreparedStatement evps = null;
		ResultSet evrs = null;
		Connection con = null;

		Date lastUpdate = null;
		Date lastCreate = null;
		try {
			con = obmHelper.getConnection();
			evps = con.prepareStatement(ev);
			evps.setString(1, calendar);
			evrs = evps.executeQuery();
			if (evrs.next()) {
				lastUpdate = evrs.getTimestamp(1);
				lastCreate = evrs.getTimestamp(2);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, evps, evrs);
		}

		if (lastCreate == null && lastUpdate == null) {
			return new Date(0);
		}

		if (lastUpdate != null && lastUpdate.after(lastCreate)) {
			return lastUpdate;
		}
		return lastCreate;
	}

	private EventTimeUpdate eventTimeUpdateFromCursor(Calendar cal,
			ResultSet evrs) throws SQLException {
		EventTimeUpdate e = new EventTimeUpdate();
		e.setUid("" + evrs.getInt("event_id"));
		Date update = evrs.getTimestamp("event_timeupdate");
		if (update == null) {
			update = evrs.getTimestamp("event_timecreate");
		}
		e.setTimeUpdate(update);
		e.setExtId(evrs.getString("event_ext_id"));
		e.setRecurrenceId(evrs.getTimestamp("recurrenceId"));
		cal.setTimeInMillis(evrs.getTimestamp("event_date").getTime());
		e.setDate(cal.getTime());
		return e;
	}

	private EventRecurrence eventRecurrenceFromCursor(Calendar cal,
			ResultSet evrs) throws SQLException {
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.valueOf(evrs.getString("event_repeatkind")));
		er.setDays(evrs.getString("event_repeatdays"));
		er.setFrequence(evrs.getInt("event_repeatfrequence"));
		if (evrs.getTimestamp("event_endrepeat") != null) {
			cal.setTimeInMillis(evrs.getTimestamp("event_endrepeat").getTime());
			er.setEnd(cal.getTime());
		}
		return er;
	}

	@Override
	public Event removeEventByExtId(AccessToken token, ObmUser calendar,
			EventExtId extId, int sequence) throws SQLException {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			Event event = removeEventByExtId(con, calendar, token, extId);
			event.setSequence(sequence);
			return event;
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}

	private Event removeEventByExtId(Connection con, ObmUser calendar,
			AccessToken token, EventExtId extId) {
		Event ev = findEventByExtId(token, calendar, extId);
		if (ev == null) {
			return null;
		}
		PreparedStatement dev = null;
		try {
			dev = con
					.prepareStatement("INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, deletedevent_origin, deletedevent_timestamp, deletedevent_event_ext_id) "
							+ "VALUES (?, ?, ?, now(), ?)");
			EventObmId databaseId = ev.getObmId();
			for (Attendee at : ev.getAttendees()) {
				Integer userId = userDao.userIdFromEmail(con,
						at.getEmail(), token.getDomain().getId());
				if (userId != null) {
					dev.setInt(1, databaseId.getObmId());
					dev.setInt(2, userId);
					dev.setString(3, token.getOrigin());
					dev.setString(4, extId.getExtId());
					dev.addBatch();
				}
			}
			dev.executeBatch();
			dev.close();

			removeEventExceptions(con, databaseId);

			dev = con.prepareStatement("DELETE FROM Event WHERE event_id=?");
			dev.setInt(1, databaseId.getObmId());
			dev.executeUpdate();

		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, dev, null);
		}

		return ev;
	}

	@Override
	public boolean changeParticipationState(AccessToken token, ObmUser calendar,
			EventExtId extId, ParticipationState participationState) throws SQLException {

		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return changeParticipationState(con, token, extId, calendar, participationState);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	private boolean changeParticipationState(Connection con, AccessToken token, EventExtId extId, ObmUser calendarOwner,
			ParticipationState participationState) throws SQLException {
		
		PreparedStatement ps = null;
				
		String q = "UPDATE EventLink " 
			+ "SET eventlink_state = ?, eventlink_userupdate = ? "
			+ "WHERE eventlink_event_id IN "
			+ "( SELECT event_id FROM Event WHERE event_ext_id = ? ) AND "
			+ "eventlink_entity_id IN "
			+ "( SELECT userentity_entity_id FROM UserEntity WHERE userentity_user_id = ? )";
		
		Integer loggedUserId = token.getObmId();

		try {
			ps = con.prepareStatement(q);		

			int idx = 1;
			ps.setObject(idx++, participationState.getJdbcObject(obmHelper.getType()));
			ps.setInt(idx++, loggedUserId);
			ps.setString(idx++, extId.getExtId());
			ps.setInt(idx++, calendarOwner.getUid());
			ps.execute();
			if (ps.getUpdateCount() > 0) {
				return true;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
		return false;
	}
	
	@Override
	public boolean changeParticipationState(AccessToken token, ObmUser calendar,
			EventExtId extId, RecurrenceId recurrenceId, ParticipationState participationState) throws SQLException, ParseException {

		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return changeParticipationState(con, token, extId, recurrenceId, calendar, participationState);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	private boolean changeParticipationState(Connection con, AccessToken token, EventExtId extId, RecurrenceId recurrenceId, ObmUser calendarOwner,
			ParticipationState participationState) throws SQLException, ParseException {
		
		PreparedStatement ps = null;
				
		String q = "UPDATE EventLink " 
			+ "SET eventlink_state = ?, eventlink_userupdate = ? "
			+ "WHERE eventlink_event_id IN "
			+ "("
			+ "SELECT event_id "
			+ "FROM Event e "
			+ "LEFT JOIN EventException eexp ON e.event_id = eventexception_child_id "
			+ "WHERE event_ext_id = ? "
			+ "AND eexp.eventexception_date = ?"
			+ ") AND "
			+ "eventlink_entity_id IN "
			+ "( SELECT userentity_entity_id FROM UserEntity WHERE userentity_user_id = ? )";
		
		Integer loggedUserId = token.getObmId();

		try {
			ps = con.prepareStatement(q);		

			int idx = 1;
			ps.setObject(idx++, participationState.getJdbcObject(obmHelper.getType()));
			ps.setInt(idx++, loggedUserId);
			ps.setString(idx++, extId.getExtId());
			Date recId = new DateTime(recurrenceId.getRecurrenceId());
			ps.setTimestamp(idx++, new Timestamp(recId.getTime()));
			ps.setInt(idx++, calendarOwner.getUid());
			ps.execute();
			if (ps.getUpdateCount() > 0) {
				return true;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
		return false;
	}
	
}
