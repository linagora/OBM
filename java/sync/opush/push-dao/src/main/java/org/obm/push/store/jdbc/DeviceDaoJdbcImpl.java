package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.Device;
import org.obm.push.bean.Device.Factory;
import org.obm.push.bean.LoginAtDomain;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeviceDaoJdbcImpl extends AbstractJdbcImpl implements DeviceDao {

	private Factory deviceFactory;

	@Inject
	private DeviceDaoJdbcImpl(IDBCP dbcp, Device.Factory deviceFactory) {
		super(dbcp);
		this.deviceFactory = deviceFactory;
	}

	@Override
	public Device getDevice(LoginAtDomain loginAtDomain, String deviceId, String userAgent) 
			throws DaoException {
	
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT id, identifier, type FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE identifier=? AND lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, loginAtDomain.getLogin());
			ps.setString(3, loginAtDomain.getDomain());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer databaseId = rs.getInt("id");
				String devId = rs.getString("identifier");
				String devType = rs.getString("type");
				
				return deviceFactory.create(databaseId, devType, userAgent, devId);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	public boolean registerNewDevice(LoginAtDomain loginAtDomain, String deviceId,
			String deviceType) throws DaoException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_device (identifier, type, owner) "
					+ "SELECT ?, ?, userobm_id FROM UserObm "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, deviceType);
			ps.setString(3, loginAtDomain.getLogin());
			ps.setString(4, loginAtDomain.getDomain());
			return ps.executeUpdate() != 0;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	public boolean syncAuthorized(LoginAtDomain loginAtDomain, String deviceId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean hasSyncPerm = false;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT policy FROM opush_sync_perms "
					+ "INNER JOIN UserObm u ON owner=userobm_id "
					+ "INNER JOIN Domain d ON userobm_domain_id=domain_id "
					+ "INNER JOIN opush_device od ON device_id=id "
					+ "WHERE od.identifier=? AND lower(u.userobm_login)=? AND lower(d.domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, loginAtDomain.getLogin());
			ps.setString(3, loginAtDomain.getDomain());

			rs = ps.executeQuery();
			if (rs.next()) {
				hasSyncPerm = true;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if (!hasSyncPerm) {
			logger.info(loginAtDomain
					+ " isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}
}
