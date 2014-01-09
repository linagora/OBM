package fr.aliacom.obm.common.calendar.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.SyncRange;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.calendar.EventUtils;
import fr.aliacom.obm.common.calendar.loader.filter.DeclinedAttendeeFilter;
import fr.aliacom.obm.common.calendar.loader.filter.EventFilter;
import fr.aliacom.obm.common.calendar.loader.filter.EventsByIdFilter;
import fr.aliacom.obm.utils.DBUtils;
import fr.aliacom.obm.utils.EventObmIdSQLCollectionHelper;

public class EventLoader {

	public static class Builder {
		private Connection conn;
		private String domainName;
		private Calendar cal;
		private Set<EventObmId> ids;
		private Set<Integer> usingResources;
		private EventObmId withAlertsFor;
		private boolean withExceptions;
		private boolean withoutMovedExceptions;
		private SyncRange occurringBetween;
		private Date updatedAfter;
		private Date updatedOrOccuringAfter;
		private Attendee withoutDeclinedAttendee;

		private Builder() {
			this.conn = null;
			this.domainName = null;
			this.cal = null;
			this.ids = Sets.newHashSet();
			this.usingResources = Sets.newHashSet();
			this.withAlertsFor = null;
			this.withExceptions = false;
			this.occurringBetween = null;
			this.updatedAfter = null;
			this.updatedOrOccuringAfter = null;
			this.withoutDeclinedAttendee = null;
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

		public Builder withAlertsFor(EventObmId withAlertsFor) {
			this.withAlertsFor = withAlertsFor;
			return this;
		}

		public Builder withExceptions(boolean withExceptions) {
			this.withExceptions = withExceptions;
			return this;
		}

		public Builder withoutMovedExceptions(boolean withoutMovedExceptions) {
			this.withoutMovedExceptions = withoutMovedExceptions;
			return this;
		}

		// Excludes events when the attendees has a status of declined in the event and ALL of its exceptions
		public Builder withoutDeclinedAttendee(Attendee withoutDeclinedAttendee) {
			this.withoutDeclinedAttendee = withoutDeclinedAttendee;
			return this;
		}

		public Builder ids(EventObmId... ids) {
			for (EventObmId id : ids) {
				this.ids.add(id);
			}
			return this;
		}

		public Builder ids(Collection<EventObmId> ids) {
			this.ids.addAll(ids);
			return this;
		}

		public Builder usingResources(int... usingResources) {
			for (int resourceId : usingResources) {
				this.usingResources.add(resourceId);
			}
			return this;
		}

		public Builder usingResources(Collection<Integer> usingResources) {
			Preconditions.checkNotNull(usingResources);
			this.usingResources.addAll(usingResources);
			return this;
		}
		
		public Builder updatedAfter(Date updatedAfter) {
			this.updatedAfter = updatedAfter;
			return this;
		}

		public Builder updatedOrOccuringAfter(Date updatedOrOccuringAfter) {
			this.updatedOrOccuringAfter = updatedOrOccuringAfter;
			return this;
		}

		public Builder occurringBetween(SyncRange occurringBetween) {
			this.occurringBetween = occurringBetween;
			return this;
		}

		public EventLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(domainName != null, "The domain name parameter is mandatory");
			Preconditions.checkState(cal != null, "The calendar parameter is mandatory");
			Preconditions.checkState((updatedAfter != null || updatedOrOccuringAfter != null
					|| !ids.isEmpty() || !usingResources.isEmpty()),
					"At least one filtering condition is needed");
			Preconditions
					.checkState((updatedAfter == null || updatedOrOccuringAfter == null),
							"The updatedAfter and updatedOrOccurringAfter arguments are mutually exclusive");

			List<EventFilter> filters = Lists.newArrayList();
			if (this.withoutDeclinedAttendee != null) {
				filters.add(new DeclinedAttendeeFilter(this.withoutDeclinedAttendee));
			}
			return new EventLoader(conn, domainName, cal, ids, updatedAfter,
					updatedOrOccuringAfter, occurringBetween, usingResources, withExceptions, withoutMovedExceptions,
					withAlertsFor, filters);
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
					"NULL AS recurrence_id",
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
					"c.userobm_email AS creatorEmail" });
	private Connection conn;
	private String domainName;
	private Calendar cal;
	private Set<EventObmId> ids;
	private Date updatedAfter;
	private Date updatedOrOccuringAfter;
	private SyncRange occurringBetween;
	private Set<Integer> usingResources;
	private boolean withExceptions;
	private boolean withoutMovedExceptions;
	private EventObmId withAlertsFor;
	private List<EventFilter> eventFilters;

	private EventLoader(Connection conn, String domainName, Calendar cal, Set<EventObmId> ids,
			Date updatedAfter, Date updatedOrOccuringAfter, SyncRange occurringBetween,
			Set<Integer> usingResources, boolean withExceptions, boolean withoutMovedExceptions, EventObmId withAlertsFor,
			List<EventFilter> eventFilters) {
		this.conn = conn;
		this.domainName = domainName;
		this.cal = cal;
		this.ids = ids;
		this.updatedAfter = updatedAfter;
		this.updatedOrOccuringAfter = updatedOrOccuringAfter;
		this.occurringBetween = occurringBetween;
		this.usingResources = usingResources;
		this.withExceptions = withExceptions;
		this.withoutMovedExceptions = withoutMovedExceptions;
		this.withAlertsFor = withAlertsFor;
		this.eventFilters = eventFilters;
	}

	public Map<EventObmId, Event> load() throws SQLException {
		EventObmIdSQLCollectionHelper idsHelper = !this.ids.isEmpty() ? new EventObmIdSQLCollectionHelper(
				ids) : null;
		IntegerSQLCollectionHelper resourcesHelper = !this.usingResources.isEmpty() ? new IntegerSQLCollectionHelper(
				usingResources) : null;
		String query = buildQuery(idsHelper, resourcesHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper, resourcesHelper);
			rs = stat.executeQuery();
			Map<EventObmId, Event> eventsById = buildEvents(rs);
			loadObjectGraph(eventsById);
			addPostBuildFilters(eventsById);
			Map<EventObmId, Event> filteredEventsById = filterEvents(eventsById);
			computeIsInternal(filteredEventsById);
			return filteredEventsById;
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private void addPostBuildFilters(Map<EventObmId, Event> eventsById) throws SQLException {
		if (withoutMovedExceptions) {
			Set<Integer> idsToFilter = getExistingEventExceptionChildIds(eventsById);
			this.eventFilters.add(new EventsByIdFilter(idsToFilter));
		}
	}

	private Set<Integer> getExistingEventExceptionChildIds(Map<EventObmId, Event> eventsById) throws SQLException {
		ResultSet rs = null;
		PreparedStatement st = null;
		EventObmIdSQLCollectionHelper eventIds = new EventObmIdSQLCollectionHelper(eventsById.keySet());

		try {
			st = conn.prepareStatement("SELECT eventexception_child_id FROM EventException WHERE eventexception_child_id IN (" + eventIds.asPlaceHolders() + ")");
			eventIds.insertValues(st, 1);
			rs = st.executeQuery();
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			while(rs.next()) {
				builder.add(rs.getInt("eventexception_child_id"));
			}
			return builder.build();
		} catch (SQLException ex) {
			throw ex;
		} finally {
			DBUtils.cleanup(null, st, rs);
		}
	}

	private Map<EventObmId, Event> filterEvents(Map<EventObmId, Event> events) {
		Map<EventObmId, Event> filteredEvents = events;
		for (EventFilter filter : eventFilters) {
			filteredEvents = filter.filter(filteredEvents);
		}
		return filteredEvents;
	}

	private void computeIsInternal(Map<EventObmId, Event> eventsById) {
		for (Event event : eventsById.values()) {
			event.setInternalEvent(EventUtils.isInternalEvent(event));
		}
	}

	private void loadObjectGraph(Map<EventObmId, Event> eventsById) throws SQLException {
		if (eventsById.isEmpty()) {
			return;
		}
		this.loadAttendees(eventsById);
		if (this.withExceptions) {
			this.loadExceptions(eventsById);
			this.loadEventExceptions(eventsById);
		}
		if (this.withAlertsFor != null) {
			this.loadAlerts(eventsById);
		}
	}

	private void loadAttendees(Map<EventObmId, Event> eventsById) throws SQLException {
		AttendeeLoader attendeeLoader = AttendeeLoader.builder().connection(conn)
				.domainName(domainName).eventsById(eventsById).build();
		attendeeLoader.load();
	}

	private void loadExceptions(Map<EventObmId, Event> eventsById) throws SQLException {
		if (!withExceptions) {
			return;
		}
		ExceptionLoader exceptionLoader = ExceptionLoader.builder().connection(conn).calendar(cal)
				.parentEventsById(eventsById).build();
		exceptionLoader.load();
	}

	private void loadEventExceptions(Map<EventObmId, Event> eventsById) throws SQLException {
		EventExceptionLoader.Builder builder = EventExceptionLoader.builder().connection(conn).
				domainName(domainName).calendar(cal).parentEventsById(eventsById);
		if (this.withAlertsFor != null) {
			builder = builder.withAlertsFor(withAlertsFor);
		}
		EventExceptionLoader loader = builder.build();
		loader.load();
	}

	private void loadAlerts(Map<EventObmId, Event> eventsById) throws SQLException {
		if (this.withAlertsFor == null) {
			return;
		}
		AlertLoader alertLoader = AlertLoader.builder().connection(conn).eventsById(eventsById)
				.build();
		alertLoader.load();
	}

	private Map<EventObmId, Event> buildEvents(ResultSet rs) throws SQLException {
		Map<EventObmId, Event> events = Maps.newHashMap();
		EventBuilder eventBuilder = new EventBuilder(cal);
		while (rs.next()) {
			Event event = eventBuilder.buildFromResultSet(rs);
			events.put(event.getObmId(), event);
		}
		return events;
	}

	private String buildQuery(EventObmIdSQLCollectionHelper idsHelper, IntegerSQLCollectionHelper resourcesHelper) {
		String eventQuery = String.format("SELECT DISTINCT %s " + "FROM Event e "
				+ "LEFT JOIN EventCategory1 ec1 ON e.event_category1_id=eventcategory1_id "
				+ "INNER JOIN Domain d ON event_domain_id=d.domain_id "
				+ "INNER JOIN EventEntity ee ON ee.evententity_event_id=event_id "
				+ "INNER JOIN UserObm o ON e.event_owner=o.userobm_id "
				+ "INNER JOIN UserObm c ON e.event_usercreate=c.userobm_id ",
				EVENT_FIELDS
		);
		if (resourcesHelper != null) {
			eventQuery += "INNER JOIN EventLink att ON e.event_id=att.eventlink_event_id "
				+ "INNER JOIN ResourceEntity re ON att.eventlink_entity_id=re.resourceentity_entity_id ";
		}
		List<String> parentEventfilters = buildParentEventFilters(idsHelper, resourcesHelper);
		eventQuery += filtersToWhere(parentEventfilters);
		return eventQuery;
	}

	private List<String> buildParentEventFilters(EventObmIdSQLCollectionHelper idsHelper, IntegerSQLCollectionHelper resourcesHelper) {
		List<String> filters = buildFiltersFromCriteria(idsHelper, resourcesHelper);
		return filters;
	}

	private List<String> buildFiltersFromCriteria(EventObmIdSQLCollectionHelper idsHelper, IntegerSQLCollectionHelper resourcesHelper) {
		List<String> filters = Lists.newArrayList();
		if (idsHelper != null) {
			filters.add(String.format("e.event_id IN (%s)", idsHelper.asPlaceHolders()));
		}
		if (updatedAfter != null) {
			filters.add("(e.event_timeupdate >= ? OR attupd.eventlink_timeupdate >= ?)");
		} else if (updatedOrOccuringAfter != null) {
			filters.add("(e.event_timeupdate >= ? OR attupd.eventlink_timeupdate >= ? "
					+ "OR e.event_date >= ? OR e.event_repeatkind != 'none')");
		}
		if (occurringBetween != null) {
			List<String> subCriteria = Lists.newArrayListWithCapacity(2);
			if (occurringBetween.getAfter() != null) {
				subCriteria.add("e.event_date >= ?");
			}
			if (occurringBetween.getBefore() != null) {
				subCriteria.add("e.event_date <= ?");
			}
			filters.add(String.format("((%s) OR e.event_repeatkind != 'none')", Joiner
					.on(" AND ").join(subCriteria)));
		}
		if (resourcesHelper != null) {
			filters.add(String.format("re.resourceentity_resource_id IN (%s)", resourcesHelper.asPlaceHolders()));
		}
		return filters;
	}

	private String filtersToWhere(List<String> filters) {
		return String.format("WHERE %s", Joiner.on(" AND ").join(filters));
	}

	private Timestamp buildTimestamp(Date date) {
		return new Timestamp(date.getTime());
	}

	private void setParameters(PreparedStatement stat, EventObmIdSQLCollectionHelper idsHelper, IntegerSQLCollectionHelper resourcesHelper)
			throws SQLException {
		int pos = 1;
		pos = setSubQueryParameters(stat, pos, idsHelper, resourcesHelper);
	}
	
	private int setSubQueryParameters(PreparedStatement stat, int pos,
			EventObmIdSQLCollectionHelper idsHelper, IntegerSQLCollectionHelper resourcesHelper)
			throws SQLException {
		if (idsHelper != null) {
			pos = idsHelper.insertValues(stat, pos);
		}
		if (updatedAfter != null) {
			stat.setTimestamp(pos++, buildTimestamp(updatedAfter));
			stat.setTimestamp(pos++, buildTimestamp(updatedAfter));
		}
		if (updatedOrOccuringAfter != null) {
			stat.setTimestamp(pos++, buildTimestamp(updatedOrOccuringAfter));
			stat.setTimestamp(pos++, buildTimestamp(updatedOrOccuringAfter));
			stat.setTimestamp(pos++, buildTimestamp(updatedOrOccuringAfter));
		}
		if (occurringBetween != null) {
			if (occurringBetween.getAfter() != null) {
				stat.setTimestamp(pos++, buildTimestamp(occurringBetween.getAfter()));
			}
			if (occurringBetween.getBefore() != null) {
				stat.setTimestamp(pos++, buildTimestamp(occurringBetween.getBefore()));
			}
		}
		if (resourcesHelper != null) {
			pos = resourcesHelper.insertValues(stat, pos);
		}
		return pos;
	}
}
