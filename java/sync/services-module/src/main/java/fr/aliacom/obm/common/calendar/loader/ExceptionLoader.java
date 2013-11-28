package fr.aliacom.obm.common.calendar.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.utils.DBUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import fr.aliacom.obm.utils.EventObmIdSQLCollectionHelper;

public class ExceptionLoader {
	public static class Builder {
		private Connection conn;
		private Calendar cal;
		private Map<EventObmId, Event> parentEventsById;

		public Builder() {
			this.conn = null;
			this.cal = null;
			this.parentEventsById = Maps.newHashMap();
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder calendar(Calendar cal) {
			this.cal = cal;
			return this;
		}

		public Builder parentEventsById(Map<EventObmId, Event> parentEventsById) {
			Preconditions.checkNotNull(parentEventsById);
			this.parentEventsById = parentEventsById;
			return this;
		}

		public ExceptionLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(cal != null, "The calendar parameter is mandatory");
			Preconditions.checkState(!parentEventsById.isEmpty(),
					"There should be at least one parent id");
			return new ExceptionLoader(conn, cal, parentEventsById);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final Connection conn;
	private final Calendar cal;
	private final Map<EventObmId, Event> parentEventsById;

	public ExceptionLoader(Connection conn, Calendar cal, Map<EventObmId, Event> parentEventsById) {
		this.conn = conn;
		this.cal = cal;
		this.parentEventsById = parentEventsById;
	}

	private String buildQuery(EventObmIdSQLCollectionHelper idsHelper) {
		return String.format("SELECT e.event_id, ev_ex.eventexception_date "
				+ "FROM Event e "
					+ "LEFT JOIN EventException ev_ex ON e.event_id=ev_ex.eventexception_parent_id "
				+ "WHERE e.event_id IN (%s) "
					+ "AND ev_ex.eventexception_child_id IS NULL", idsHelper.asPlaceHolders());
	}

	public Map<EventObmId, Date> load() throws SQLException {
		EventObmIdSQLCollectionHelper idsHelper = new EventObmIdSQLCollectionHelper(
				parentEventsById.keySet());
		String query = buildQuery(idsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper);
			rs = stat.executeQuery();
			return buildExceptions(rs);
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private void setParameters(PreparedStatement stat, EventObmIdSQLCollectionHelper idsHelper)
			throws SQLException {
		idsHelper.insertValues(stat, 1);
	}

	private Map<EventObmId, Date> buildExceptions(ResultSet rs) throws SQLException {
		Map<EventObmId, Date> exceptionsByEventId = Maps.newHashMap();
		while (rs.next()) {
			Date exception = buildException(rs);
			if (exception != null) {
				EventObmId parentEventId = buildParentEventId(rs);
				exceptionsByEventId.put(parentEventId, exception);
				addExceptionToEvent(parentEventId, exception);
			}
		}
		return exceptionsByEventId;
	}

	private Date buildException(ResultSet rs) throws SQLException {
		Timestamp t = rs.getTimestamp(2);
		Date exception;
		if (t != null) {
			cal.setTimeInMillis(t.getTime());
			exception = cal.getTime();
		}
		else {
			exception = null;
		}
		return exception;
	}

	private EventObmId buildParentEventId(ResultSet rs) throws SQLException {
		return new EventObmId(rs.getInt(1));
	}
	
	private void addExceptionToEvent(EventObmId eventId, Date exception) {
		Event e = parentEventsById.get(eventId);
		if (e == null) {
			throw new IllegalStateException(String.format(
					"Found an event %d not present in the parent events", eventId.getObmId()));
		}
		EventRecurrence er = e.getRecurrence();
		er.addException(exception);
	}
}
