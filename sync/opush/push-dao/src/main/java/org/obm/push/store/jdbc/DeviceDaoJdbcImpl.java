package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.Device;
import org.obm.push.bean.Device.Factory;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.JDBCUtils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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

	public Device getDevice(String loginAtDomain, String deviceId, String userAgent) 
			throws SQLException {
		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		con = dbcp.getConnection();
		try {
			ps = con.prepareStatement("SELECT id, identifier, type FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE identifier=? AND lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, login);
			ps.setString(3, domain);
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer databaseId = rs.getInt("id");
				String devId = rs.getString("identifier");
				String devType = rs.getString("type");
				
				return deviceFactory.create(databaseId, devType, userAgent, devId);
			}
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	public boolean registerNewDevice(String loginAtDomain, String deviceId,
			String deviceType) throws SQLException {
		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		con = dbcp.getConnection();
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_device (identifier, type, owner) "
					+ "SELECT ?, ?, userobm_id FROM UserObm "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, deviceType);
			ps.setString(3, login);
			ps.setString(4, domain);
			return ps.executeUpdate() != 0;
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	public boolean syncAuthorized(String loginAtDomain, String deviceId) {
		IniFile ini = new IniFile("/etc/opush/sync_perms.ini") {
			@Override
			public String getCategory() {
				return null;
			}
		};

		if (userIsBlacklisted(loginAtDomain, ini)) {
			return false;
		}

		String syncperm = ini.getData().get("allow.unknown.pda");

		if ("true".equals(syncperm)) {
			return true;
		}

		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();
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
			ps.setString(2, login);
			ps.setString(3, domain);

			rs = ps.executeQuery();
			if (rs.next()) {
				hasSyncPerm = true;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if (!hasSyncPerm) {
			logger.info(loginAtDomain
					+ " isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}

	private boolean userIsBlacklisted(String loginAtDomain, IniFile ini) {
		String blacklist = Strings.nullToEmpty(ini.getData().get(
				"blacklist.users"));
		Iterable<String> users = Splitter.on(',').trimResults()
				.split(blacklist);
		for (String user : users) {
			if (user.equalsIgnoreCase(loginAtDomain)) {
				return true;
			}
		}
		return false;
	}

}
