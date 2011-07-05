/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 * 
 */
public class DataSource implements javax.sql.DataSource {

	private static Log logger = LogFactory.getLog(DataSource.class);

	private ConnectionPool pool;
	private int loginTimeout;
	private PrintWriter logWriter;

	public DataSource(String driverClass, String url, String login,
			String password, Properties jdbcProperties, int max, String pingQuery) throws SQLException {
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		logWriter = new PrintWriter(System.err);
		logger.info("Starting pool...");
		pool = new ConnectionPool(url, login, password, jdbcProperties, max, pingQuery);
	}

	/**
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	/**
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}

	/**
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return logWriter;
	}

	/**
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	/**
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		Connection con = null;
		try {
			con = pool.getConnection();
		} catch (InterruptedException e) {
			logger.error("interrupted", e);
			SQLException sqle = new SQLException(e.getMessage());
			sqle.initCause(e);
			throw sqle;
		}
		return con;
	}

	/**
	 * @see javax.sql.DataSource#getConnection(java.lang.String,
	 *      java.lang.String)
	 */
	public Connection getConnection(String username, String password)
			throws SQLException {
		throw new SQLException("Not implemented !");
	}

	public void stop() throws SQLException, InterruptedException {
		pool.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		stop();
		super.finalize();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

}
