/**
 * 
 */
package fr.aliasource.obm.autoconf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.utils.ObmHelper;

/**
 * @author nicolasl
 *
 */
public class DBQueryTool {

	private DBConfig dc;
	private Log logger;
	
	public DBQueryTool(DBConfig dc) {
		logger = LogFactory.getLog(getClass());
		this.dc = dc;
	}
	
	/**
	 * 
	 * @param q sql query
	 * @return {'imap' => 'imap_host_ip', 'smtp' => 'smtp_host_ip'}
	 */
	HashMap<String,String> getDBInformation() {
		HashMap<String,String> ret = new HashMap<String,String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		String query;
		
		query =
				" SELECT 'imap' as service_name, host_ip" +
				" FROM Domain" +
				" INNER JOIN DomainEntity ON domainentity_domain_id = domain_id" +
				" INNER JOIN UserObm ON userobm_domain_id = domain_id AND userobm_login = ?" +
				" LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id" +
				" LEFT JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value" +
				" WHERE serviceproperty_service = 'mail'" +
				"   AND serviceproperty_property IN ('imap')" +
				"   AND (domain_name = ? OR domain_global = true)" +
				"   AND userobm_mail_server_id = host_id" +
				" GROUP BY serviceproperty_property, host_id, host_name, host_ip" +
				
				/*
				 * 
				seems to be EQUIVALENT (with arguments '?' inverted) to
				
			    " SELECT 'imap' as service_name, host_ip" +
				"   FROM UserObm" +
				"   INNER JOIN Domain ON domain_id = userobm_domain_id" +
				"     AND domain_name = ?" +
				"   INNER JOIN Host ON userobm_mail_server_id = host_id" +
				"   INNER JOIN HostEntity ON hostentity_host_id = host_id" +
				"   INNER JOIN Service as imap ON imap.service_service = 'imap'" +
				"     AND imap.service_entity_id = hostentity_entity_id" +
				"   WHERE userobm_login = ?" +
				
				*/
				
				" UNION" +
				" SELECT 'smtp' as service_name, host_ip" +
				" FROM Domain" +
				" INNER JOIN DomainEntity ON domainentity_domain_id = domain_id" +
				" LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id" +
				" LEFT JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value" +
				" WHERE serviceproperty_service = 'mail'" +
				"   AND serviceproperty_property IN ('smtp_out')" +
				"   AND (domain_name = ? OR domain_global = true)" +
				" GROUP BY serviceproperty_property, host_id, host_name, host_ip" +
				
				" LIMIT 2";
		
		try {
			con = ObmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setString(1, dc.getLogin());
			ps.setString(2, dc.getDomainName());
			ps.setString(3, dc.getDomainName());
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.put((String) rs.getString(1), (String) rs.getString(2));
			}
			
			return ret;
		} catch (SQLException e) {
			logger.error("Could not find user in OBM", e);
			return null;
		} finally {
			try {
				ObmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}
	}
}
