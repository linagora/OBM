package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.obm.dbcp.jdbc.IJDBCDriver;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class DBConnectionPool {

	private final PoolingDataSource poolingDataSource;
	private final IJDBCDriver cf;

	/* package */ DBConnectionPool(IJDBCDriver cf, String dbHost, String dbName,
			String login, String password) {
		this.cf = cf;
		this.poolingDataSource = buildConnectionFactory(cf, dbHost, dbName, login, password);
	}

	private PoolingDataSource buildConnectionFactory(
			IJDBCDriver cf, String dbHost, String dbName, String login,
			String password) {
		PoolingDataSource poolds = new PoolingDataSource();
		poolds.setClassName(cf.getDataSourceClassName());
		poolds.setUniqueName(cf.getUniqueName());
		poolds.setMaxPoolSize(10);
		poolds.setAllowLocalTransactions(true);
		poolds.getDriverProperties().putAll(cf.getDriverProperties(login, password, dbName, dbHost));
		poolds.init();
		return poolds;
	}
	
	/* package */ Connection getConnection() throws SQLException {
		Connection connection = poolingDataSource.getConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(cf.setGMTTimezoneQuery());
		} catch (SQLException e) {
			connection.close();
		} finally {
			if (statement != null) {
				statement.close();	
			}
		}
		return connection;
	}

}
