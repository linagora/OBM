/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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


import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;

import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.StaticConfigurationService;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.TransactionalBinder;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

@RunWith(GuiceRunner.class)
@GuiceModule(DatabaseConnectionProviderIntegrationTest.Env.class)
public class DatabaseConnectionProviderIntegrationTest implements H2TestClass {

	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/empty.sql");
	@Inject H2InMemoryDatabase db;
	@Inject DatabaseConnectionProvider databaseConnectionProvider;
	@Inject PoolingDataSourceDecorator poolingDataSourceDecorator;
	
	public static class Env extends AbstractModule {
		
		private final Logger configurationLogger;
		final IMocksControl control = createControl();

		public Env() {
			configurationLogger = LoggerFactory.getLogger(getClass());
		}
		
		@Override
		protected void configure() {
			Configuration.Transaction transactionConfiguration = new Configuration.Transaction();
			transactionConfiguration.timeoutInSeconds = 3600;
			
			bind(IMocksControl.class).toInstance(control);
			bind(DatabaseConnectionProvider.class).to(DatabaseConnectionProviderImpl.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
			bind(ITransactionAttributeBinder.class).to(TransactionalBinder.class);
			Multibinder<DatabaseDriverConfiguration> databaseDrivers = Multibinder.newSetBinder(binder(), DatabaseDriverConfiguration.class);
			databaseDrivers.addBinding().to(MyDriverConfiguration.class);
			
			bind(TransactionConfiguration.class).toInstance(new StaticConfigurationService.Transaction(transactionConfiguration));
			install(new TransactionalModule());
			
			bind(Logger.class).annotatedWith(Names.named(LoggerModule.CONFIGURATION)).toInstance(configurationLogger);
		}
		
		protected <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(control.createMock(cls));
		}
	}
	
	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	@Test(expected=SQLException.class)
	public void testGetConnection() throws SQLException {
		try {
			databaseConnectionProvider.getConnection();
		} finally {
			// One connection as already been instantiate and is available in the pool
			assertThat(poolingDataSourceDecorator.poolingDataSource.getInPoolSize()).isEqualTo(1);
		}
	}
	
	private static class MyDriverConfiguration extends H2DriverConfiguration {
		
		@Override
		public String getGMTTimezoneQuery() {
			return "Will fail!!!";
		}
	}
}
