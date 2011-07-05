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

package org.minig.obm.pool;

import org.minig.obm.pool.IJDBCDriver;

public class PgSQLConnectionFactory implements IJDBCDriver {

	@Override
	public String getSupportedDbType() {
		return "pgsql";
	}

	@Override
	public String getDriverClass() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getJDBCUrl(String host, String dbName, String login,
			String password) {
		return "jdbc:postgresql://" + host + "/" + dbName;
	}

	@Override
	public String getKeepAliveQuery() {
		return "SELECT 1";
	}

	@Override
	public String getLastInsertIdQuery() {
		return "SELECT lastval()";
	}

	@Override
	public String getTimeZoneStatement() {
		return "SET TIME ZONE 'GMT'";
	}

}
