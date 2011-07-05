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

package fr.aliasource.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.UserTransaction;

/**
 * @author tom
 * 
 */
public class JDBCUtils {

	public static final void rollback(Connection con) {
		if (con != null) {
			try {
				con.rollback();
			} catch (SQLException se) {
			}
		}
	}

	/**
	 * PostgreSQL only
	 * 
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static int lastInsertId(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT lastval()");
			rs.next();
			return rs.getInt(1);
		} finally {
			cleanup(null, st, rs);
		}
	}

	public static final void cleanup(Connection con, Statement ps, ResultSet rs) {
		closeResultSet(rs);
		closeStatement(ps);
		closeConnection(con);
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException se) {
			}
		}
	}

	public static void closeStatement(Statement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException se) {
			}
		}
	}

	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}

	public static void rollback(UserTransaction ut) {
		try {
			ut.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
