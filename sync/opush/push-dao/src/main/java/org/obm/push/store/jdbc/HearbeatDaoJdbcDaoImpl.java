package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DBCP;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.HearbeatDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HearbeatDaoJdbcDaoImpl extends AbstractJdbcImpl implements HearbeatDao{

	private final DeviceDao deviceDao;
	
	@Inject
	private HearbeatDaoJdbcDaoImpl(DBCP dbcp, DeviceDao deviceDao) {
		super(dbcp);
		this.deviceDao = deviceDao;
	}

	@Override
	public long findLastHearbeat(String loginAtDomain, String devId) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT last_heartbeat FROM opush_ping_heartbeat WHERE device_id=?");
			ps.setInt(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong("last_heartbeat");
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return 0L;
	}

	@Override
	public void updateLastHearbeat(String loginAtDomain, String devId, long hearbeat) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("DELETE FROM opush_ping_heartbeat WHERE device_id=? ");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps.close();
			ps = con.prepareStatement("INSERT INTO opush_ping_heartbeat (device_id, last_heartbeat) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setLong(2, hearbeat);
			ps.executeUpdate();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
}
