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
package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.breakdownduration.bean.Watch;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.BreakdownGroups;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CalendarDao;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.calendar.EventExtId;

import com.google.inject.Inject;

@Watch(BreakdownGroups.SQL)
public class CalendarDaoJdbcImpl extends AbstractJdbcImpl implements CalendarDao {

	@Inject
	/* allow cglib proxy */ CalendarDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		super(dbcp);
	}
	
	@Override
	public EventExtId getEventExtIdFor(MSEventUid msEventUid, Device device) throws DaoException, EventNotFoundException {
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT event_ext_id FROM opush_event_mapping WHERE event_uid=? AND device_id=?");
			ps.setString(1, msEventUid.serializeToString());
			ps.setInt(2, device.getDatabaseId());
			rs = ps.executeQuery();
			if (rs.next()) {
				return new EventExtId(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		String msg = String.format("No ExtId mapping found for Uid:{%s} and Device:{%s}", msEventUid.serializeToString(), device.getDatabaseId());
		throw new EventNotFoundException(msg);
	}
	
	@Override
	public MSEventUid getMSEventUidFor(EventExtId eventExtId, Device device)
			throws DaoException {
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT event_uid FROM opush_event_mapping WHERE event_ext_id=? AND device_id=?");
			ps.setString(1, eventExtId.getExtId());
			ps.setInt(2, device.getDatabaseId());
			rs = ps.executeQuery();
			if (rs.next()) {
				return new MSEventUid(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	@Override
	public void insertExtIdMSEventUidMapping(EventExtId eventExtId,
			MSEventUid msEventUid, Device device, byte[] hashedExtId) throws DaoException {
		
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_event_mapping (device_id, event_ext_id, event_uid, event_ext_id_hash) VALUES (?, ?, ?, ?)");
			ps.setInt(1, device.getDatabaseId());
			ps.setString(2, eventExtId.getExtId());
			ps.setString(3, msEventUid.serializeToString());
			ps.setBytes(4, hashedExtId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
	}
}
