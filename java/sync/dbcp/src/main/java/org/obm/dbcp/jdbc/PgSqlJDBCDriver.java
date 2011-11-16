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

package org.obm.dbcp.jdbc;

import java.util.Map;

import com.google.common.collect.ImmutableMap;


public class PgSqlJDBCDriver implements IJDBCDriver {

	@Override
	public String getSupportedDbType() {
		return "pgsql";
	}

	@Override
	public String getJDBCUrl(String host, String dbName) {
		return "jdbc:postgresql://" + host + "/" + dbName;
	}
	
	@Override
	public String getLastInsertIdQuery() {
		return "SELECT lastval()";
	}

	@Override
	public String setGMTTimezoneQuery() {
		return "SET TIME ZONE 'GMT'";
	}

	@Override
	public String getDataSourceClassName() {
		return "org.postgresql.xa.PGXADataSource";
	}

	@Override
	public String getUniqueName() {
		return "pgsql";
	}

	@Override
	public Map<String, String> getDriverProperties(String login, String password, String dbName, String dbHost) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builder.put("user", login);
		builder.put("password", password);
		builder.put("databaseName", dbName);
		builder.put("serverName", dbHost);
		return builder.build();
		
		
	}
}
