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
package org.obm.domain.dao.loader;

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
