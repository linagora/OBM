package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DBCP;
import org.obm.push.bean.Device;
import org.obm.push.exception.DaoException;
import org.obm.push.store.HearbeatDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HearbeatDaoJdbcDaoImpl extends AbstractJdbcImpl implements HearbeatDao{

	@Inject
	private HearbeatDaoJdbcDaoImpl(DBCP dbcp) {
		super(dbcp);
	}

	@Override
	public long findLastHearbeat(Device device) throws DaoException {
		final Integer devDbId = device.getDatabaseId();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT last_heartbeat FROM opush_ping_heartbeat WHERE device_id=?");
			ps.setInt(1, devDbId);

			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong("last_heartbeat");
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return 0L;
	}

	@Override
	public void updateLastHearbeat(Device device, long hearbeat) throws DaoException {
		final Integer devDbId = device.getDatabaseId();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("DELETE FROM opush_ping_heartbeat WHERE device_id=? ");
			ps.setInt(1, devDbId);
			ps.executeUpdate();

			ps.close();
			ps = con.prepareStatement("INSERT INTO opush_ping_heartbeat (device_id, last_heartbeat) VALUES (?, ?)");
			ps.setInt(1, devDbId);
			ps.setLong(2, hearbeat);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
}
