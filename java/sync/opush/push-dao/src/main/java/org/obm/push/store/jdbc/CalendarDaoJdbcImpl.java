package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CalendarDao;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.calendar.EventExtId;

import com.google.inject.Inject;

public class CalendarDaoJdbcImpl extends AbstractJdbcImpl implements CalendarDao {

	@Inject
	private CalendarDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		super(dbcp);
	}
	
	@Override
	public EventExtId getEventExtIdFor(MSEventUid msEventUid, Device device) throws DaoException {
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
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
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
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	@Override
	public void insertExtIdMSEventUidMapping(EventExtId eventExtId,
			MSEventUid msEventUid, Device device) throws DaoException {
		
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_event_mapping (device_id, event_ext_id, event_uid) VALUES (?, ?, ?)");
			ps.setInt(1, device.getDatabaseId());
			ps.setString(2, eventExtId.getExtId());
			ps.setString(3, msEventUid.serializeToString());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
}
