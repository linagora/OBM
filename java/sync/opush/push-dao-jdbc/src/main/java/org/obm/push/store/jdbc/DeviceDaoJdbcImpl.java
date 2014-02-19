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
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.BreakdownGroups;
import org.obm.push.bean.Device;
import org.obm.push.bean.Device.Factory;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Watch(BreakdownGroups.SQL)
public class DeviceDaoJdbcImpl extends AbstractJdbcImpl implements DeviceDao {
	
	private final Factory deviceFactory;

	@Inject
	/* allow cglib proxy */ DeviceDaoJdbcImpl(DatabaseConnectionProvider dbcp, Device.Factory deviceFactory) {
		super(dbcp);
		this.deviceFactory = deviceFactory;
	}

	@Override
	public Device getDevice(User user, DeviceId deviceId, String userAgent, ProtocolVersion protocolVersion) 
			throws DaoException {
	
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT id, identifier, type FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE identifier=? AND userobm_login=? AND domain_name=?");
			ps.setString(1, deviceId.getDeviceId());
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getDomain());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer databaseId = rs.getInt("id");
				String devId = rs.getString("identifier");
				String devType = rs.getString("type");
				
				return deviceFactory.create(databaseId, devType, userAgent, new DeviceId(devId), protocolVersion);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	@Override
	public void registerNewDevice(User user, DeviceId deviceId,
			String deviceType) throws DaoException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_device (identifier, type, owner) "
					+ "SELECT ?, ?, userobm_id FROM UserObm "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE userobm_login=? AND domain_name=?");
			ps.setString(1, deviceId.getDeviceId());
			ps.setString(2, deviceType);
			ps.setString(3, user.getLogin());
			ps.setString(4, user.getDomain());
			int updateStatus = ps.executeUpdate();
			if (updateStatus == 0) {
				throw new IllegalStateException("unknown user " + user.getLoginAtDomain());
			} else if (updateStatus > 1) {
				throw new IllegalStateException("several user " + user.getLoginAtDomain());
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	@Override
	public boolean syncAuthorized(User user, DeviceId deviceId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT policy FROM opush_sync_perms "
					+ "INNER JOIN UserObm u ON opush_sync_perms.owner=userobm_id "
					+ "INNER JOIN Domain d ON userobm_domain_id=domain_id "
					+ "INNER JOIN opush_device od ON device_id=id "
					+ "WHERE od.identifier=? AND u.userobm_login=? AND d.domain_name=?");
			ps.setString(1, deviceId.getDeviceId());
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getDomain());

			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
		logger.info(user + " isn't authorized to synchronize in OBM-UI");
		return false;
	}

	@Override
	public Long getPolicyKey(User user, DeviceId deviceId, PolicyStatus status) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT policy FROM opush_sync_perms "
					+ "INNER JOIN UserObm ON opush_sync_perms.owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "INNER JOIN opush_device ON device_id=id "
					+ "WHERE identifier=? AND userobm_login=? AND domain_name=? "
					+ "AND policy IS NOT NULL "
					+ "AND pending_accept=?");
			int index = 1;
			ps.setString(index++, deviceId.getDeviceId());
			ps.setString(index++, user.getLogin());
			ps.setString(index++, user.getDomain());
			ps.setBoolean(index++, policyStatusToPendingAccept(status));
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong("policy");
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	protected static boolean policyStatusToPendingAccept(PolicyStatus policyStatus) {
		switch (policyStatus) {
		case ACCEPTED:
			return false;
		case PENDING:
			return true;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public long allocateNewPolicyKey(User user, DeviceId deviceId, PolicyStatus status) throws DaoException {

		long newPolicyKeyId = allocateNewPolicyKey();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_perms (policy, device_id, owner, pending_accept) "
					+ "SELECT ?, id, owner, ? FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE userobm_login=? AND domain_name=? AND identifier=?");

			int index = 1;
			ps.setLong(index++, newPolicyKeyId);
			ps.setBoolean(index++, policyStatusToPendingAccept(status));
			ps.setString(index++, user.getLogin());
			ps.setString(index++, user.getDomain());
			ps.setString(index++, deviceId.getDeviceId());
			int newRowCount = ps.executeUpdate();
			if (newRowCount == 1) {
				return newPolicyKeyId;
			} else {
				throw new DaoException("SyncPerms insertion fails, only one new entry expected but found: " + newRowCount);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}

	private long allocateNewPolicyKey() throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sec_policy (id, device_password_enabled) VALUES(DEFAULT, DEFAULT)");
			if (ps.executeUpdate() != 1) {
				throw new DaoException("Cannot find the new generated id in result set");
			}
			return dbcp.lastInsertId(con);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}

	@Override
	public void removePolicyKey(User user, Device device) throws DaoException {
		Integer deviceDbId = device.getDatabaseId();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("DELETE FROM opush_sec_policy "
					+ "WHERE id IN ( "
						+ "SELECT policy FROM opush_sync_perms "
						+ "INNER JOIN UserObm ON owner=userobm_id "
						+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
						+ "WHERE userobm_login=? AND domain_name=? AND device_id=?);");

			ps.setString(1, user.getLogin());
			ps.setString(2, user.getDomain());
			ps.setInt(3, deviceDbId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}

	@Override
	public void removeUnknownDeviceSyncPerm(User user, Device device) {
		Integer deviceDbId = device.getDatabaseId();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"DELETE FROM opush_sync_perms " +
					"WHERE device_id IN (" +
					"SELECT id FROM opush_device " +
					"INNER JOIN UserObm ON owner=userobm_id " +
					"WHERE device_id = ? AND policy IS NULL)");
			
			ps.setInt(1, deviceDbId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}
}
