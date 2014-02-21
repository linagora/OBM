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

import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.ssh.SSHPlugin;
import org.obm.auth.crsh.ObmSyncAuthenticationPlugin;
import org.obm.push.configuration.LoggerModule;
import org.obm.push.configuration.RemoteConsoleConfiguration;
import org.obm.sync.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.linagora.crsh.guice.CrashGuiceConfiguration;
import com.linagora.crsh.guice.CrashGuiceSupport;
import com.linagora.crsh.guice.CrashGuiceSupport.Bootstrap;

public class OpushCrashModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(LoggerModule.CONFIGURATION);
	
	protected void configure() {
		boolean autostart = false;
		install(new CrashGuiceSupport(autostart));
		Multibinder<CRaSHPlugin<?>> pluginBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<CRaSHPlugin<?>>(){});
		pluginBinder.addBinding().to(ObmSyncAuthenticationPlugin.class);
		bind(CRaSHBootstrap.class).asEagerSingleton();
		
		Multibinder<LifecycleListener> lifecycleListeners = Multibinder.newSetBinder(binder(), LifecycleListener.class);
		lifecycleListeners.addBinding().to(CRaSHBootstrap.class);
	}

	public static class CRaSHBootstrap implements LifecycleListener {

		private final RemoteConsoleConfiguration configuration;
		private final Bootstrap bootstrap;

		@Inject
		private CRaSHBootstrap(RemoteConsoleConfiguration configuration, Bootstrap bootstrap) {
			this.configuration = configuration;
			this.bootstrap = bootstrap;
			bootstrap();
		}

		private void bootstrap() {
			boolean enable = configuration.enable();
			logger.debug("CRaSH remote shell : " + (enable ? "enable" : "disable"));
			if (enable) {
				logger.debug("CRaSH remote shell started on port : " + configuration.port());
				bootstrap.start();
			}
		}

		@Override
		public void shutdown() throws Exception {
			bootstrap.destroy();
		}

	}

	@Provides
	public CrashGuiceConfiguration crashConfiguration(RemoteConsoleConfiguration configuration) {
		return CrashGuiceConfiguration.builder()
				.property(SSHPlugin.SSH_PORT.getName(), configuration.port())
				.property(AuthenticationPlugin.AUTH.getName(), "obm-sync")
				.build();
	}
}
