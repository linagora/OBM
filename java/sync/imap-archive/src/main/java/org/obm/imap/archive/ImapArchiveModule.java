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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.cyrus.imap.CyrusClientModule;
import org.obm.dbcp.DatabaseModule;
import org.obm.domain.dao.UserSystemDao;
import org.obm.domain.dao.UserSystemDaoJdbcImpl;
import org.obm.imap.archive.authentication.AuthenticationFilter;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationModule;
import org.obm.imap.archive.logging.LoggerFactory;
import org.obm.imap.archive.logging.LoggerFileNameService;
import org.obm.imap.archive.logging.LoggerFileNameServiceImpl;
import org.obm.imap.archive.resources.ConfigurationResource;
import org.obm.imap.archive.resources.DomainBasedSubResource;
import org.obm.imap.archive.resources.HealthcheckHandler;
import org.obm.imap.archive.resources.ObmDomainFactory;
import org.obm.imap.archive.resources.RootHandler;
import org.obm.imap.archive.resources.TreatmentsResource;
import org.obm.imap.archive.resources.cyrus.CyrusStatusHandler;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus;
import org.obm.imap.archive.scheduling.ArchiveSchedulerQueue;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.obm.imap.archive.scheduling.OnlyOnePerDomainMonitorFactory;
import org.obm.imap.archive.scheduling.OnlyOnePerDomainMonitorFactory.OnlyOnePerDomainMonitorFactoryImpl;
import org.obm.imap.archive.services.ArchiveDaoTracking;
import org.obm.imap.archive.services.ArchiveService;
import org.obm.imap.archive.services.ArchiveServiceImpl;
import org.obm.imap.archive.services.DomainClient;
import org.obm.imap.archive.services.DomainClientImpl;
import org.obm.imap.archive.services.DomainConfigurationService;
import org.obm.imap.archive.services.RunningArchiveTracking;
import org.obm.imap.archive.services.SchedulingDatesService;
import org.obm.imap.archive.services.StoreClientFactory;
import org.obm.imap.archive.startup.RestoreTasksOnStartupHandler;
import org.obm.jersey.injection.JerseyResourceConfig;
import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.push.utils.UUIDFactory;
import org.obm.server.EmbeddedServerModule;
import org.obm.server.ServerConfiguration;
import org.obm.sync.XTrustProvider;
import org.obm.sync.date.DateProvider;
import org.obm.utils.ObmHelper;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ImapArchiveModule extends AbstractModule {
	
	static {
		XTrustProvider.install();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	private final ServerConfiguration configuration;
	private static final String APPLICATION_ORIGIN = "imap-archive";
	public  static final Class<RestoreTasksOnStartupHandler> STARTUP_HANDLER_CLASS = RestoreTasksOnStartupHandler.class;
	
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
		bind(Boolean.class).annotatedWith(Names.named("endlessTask")).toInstance(Boolean.TRUE);
		bind(TimeUnit.class).annotatedWith(Names.named("schedulerResolution")).toInstance(TimeUnit.SECONDS);
		
		bind(ArchiveDomainTask.Factory.class).to(ArchiveDomainTask.FactoryImpl.class);
		bind(ArchiveSchedulerBus.class);
		Multibinder<ArchiveSchedulerBus.Client> busClients = Multibinder.newSetBinder(binder(), ArchiveSchedulerBus.Client.class);
		busClients.addBinding().to(ArchiveDaoTracking.class);
		busClients.addBinding().to(RunningArchiveTracking.class);
		
		bindImapArchiveServices();
	}
	
	private void bindImapArchiveServices() {
		bind(OnlyOnePerDomainMonitorFactory.class).to(OnlyOnePerDomainMonitorFactoryImpl.class);
		bind(ArchiveSchedulingService.class);
		bind(DomainConfigurationService.class);
		bind(SchedulingDatesService.class);
		bind(UUIDFactory.class);
		bind(LoggerFactory.class);
		bind(ArchiveTreatmentRunId.Factory.class);
		bind(ArchiveService.class).to(ArchiveServiceImpl.class);
		bind(ArchiveScheduler.class);
		bind(ArchiveSchedulerQueue.class);
		bind(DateTimeProvider.class).toInstance(DateTimeProvider.SYSTEM_UTC);
		bind(LoggerFileNameService.class).to(LoggerFileNameServiceImpl.class);
		bind(StoreClientFactory.class);
		bind(DomainClient.class).to(DomainClientImpl.class);
	}
	
	public static class ImapArchiveServletModule extends ServletModule {

		public final static String URL_PREFIX = "/imap-archive/service/v1";
		public final static String URL_PATTERN = URL_PREFIX + "/*";
		public final static String URL_HEALTHCHECK_PREFIX = "/imap-archive/healthcheck";
		public final static String URL_HEALTHCHECK_PATTERN = URL_HEALTHCHECK_PREFIX + "/*";
		
		@Override
		protected void configureServlets() {
			filter(URL_PATTERN).through(AuthenticationFilter.class);
			serve(URL_PATTERN).with(ImapArchiveServicesContainer.class);
			serve(URL_HEALTHCHECK_PATTERN).with(ImapArchiveHealthcheckContainer.class);
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
					.register(DomainBasedSubResource.class)
					.register(ConfigurationResource.class)
					.register(TreatmentsResource.class)
					.register(ImapArchiveObjectMapper.class));
		}
		
	}
	
	@Singleton
	public static class ImapArchiveHealthcheckContainer extends ServletContainer {
		
		@Inject
		public ImapArchiveHealthcheckContainer(Injector injector) {
			super(new JerseyResourceConfig(injector)
					.register(HealthcheckHandler.class)
					.register(CyrusStatusHandler.class));
		}
		
	}
	
	public static class LoggerModule extends AbstractModule {

		private static final String LOG_PATH = "/var/log/obm-imap-archive/";
		public static final String TASK = "TASK";
		
		@Override
		protected void configure() {
			install(new org.obm.configuration.module.LoggerModule());
			bind(Logger.class).annotatedWith(Names.named(TASK)).toInstance(org.slf4j.LoggerFactory.getLogger(TASK));
			bind(String.class).annotatedWith(Names.named("logPath")).toInstance(LOG_PATH);
		}
		
	}
}
