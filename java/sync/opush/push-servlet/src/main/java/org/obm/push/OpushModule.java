/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.ConfigurationServiceImpl;
import org.obm.configuration.DatabaseConfigurationImpl;
import org.obm.configuration.DefaultTransactionConfiguration;
import org.obm.configuration.GlobalAppConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dbcp.DatabaseModule;
import org.obm.healthcheck.HealthCheckDefaultHandlersModule;
import org.obm.healthcheck.HealthCheckModule;
import org.obm.push.store.ehcache.EhCacheDaoModule;
import org.obm.push.store.jdbc.JdbcDaoModule;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class OpushModule extends AbstractModule {

	private static final String APPLICATION_NAME = "opush";
	private static final String GLOBAL_CONFIGURATION_FILE = ConfigurationService.GLOBAL_OBM_CONFIGURATION_PATH;

	private final GlobalAppConfiguration<ConfigurationService> globalConfiguration;
	private Module databaseModule;
	
	public OpushModule() {
		this(buildConfiguration(), new DatabaseModule());
	}
	
	public OpushModule(GlobalAppConfiguration<ConfigurationService> globalConfiguration, Module databaseModule) {
		this.globalConfiguration = globalConfiguration;
		this.databaseModule = databaseModule;
	}
	
	private static GlobalAppConfiguration<ConfigurationService> buildConfiguration() {
		ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl.Factory().create(GLOBAL_CONFIGURATION_FILE, APPLICATION_NAME);
		return 	GlobalAppConfiguration.builder()
					.mainConfiguration(configurationService)
					.locatorConfiguration(configurationService)
					.databaseConfiguration(new DatabaseConfigurationImpl.Factory().create(GLOBAL_CONFIGURATION_FILE))
					.transactionConfiguration(new DefaultTransactionConfiguration.Factory().create(APPLICATION_NAME, configurationService))
					.build();
	}
	
	@Override
	protected void configure() {
		install(new LinagoraImapModule());
		install(new OpushImplModule(globalConfiguration));
		install(new OpushMailModule());
		install(new ObmBackendModule());
		install(new LoggerModule());
		install(new OpushCrashModule());
		install(new HealthCheckModule());
		install(new HealthCheckDefaultHandlersModule());
		install(new EhCacheDaoModule());
		install(new JdbcDaoModule());
		install(databaseModule);
		bind(Boolean.class).annotatedWith(Names.named("enable-push")).toInstance(false);
 	}

}
