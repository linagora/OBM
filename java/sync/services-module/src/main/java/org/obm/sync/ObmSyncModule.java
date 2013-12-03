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
package org.obm.sync;

import javax.servlet.ServletContext;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.ConfigurationModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.DatabaseConfigurationImpl;
import org.obm.configuration.DefaultTransactionConfiguration;
import org.obm.configuration.GlobalAppConfiguration;
import org.obm.domain.dao.DaoModule;
import org.obm.healthcheck.HealthCheckDefaultHandlersModule;
import org.obm.healthcheck.HealthCheckModule;
import org.obm.provisioning.ProvisioningService;

import com.google.inject.AbstractModule;
import com.sun.jersey.guice.JerseyServletModule;

import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationServiceImpl;

public class ObmSyncModule extends AbstractModule {
	
	private ServletContext servletContext;
	
	public ObmSyncModule(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	private static final String APPLICATION_NAME = "sync";
	private static final String GLOBAL_CONFIGURATION_FILE = ConfigurationService.GLOBAL_OBM_CONFIGURATION_PATH;
	
	@Override
	protected void configure() {
		final GlobalAppConfiguration<ObmSyncConfigurationService> globalConfiguration = buildConfiguration();
		bind(ObmSyncConfigurationService.class).toInstance(globalConfiguration.getConfigurationService());
		install(new ConfigurationModule(globalConfiguration));
		install(new ObmSyncServletModule());
		install(new ObmSyncServicesModule());
		install(new MessageQueueModule());
		install(new TransactionalModule());
		install(new DatabaseModule());
		install(new DaoModule());
		install(new SolrJmsModule());
		install(new HealthCheckModule());
		install(new HealthCheckDefaultHandlersModule());
		install(new DatabaseMetadataModule());
		install(new ProvisioningService(servletContext));
		install(new JerseyServletModule());
	}

	private GlobalAppConfiguration<ObmSyncConfigurationService> buildConfiguration() {
		ObmSyncConfigurationServiceImpl configurationService = new ObmSyncConfigurationServiceImpl.Factory().create(GLOBAL_CONFIGURATION_FILE, APPLICATION_NAME);
		return GlobalAppConfiguration.<ObmSyncConfigurationService>builder()
					.mainConfiguration(configurationService)
					.locatorConfiguration(configurationService)
					.databaseConfiguration(new DatabaseConfigurationImpl.Factory().create(GLOBAL_CONFIGURATION_FILE))
					.transactionConfiguration(new DefaultTransactionConfiguration.Factory().create(APPLICATION_NAME, configurationService))
					.build();
	}
}
