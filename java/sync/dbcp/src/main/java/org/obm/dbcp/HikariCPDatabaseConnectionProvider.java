/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.obm.annotations.technicallogging.KindToBeLogged;
import org.obm.annotations.technicallogging.ResourceType;
import org.obm.annotations.technicallogging.TechnicalLogging;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.logger.LoggerModule;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariDataSource;

@Singleton
public class HikariCPDatabaseConnectionProvider extends DatabaseConnectionProviderImpl {

	private final HikariDataSource pool;

	@Inject
	protected HikariCPDatabaseConnectionProvider(
			DatabaseDriverConfigurationFactory databaseDriverConfigurationFactory,
			DatabaseConfiguration databaseConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		super(databaseDriverConfigurationFactory.create());

		logger.info("Starting OBM/HikariCP connection pool...");

		configurationLogger.info("Database system : {}", databaseConfiguration.getDatabaseSystem());
		configurationLogger.info("Database name {} on host {}", databaseConfiguration.getDatabaseName(), databaseConfiguration.getDatabaseHost());
		configurationLogger.info("Database connection min pool size : {}", databaseConfiguration.getDatabaseMinConnectionPoolSize());
		configurationLogger.info("Database connection pool size : {}", databaseConfiguration.getDatabaseMaxConnectionPoolSize());
		configurationLogger.info("Databse login : {}", databaseConfiguration.getDatabaseLogin());

		pool = new HikariDataSource();
		pool.setDataSourceClassName(driverConfiguration.getNonXADataSourceClassName());
		pool.setPoolName(driverConfiguration.getFlavour().name());
		pool.setAutoCommit(false);
		pool.setMaximumPoolSize(databaseConfiguration.getDatabaseMaxConnectionPoolSize());
		pool.setJdbc4ConnectionTest(true);
		pool.setConnectionInitSql(driverConfiguration.getGMTTimezoneQuery());
		pool.setDataSourceProperties(mapToProperties(driverConfiguration.getDriverProperties(databaseConfiguration)));
	}

	@VisibleForTesting
	HikariDataSource getPool() {
		return pool;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.JDBC_CONNECTION)
	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	@Override
	public void cleanup() {
		pool.close();
	}

	private Properties mapToProperties(Map<String, String> map) {
		Properties p = new Properties();

		for (Entry<String, String> entry : map.entrySet()) {
			p.setProperty(entry.getKey(), entry.getValue());
		}

		return p;
	}

}
