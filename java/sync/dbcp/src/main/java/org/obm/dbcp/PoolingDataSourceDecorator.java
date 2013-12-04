/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2013  Linagora
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

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.slf4j.Logger;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PoolingDataSourceDecorator {

	private final DatabaseDriverConfiguration driverConfiguration;
	@VisibleForTesting final PoolingDataSource poolingDataSource;
	
	@Inject
	@VisibleForTesting PoolingDataSourceDecorator(
			DatabaseDriverConfigurationProvider databaseDriverConfigurationProvider, 
			DatabaseConfiguration databaseConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		
		configurationLogger.info("Database system : {}", databaseConfiguration.getDatabaseSystem());
		configurationLogger.info("Database name {} on host {}", databaseConfiguration.getDatabaseName(), databaseConfiguration.getDatabaseHost());
		configurationLogger.info("Database connection min pool size : {}", databaseConfiguration.getDatabaseMinConnectionPoolSize());
		configurationLogger.info("Database connection pool size : {}", databaseConfiguration.getDatabaseMaxConnectionPoolSize());
		configurationLogger.info("Databse login : {}", databaseConfiguration.getDatabaseLogin());
		
		driverConfiguration = databaseDriverConfigurationProvider.get();
		
		poolingDataSource = new PoolingDataSource();
		poolingDataSource.setClassName(driverConfiguration.getDataSourceClassName());
		poolingDataSource.setUniqueName(driverConfiguration.getFlavour().name());
		if (databaseConfiguration.getDatabaseMinConnectionPoolSize() != null) {
			poolingDataSource.setMinPoolSize(databaseConfiguration.getDatabaseMinConnectionPoolSize());
		}
		poolingDataSource.setMaxPoolSize(databaseConfiguration.getDatabaseMaxConnectionPoolSize());
		poolingDataSource.setAllowLocalTransactions(true);
		poolingDataSource.getDriverProperties().putAll(
				driverConfiguration.getDriverProperties(databaseConfiguration));
		poolingDataSource.setEnableJdbc4ConnectionTest(true);
		poolingDataSource.setShareTransactionConnections(true);
	}

	public DatabaseDriverConfiguration getDatabaseDriverConfiguration() {
		return driverConfiguration;
	}

	public Connection getConnection() throws SQLException {
		return poolingDataSource.getConnection();
	}

	public void close() {
		poolingDataSource.close();
	}
}
