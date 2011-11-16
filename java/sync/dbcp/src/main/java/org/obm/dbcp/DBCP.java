/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.obm.dbcp.impl.ObmConfIni;
import org.obm.dbcp.jdbc.IJDBCDriver;
import org.obm.dbcp.jdbc.MySqlJDBCDriver;
import org.obm.dbcp.jdbc.PgSqlJDBCDriver;
import org.obm.push.utils.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DBCP implements IDBCP{

	private Logger logger = LoggerFactory.getLogger(getClass());

	private DBConnectionPool ds;

	private String lastInsertIdQuery;

	private String dbType;
	private String login;
	private String password;
	private String dbHost;
	private String dbName;

	@Inject
	public DBCP() {
		logger.info("Starting OBM connection pool...");
		readObmConfIni();
		createDataSource();
	}
	
	private void readObmConfIni() {
		ObmConfIni oci = new ObmConfIni();
		dbType = oci.getDbType();
		login = oci.getLogin();
		password = oci.getPassword();
		dbHost = oci.getDbHost();
		dbName = oci.getDbName();
		logger.info("dbtype from obm_conf.ini is " + dbType);
	}

	private void createDataSource() {
		try {
			IJDBCDriver cf = buildJDBCConnectionFactory(dbType);
			ds = new DBConnectionPool(cf, dbHost, dbName, login, password);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	private IJDBCDriver buildJDBCConnectionFactory(String dbType)
			throws Exception {
		IJDBCDriver cf = null;
		if (dbType.equalsIgnoreCase("PGSQL")) {
			cf = new PgSqlJDBCDriver();
		}
		if (dbType.equalsIgnoreCase("MYSQL")) {
			cf = new MySqlJDBCDriver();
		}
		if (cf == null) {
			logger.error("No connection factory found for dbtype " + dbType);
			throw new Exception("No connection factory found for dbtype "
					+ dbType);
		}
		lastInsertIdQuery = cf.getLastInsertIdQuery();
		return cf;
	}

	public int lastInsertId(Connection con) throws SQLException {
		int ret = 0;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(lastInsertIdQuery);
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} finally {
			JDBCUtils.cleanup(null, st, rs);
		}
		return ret;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
}
