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
import org.obm.push.utils.JDBCUtils;
import org.obm.push.utils.jdbc.AbstractSQLCollectionHelper;
import org.obm.push.utils.jdbc.IntegerIndexedSQLCollectionHelper;
import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.push.utils.jdbc.WildcardStringSQLCollectionHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDaysParser;
import org.obm.sync.calendar.RecurrenceDaysSerializer;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.solr.SolrHelper;
import org.obm.sync.solr.SolrHelper.Factory;
import org.obm.sync.utils.DisplayNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.SQLUtils;
import fr.aliacom.obm.common.calendar.loader.AttendeeLoader;
import fr.aliacom.obm.common.calendar.loader.EventLoader;
import fr.aliacom.obm.common.calendar.loader.ResourceLoader;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
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

	private static final String ATT_INSERT_FIELDS = "eventlink_event_id, eventlink_entity_id, "
			+ "eventlink_state, eventlink_required, eventlink_percent, eventlink_usercreate, eventlink_is_organizer";

	private static final String EXCEPS_FIELDS = "event_id, eventexception_date";

	private final UserDao userDao;
	private final ObmHelper obmHelper;

	private final Ical4jHelper ical4jHelper;

	private final Factory solrHelperFactory;


	@Inject
	@VisibleForTesting CalendarDaoJdbcImpl(UserDao userDao, SolrHelper.Factory solrHelperFactory, ObmHelper obmHelper, Ical4jHelper ical4jHelper) {
		this.userDao = userDao;
		this.solrHelperFactory = solrHelperFactory;
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

		obmHelper.linkEntity(con, "EventEntity", "event_id", id.getObmId());

		insertAttendees(editor, ev, con, ev.getAttendees());

		insertExceptions(editor, ev, con, id);
		if (ev.isRecurrent()) {
			insertEventExceptions(editor, calendar, ev.getRecurrence()
					.getEventExceptions(), con, id, useObmUser);
		}
		Integer alert = ev.getAlert();
		if (alert != null) {
			insertEventAlert(con, editor, ownerId, ev);
		}

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
			Set<Event> eventException, Connection con, EventObmId id, Boolean useObmUser)
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
		e.setTimeUpdate(JDBCUtils.getDate(evrs, "event_timeupdate"));
		e.setTimeCreate(JDBCUtils.getDate(evrs, "event_timecreate"));
		e.setType(EventType.valueOf(evrs.getString("event_type")));
		e.setExtId(new EventExtId(evrs.getString("event_ext_id")));
		e.setOpacity(EventOpacity.getValueOf(evrs.getString("event_opacity")));
		e.setCategory(evrs.getString("eventcategory1_label"));
		e.setTitle(evrs.getString("event_title"));
		e.setLocation(evrs.getString("event_location"));
		cal.setTimeInMillis(evrs.getTimestamp("event_date").getTime());
		e.setStartDate(cal.getTime());
		e.setDuration(JDBCUtils.convertNegativeIntegerToZero(evrs, "event_duration"));
		e.setPriority(evrs.getInt("event_priority"));
		e.setPrivacy(EventPrivacy.fromSqlIntCode(evrs.getInt("event_privacy")));
		e.setAllday(evrs.getBoolean("event_allday"));
		e.setDescription(evrs.getString("event_description"));
		e.setSequence(evrs.getInt("event_sequence"));

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.valueOf(evrs.getString("event_repeatkind")));
		er.setDays(new RecurrenceDaysParser().parse(evrs.getString("event_repeatdays")));
		er.setFrequence(evrs.getInt("event_repeatfrequence"));
		if (evrs.getTimestamp("event_endrepeat") != null) {
			cal.setTimeInMillis(evrs.getTimestamp("event_endrepeat").getTime());
			er.setEnd(cal.getTime());			
		}
		e.setRecurrence(er);

		e.setOwner(evrs.getString("owner"));
		e.setOwnerEmail( getUserObmEmail(evrs, evrs.getString("domain_name")) );
		e.setOwnerDisplayName(getOwnerDisplayName(evrs));
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
		ps.setObject(idx++, obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VOPACITY, ev.getOpacity().toString()));
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
		ps.setInt(idx++, ev.getPrivacy().toSqlIntCode());
		if (ev.getStartDate() != null) {
			ps.setTimestamp(idx++, new Timestamp(ev.getStartDate().getTime()));
		} else {
			ps.setNull(idx++, Types.DATE);
		}
		ps.setInt(idx++, ev.getDuration());
		ps.setBoolean(idx++, ev.isAllday());
		EventRecurrence r = ev.getRecurrence();
		ps.setString(idx++, r.getKind().toString());
		ps.setInt(idx++, r.getFrequence());
		ps.setString(idx++, new RecurrenceDaysSerializer().serialize(r.getDays()));
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
		ps.setObject(idx++, obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VCOMPONENT, ev.getType().toString()));
		ps.setInt(idx++, ev.getSequence());
		return idx;
	}
	
    private List<DeletedEvent> findDeletedEvents(ObmUser calendarUser, Date d,
                    EventType eventType, List<DeletedEvent> declined) {

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
                    ps.setObject(2, obmHelper.getDBCP()
                    		.getJdbcObject(ObmHelper.VCOMPONENT, eventType.toString()));
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
				loadAttendees(con, eventById, domainName);
				loadAlerts(con, token, eventById, eventIdSQLCollectionHelper);
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
				dayAfter.setTimeInMillis(event.getStartDate().getTime());
				dayAfter.add(java.util.Calendar.HOUR_OF_DAY, 11);

				java.util.Calendar dayBefore = java.util.Calendar.getInstance();
				dayBefore.setTimeZone(TimeZone.getTimeZone("GMT"));
				dayBefore.setTimeInMillis(event.getStartDate().getTime());
				dayBefore.add(java.util.Calendar.HOUR_OF_DAY, -11);

				query.setTimestamp(index++,
						new java.sql.Timestamp(dayBefore.getTimeInMillis()));

				query.setTimestamp(index++,
						new java.sql.Timestamp(dayAfter.getTimeInMillis()));
			} else {
				query.setTimestamp(index++, new java.sql.Timestamp(event
						.getStartDate().getTime()));
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
					+ event.getTitle() + " date: " + event.getStartDate()
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

	private Participation getAttendeeState(ResultSet rs)
			throws SQLException {
		return Participation.builder()
							.state(State.getValueOf(rs.getString("eventlink_state")))
							.comment(rs.getString("eventlink_comment"))
							.build();
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
						ps.setObject(idx++, obmHelper.getDBCP()
								.getJdbcObject(ObmHelper.VCOMPONENT, EventType.VEVENT.toString()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getStart()
								.getTime()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getStart()
								.getTime()));
						ps.setTimestamp(idx++, new Timestamp(fbr.getEnd()
								.getTime()));
						ps.setObject(idx++, obmHelper.getDBCP()
								.getJdbcObject(ObmHelper.VPARTSTAT, State.DECLINED.toString()));
						ps.setObject(idx++, obmHelper.getDBCP()
								.getJdbcObject(ObmHelper.VOPACITY, EventOpacity.OPAQUE.toString()));
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
	public ResourceInfo getResource(String resourceEmail) throws FindException {
		Connection conn = null;
		try {
			conn = obmHelper.getConnection();
			ResourceLoader loader = ResourceLoader.builder().connection(conn).emails(resourceEmail).build();
			Collection<ResourceInfo> res = loader.load();
			return Iterables.getFirst(res, null);
		}
		catch (SQLException ex) {
			throw new FindException(ex);
		}
		finally {
			obmHelper.cleanup(conn, null, null);
		}
	}

	@Override
	public Collection<Event> getResourceEvents(ResourceInfo resourceInfo,
			SyncRange syncRange)
			throws FindException {
		Calendar cal = getGMTCalendar();
		String domainName = resourceInfo.getDomainName();
		Attendee resourceAtt = ResourceAttendee.builder().email(resourceInfo.getMail()).build();
		Connection conn = null;
		
		try {
			conn = obmHelper.getConnection();
			EventLoader loader = EventLoader.builder().connection(conn).domainName(domainName).calendar(cal).
				occurringBetween(syncRange).usingResources(resourceInfo.getId()).withExceptions(true).
				withoutDeclinedAttendee(resourceAtt).build();
			Map<EventObmId, Event> events = loader.load();
			return events.values();
		}
		catch (SQLException ex) {
			throw new FindException(ex);
		} finally {
			obmHelper.cleanup(conn, null, null);
		}
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
			fetchIds.append("(e.event_repeatkind != 'none' AND (e.event_endrepeat IS NULL OR e.event_endrepeat >= ?)) OR ");
		
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
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VCOMPONENT, typeFilter.toString()));
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
				// Recurrent events
				evps.setTimestamp(idx++, new Timestamp(syncRange.getAfter().getTime()));
				// Non-recurrent events
				evps.setTimestamp(idx++, new Timestamp(syncRange.getAfter().getTime()));
				if(syncRange.getBefore() != null){
					evps.setTimestamp(idx++, new Timestamp(syncRange.getBefore().getTime()));
				}
			}

			
			evrs = evps.executeQuery();
			while (evrs.next()) {
				int recurentParentId = evrs.getInt(4);
				State state = State.getValueOf(evrs.getString(2));
				Integer eventId = evrs.getInt(1);
				if (state == State.DECLINED) {
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
						ret.setLastSync(JDBCUtils.getDate(evrs, "last_sync"));
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
				loadAttendees(conComp, eventById, calendarUser.getDomain().getName());
				loadAlerts(conComp, token, eventById, changedIds);
				loadExceptions(conComp, cal, eventById, changedIds);
				loadEventExceptions(conComp, token, eventById, changedIds);
			}

			touchParentOfDeclinedRecurrentEvents(
					parentOfDeclinedRecurrentEvent, changedEvent,
					touchDateForFakeExDates);

			if (!changedEvent.isEmpty()) {
				replaceDeclinedEventExceptionByException(calendarUser, changedEvent);
			}
		} catch (SQLException e) {
			logger.error("error loading attendees, alerts, exceptions, eventException", e);
		} finally {
			obmHelper.cleanup(conComp, null, null);
		}
		ret.setUpdated(changedEvent);

		Iterable<DeletedEvent> deletedEvents = Iterables.concat(findDeletedEvents(calendarUser, lastSync, typeFilter,declined), findDeletedEventsLinks(calendarUser, lastSync));

		ret.setDeletedEvents(Sets.newHashSet(deletedEvents));
		
		return ret;
	}

	private Collection<DeletedEvent> findDeletedEventsLinks(ObmUser calendarUser, Date lastSync) {
		List<DeletedEvent> result = new LinkedList<DeletedEvent>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String q = "SELECT deletedeventlink_event_id, deletedeventlink_event_ext_id FROM DeletedEventLink WHERE deletedeventlink_userobm_id=? ";
		if (lastSync != null) {
				q += "AND deletedeventlink_time_removed >= ? ";
		}
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, calendarUser.getUid());
			if (lastSync != null) {
					ps.setTimestamp(2, new Timestamp(lastSync.getTime()));
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

	@VisibleForTesting void touchParentOfDeclinedRecurrentEvents(
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

	private void replaceDeclinedEventExceptionByException(ObmUser calendarUser,
			List<Event> changedEvent) {
		for(Event event: changedEvent) {
			if(event.isRecurrent()) {
				event.getRecurrence().replaceUnattendedEventExceptionByException(calendarUser.getEmail());
			}
		}
	}
	
	private String buildUserCalendarsQuery(Collection<String> calendarEmails) {
		String query = "select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join UserEntity u1 on entityright_consumer_id=u1.userentity_entity_id "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "where u1.userentity_user_id=? and (entityright_read=1 or entityright_write=1) "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1 "
				+ "and userobm_id != ? "
				+ SQLUtils.selectCalendarsCondition(calendarEmails);
				
		return query;
	}

	private String buildPublicCalendarsQuery(Collection<String> calendarEmails) {
		String query = "select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "where entityright_consumer_id is null and (entityright_read=1 or entityright_write=1) "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1 "
				+ "and userobm_domain_id= ? "
				+ "and userobm_id != ? "
				+ SQLUtils.selectCalendarsCondition(calendarEmails);
		
		return query;
	}

	private String buildGroupCalendarsQuery(Collection<String> calendarEmails) {
		String query = "select userobm_login, userobm_firstname, userobm_lastname, userobm_email, entityright_read, entityright_write "
				+ "from EntityRight "
				+ "inner join GroupEntity u1 on entityright_consumer_id=u1.groupentity_entity_id "
				+ "inner join CalendarEntity u2 on u2.calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=u2.calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = u1.groupentity_group_id "
				+ "where of_usergroup_user_id=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1 "
				+ "and userobm_id != ? "
				+ SQLUtils.selectCalendarsCondition(calendarEmails);
		
		return query;
	}

	private String buildPublicAndUserAndGroupCalendarsQuery(String publicQuery, String groupQuery, String userQuery) {
		return String.format("SELECT userobm_login, userobm_firstname, userobm_lastname, userobm_email, "
				+ "MAX(entityright_read), MAX(entityright_write) "
				+ "FROM (%s UNION %s UNION %s) calendars_union "
				+ "GROUP BY userobm_login, userobm_firstname, userobm_lastname, userobm_email",
				publicQuery, groupQuery, userQuery);
	}
	
	private Collection<CalendarInfo> listUserAndPublicCalendars(ObmUser user, Collection<String> emails) throws FindException {
		StringSQLCollectionHelper helper = null;

		if (emails != null && !emails.isEmpty()) {
			helper = new WildcardStringSQLCollectionHelper(emails);
		}
		
		String publicQuery = buildPublicCalendarsQuery(emails);
		String userQuery = buildUserCalendarsQuery(emails);
		String groupQuery = buildGroupCalendarsQuery(emails);
		String query = buildPublicAndUserAndGroupCalendarsQuery(publicQuery, groupQuery, userQuery);

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Set<CalendarInfo> caldendarInfo = new HashSet<CalendarInfo>();
		String domainName = user.getDomain().getName();
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			int pos = 1;

			// For the public query
			ps.setInt(pos++, user.getDomain().getId());
			ps.setInt(pos++, user.getUid());
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			// For the group query
			ps.setInt(pos++, user.getUid());
			ps.setInt(pos++, user.getUid());
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			// For the user query
			ps.setInt(pos++, user.getUid());
			ps.setInt(pos++, user.getUid());
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			rs = ps.executeQuery();

			while (rs.next()) {
				caldendarInfo.add(makeCalendarInfo(rs, domainName));
			}
		} catch (SQLException e) {
			logger.error("Error listing calendar rights.", e);
			throw new FindException(e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		
		return caldendarInfo;
	}

	@Override
	public Collection<CalendarInfo> listCalendars(ObmUser user) throws FindException {
		return listUserAndPublicCalendars(user, null);
	}

	@Override
	public Collection<ResourceInfo> listResources(ObmUser user) throws FindException {
		return listUserAndPublicResources(user, null);
	}

	private String buildUserResourcesQuery(StringSQLCollectionHelper helper) {
		String query = "SELECT resource_id, resource_name, resource_email, "
				+ "resource_description, e.entityright_read, e.entityright_write "
			+ "FROM Resource r "
				+ "INNER JOIN ResourceEntity re ON r.resource_id=re.resourceentity_resource_id "
				+ "INNER JOIN EntityRight e ON re.resourceentity_entity_id =e.entityright_entity_id "
				+ "INNER JOIN UserEntity ue ON e.entityright_consumer_id=ue.userentity_entity_id "
			+ "WHERE ue.userentity_user_id=? ";
		if (helper != null)
			query +=" AND r.resource_email IN (" + helper.asPlaceHolders() + ")";
		return query;
	}

	private String buildPublicResourcesQuery(StringSQLCollectionHelper helper) {
		String query = "SELECT resource_id, resource_name, resource_email, "
				+ "resource_description, e.entityright_read, e.entityright_write "
			+ "FROM Resource r "
				+ "INNER JOIN ResourceEntity re ON r.resource_id=re.resourceentity_resource_id "
				+ "INNER JOIN EntityRight e ON re.resourceentity_entity_id =e.entityright_entity_id "
				+ "WHERE e.entityright_consumer_id IS NULL ";
		if (helper != null)
			query +=" AND r.resource_email IN (" + helper.asPlaceHolders() + ")";
		return query;
	}

	private String buildGroupResourcesQuery(StringSQLCollectionHelper helper) {
		String query = "SELECT resource_id, resource_name, resource_email, "
			+ "resource_description, e.entityright_read, e.entityright_write "
		+ "FROM Resource r "
			+ "INNER JOIN ResourceEntity re ON r.resource_id=re.resourceentity_resource_id "
			+ "INNER JOIN EntityRight e ON re.resourceentity_entity_id =e.entityright_entity_id "
			+ "INNER JOIN GroupEntity ge ON e.entityright_consumer_id=ge.groupentity_entity_id "
			+ "INNER JOIN of_usergroup ug ON ge.groupentity_group_id=ug.of_usergroup_group_id "
		+ "WHERE ug.of_usergroup_user_id=? ";
		if (helper != null)
			query +=" AND r.resource_email IN (" + helper.asPlaceHolders() + ")";
		return query;
	}

	private String buildPublicAndUserAndGroupResourcesQuery(String publicQuery, String groupQuery, String userQuery) {
		return String.format("SELECT resource_id, resource_name, resource_email, resource_description, "
				+ "SUM(entityright_read), SUM(entityright_write) "
				+ "FROM (%s UNION %s UNION %s) resource_union "
				+ "GROUP BY resource_id, resource_name, resource_email, resource_description",
				publicQuery, groupQuery, userQuery);
	}

	private Collection<ResourceInfo> listUserAndPublicResources(ObmUser user, Collection<String> emails) throws FindException {
		StringSQLCollectionHelper helper = null;
		if (emails != null && !emails.isEmpty()) {
			helper = new StringSQLCollectionHelper(emails);
		}
		String publicQuery = buildPublicResourcesQuery(helper);
		String userQuery = buildUserResourcesQuery(helper);
		String groupQuery = buildGroupResourcesQuery(helper);
		String query = buildPublicAndUserAndGroupResourcesQuery(publicQuery, groupQuery, userQuery);

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Set<ResourceInfo> resourceInfo = new HashSet<ResourceInfo>();
		String domainName = user.getDomain().getName();
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			int pos = 1;

			// For the public query
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			// For the group query
			ps.setInt(pos, user.getUid());
			pos++;
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			// For the user query
			ps.setInt(pos, user.getUid());
			pos++;
			if (helper != null) {
				pos = helper.insertValues(ps, pos);
			}

			rs = ps.executeQuery();

			while (rs.next()) {
				resourceInfo.add(buildResourceInfo(rs, domainName));
			}
		} catch (SQLException e) {
			logger.error("Error finding resources", e);
			throw new FindException(e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff", e);
			}
		}
		return resourceInfo;
	}

	private ResourceInfo buildResourceInfo(ResultSet rs, String domainName) throws SQLException {
		return ResourceInfo.builder().id(rs.getInt(1)).name(rs.getString(2)).mail(rs.getString(3)).
				description(rs.getString(4)).read(rs.getBoolean(5)).write(rs.getBoolean(6)).domainName(domainName).build();
	}

	@Override
	public Collection<CalendarInfo> getCalendarMetadata(ObmUser user, Collection<String> calendarEmails) throws FindException {
		return this.listUserAndPublicCalendars(user, calendarEmails);
	}

	@Override
	public Collection<ResourceInfo> getResourceMetadata(ObmUser user, Collection<String> resourceEmails)
			throws FindException {
		return this.listUserAndPublicResources(user, resourceEmails);
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
	
	private void loadAttendees(Connection con, Map<EventObmId, Event> eventById, String domainName) throws SQLException {
		if (eventById.isEmpty()) {
			return;
		}
		AttendeeLoader attendeeLoader = AttendeeLoader.builder().connection(con).domainName(domainName).eventsById(eventById).build();
		attendeeLoader.load();
		defineEventsInternalStatus(eventById.values());
	}

	private void loadAlerts(Connection con, AccessToken token, Map<EventObmId, Event> eventById,
			IntegerSQLCollectionHelper eventIds) throws SQLException {

		String alertsQuery = "SELECT eventalert_event_id, eventalert_duration "
				+ "FROM EventAlert "
				+ "WHERE eventalert_event_id IN (" + eventIds.asPlaceHolders() + ") "
				+ "AND eventalert_user_id = ?";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(alertsQuery);
			int nextId = eventIds.insertValues(ps, 1);
			ps.setInt(nextId, token.getObmId());
			rs = ps.executeQuery();
			while(rs.next()) {
				EventObmId eventId = new EventObmId(rs.getInt("eventalert_event_id"));
				int alertDuration = rs.getInt("eventalert_duration");
				//eventalert_duration is sometimes set to -1 to disable an alert, 
				//this behaviour does not conform to the rfc but happens in OBM nonetheless
				if (alertDuration >= 0) {
					Event event = eventById.get(eventId);
					event.setAlert(alertDuration);
				}
			}
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private void defineEventsInternalStatus(Collection<Event> events) {
		for (Event evt: events) {
			evt.setInternalEvent(EventUtils.isInternalEvent(evt));
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
		Multimap<Event, Event> eventChildren = ArrayListMultimap.create();
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
					eventChildren.put(event, eventExcept);
					changedEvent.add(eventExcept);
					evenExcepttById.put(eventExcept.getObmId(),
							eventExcept);
				}
			}
			IntegerIndexedSQLCollectionHelper changedIds = new IntegerIndexedSQLCollectionHelper(changedEvent);
			loadAttendees(con, evenExcepttById, domainName);
			loadAlerts(con, token, evenExcepttById, changedIds);
			
			for (Entry<Event, Event> entry: eventChildren.entries()) {
				entry.getKey().addEventException(entry.getValue());
			}
			
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
				+ " date: " + ev.getStartDate() + " id: " + ev.getObmId());
		
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
				+ " date: " + ev.getStartDate() + " id: " + ev.getObmId());
		
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
				removeAttendees(con, attendeetoRemove, ev);
				insertDeletedEventLinks(editor, con, attendeetoRemove, ev);
			}

			if (updateAttendees) {
				updateAttendees(editor, con, ev);
				markUpdated(con, ev.getObmId());
			}

			int ownerId = userDao.userIdFromEmail(con, calendar, editor.getDomain().getId());
			updateAlerts(editor, con, ev, ownerId);

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

	private void insertDeletedEventLinks(AccessToken editor, Connection con, List<Attendee> attendeetoRemove, Event ev)
		throws SQLException {
			String q = "INSERT INTO DeletedEventLink ( deletedeventlink_userobm_id, deletedeventlink_event_id, deletedeventlink_event_ext_id ) VALUES ( ?, ?, ? ) ";
			PreparedStatement ps = null;
			logger.info("Event Modification : will insert {} deleted event links.", attendeetoRemove.size());
			
			List<Integer> attendeeUserIds = filterOutContactsAttendees(editor, con, attendeetoRemove);

			try {
				ps = con.prepareStatement(q);

				for (Integer id : attendeeUserIds) {
					ps.setInt(1, id);
					ps.setInt(2, ev.getObmId().getObmId());
					ps.setString(3, ev.getExtId().getExtId());
					ps.addBatch();
				}

				ps.executeBatch();
			} finally {
				obmHelper.cleanup(null, ps, null);
			}
	}

	private List<Integer> filterOutContactsAttendees(AccessToken editor, Connection con, List<Attendee> attendeetoRemove) throws SQLException {
		List<Integer> attendeeUserIds = new ArrayList<Integer>();
		for (Attendee at : attendeetoRemove) {
			Integer id = userDao.userIdFromEmail(con, at.getEmail(), editor.getDomain().getId());

			if (id != null) {
				attendeeUserIds.add(id);
			}
		}
		return attendeeUserIds;
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
		ps.setObject(2, obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VCOMPONENT, ev.getType().toString()));
		ps.setString(3, ev.getTimezoneName() != null ? ev.getTimezoneName()
				: "Europe/Paris");
		ps.setObject(4, obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VOPACITY, ev.getOpacity().toString()));
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
		ps.setInt(9, ev.getPrivacy().toSqlIntCode());
		ps.setTimestamp(10, new Timestamp(ev.getStartDate().getTime()));
		ps.setInt(11, ev.getDuration());
		ps.setBoolean(12, ev.isAllday());
		EventRecurrence er = ev.getRecurrence();
		ps.setString(13, er.getKind().toString());
		ps.setInt(14, er.getFrequence());
		ps.setString(15, new RecurrenceDaysSerializer().serialize(er.getDays()));
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

	private void removeAttendees(Connection con, List<Attendee> toRemove, Event ev) throws SQLException {
		PreparedStatement ps = null;
		String q = "DELETE FROM EventLink WHERE eventlink_event_id=? AND eventlink_entity_id=? ";
		
		logger.info("event update will remove {} attendees.", toRemove.size());
		try {
			ps = con.prepareStatement(q);
			
			for (Attendee at : toRemove) {
				ps.setInt(1, ev.getObmId().getObmId());
				ps.setInt(2, at.getEntityId());
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
			throws SQLException, EventNotFoundException, ServerFault {
		
		Event ev = findEventById(token, uid);
		Event deleted = removeEvent(con, token, et, ev);
		deleted.setSequence(sequence);
		return deleted;
	}

	private Collection<Integer> extractAttendeeIds(Connection con, AccessToken token, Event event)
			throws SQLException {
		List<Attendee> attendees = event.getAttendees();
		Map<String, Integer> attendeesEmailToId = Maps.newHashMapWithExpectedSize(attendees.size());
		int domainId = token.getDomain().getId();
		for (Attendee attendee : event.getAttendees()) {
			addAttendeeIdMapping(con, domainId, attendeesEmailToId, attendee);
		}
		return attendeesEmailToId.values();
	}

	private void addAttendeeIdMapping(Connection con, int domainId,
			Map<String, Integer> attendeesEmailToId, Attendee attendee) throws SQLException {
		
		if (!attendeesEmailToId.containsKey(attendee.getEmail())) {
			Integer userId = userDao.userIdFromEmail(con, attendee.getEmail(), domainId);
			if (userId != null) {
				attendeesEmailToId.put(attendee.getEmail(), userId);
			}
		}
	}

	private void removeFromDeletedEvent(Connection con, Event ev, Collection<Integer> attendeeIds)
			throws SQLException {
		PreparedStatement deleteStatement = null;
		try {
			deleteStatement = con.prepareStatement("DELETE FROM DeletedEvent "
					+ "WHERE deletedevent_event_ext_id=? AND deletedevent_user_id=?");
			String extId = ev.getExtId().getExtId();
			for (int attendeeId : attendeeIds) {
				deleteStatement.setString(1, extId);
				deleteStatement.setInt(2, attendeeId);
				deleteStatement.addBatch();
			}
			deleteStatement.executeBatch();
		} finally {
			obmHelper.cleanup(null, deleteStatement, null);
		}
	}

	private void insertIntoDeletedEvent(Connection con, AccessToken token, Event event, EventType eventType,
			Collection<Integer> attendeeIds) throws SQLException {
		PreparedStatement insertStatement = null;
		try {
			insertStatement = con
					.prepareStatement("INSERT INTO DeletedEvent (deletedevent_event_id, deletedevent_user_id, "
							+ "deletedevent_origin, deletedevent_type, deletedevent_timestamp, deletedevent_event_ext_id) "
							+ "VALUES (?, ?, ?, ?, now(), ?)");
			EventObmId databaseId = event.getObmId();
			for (int attendeeId : attendeeIds) {
				insertStatement.setInt(1, databaseId.getObmId());
				insertStatement.setInt(2, attendeeId);
				insertStatement.setString(3, token.getOrigin());
				insertStatement.setObject(4, obmHelper.getDBCP()
						.getJdbcObject(ObmHelper.VCOMPONENT, eventType.toString()));
				insertStatement.setString(5, event.getExtId().getExtId());
				insertStatement.addBatch();
			}
			insertStatement.executeBatch();
		} finally {
			obmHelper.cleanup(null, insertStatement, null);
		}
	}

	private void removeFromEvent(Connection con, Event event) throws SQLException {
		PreparedStatement deleteStatement = null;
		try {
			deleteStatement = con.prepareStatement("DELETE FROM Event WHERE event_id=?");
			deleteStatement.setInt(1, event.getObmId().getObmId());
			deleteStatement.executeUpdate();
		} finally {
			obmHelper.cleanup(null, deleteStatement, null);
		}
	}

	private Event removeEvent(Connection con, AccessToken token, EventType eventType, Event event)
			throws SQLException {
		Preconditions.checkArgument(event.getRecurrenceId() == null,
				"Cannot remove an event exception via removeEvent()");

		Collection<Integer> attendeeIds = extractAttendeeIds(con, token, event);

		// Avoids potential duplicates
		removeFromDeletedEvent(con, event, attendeeIds);

		insertIntoDeletedEvent(con, token, event, eventType, attendeeIds);
		removeFromEvent(con, event);
		removeEventFromSolr(token, event);

		return event;
	}

	private void removeEventFromSolr(AccessToken token, Event ev) {
		try {
			solrHelperFactory.createClient(token).delete(ev);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	@VisibleForTesting void insertAttendees(AccessToken editor, Event ev, Connection con,
			List<Attendee> attendees) throws SQLException {
		String attQ = "INSERT INTO EventLink (" + ATT_INSERT_FIELDS
				+ ") VALUES (" + "?, " + // event_id
				"?, " + // entity_id
				"?, " + // state
				"?, " + // required
				"?," + // percent
				"?," + // user_create
				"?" + // is_organizer
				")";
		boolean shouldClearOrganizer = false;
		PreparedStatement ps = null;
		
		try {
			ps = con.prepareStatement(attQ);
			
			final int eventObmId = ev.getObmId().getObmId();
			final Set<Attendee> listAttendee = removeDuplicateAttendee(attendees);
			Set<Integer> alreadyAddedAttendees = Sets.newHashSet();
			
			for (final Attendee at : listAttendee) {
				boolean isOrganizer = Objects.firstNonNull(at.isOrganizer(), false);
				
				String attendeeEmail = at.getEmail();
				Integer userEntity = at.getEntityId();

				// There must be only one organizer in a given event
				if (isOrganizer) {
					shouldClearOrganizer = true;
				}
				
				if (alreadyAddedAttendees.contains(userEntity)) {
					logger.info("Attendee {} with entity ID {} already added, skipping.", attendeeEmail, userEntity);
					
					continue;
				}
				
				ps.setInt(1, eventObmId);
				ps.setInt(2, userEntity);
				ps.setObject(3, getJdbcObjectParticipation(at));
				ps.setObject(4, getJdbcObjectParticipationRole(at));
				ps.setInt(5, at.getPercent());
				ps.setInt(6, editor.getObmId());
				ps.setBoolean(7, isOrganizer);
				ps.addBatch();
				logger.info(LogUtils.prefix(editor) + "Adding " + attendeeEmail + ( isOrganizer ? " as organizer" : " as attendee"));
				
				alreadyAddedAttendees.add(userEntity);
			}
			
			// Clear the previous organizer if needed
			if (shouldClearOrganizer) {
				clearOrganizer(eventObmId, con);
			}
			
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}
	
	private void clearOrganizer(int eventId, Connection con) throws SQLException {
		PreparedStatement ps = null;
		String query = "UPDATE EventLink SET eventlink_is_organizer = false WHERE eventlink_event_id = ?";
		
		try {
			ps = con.prepareStatement(query);
			
			ps.setInt(1, eventId);
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private Object getJdbcObjectParticipation(final Attendee at) throws SQLException {
		final Participation participation = RFC2445.getParticipationOrDefault(at.getParticipation());
		return obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VPARTSTAT, participation.getState().toString());
	}

	private Object getJdbcObjectParticipationRole(final Attendee at) throws SQLException {
		final ParticipationRole pRole = RFC2445.getParticipationRoleOrDefault(at.getParticipationRole());
		return obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VROLE, pRole.toString());
	}
	
	private Set<Attendee> removeDuplicateAttendee(List<Attendee> attendees) {
		return ImmutableSet.copyOf(attendees);
	}

	private void updateAttendees(AccessToken token, Connection con, Event event) throws SQLException {
		String q = "update EventLink set eventlink_state=?, eventlink_required=?, eventlink_userupdate=?, eventlink_percent=?, eventlink_is_organizer=? "
				+ "where eventlink_event_id = ? AND eventlink_entity_id = ?";
		PreparedStatement ps = null;
		int[] updatedAttendees;
		List<Attendee> mightInsert = new LinkedList<Attendee>();
		List<Attendee> toInsert = new LinkedList<Attendee>();

		try {
			ps = con.prepareStatement(q);

			for (Attendee at : event.getAttendees()) {
				Integer userEntity = at.getEntityId();
				int idx = 1;
				
				ps.setObject(idx++, obmHelper.getDBCP()
						.getJdbcObject(ObmHelper.VPARTSTAT, at.getParticipation().getState().toString()));
				ps.setObject(idx++, obmHelper.getDBCP()
						.getJdbcObject(ObmHelper.VROLE, at.getParticipationRole().toString()));
				ps.setInt(idx++, token.getObmId());
				ps.setInt(idx++, at.getPercent());
				ps.setBoolean(idx++, at.isOrganizer());
				ps.setInt(idx++, event.getObmId().getObmId());
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
		insertAttendees(token, event, con, toInsert);
	}

	private void updateAlerts(AccessToken token, Connection con, Event event, int ownerId)
			throws SQLException {
		if (event.getAlert() == null) {
			deleteEventAlert(con, token, ownerId, event.getObmId());
		} else {
			if (!updateEventAlert(con, token, ownerId, event)) {
				insertEventAlert(con, token, ownerId, event);
			}
		}
	}

	private void insertEventAlert(Connection con, AccessToken token, Integer ownerId, Event event) throws SQLException {

		int tokenId = token.getObmId();
		Integer alert = event.getAlert();
		int eventId = event.getObmId().getObmId();

		String insertAlertQuery = "INSERT INTO EventAlert "
				+ "(eventalert_event_id, eventalert_user_id, eventalert_duration, eventalert_usercreate) "
				+ "VALUES (?, ?, ?, ?)";

		if (tokenId != ownerId) {
			insertAlertQuery += ",(?, ?, ?, ?)";
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement(insertAlertQuery);
			ps.setInt(1, eventId);
			ps.setInt(2, tokenId);
			ps.setInt(3, alert);
			ps.setInt(4, tokenId);
			if(tokenId != ownerId) {
				ps.setInt(5, eventId);
				ps.setInt(6, ownerId);
				ps.setInt(7, alert);
				ps.setInt(8, tokenId);
			}
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private boolean updateEventAlert(Connection con, AccessToken token, Integer ownerId, Event event) throws SQLException {

		IntegerSQLCollectionHelper userIds = new IntegerSQLCollectionHelper(Sets.newHashSet(ownerId, token.getObmId()));

		String updateAlertQuery = "UPDATE EventAlert "
				+ "SET eventalert_duration = ?, eventalert_userupdate = ? "
				+ "WHERE eventalert_user_id IN ("+ userIds.asPlaceHolders() +") "
				+ "AND eventalert_event_id = ?";

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement(updateAlertQuery);
			ps.setInt(1, event.getAlert());
			ps.setInt(2, token.getObmId());
			int nextId = userIds.insertValues(ps, 3);
			ps.setInt(nextId, event.getObmId().getObmId());

			return (ps.executeUpdate() > 0);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void deleteEventAlert(Connection con, AccessToken token, Integer ownerId, EventObmId eventObmId) throws SQLException {
		IntegerSQLCollectionHelper userIds = new IntegerSQLCollectionHelper(Sets.newHashSet(ownerId, token.getObmId()));
		String deleteAlertQuery = "DELETE FROM EventAlert "
				+ "WHERE eventalert_user_id in ("+ userIds.asPlaceHolders() +") "
				+ "AND eventalert_event_id = ?";

		PreparedStatement ps = null;

		try {

			ps = con.prepareStatement(deleteAlertQuery);
			int nextId = userIds.insertValues(ps, 1);
			ps.setInt(nextId, eventObmId.getObmId());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(null, ps, null);
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
				loadAttendees(con, eventById, domainName);
				loadAlerts(con, token, eventById, eventIds);
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

	@Override
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
				loadAttendees(con, eventById, domainName);
				loadAlerts(con, token, eventById, eventIds);
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
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VCOMPONENT, typeFilter.toString()));
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
			loadAttendees(con, eventById, obmUser.getDomain().getName());
			loadAlerts(con, token, eventById, changedIds);
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
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VCOMPONENT, typeFilter.toString()));
			evrs = evps.executeQuery();

			while (evrs.next()) {
				Event event = eventFromCursor(cal, evrs);
				eventById.put(event.getObmId(), event);
				ret.add(event);
			}
			
			IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(ret);
			loadAttendees(con, eventById, calendarUser.getDomain().getName());
			loadAlerts(con, token, eventById, eventIds);
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
		String ev = "SELECT e.event_id, e.event_title, att.eventlink_state, att.eventlink_comment, e.event_date, e.event_repeatkind, e.event_repeatdays, e.event_repeatfrequence, e.event_endrepeat, al.eventalert_duration"
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
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VCOMPONENT, typeFilter.toString()));
			evps.setObject(idx++, calendarUser.getUid());
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			evps.setTimestamp(idx++, new Timestamp(start.getTime()));
			evps.setTimestamp(idx++, new Timestamp(end.getTime()));

			evrs = evps.executeQuery();
			while (evrs.next()) {
				EventParticipationState psEvent = participationEventFromCursor(evrs);
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

	private EventParticipationState participationEventFromCursor(
			ResultSet evrs) throws SQLException {
		EventParticipationState e = new EventParticipationState();
		int id = evrs.getInt("event_id");
		e.setUid("" + id);
		e.setTitle(evrs.getString("event_title"));
		e.setParticipation(getAttendeeState(evrs));
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
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VCOMPONENT, typeFilter.toString()));
			State declined = Participation.declined().getState();
			evps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VPARTSTAT, declined.toString()));
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
				lastUpdate = JDBCUtils.getDate(evrs, evrs.getMetaData().getColumnName(1));
				lastCreate = JDBCUtils.getDate(evrs, evrs.getMetaData().getColumnName(2));
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
			update = JDBCUtils.getDate(evrs, "event_timecreate");
		}
		e.setTimeUpdate(update);
		e.setExtId(evrs.getString("event_ext_id"));
		e.setRecurrenceId(JDBCUtils.getDate(evrs, "recurrenceId"));
		cal.setTimeInMillis(evrs.getTimestamp("event_date").getTime());
		e.setDate(cal.getTime());
		return e;
	}

	private EventRecurrence eventRecurrenceFromCursor(Calendar cal,
			ResultSet evrs) throws SQLException {
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.valueOf(evrs.getString("event_repeatkind")));
		er.setDays(new RecurrenceDaysParser().parse(evrs.getString("event_repeatdays")));
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
	public boolean changeParticipation(AccessToken token, ObmUser calendar,
			EventExtId extId, Participation participation) throws SQLException {

		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return changeParticipation(con, token, extId, calendar, participation);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	private boolean changeParticipation(Connection con, AccessToken token, EventExtId extId, ObmUser calendarOwner,
			Participation participation) throws SQLException {
		
		PreparedStatement ps = null;
				
		String q = "UPDATE EventLink " 
			+ "SET eventlink_state = ?, eventlink_userupdate = ?, eventlink_comment = ? "
			+ "WHERE eventlink_event_id IN "
			+ "( SELECT event_id FROM Event WHERE event_ext_id = ? ) AND "
			+ "eventlink_entity_id IN "
			+ "( SELECT userentity_entity_id FROM UserEntity WHERE userentity_user_id = ? )";
		
		Integer loggedUserId = token.getObmId();

		try {
			ps = con.prepareStatement(q);		

			int idx = 1;
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VPARTSTAT, participation.getState().toString()));
			ps.setInt(idx++, loggedUserId);
			ps.setString(idx++, participation.getSerializedCommentToString());
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
	public boolean changeParticipation(AccessToken token, ObmUser calendar,
			EventExtId extId, RecurrenceId recurrenceId, Participation participation) throws SQLException, ParseException {

		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return changeParticipation(con, token, extId, recurrenceId, calendar, participation);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	private boolean changeParticipation(Connection con, AccessToken token, EventExtId extId, RecurrenceId recurrenceId, ObmUser calendarOwner,
			Participation participation) throws SQLException, ParseException {
		
		PreparedStatement ps = null;
				
		String q = "UPDATE EventLink " 
			+ "SET eventlink_state = ?, eventlink_userupdate = ?, eventlink_comment = ? "
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
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(ObmHelper.VPARTSTAT, participation.getState().toString()));
			ps.setInt(idx++, loggedUserId);
			ps.setString(idx++, participation.getSerializedCommentToString());
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
