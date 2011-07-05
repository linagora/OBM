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

public class MySQLConnectionFactory implements IJDBCDriver {

	@Override
	public String getSupportedDbType() {
		return "mysql";
	}

	@Override
	public String getJDBCUrl(String host, String dbName, String login,
			String password) {
		String jdbcUrl = "jdbc:mysql://" + host + "/" + dbName + "?"
				+ getMySQLOptions();
		return jdbcUrl;
	}

	private String getMySQLOptions() {
		StringBuilder b = new StringBuilder();
		b.append("zeroDateTimeBehavior=convertToNull");
		b.append("&relaxAutocommit=true");
		b.append("&jdbcCompliantTruncation=false");
		b.append("&interactiveClient=true");
		b.append("&serverTimezone=GMT");
		b.append("&useGmtMillisForDatetime=true");
		b.append("&useUnicode=true");
		b.append("&characterEncoding=utf8");
		b.append("&characterSetResults=utf8");
		b.append("&connectionCollation=utf8_general_ci");
		return b.toString();
	}

	@Override
	public String getDriverClass() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getKeepAliveQuery() {
		return "SELECT 1";
	}

	@Override
	public String getLastInsertIdQuery() {
		return "SELECT last_insert_id()";
	}

	@Override
	public String getTimeZoneStatement() {
		return "set time_zone='+00:00'";
	}

}
