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

import org.obm.dbcp.jdbc.IJDBCDriver;

import com.google.common.annotations.VisibleForTesting;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class DBConnectionPool {

	private final PoolingDataSource poolingDataSource;
	private static final String VALIDATION_QUERY = "SELECT 666";

	/* package */ DBConnectionPool(IJDBCDriver cf, String dbHost, String dbName,
			String login, String password, Integer maxPoolSize) {
		poolingDataSource = buildConnectionFactory(cf, dbHost, dbName, login, password, maxPoolSize);
	}

	private PoolingDataSource buildConnectionFactory(
			IJDBCDriver cf, String dbHost, String dbName, String login,
			String password, Integer maxPoolSize) {

		PoolingDataSource poolds = new PoolingDataSource();
		poolds.setClassName(cf.getDataSourceClassName());
		poolds.setUniqueName(cf.getUniqueName());
		poolds.setMaxPoolSize(maxPoolSize);
		poolds.setAllowLocalTransactions(true);
		poolds.getDriverProperties().putAll(cf.getDriverProperties(login, password, dbName, dbHost));
		poolds.setTestQuery(VALIDATION_QUERY);
		poolds.init();
		return poolds;
	}

	@VisibleForTesting Connection getConnection() throws SQLException {
		return poolingDataSource.getConnection();
	}
	
	public void cleanup() {
	    poolingDataSource.close();
	}

}
