package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.TransactionManager;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.obm.dbcp.jdbc.IJDBCDriver;

import com.atomikos.icatch.jta.UserTransactionManager;

public class DataSource {

	private final ConnectionFactory connectionFactory;
	private TransactionManager transactionManager;

	public DataSource(IJDBCDriver cf, String dbHost, String dbName,
			String login, String password) {
		connectionFactory = buildConnectionFactory(cf, dbHost,
				dbName, login, password);

		buildManagedDataSource(connectionFactory);
	}

	private ConnectionFactory buildConnectionFactory(
			IJDBCDriver cf, String dbHost, String dbName, String login,
			String password) {

		transactionManager = new UserTransactionManager();

		String jdbcUrl = cf.getJDBCUrl(dbHost, dbName);
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				jdbcUrl, login, password);
		connectionFactory = new LocalXAConnectionFactory(transactionManager,
				connectionFactory);

		return connectionFactory;
	}

	private void buildManagedDataSource(
			ConnectionFactory connectionFactory)
			throws IllegalStateException {

		GenericObjectPool pool = new GenericObjectPool();
		PoolableConnectionFactory factory = new PoolableConnectionFactory(
				connectionFactory, pool, null, null, false, true);
		pool.setFactory(factory);
	}

	public Connection getConnection() throws SQLException {
		return connectionFactory.createConnection();
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

}
