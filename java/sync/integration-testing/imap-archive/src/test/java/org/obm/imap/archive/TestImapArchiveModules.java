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

import org.apache.http.client.HttpClient;
import org.joda.time.DateTime;
import org.obm.Configuration;
import org.obm.StaticConfigurationService;
import org.obm.configuration.DomainConfiguration;
import org.obm.configuration.TransactionConfiguration;
import org.obm.dao.utils.DaoTestModule;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.mail.greenmail.GreenMailProviderModule;
import org.obm.server.ServerConfiguration;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.date.DateProvider;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class TestImapArchiveModules {
	
	public static final DateTime LOCAL_DATE_TIME = new DateTime(2014, 6, 18, 0, 0);

	public static class Simple extends AbstractModule {
	
		@Override
		protected void configure() {
			ServerConfiguration config = ServerConfiguration.defaultConfiguration();
			install(Modules.override(new ImapArchiveModule(config)).with(
				new DaoTestModule(),
				new LocalLocatorModule(),
				new ObmSyncModule(),
				new TransactionalModule(),
				new DateProviderModule()
			));
		}
	}
	
	public static class WithGreenmail extends AbstractModule {

		@Override
		protected void configure() {
			install(Modules.override(new Simple()).with(new AbstractModule() {

				@Override
				protected void configure() {
					install(new GreenMailProviderModule());
					bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(3600);
				}})
			);
		}
	}
	
	public static class LocalLocatorModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(LocatorService.class).toInstance(new LocatorService() {
				
				@Override
				public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
					return "localhost";
				}
			});
		}
	}
    
	public static class ObmSyncModule extends AbstractModule {

	 	public class FakeLoginClient extends LoginClient {

	 		protected FakeLoginClient(String origin, DomainConfiguration domainConfiguration, SyncClientAssert syncClientAssert, Locator locator, Logger obmSyncLogger, HttpClient httpClient) {
 	 			super(origin, domainConfiguration, syncClientAssert, locator, obmSyncLogger, httpClient);
 	 		}

 	 		@Override
 	 		public AccessToken trustedLogin(String loginAtDomain, String password) throws AuthFault {
 	 			return new AccessToken(1, "origin");
 	 		}
 	 	}

 	 	public class FakeLoginClientFactory extends LoginClient.Factory {

 	 		public FakeLoginClientFactory() {
 	 			super(null, null, null, null, null);
 	 		}

 	 		@Override
 	 		public LoginClient create(HttpClient httpClient) {
 	 			return new FakeLoginClient(origin, domainConfiguration, syncClientAssert, locator, obmSyncLogger, httpClient);
 	 		}
 	 	}

 	 	@Override
 	 	protected void configure() {
 	 		bind(LoginClient.Factory.class).toInstance(new FakeLoginClientFactory());
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