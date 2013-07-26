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
package org.obm.locator;

import java.util.Collections;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.ConfigurationModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.ConfigurationServiceImpl;
import org.obm.configuration.DatabaseConfigurationImpl;
import org.obm.configuration.DefaultTransactionConfiguration;
import org.obm.configuration.GlobalAppConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.dbcp.DatabaseConnectionProviderImpl;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.DatabaseDriverConfigurationProvider;
import org.obm.sync.XTrustProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

public class GuiceServletContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(GuiceServletContextListener.class);

	private static final String GLOBAL_CONFIGURATION_FILE = "/etc/obm/obm_conf.ini";
	private static final String APPLICATION_NAME = "obm-locator";

	public void contextInitialized(ServletContextEvent servletContextEvent) {

		try {
			Injector injector = createInjector();
			if (injector == null) {
				failStartup("Could not create injector: createInjector() returned null");
			}
			XTrustProvider.install();
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			failStartup(e.getMessage());
		}
	}

	private Injector createInjector() {
		final GlobalAppConfiguration<ConfigurationService> globalConfiguration = buildConfiguration();
		return Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				install(new ConfigurationModule(globalConfiguration));
				bind(DatabaseDriverConfiguration.class).toProvider(DatabaseDriverConfigurationProvider.class);
				bind(DatabaseConnectionProvider.class).to(DatabaseConnectionProviderImpl.class);
				bind(String.class).annotatedWith(Names.named("application-name")).toInstance(APPLICATION_NAME);
				bind(Logger.class).annotatedWith(Names.named(LoggerModule.CONFIGURATION)).toInstance(LoggerFactory.getLogger(LoggerModule.CONFIGURATION));
				install(new TransactionalModule());
				install(new LocatorServletModule());
			}
		});
	}

	private GlobalAppConfiguration<ConfigurationService> buildConfiguration() {
		ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl.Factory().create(GLOBAL_CONFIGURATION_FILE, APPLICATION_NAME);
		return GlobalAppConfiguration.builder()
					.mainConfiguration(configurationService)
					.databaseConfiguration(new DatabaseConfigurationImpl.Factory().create(GLOBAL_CONFIGURATION_FILE))
					.transactionConfiguration(new DefaultTransactionConfiguration.Factory().create(APPLICATION_NAME, configurationService))
					.build();
	}

	private void failStartup(String message) {
		throw new CreationException(Collections.nCopies(1, new Message(this, message)));
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

}