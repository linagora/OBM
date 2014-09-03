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

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.rules.TemporaryFolder;
import org.obm.Configuration;
import org.obm.StaticConfigurationService;
import org.obm.configuration.TransactionConfiguration;
import org.obm.dao.utils.DaoTestModule;
import org.obm.domain.dao.UserSystemDao;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.logging.LoggerFileNameService;
import org.obm.imap.archive.scheduling.AbstractArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus;
import org.obm.imap.archive.scheduling.OnlyOnePerDomainMonitorFactory;
import org.obm.imap.archive.scheduling.OnlyOnePerDomainMonitorFactory.OnlyOnePerDomainMonitorFactoryImpl;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.mail.greenmail.GreenMailProviderModule;
import org.obm.push.utils.UUIDFactory;
import org.obm.server.ServerConfiguration;
import org.obm.sync.date.DateProvider;
import org.obm.sync.locators.Locator;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.Monitor;

public class TestImapArchiveModules {
	
	public static final UUID uuid = UUID.fromString("08c00ba3-fb00-48ac-a077-24b47c123692");
	public static final UUID uuid2 = UUID.fromString("e72906d1-4b6f-4727-8be8-3e78441623ea");
	
	public static final DateTime LOCAL_DATE_TIME = new DateTime()
		.withZone(DateTimeZone.UTC)
		.withYear(2014)
		.withMonthOfYear(6)
		.withDayOfMonth(18)
		.withHourOfDay(0)
		.withMinuteOfHour(0)
		.withSecondOfMinute(0)
		.withMillisOfSecond(0);
	
	public static class Simple extends AbstractModule {
	
		private final ClientDriverRule obmSyncHttpMock;
		private final ServerConfiguration config;
		private final Provider<TemporaryFolder> temporaryFolder;

		public Simple(ClientDriverRule obmSyncHttpMock, Provider<TemporaryFolder> temporaryFolder) {
			this.obmSyncHttpMock = obmSyncHttpMock;
			this.temporaryFolder = temporaryFolder;
			this.config = ServerConfiguration.builder()
					.lifeCycleHandler(ImapArchiveModule.STARTUP_HANDLER_CLASS)
					.build();
		}
		
		@Override
		protected void configure() {
			install(Modules.override(new ImapArchiveModule(config)).with(
				new DaoTestModule(),
				new TransactionalModule(),
				new TimeBasedModule(),
				new StaticUUIDModule(),
				logFileModule(),
				new SchedulerModule(),
				new LocalLocatorModule(obmSyncHttpMock.getBaseUrl() + "/obm-sync"),
				new AbstractModule() {
					
					@Override
					protected void configure() {
						IMocksControl control = EasyMock.createControl();
						bind(IMocksControl.class).toInstance(control);
						bind(UserSystemDao.class).toInstance(control.createMock(UserSystemDao.class));

						Multibinder<ArchiveSchedulerBus.Client> busClients = Multibinder.newSetBinder(binder(), ArchiveSchedulerBus.Client.class);
						busClients.addBinding().to(FutureSchedulerBusClient.class);
					}
				}
			));
		}

		protected AbstractModule logFileModule() {
			return new AbstractModule() {
				
				@Override
				protected void configure() {
					bind(LoggerFileNameService.class).toInstance(new TemporaryLoggerFileNameService(temporaryFolder));
				}
			};
		}
	}
	
	public static class WithGreenmail extends AbstractModule {

		private ClientDriverRule obmSyncHttpMock;
		private Provider<TemporaryFolder> temporaryFolder;

		public WithGreenmail(ClientDriverRule obmSyncHttpMock, Provider<TemporaryFolder> temporaryFolder) {
			this.obmSyncHttpMock = obmSyncHttpMock;
			this.temporaryFolder = temporaryFolder;
		}
		
		@Override
		protected void configure() {
			install(Modules.override(new Simple(obmSyncHttpMock, temporaryFolder)).with(new AbstractModule() {

				@Override
				protected void configure() {
					install(new GreenMailProviderModule());
					bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(3600);
				}})
			);
		}
	}

	public static class WithTestingMonitor extends AbstractModule {

		private ClientDriverRule obmSyncHttpMock;
		private Provider<TemporaryFolder> temporaryFolder;

		public WithTestingMonitor(ClientDriverRule obmSyncHttpMock, Provider<TemporaryFolder> temporaryFolder) {
			this.obmSyncHttpMock = obmSyncHttpMock;
			this.temporaryFolder = temporaryFolder;
		}
		
		@Override
		protected void configure() {
			install(Modules.override(new Simple(obmSyncHttpMock, temporaryFolder)).with(new AbstractModule() {

				@Override
				protected void configure() {
					TestingOnlyOnePerDomainMonitorFactory factory = new TestingOnlyOnePerDomainMonitorFactory();
					bind(OnlyOnePerDomainMonitorFactory.class).toInstance(factory);
					bind(TestingOnlyOnePerDomainMonitorFactory.class).toInstance(factory);
				}})
			);
		}

		public static class TestingOnlyOnePerDomainMonitorFactory extends OnlyOnePerDomainMonitorFactoryImpl {
			
			Monitor<AbstractArchiveDomainTask> monitor;

			@Override
			public Monitor<AbstractArchiveDomainTask> create() {
				monitor = super.create();
				return monitor;
			}
			
			public Monitor<AbstractArchiveDomainTask> get() {
				return monitor;
			}
		}

	}
	
	public static class LocalLocatorModule extends AbstractModule {

		private String obmSyncBaseUrl;

		public LocalLocatorModule(String obmSyncBaseUrl) {
			this.obmSyncBaseUrl = obmSyncBaseUrl;
		}
		
		@Override
		protected void configure() {
			bind(LocatorService.class).toInstance(new LocatorService() {
				
				@Override
				public String getServiceLocation(String serviceSlashProperty,
						String loginAtDomain) throws LocatorClientException {
					if (serviceSlashProperty.equals("mail/imap_frontend")) {
						return "localhost";
					}
					throw new IllegalStateException();
				}
			});
			bind(Locator.class).toInstance(new TestLocator(obmSyncBaseUrl));
		}
	}

	public static class TestLocator extends Locator {
		
		private String obmSyncBaseUrl;

		public TestLocator(String obmSyncBaseUrl) {
			super(null, null);
			this.obmSyncBaseUrl = obmSyncBaseUrl;
		}
		
		@Override
		public String backendUrl(String loginAtDomain) throws LocatorClientException {
			return obmSyncBaseUrl;
		}
		
		@Override
		public String backendBaseUrl(String loginAtDomain) throws LocatorClientException {
			return obmSyncBaseUrl;
		}
	}
	
	public static class TransactionalModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(TransactionConfiguration.class).toInstance(new StaticConfigurationService.Transaction(new Configuration.Transaction()));
		}
	}
	
	public static class TimeBasedModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(DateProvider.class).to(TestDateProvider.class);
			bind(DateTimeProvider.class).to(TestDateProvider.class);
			bind(TimeUnit.class).annotatedWith(Names.named("schedulerResolution")).toInstance(TimeUnit.MILLISECONDS);
		}
		
		@Singleton
		public static class TestDateProvider implements DateTimeProvider, DateProvider {

			private DateTime current;

			public TestDateProvider() {
				this.current = LOCAL_DATE_TIME;
			}
			
			public void setCurrent(DateTime current) {
				this.current = current;
			}
			
			@Override
			public Date getDate() {
				return current.toDate();
			}

			@Override
			public DateTime now() {
				return current;
			}
			
		}
	}
	
	public static class StaticUUIDModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(UUIDFactory.class).toInstance(new UUIDFactory() {

				boolean use1 = true;
				
				@Override
				public synchronized UUID randomUUID() {
					if (use1) {
						use1 = !use1;
						return uuid;
					} else {
						use1 = !use1;
						return uuid2;
					}
				}
			});
		}
	}
	
	public static class SchedulerModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Boolean.class).annotatedWith(Names.named("endlessTask")).toInstance(Boolean.FALSE);
		}
	}
	
	public static class TemporaryLoggerFileNameService implements LoggerFileNameService {

		private final Provider<TemporaryFolder> temporaryFolderProvider;
		private String loggerFileName;

		public TemporaryLoggerFileNameService(Provider<TemporaryFolder> temporaryFolderProvider) {
			this.temporaryFolderProvider = temporaryFolderProvider;
		}
		
		@Override
		public String loggerFileName(ArchiveTreatmentRunId runId) {
			try {
				TemporaryFolder temporaryFolder = temporaryFolderProvider.get();
				createIfNone(temporaryFolder);
				
				loggerFileName = temporaryFolder.getRoot().getAbsolutePath() + "/" + runId.serialize() + ".log";
				return loggerFileName;
			} catch (IOException e) {
				Throwables.propagate(e);
				return null;
			}
		}

		private void createIfNone(TemporaryFolder temporaryFolder) throws IOException {
			try {
				temporaryFolder.getRoot();
			} catch (IllegalStateException e) {
				temporaryFolder.create();
			}
		}

	}
}