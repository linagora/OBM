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

import java.util.Date;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.obm.Configuration;
import org.obm.StaticConfigurationService;
import org.obm.configuration.TransactionConfiguration;
import org.obm.dao.utils.DaoTestModule;
import org.obm.domain.dao.UserSystemDao;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.mail.greenmail.GreenMailProviderModule;
import org.obm.server.ServerConfiguration;
import org.obm.sync.date.DateProvider;
import org.obm.sync.locators.Locator;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class TestImapArchiveModules {
	
	public static final DateTime LOCAL_DATE_TIME = new DateTime(2014, 6, 18, 0, 0);

	public static class Simple extends AbstractModule {
	
		private final ClientDriverRule obmSyncHttpMock;

		public Simple(ClientDriverRule obmSyncHttpMock) {
			this.obmSyncHttpMock = obmSyncHttpMock;
		}
		
		@Override
		protected void configure() {
			ServerConfiguration config = ServerConfiguration.defaultConfiguration();
			install(Modules.override(new ImapArchiveModule(config)).with(
				new DaoTestModule(),
				new TransactionalModule(),
				new DateProviderModule(),
				new LocalLocatorModule(obmSyncHttpMock.getBaseUrl() + "/obm-sync"),
				new AbstractModule() {
					
					@Override
					protected void configure() {
						IMocksControl control = EasyMock.createControl();
						bind(IMocksControl.class).toInstance(control);
						bind(UserSystemDao.class).toInstance(control.createMock(UserSystemDao.class));
					}
				}
			));
		}
	}
	
	public static class WithGreenmail extends AbstractModule {

		private ClientDriverRule obmSyncHttpMock;

		public WithGreenmail(ClientDriverRule obmSyncHttpMock) {
			this.obmSyncHttpMock = obmSyncHttpMock;
		}
		
		@Override
		protected void configure() {
			install(Modules.override(new Simple(obmSyncHttpMock)).with(new AbstractModule() {

				@Override
				protected void configure() {
					install(new GreenMailProviderModule());
					bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(3600);
				}})
			);
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
	}
	
	public static class TransactionalModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(TransactionConfiguration.class).toInstance(new StaticConfigurationService.Transaction(new Configuration.Transaction()));
		}
	}
	
	public static class DateProviderModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(DateProvider.class).toInstance(new DateProvider() {
				
				@Override
				public Date getDate() {
					return LOCAL_DATE_TIME.toDate();
				}
			});
		}
	}
}