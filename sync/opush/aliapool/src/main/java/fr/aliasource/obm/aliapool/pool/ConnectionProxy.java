/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.pool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 * 
 */
public class ConnectionProxy implements XAResource, Connection {

	private Log logger = LogFactory.getLog(ConnectionProxy.class);

	private boolean inTransaction;
	private Connection con;
	private ConnectionPool pool;
	private Set<AbstractStatementProxy> openStatements;

	public ConnectionProxy(Connection con, ConnectionPool pool)
			throws SQLException {
		this.con = con;
		this.pool = pool;
		openStatements = Collections
				.synchronizedSet(new HashSet<AbstractStatementProxy>());
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void clearWarnings() throws SQLException {
		con.clearWarnings();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void close() throws SQLException {
		synchronized (openStatements) {
			for (AbstractStatementProxy asp : openStatements) {
				asp.checkOpenResults();
			}
			openStatements.clear();
		}

		if (!inTransaction) {
			try {
				pool.releaseConnection(this);
			} catch (InterruptedException e) {
				throw new SQLException(e.getMessage());
			}
		}
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void commit() throws SQLException {
		if (logger.isErrorEnabled()) {
			logger.error("Commit called directly");
		}
		throw new SQLException("Use the JTA api to manage your transactions");
	}

	private void privateCommit() throws SQLException {
		con.commit();
	}

	private void privateRollback() throws SQLException {
		con.rollback();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Statement createStatement() throws SQLException {
		return new StatementProxy(con.createStatement(), this);
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new StatementProxy(con.createStatement(resultSetType,
				resultSetConcurrency), this);
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new StatementProxy(con.createStatement(resultSetType,
				resultSetConcurrency, resultSetHoldability), this);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean getAutoCommit() throws SQLException {
		return con.getAutoCommit();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public String getCatalog() throws SQLException {
		return con.getCatalog();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getHoldability() throws SQLException {
		return con.getHoldability();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return con.getMetaData();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getTransactionIsolation() throws SQLException {
		return con.getTransactionIsolation();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public SQLWarning getWarnings() throws SQLException {
		return con.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return con.hashCode();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isClosed() throws SQLException {
		return con.isClosed();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isReadOnly() throws SQLException {
		return con.isReadOnly();
	}

	/**
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 */
	public String nativeSQL(String sql) throws SQLException {
		return con.nativeSQL(sql);
	}

	/**
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return con.prepareCall(sql);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws java.sql.SQLException
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return con.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return con.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	/**
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new PreparedStatementProxy(con.prepareStatement(sql), this);
	}

	/**
	 * @param sql
	 * @param autoGeneratedKeys
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return new PreparedStatementProxy(con.prepareStatement(sql,
				autoGeneratedKeys), this);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return new PreparedStatementProxy(con.prepareStatement(sql,
				resultSetType, resultSetConcurrency), this);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new PreparedStatementProxy(con.prepareStatement(sql,
				resultSetType, resultSetConcurrency, resultSetHoldability),
				this);
	}

	/**
	 * @param sql
	 * @param columnIndexes
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return new PreparedStatementProxy(con.prepareStatement(sql,
				columnIndexes), this);
	}

	/**
	 * @param sql
	 * @param columnNames
	 * @return
	 * @throws java.sql.SQLException
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return new PreparedStatementProxy(con
				.prepareStatement(sql, columnNames), this);
	}

	/**
	 * @param savepoint
	 * @throws java.sql.SQLException
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		con.releaseSavepoint(savepoint);
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void rollback() throws SQLException {
		if (logger.isErrorEnabled()) {
			logger.error("Rollback called directly", new Throwable(
					"Rollback called directly"));
		}
		throw new SQLException("Use the JTA api to manage your transactions");
	}

	/**
	 * @param savepoint
	 * @throws java.sql.SQLException
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		con.rollback(savepoint);
	}

	/**
	 * @param autoCommit
	 * @throws java.sql.SQLException
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		throw new SQLException("Use JTA to manage transactions");
	}

	/**
	 * @param catalog
	 * @throws java.sql.SQLException
	 */
	public void setCatalog(String catalog) throws SQLException {
		con.setCatalog(catalog);
	}

	/**
	 * @param holdability
	 * @throws java.sql.SQLException
	 */
	public void setHoldability(int holdability) throws SQLException {
		con.setHoldability(holdability);
	}

	/**
	 * @param readOnly
	 * @throws java.sql.SQLException
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		con.setReadOnly(readOnly);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Savepoint setSavepoint() throws SQLException {
		return con.setSavepoint();
	}

	/**
	 * @param name
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return con.setSavepoint(name);
	}

	/**
	 * @param level
	 * @throws java.sql.SQLException
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		con.setTransactionIsolation(level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + " " + con.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#getTransactionTimeout()
	 */
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
	 */
	public boolean setTransactionTimeout(int arg0) throws XAException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.transaction.xa.XAResource#isSameRM(javax.transaction.xa.XAResource)
	 */
	public boolean isSameRM(XAResource arg0) throws XAException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#recover(int)
	 */
	public Xid[] recover(int arg0) throws XAException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
	 */
	public int prepare(Xid arg0) throws XAException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
	 */
	public void forget(Xid arg0) throws XAException {
		inTransaction = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
	 */
	public void rollback(Xid arg0) throws XAException {
		try {
			privateRollback();
		} catch (SQLException e) {
			throw new XAException(e.getMessage());
		} finally {
			inTransaction = false;
			try {
				pool.releaseConnection(this);
			} catch (Exception e1) {
				throw new XAException(e1.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)
	 */
	public void end(Xid arg0, int arg1) throws XAException {
		inTransaction = false;
		try {
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new XAException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)
	 */
	public void start(Xid arg0, int arg1) throws XAException {
		inTransaction = true;
		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
			throw new XAException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid,
	 * boolean)
	 */
	public void commit(Xid arg0, boolean arg1) throws XAException {
		try {
			privateCommit();
		} catch (SQLException e) {
			throw new XAException(e.getMessage());
		} finally {
			inTransaction = false;
			if (logger.isDebugEnabled()) {
				logger.debug("Transaction commited");
			}
			try {
				pool.releaseConnection(this);
			} catch (Exception e1) {
				throw new XAException(e1.getMessage());
			}
		}
	}

	Connection getPhysicalConnection() {
		return con;
	}

	/**
	 * @param proxy
	 */
	void addOpenStatement(AbstractStatementProxy proxy) {
		synchronized (openStatements) {
			openStatements.add(proxy);
		}
	}

	/**
	 * @param proxy
	 */
	void closeStatement(Statement proxy) {
		synchronized (openStatements) {
			if (!openStatements.contains(proxy)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Closing a statement I do not own: " + proxy
							+ " _or_ connection closed before statement.");
				}
			} else {
				openStatements.remove(proxy);
			}
		}
	}

	/**
	 * 
	 */
	void recycleConnection() throws InterruptedException {
		try {
			con.close();
		} catch (SQLException sqle) {
			logger.warn("Error while trying to close known broken connection.");
		}
		con = null;

		while (con == null) {
			try {
				con = pool.getNewPhysicalConnection();
				synchronized (openStatements) {
					openStatements.clear();
				}
			} catch (SQLException sqle) {
				logger
						.fatal("Could not recycle connection to the database. Retrying in 2 seconds...");
			}
			Thread.sleep(2000);
		}
		logger.info("Connexion recycled successfuly.");
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return con.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return con.createBlob();
	}

	public Clob createClob() throws SQLException {
		return con.createClob();
	}

	public NClob createNClob() throws SQLException {
		return con.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return con.createSQLXML();
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return con.createStruct(typeName, attributes);
	}

	public Properties getClientInfo() throws SQLException {
		return con.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		return con.getClientInfo(name);
	}

	public boolean isValid(int timeout) throws SQLException {
		return con.isValid(timeout);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return con.isWrapperFor(iface);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		con.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		con.setClientInfo(name, value);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return con.unwrap(iface);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return con.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		con.setTypeMap(arg0);
	}
}
