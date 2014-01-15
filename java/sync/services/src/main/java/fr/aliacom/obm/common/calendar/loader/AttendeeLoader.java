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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.utils.DisplayNameUtils;
import org.obm.sync.utils.MailUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.aliacom.obm.utils.DBUtils;
import fr.aliacom.obm.utils.EventObmIdSQLCollectionHelper;

public class AttendeeLoader {
	
	private static enum AttendeeType {
		USER, CONTACT, RESOURCE;
	}
	
	public static class Builder {
		private Connection conn;
		private String domainName;
		private Map<EventObmId, Event> eventsById;

		private Builder() {
			this.conn = null;
			this.domainName = null;
			this.eventsById = null;
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder domainName(String domainName) {
			this.domainName = domainName;
			return this;
		}

		public Builder eventsById(Map<EventObmId, Event> eventsById) {
			if (eventsById != null) {
				Preconditions.checkArgument(!eventsById.isEmpty(),
						"The eventsById map should not be empty");
			}
			this.eventsById = eventsById;
			return this;
		}

		public AttendeeLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(domainName != null, "The domainName parameter is mandatory");
			Preconditions.checkState(eventsById != null, "The eventsById parameter is mandatory");
			return new AttendeeLoader(conn, domainName, eventsById);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final static String INTERNAL_ATTENDEE_FIELDS = Joiner
		.on(", ")
		.join(new String[] {
				"eventlink_event_id",
				"eventlink_state",
				"eventlink_comment",
				"eventlink_required",
				"eventlink_percent",
				"eventlink_is_organizer",
				"userobm_email AS attendee_email",
				"userobm_firstname AS attendee_firstname",
				"userobm_lastname AS attendee_lastname",
				"userobm_commonname AS attendee_commonname",
				"userentity_entity_id AS attendee_entity_id",
				"'USER' as attendee_type"
		});

	private static final String EXTERNAL_ATTENDEE_FIELDS = Joiner
		.on(", ")
		.join(new String[] {
				"eventlink_event_id",
				"eventlink_state",
				"eventlink_comment",
				"eventlink_required",
				"eventlink_percent",
				"eventlink_is_organizer",
				"email_address AS attendee_email",
				"contact_firstname AS attendee_firstname",
				"contact_lastname AS attendee_lastname",
				"contact_commonname AS attendee_commonname",
				"contactentity_entity_id AS attendee_entity_id",
				"'CONTACT' as attendee_type"
		});

	private static final String RESOURCE_ATTENDEE_FIELDS = Joiner
		.on(", ")
		.join(new String[] {
				"eventlink_event_id",
				"eventlink_state",
				"eventlink_comment",
				"eventlink_required",
				"eventlink_percent",
				"eventlink_is_organizer",
				"resource_email AS attendee_email",
				"NULL AS attendee_firstname",
				"NULL AS attendee_lastname",
				"resource_name AS attendee_commonname",
				"resourceentity_entity_id AS attendee_entity_id",
				"'RESOURCE' as attendee_type"
		});
	// An attendee can be either an OBM user, a contact or a resource
	private final static int ENTITY_TYPE_COUNT = 3;

	private Connection conn;
	private Map<EventObmId, Event> eventsById;
	private String domainName;

	public AttendeeLoader(Connection conn, String domainName, Map<EventObmId, Event> eventsById) {
		this.conn = conn;
		this.eventsById = eventsById;
		this.domainName = domainName;
	}

	public Multimap<EventObmId, Attendee> load() throws SQLException {
		EventObmIdSQLCollectionHelper idsHelper = new EventObmIdSQLCollectionHelper(
				eventsById.keySet());
		String query = buildQuery(idsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper);
			rs = stat.executeQuery();
			return buildAttendees(rs);
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private String buildQuery(EventObmIdSQLCollectionHelper idsHelper) {
		return String.format("%s UNION %s UNION %s", buildInternalAttendeesQuery(idsHelper),
				buildExternalAttendeesQuery(idsHelper), buildResourceAttendeesQuery(idsHelper));
	}

	private String buildInternalAttendeesQuery(EventObmIdSQLCollectionHelper idsHelper) {
		return String.format("SELECT %s "
			+ "FROM EventLink att "
			+ "INNER JOIN UserEntity ON att.eventlink_entity_id=userentity_entity_id "
			+ "INNER JOIN UserObm ON userobm_id=userentity_user_id "
			+ "WHERE eventlink_event_id IN (%s)", INTERNAL_ATTENDEE_FIELDS, idsHelper.asPlaceHolders());
	}

	private String buildExternalAttendeesQuery(EventObmIdSQLCollectionHelper idsHelper) {
		String query = String.format("SELECT %s "
			+ "FROM EventLink att "
			+ "INNER JOIN ContactEntity ON att.eventlink_entity_id=contactentity_entity_id "
			+ "INNER JOIN Contact ON contact_id=contactentity_contact_id "
			+ "INNER JOIN Email ON email_entity_id=contactentity_entity_id "
			+ "WHERE eventlink_event_id IN (%s) "
			+ "AND email_label='INTERNET;X-OBM-Ref1'", EXTERNAL_ATTENDEE_FIELDS, idsHelper.asPlaceHolders());
		return query;
	}

	private String buildResourceAttendeesQuery(EventObmIdSQLCollectionHelper idsHelper) {
		String query = String.format("SELECT %s "
			+ "FROM EventLink att "
			+ "INNER JOIN ResourceEntity ON att.eventlink_entity_id=resourceentity_entity_id "
			+ "INNER JOIN Resource ON resourceentity_resource_id=resource_id "
			+ "WHERE eventlink_event_id IN (%s) ", RESOURCE_ATTENDEE_FIELDS, idsHelper.asPlaceHolders());
		return query;
	}
	
	private void setParameters(PreparedStatement stat, EventObmIdSQLCollectionHelper idsHelper)
			throws SQLException {
		int pos = 1;
		for (int i = 0 ; i < ENTITY_TYPE_COUNT ; i++) {
			pos = idsHelper.insertValues(stat, pos);
		}
	}
	
	private Multimap<EventObmId, Attendee> buildAttendees(ResultSet rs) throws SQLException {
		Multimap<EventObmId, Attendee> attendeesByEventId = ArrayListMultimap.create();
		while (rs.next()) {
			Attendee att = buildAttendee(rs);
			EventObmId eventId = buildEventId(rs);
			appendAttendeeToEvent(eventId, att);
			attendeesByEventId.put(eventId, att);
		}
		return attendeesByEventId;
	}

	private Attendee buildAttendee(ResultSet rs) throws SQLException {
		Attendee att = createAttendeeFromType(AttendeeType.valueOf(rs.getString("attendee_type")));
		
		att.setEntityId(getAttendeeEntityId(rs));
		att.setDisplayName(getAttendeeDisplayName(rs));
		att.setEmail(getAttendeeEmail(rs, domainName));
		att.setParticipation(getAttendeeState(rs));
		att.setParticipationRole(getAttendeeRequired(rs));
		att.setPercent(getAttendeePercent(rs));
		att.setOrganizer(getAttendeeOrganizer(rs));
		
		return att;
	}

	private Attendee createAttendeeFromType(AttendeeType type) {
		switch (type) {
			case USER:
				return new UserAttendee();
			case CONTACT:
				return new ContactAttendee();
			case RESOURCE:
				return new ResourceAttendee();
		}
		
		throw new IllegalArgumentException("Couldn't instantiate attendee of type " + type + ".");
	}

	private EventObmId buildEventId(ResultSet rs) throws SQLException {
		return new EventObmId(rs.getInt("eventlink_event_id"));
	}

	private void appendAttendeeToEvent(EventObmId eventId, Attendee att) {
		Event event = eventsById.get(eventId);
		if (event == null) {
			throw new IllegalStateException(String.format(
					"Found an event %d not present in the parent events", eventId.getObmId()));
		}
		event.getAttendees().add(att);
	}

	private String getAttendeeDisplayName(ResultSet rs) throws SQLException {
		String first = rs.getString("attendee_firstname");
		String last = rs.getString("attendee_lastname");
		String common = rs.getString("attendee_commonname");
		return DisplayNameUtils.getDisplayName(common, first, last);
	}

	private String getAttendeeEmail(ResultSet rs, String domainName) throws SQLException {
		return MailUtils.extractFirstEmail(rs.getString("attendee_email"), domainName);
	}

	private ParticipationRole getAttendeeRequired(ResultSet rs) throws SQLException {
		return ParticipationRole.valueOf(rs.getString("eventlink_required"));
	}

	private Participation getAttendeeState(ResultSet rs) throws SQLException {
		return Participation.builder()
							.state(State.getValueOf(rs.getString("eventlink_state")))
							.comment(rs.getString("eventlink_comment"))
							.build();
	}

	private int getAttendeePercent(ResultSet rs) throws SQLException {
		return rs.getInt("eventlink_percent");
	}

	private boolean getAttendeeOrganizer(ResultSet rs) throws SQLException {
		return rs.getBoolean("eventlink_is_organizer");
	}
	
	private int getAttendeeEntityId(ResultSet rs) throws SQLException {
		return rs.getInt("attendee_entity_id");
	}
}