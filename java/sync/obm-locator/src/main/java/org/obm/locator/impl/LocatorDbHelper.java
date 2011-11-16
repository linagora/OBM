package org.obm.locator.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.obm.dbcp.DBCP;
import org.obm.dbcp.IDBCP;
import org.obm.push.utils.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorDbHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(LocatorDbHelper.class);

	private static LocatorDbHelper instance;
	private final IDBCP dbcp;
	
	private LocatorDbHelper(IDBCP dbcp) {
		this.dbcp = dbcp;
	}
	
	public static synchronized LocatorDbHelper getInstance() {
		if (instance == null) {
			instance = new LocatorDbHelper(new DBCP());
		}
		return instance;
	}

	/**
	 * Returns the ips of the hosts with the given service/property in the users
	 * domain.
	 * 
	 * @param loginAtDomain
	 * @param service
	 * @param prop
	 * @return
	 */
	public Set<String> findDomainHost(String loginAtDomain,
			String service, String prop) {
		HashSet<String> ret = new HashSet<String>();

		String q = "SELECT host_ip "
				+ "FROM Domain "
				+ "INNER JOIN DomainEntity ON domainentity_domain_id=domain_id "
				+ " INNER JOIN ServiceProperty ON serviceproperty_entity_id=domainentity_entity_id "
				+ "INNER JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value "
				+ "WHERE domain_name=? "
				+ "AND serviceproperty_service=? "
				+ "AND serviceproperty_property=?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int idx = loginAtDomain.indexOf("@");
		String domain = loginAtDomain.substring(idx + 1);
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(q);
			ps.setString(1, domain);
			ps.setString(2, service);
			ps.setString(3, prop);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(rs.getString(1));
			}

		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return ret;
	}
	
}
