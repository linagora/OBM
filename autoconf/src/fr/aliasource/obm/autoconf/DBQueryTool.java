/**
 * 
 */
package fr.aliasource.obm.autoconf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	String getDBInformation() {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		String q =dc.getQuery();

		try {
			con = ObmHelper.getConnection();
			ps = con.prepareStatement(q);

			ps.setString(1, dc.getLogin());
			ps.setString(2, dc.getDomainName());
			rs = ps.executeQuery();

			rs.next();
			String ret = (String) rs.getString(1);

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
