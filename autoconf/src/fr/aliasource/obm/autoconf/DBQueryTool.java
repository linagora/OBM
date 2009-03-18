/**
 * 
 */
package fr.aliasource.obm.autoconf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.utils.ObmHelper;

/**
 * @author nicolasl
 * 
 */
public class DBQueryTool {

	private Log logger;

	public DBQueryTool() {
		logger = LogFactory.getLog(getClass());
	}

	/**
	 * Returns FQDNs hashed by service
	 * 
	 * @return a Map<service_name, fqdn> with service in ('imap', 'smtp')
	 */
	HashMap<String, String> getDBInformation(String login, String domainName) {
		HashMap<String, String> ret = new HashMap<String, String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		String query;

		query = " SELECT 'imap' as service_name, host_fqdn "
				+ " FROM Domain"
				+ " INNER JOIN DomainEntity ON domainentity_domain_id = domain_id"
				+ " INNER JOIN UserObm ON userobm_domain_id = domain_id AND userobm_login = ?"
				+ " LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id"
				+ " LEFT JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value"
				+ " WHERE serviceproperty_service = 'mail'"
				+ "   AND serviceproperty_property IN ('imap')"
				+ "   AND (domain_name = ? OR domain_global = true)"
				+ "   AND userobm_mail_server_id = host_id"
				+

				" UNION"
				+ " SELECT 'smtp' as service_name, host_fqdn "
				+ " FROM Domain"
				+ " INNER JOIN DomainEntity ON domainentity_domain_id = domain_id"
				+ " LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id"
				+ " LEFT JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value"
				+ " WHERE serviceproperty_service = 'mail'"
				+ "   AND serviceproperty_property IN ('smtp_out')"
				+ "   AND (domain_name = ? OR domain_global = true)";

		try {
			con = ObmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setString(1, login);
			ps.setString(2, domainName);
			ps.setString(3, domainName);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.put((String) rs.getString(1), (String) rs.getString(2));
			}

			return ret;
		} catch (SQLException e) {
			logger.error("Could not find user in OBM", e);
			return null;
		} finally {
			ObmHelper.cleanup(con, ps, rs);
		}
	}

	public String getDomain(String login) {
		String domain = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		String query = " SELECT domain_name from Domain INNER JOIN UserObm ON userobm_domain_id=domain_id WHERE userobm_login=?";

		try {
			con = ObmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setString(1, login);
			rs = ps.executeQuery();

			if (rs.next()) {
				domain = rs.getString(1);
			} else {
				logger.error("Can't find domain name for login '"+login+"'");
			}

		} catch (SQLException e) {
			logger.error("Could not find user in OBM", e);
		} finally {
			ObmHelper.cleanup(con, ps, rs);
		}
		return domain;
	}
}
