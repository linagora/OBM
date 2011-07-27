package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.TransactionManager;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.obm.dbcp.jdbc.IJDBCDriver;

public class DBConnectionPool {

	private final TransactionManager transactionManager;
	private final PoolingDataSource poolingDataSource;

	/* package */ DBConnectionPool(TransactionManager transactionManager, IJDBCDriver cf, String dbHost, String dbName,
			String login, String password) {
		this.transactionManager = transactionManager;

		ConnectionFactory connectionFactory = 
				buildConnectionFactory(cf, dbHost, dbName, login, password);

		poolingDataSource = buildManagedDataSource(connectionFactory);
	}

	private ConnectionFactory buildConnectionFactory(
			IJDBCDriver cf, String dbHost, String dbName, String login,
			String password) {

		String jdbcUrl = cf.getJDBCUrl(dbHost, dbName);
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				jdbcUrl, login, password);
		connectionFactory = new LocalXAConnectionFactory(this.transactionManager,
				connectionFactory);

		return connectionFactory;
	}

	private PoolingDataSource buildManagedDataSource(
			ConnectionFactory connectionFactory)
			throws IllegalStateException {

		GenericObjectPool pool = new GenericObjectPool();
		PoolableConnectionFactory factory = new PoolableConnectionFactory(
				connectionFactory, pool, null, null, false, true);
		pool.setFactory(factory);
		return new PoolingDataSource(pool);
	}

	/* package */ Connection getConnection() throws SQLException {
		return poolingDataSource.getConnection();
	}

	/* package */ TransactionManager getTransactionManager() {
		return transactionManager;
	}

}
