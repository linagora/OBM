/*
 * Created on Aug 8, 2003
 *
 */
package fr.aliasource.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 * 
 */
public class ObmHelper {

	private static final Log logger = LogFactory.getLog(ObmHelper.class);

	public static final String USER_TRANSACTION = "java:comp/UserTransaction";
	public static final String DATA_SOURCE = "java:comp/env/jdbc/AutoConfDS";

	private static DataSource ds;
	private static UserTransaction ut;

	static {
		InitialContext context;
		try {
			context = new InitialContext();
			logger.info("ctx: " + context);
			ut = (UserTransaction) context.lookup(USER_TRANSACTION);
			ds = (DataSource) context.lookup(DATA_SOURCE);
			logger.info("ut: " + ut);
			logger.info("ds: " + ds);
		} catch (NamingException e) {
			logger.error("Cannot locate datasource at " + "jdbc/AutoConfDS", e);
		}

	}

	/**
	 * Permet d'obtenir une connection du pool. On obtient la même pendant toute
	 * la durée de la transaction.
	 * 
	 * @return Une connection du pool
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		Connection con = ds.getConnection();
		return con;
	}

	/**
	 * Permet d'accéder à la transaction liée au thread courant.
	 * 
	 * @return la transaction courante
	 */
	public static UserTransaction getUserTransaction() {
		return ut;
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
	public static void cleanup(Connection con, Statement st, ResultSet rs) {
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
	public static void cleanup(Connection con, Statement st)
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
	public static int getLastInsertId(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT last_insert_id()");
			rs.next();
			return rs.getInt(1);
		} finally {
			ObmHelper.cleanup(null, st, rs);
		}
	}

	public static void rollback(UserTransaction ut) {
		try {
			ut.rollback();
		} catch (Exception e) {
			logger.error("Error while rolling-back", e);
		}
	}

}
