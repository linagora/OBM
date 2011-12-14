/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
	private final IJDBCDriver cf;
	private static final String VALIDATION_QUERY = "SELECT 666";

	/* package */ DBConnectionPool(TransactionManager transactionManager, IJDBCDriver cf, String dbHost, String dbName,
			String login, String password) {
		this.transactionManager = transactionManager;
		this.cf = cf;

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
		pool.setTestOnBorrow(true);
		List<String> initConnectionSqls = Arrays.asList(cf.setGMTTimezoneQuery());
		PoolableConnectionFactory factory = new PoolableConnectionFactory(
				connectionFactory, pool, null, VALIDATION_QUERY, initConnectionSqls, false, true);
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
