/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive;

import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.module.LoggerModule;
import org.obm.cyrus.imap.CyrusClientModule;
import org.obm.dbcp.DatabaseModule;
import org.obm.domain.dao.UserSystemDao;
import org.obm.domain.dao.UserSystemDaoJdbcImpl;
import org.obm.imap.archive.authentication.AuthenticationFilter;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationModule;
import org.obm.imap.archive.resources.ConfigurationResource;
import org.obm.imap.archive.resources.DomainBasedSubResource;
import org.obm.imap.archive.resources.ObmDomainFactory;
import org.obm.imap.archive.resources.RootHandler;
import org.obm.imap.archive.resources.cyrus.CyrusStatusHandler;
import org.obm.imap.archive.service.SchedulingDatesService;
import org.obm.imap.archive.services.DomainConfigurationService;
import org.obm.jersey.injection.JerseyResourceConfig;
import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.server.EmbeddedServerModule;
import org.obm.server.ServerConfiguration;
import org.obm.sync.XTrustProvider;
import org.obm.sync.date.DateProvider;
import org.obm.utils.ObmHelper;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ImapArchiveModule extends AbstractModule {
	
	static {
		XTrustProvider.install();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	private final ServerConfiguration configuration;
	private static final String APPLICATION_ORIGIN = "imap-archive";
	
	public ImapArchiveModule(ServerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void configure() {
		install(new EmbeddedServerModule(configuration));
		install(new ImapArchiveServletModule());
		install(new CyrusClientModule());
		install(new ImapArchiveConfigurationModule());
		install(new TransactionalModule());
		install(new DatabaseModule());
		install(new LoggerModule());
		install(new DaoModule());
		
		bind(DateProvider.class).to(ObmHelper.class);
		bind(LocatorService.class).to(LocatorCache.class);
		bind(UserSystemDao.class).to(UserSystemDaoJdbcImpl.class);
		bind(String.class).annotatedWith(Names.named("origin")).toInstance(APPLICATION_ORIGIN);
		
		bindImapArchiveServices();
	}
	
	private void bindImapArchiveServices() {
		bind(DomainConfigurationService.class);
		bind(SchedulingDatesService.class);
	}
	
	public static class ImapArchiveServletModule extends ServletModule {

		public final static String URL_PREFIX = "/imap-archive/service/v1";
		public final static String URL_PATTERN = URL_PREFIX + "/*";
		
		@Override
		protected void configureServlets() {
			filter(URL_PATTERN).through(AuthenticationFilter.class);
			serve(URL_PATTERN).with(ImapArchiveServicesContainer.class);
		}
	}
	
	@Singleton
	public static class ImapArchiveServicesContainer extends ServletContainer {
		
		@Inject
		public ImapArchiveServicesContainer(Injector injector) {
			super(new JerseyResourceConfig(injector)
					.register(new AbstractBinder() {
						@Override
						protected void configure() {
							bindFactory(ObmDomainFactory.class).to(ObmDomain.class);
						}
					})
					.register(RootHandler.class)
					.register(CyrusStatusHandler.class)
					.register(DomainBasedSubResource.class)
					.register(ConfigurationResource.class));
		}
		
	}
}
