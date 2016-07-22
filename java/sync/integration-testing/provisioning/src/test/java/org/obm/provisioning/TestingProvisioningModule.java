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
package org.obm.provisioning;

import org.obm.Configuration;
import org.obm.ConfigurationModule;
import org.obm.SolrModuleUtils;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.server.EmbeddedServerModule;
import org.obm.server.ServerConfiguration;
import org.obm.server.context.NoContext;
import org.obm.service.MessageQueueServerModule;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class TestingProvisioningModule extends AbstractModule {

	private Module module;

	public TestingProvisioningModule() {
		module = Modules.override(new ProvisioningServerService(new NoContext())).with(new OverridingModule());
	}

	@Override
	protected void configure() {
		install(module);
	}

	public static class OverridingModule extends AbstractModule {
		
		@Override
		protected void configure() {
			install(new ConfigurationModule(buildConfiguration()));
			install(new EmbeddedServerModule(ServerConfiguration.defaultConfiguration()));
			install(new MessageQueueServerModule());
			install(SolrModuleUtils.buildDummySolrModule());
			bind(Boolean.class).annotatedWith(Names.named("queueIsRemote")).toInstance(false);
			bind(LocatorService.class).toInstance(alwaysLocalLocatorService());
			
			Multibinder<DatabaseDriverConfiguration> databaseDrivers = Multibinder.newSetBinder(binder(), DatabaseDriverConfiguration.class);
			databaseDrivers.addBinding().to(H2DriverConfiguration.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
		}

		private LocatorService alwaysLocalLocatorService() {
			return new LocatorService() {
				
				@Override
				public String getServiceLocation(String serviceSlashProperty, String loginAtDomain)
						throws LocatorClientException {
					return "localhost";
				}
			};
		}
		
		private Configuration buildConfiguration() {
			Configuration configuration = new Configuration();
			configuration.obmUiBaseUrl = "localhost";
			configuration.locator.url = "localhost";
			configuration.dataDir = Files.createTempDir();
			configuration.transaction.timeoutInSeconds = 3600;
			return configuration;
		}
	}
}
