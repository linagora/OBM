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
import java.util.Map;

import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.utils.DBUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class AlertLoader {
	public static class Builder {
		private Connection conn;
		private Map<EventObmId, Event> eventsById;
		private EventObmId userId;

		private Builder() {
			this.conn = null;
			this.userId = null;
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder eventsById(Map<EventObmId, Event> eventsById) {
			this.eventsById = eventsById;
			return this;
		}

		public Builder userId(EventObmId userId) {
			this.userId = userId;
			return this;
		}

		public AlertLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(userId != null, "The userId parameter is mandatory");
			Preconditions.checkState(!eventsById.isEmpty(),
					"There should be at least one event present");
			return new AlertLoader(conn, eventsById, userId);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final Connection conn;
	private final Map<EventObmId, Event> eventsById;
	private final EventObmId userId;

	private AlertLoader(Connection conn, Map<EventObmId, Event> eventsById, EventObmId userId) {
		this.conn = conn;
		this.eventsById = eventsById;
		this.userId = userId;
	}

	public Map<EventObmId, Integer> load() throws SQLException {
		EventObmIdSQLCollectionHelper idsHelper = new EventObmIdSQLCollectionHelper(
				eventsById.keySet());
		String query = buildQuery(idsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper);
			rs = stat.executeQuery();
			return buildAlerts(rs);
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private String buildQuery(EventObmIdSQLCollectionHelper idsHelper) {
		return String.format("SELECT eventalert_event_id, eventalert_duration "
				+ "FROM EventAlert " 
				+ "WHERE eventalert_event_id IN (%s) "
				+ "AND eventalert_user_id = ?", idsHelper.asPlaceHolders());
	}

	private void setParameters(PreparedStatement stat, EventObmIdSQLCollectionHelper idsHelper)
			throws SQLException {
		int pos = 1;
		pos = idsHelper.insertValues(stat, pos);
		stat.setInt(pos++, userId.getObmId());
	}
	
	private Map<EventObmId, Integer> buildAlerts(ResultSet rs) throws SQLException {
		Map<EventObmId, Integer> alertsByEventId = Maps.newHashMap();
		while (rs.next()) {
			Integer alertDuration = buildAlert(rs);
			if (alertDuration != null) {
				EventObmId eventId = buildEventId(rs);
				alertsByEventId.put(eventId, alertDuration);
				addAlertToEvent(eventId, alertDuration);
			}
		}
		return alertsByEventId;
	}
	
	private Integer buildAlert(ResultSet rs) throws SQLException {
		int alertDuration = rs.getInt("eventalert_duration");
		Integer alert;
		if (alertDuration >= 0) {
			alert = Integer.valueOf(alertDuration);
		}
		else {
			alert = null;
		}
		return alert;
	}
	
	private EventObmId buildEventId(ResultSet rs) throws SQLException {
		return new EventObmId(rs.getInt("eventalert_event_id"));
	}
	
	private void addAlertToEvent(EventObmId eventId, Integer alertDuration) {
		Event event = eventsById.get(eventId);
		if (event == null) {
			throw new IllegalStateException(String.format(
					"Found an event %d not present in the parent events", eventId.getObmId()));
		}
		event.setAlert(alertDuration);
	}
}
