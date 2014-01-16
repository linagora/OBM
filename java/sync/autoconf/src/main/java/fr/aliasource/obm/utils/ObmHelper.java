/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliasource.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.obm.dbcp.DatabaseConnectionProvider;

import com.google.inject.Inject;

public class ObmHelper {
	public static final String USER_TRANSACTION = "java:comp/UserTransaction";
	public static final String DATA_SOURCE = "java:comp/env/jdbc/AutoConfDS";

	private final DatabaseConnectionProvider dbcp;

	@Inject
	public ObmHelper(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	/**
	 * Permet d'obtenir une connection du pool. On obtient la même pendant toute
	 * la durée de la transaction.
	 * 
	 * @return Une connection du pool
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection con = dbcp.getConnection();
		return con;
	}

	/**
	 * Ferme tout le 'JDBC crap' avec les tests qui vont bien. Seuls les
	 * paramètres non nuls seront fermés.
	 * 
	 * @param con
	 * @param st
	 * @param rs
	 * @throws SQLException
	 */
	public void cleanup(Connection con, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * Ferme tout le 'JDBC crap' avec les tests qui vont bien. Seuls les
	 * paramètres non nuls seront fermés.
	 * 
	 * @param con
	 * @param st
	 * @throws SQLException
	 */
	public void cleanup(Connection con, Statement st)
			throws SQLException {
		if (con != null) {
			if (st != null) {
				st.close();
			}
			con.close();
		}
	}

	/**
	 * Retourne la valeur de la dernière clef auto-générée par une séquence
	 * donnée
	 * 
	 * @param con
	 * @return le dernier id inséré
	 * @throws SQLException
	 */
	public int getLastInsertId(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT last_insert_id()");
			rs.next();
			return rs.getInt(1);
		} finally {
			cleanup(null, st, rs);
		}
	}
}
