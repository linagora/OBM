package fr.aliacom.obm.common.calendar.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.utils.DBUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import fr.aliacom.obm.common.calendar.EventUtils;
import fr.aliacom.obm.utils.EventObmIdSQLCollectionHelper;

public class EventExceptionLoader {
	public static class Builder {
		private Connection conn;
		private String domainName;
		private Calendar cal;
		private final Map<EventObmId, Event> parentEventsById;
		private EventObmId withAlertsFor;

		public Builder() {
			this.conn = null;
			this.domainName = null;
			this.cal = null;
			this.parentEventsById = Maps.newHashMap();
			this.withAlertsFor = null;
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder domainName(String domainName) {
			this.domainName = domainName;
			return this;
		}

		public Builder calendar(Calendar cal) {
			this.cal = cal;
			return this;
		}

		public Builder parentEventsById(Map<EventObmId, Event> parentEventsById) {
			Preconditions.checkNotNull(parentEventsById);
			this.parentEventsById.putAll(parentEventsById);
			return this;
		}

		public Builder withAlertsFor(EventObmId withAlertsFor) {
			this.withAlertsFor = withAlertsFor;
			return this;
		}

		public EventExceptionLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(domainName != null, "The domain name is mandatory");
			Preconditions.checkState(cal != null, "The calendar parameter is mandatory");
			Preconditions.checkState(!parentEventsById.isEmpty(),
					"There should be at least one parent id");
			return new EventExceptionLoader(conn, domainName, cal, parentEventsById, withAlertsFor);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private static final String EVENT_FIELDS = Joiner
			.on(", ")
			.join(new String[] {
					"e.event_id",
					"e.event_timeupdate",
					"e.event_timecreate",
					"e.event_type",
					"e.event_ext_id",
					"e.event_timezone",
					"e.event_opacity",
					"e.event_title",
					"e.event_location",
					"ec1.eventcategory1_label",
					"e.event_priority",
					"e.event_privacy",
					"e.event_date",
					"e.event_duration",
					"e.event_allday",
					"e.event_repeatkind",
					"e.event_repeatfrequence",
					"e.event_repeatdays",
					"e.event_endrepeat",
					"e.event_color",
					"e.event_completed",
					"e.event_url",
					"e.event_description",
					"e.event_domain_id",
					"e.event_sequence",
					"ev_ex.eventexception_date AS recurrence_id",
					"ee.evententity_entity_id",
					"o.userobm_login AS owner",
					"d.domain_name",
					"o.userobm_firstname AS ownerFirstName",
					"o.userobm_lastname AS ownerLastName",
					"o.userobm_commonname AS ownerCommonName",
					"o.userobm_email",
					"c.userobm_firstname AS creatorFirstName",
					"c.userobm_lastname AS creatorLastName",
					"c.userobm_commonname AS creatorCommonName",
					"c.userobm_email AS creatorEmail",
					"ev_ex.eventexception_parent_id AS parent_id" });

	private final Connection conn;
	private final String domainName;
	private final Calendar cal;
	private final Map<EventObmId, Event> parentEventsById;
	private final EventObmId withAlertsFor;

	public EventExceptionLoader(Connection conn, String domainName, Calendar cal,
			Map<EventObmId, Event> parentEventsById, EventObmId withAlertsFor) {
		this.conn = conn;
		this.domainName = domainName;
		this.cal = cal;
		this.parentEventsById = parentEventsById;
		this.withAlertsFor = withAlertsFor;
	}

	private String buildQuery(EventObmIdSQLCollectionHelper idsHelper) {
		return String.format("SELECT %s " 
				+ "FROM Event e "
				+ "LEFT JOIN EventCategory1 ec1 ON e.event_category1_id=ec1.eventcategory1_id "
				+ "INNER JOIN Domain d ON e.event_domain_id=d.domain_id "
				+ "INNER JOIN EventEntity ee ON ee.evententity_event_id=e.event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id "
				+ "INNER JOIN EventException ev_ex ON e.event_id = ev_ex.eventexception_child_id "
				+ "WHERE eventexception_parent_id IN (%s)", EVENT_FIELDS,
				idsHelper.asPlaceHolders());
	}

	public Map<EventObmId, Event> load() throws SQLException {
		EventObmIdSQLCollectionHelper idsHelper = new EventObmIdSQLCollectionHelper(
				parentEventsById.keySet());
		String query = buildQuery(idsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper);
			rs = stat.executeQuery();
			return buildEventExceptions(rs);
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private void setParameters(PreparedStatement stat, EventObmIdSQLCollectionHelper idsHelper)
			throws SQLException {
		idsHelper.insertValues(stat, 1);
	}

	private Map<EventObmId, Event> buildEventExceptions(ResultSet rs) throws SQLException {
		Map<EventObmId, Event> eventExceptionsByEventId = Maps.newHashMap();
		EventBuilder eventBuilder = new EventBuilder(cal);
		while (rs.next()) {
			Event eventException = eventBuilder.buildFromResultSet(rs);
			if (eventException == null) {
				continue;
			}

			EventObmId parentEventId = buildParentEventId(rs);
			eventExceptionsByEventId.put(eventException.getObmId(), eventException);
			addEventExceptionToEvent(parentEventId, eventException);
		}
		loadObjectGraph(eventExceptionsByEventId);
		computeIsInternal(eventExceptionsByEventId);
		return eventExceptionsByEventId;
	}

	private void loadObjectGraph(Map<EventObmId, Event> eventExceptionsByEventId) throws SQLException {
		if (eventExceptionsByEventId.isEmpty())
			return;
		this.loadAttendees(eventExceptionsByEventId);
		if (this.withAlertsFor != null) {
			this.loadAlerts(eventExceptionsByEventId);
		}
	}

	private void computeIsInternal(Map<EventObmId, Event> eventsById) {
		for (Event event : eventsById.values()) {
			event.setInternalEvent(EventUtils.isInternalEvent(event));
		}
	}

	private EventObmId buildParentEventId(ResultSet rs) throws SQLException {
		return new EventObmId(rs.getInt("parent_id"));
	}

	private void addEventExceptionToEvent(EventObmId eventId, Event eventException) {
		Event e = parentEventsById.get(eventId);
		if (e == null) {
			throw new IllegalStateException(String.format(
					"Found an event %d not present in the parent events", eventId.getObmId()));
		}
		EventRecurrence er = e.getRecurrence();
		er.addEventException(eventException);
	}

	private void loadAttendees(Map<EventObmId, Event> eventsById) throws SQLException {
		AttendeeLoader attendeeLoader = AttendeeLoader.builder().connection(conn)
				.domainName(domainName).eventsById(eventsById).build();
		attendeeLoader.load();
	}

	private void loadAlerts(Map<EventObmId, Event> eventExceptionsById) throws SQLException {
		AlertLoader loader = AlertLoader.builder().connection(conn).eventsById(eventExceptionsById).build();
		loader.load();
	}
}
