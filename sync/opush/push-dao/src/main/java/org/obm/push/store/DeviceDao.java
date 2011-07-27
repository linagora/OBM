package org.obm.push.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.IDBCP;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;

public class DeviceDao {

	private final IDBCP dbcp;

	@Inject
	private DeviceDao(IDBCP dbcp) {
		this.dbcp = dbcp;
	}
	
	public Integer findDevice(String loginAtDomain, String deviceId) throws SQLException {
		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();

		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		con = dbcp.getConnection();
		try {
			ps = con.prepareStatement("SELECT id FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE identifier=? AND lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, login);
			ps.setString(3, domain);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	public boolean registerNewDevice(String loginAtDomain, String deviceId, String deviceType) throws SQLException {
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
	
}
