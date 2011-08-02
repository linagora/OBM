package org.obm.push.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.store.jdbc.DeviceDaoJdbcImpl;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Store device infos, id mappings & last sync dates into OBM database
 */
@Singleton
public class SyncStorage implements ISyncStorage {

	private static final Logger logger = LoggerFactory
			.getLogger(SyncStorage.class);

	private final IDBCP dbcp;

	private final DeviceDaoJdbcImpl deviceDao;

	@Inject
	private SyncStorage(IDBCP dbcp, DeviceDaoJdbcImpl dao) {
		this.dbcp = dbcp;
		this.deviceDao = dao;
	}

	@Override
	public boolean initDevice(String loginAtDomain, String deviceId,
			String deviceType) {

		boolean ret = true;
		try {
			Integer opushDeviceId = deviceDao.findDevice(loginAtDomain,
					deviceId);
			if (opushDeviceId == null) {
				boolean registered = deviceDao.registerNewDevice(loginAtDomain,
						deviceId, deviceType);
				if (!registered) {
					logger.warn("did not insert any row in device table for device "
							+ deviceType + " of " + loginAtDomain);
					ret = false;
				}
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			ret = false;
		}
		return ret;
	}

	@Override
	public PIMDataType getDataClass(String collectionPath) {
		if (collectionPath.contains("\\calendar\\")) {
			return PIMDataType.CALENDAR;
		} else if (collectionPath.contains("\\contacts")) {
			return PIMDataType.CONTACTS;
		} else if (collectionPath.contains("\\email\\")) {
			return PIMDataType.EMAIL;
		} else if (collectionPath.contains("\\tasks\\")) {
			return PIMDataType.TASKS;
		} else {
			return PIMDataType.FOLDER;
		}
	}

	@Override
	public Long findLastHearbeat(String loginAtDomain, String devId) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
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
		return null;
	}

	@Override
	public void updateLastHearbeat(String loginAtDomain, String devId,
			long hearbeat) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
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
