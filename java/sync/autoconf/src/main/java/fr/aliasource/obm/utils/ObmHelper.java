/*
 * Created on Aug 8, 2003
 *
 */
package fr.aliasource.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.obm.dbcp.IDBCP;

import com.google.inject.Inject;

public class ObmHelper {
	public static final String USER_TRANSACTION = "java:comp/UserTransaction";
	public static final String DATA_SOURCE = "java:comp/env/jdbc/AutoConfDS";

	private final IDBCP dbcp;

	@Inject
	public ObmHelper(IDBCP dbcp) {
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
