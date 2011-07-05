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

public interface IJDBCDriver {

	/**
	 * Returns the supported dbtype as in obm_conf.ini
	 * 
	 * @return mysql or pgsql
	 */
	String getSupportedDbType();

	/**
	 * @return the jdbc driver that should be used with the given db type
	 */
	String getDriverClass();

	/**
	 * @return returns an SQL query that always work when the sql connection works
	 */
	String getKeepAliveQuery();

	String getJDBCUrl(String host, String dbName, String login, String password);

	String getLastInsertIdQuery();
	
	String getTimeZoneStatement();

}
