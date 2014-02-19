/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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
package org.obm.locator;

import org.obm.Configuration;
import org.obm.StaticConfigurationService;
import org.obm.configuration.TransactionConfiguration;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.locator.server.ContainerModule;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Modules;

public class TestLocatorModule extends AbstractModule {

	private final Configuration configuration;
	private final TransactionConfiguration transactionConfiguration;

	public TestLocatorModule() {
		configuration = new Configuration();
		configuration.locator.port = 0;
		configuration.dataDir = Files.createTempDir();
		Configuration.Transaction transaction = new Configuration.Transaction();
		transaction.timeoutInSeconds = 3600;
		transactionConfiguration = new StaticConfigurationService.Transaction(transaction);
	}
	
	@Override
	protected void configure() {
		install(
			Modules.override(new ContainerModule())
				.with(
					new DaoTestModule(),
					new AbstractModule() {
						@Override
						protected void configure() {
							bind(TransactionConfiguration.class).toInstance(transactionConfiguration);
							bind(DatabaseDriverConfiguration.class).to(H2DriverConfiguration.class);
							Multibinder<DatabaseDriverConfiguration> databaseDrivers = Multibinder.newSetBinder(binder(), DatabaseDriverConfiguration.class);
							databaseDrivers.addBinding().to(H2DriverConfiguration.class);
							bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
						}
					})
			);
	}
}