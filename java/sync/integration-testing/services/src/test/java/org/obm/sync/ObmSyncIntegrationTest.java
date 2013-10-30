/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync;

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.obm.Configuration;
import org.obm.configuration.ConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.ObmSyncStaticConfigurationService.ObmSyncConfiguration;
import org.obm.sync.calendar.CalendarBindingImplIntegrationTestModule;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObmSyncIntegrationTest {

	public static final String ARCHIVE = "ObmSyncIntegrationTestArchive";
	
	protected ObmSyncConfiguration configuration;
	protected LoginClient loginClient;
	protected CalendarClient calendarClient;
	protected BookClient bookClient;
	protected CloseableHttpClient httpClient;
	protected Locator locator;
	protected SyncClientException exceptionFactory;
	protected Logger logger;
	protected BasicCookieStore cookieStore;

	@Before
	public void setUp() {
		logger = LoggerFactory.getLogger(ObmSyncIntegrationTest.class);
		configuration = new ObmSyncConfiguration(new Configuration(), new Configuration.ObmSync());
		exceptionFactory = new SyncClientException();
		cookieStore = new BasicCookieStore();
		httpClient = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.build();
	}

	@After
	public void teardown() throws IOException {
		httpClient.close();
	}
	
	protected void configureTest(URL baseUrl) {
		LocatorService locatorService = arquillianLocatorService(baseUrl);
		locator = new Locator(configuration, locatorService) {};
		
		CalendarClient.Factory calendarClientFactory = new CalendarClientFactory(exceptionFactory, locator, logger);
		BookClient.Factory bookClientFactory = new BookClientFactory(exceptionFactory, locator, logger);
		
		loginClient = createLoginClient(configuration, exceptionFactory, locator, logger, httpClient);
		calendarClient = calendarClientFactory.create(httpClient);
		bookClient = bookClientFactory.create(httpClient);
	}

	protected LoginClient createLoginClient(ConfigurationService configuration, 
			SyncClientException exceptionFactory, 
			Locator locator, 
			Logger logger, 
			HttpClient httpClient) {
		
		LoginClient.Factory loginClientFactory = new LoginClientFactory("integration-testing", configuration, exceptionFactory, locator, logger);
		return loginClientFactory.create(httpClient);
	}
	
	private class LoginClientFactory extends LoginClient.Factory {

		protected LoginClientFactory(String origin,
				ConfigurationService configurationService,
				SyncClientException syncClientException, Locator locator,
				Logger obmSyncLogger) {
			super(origin, configurationService, syncClientException, locator, obmSyncLogger);
		}
	}
	
	private class CalendarClientFactory extends CalendarClient.Factory {

		protected CalendarClientFactory(
				SyncClientException syncClientException, Locator locator,
				Logger obmSyncLogger) {
			super(syncClientException, locator, obmSyncLogger);
		}
	}
	
	private class BookClientFactory extends BookClient.Factory {

		protected BookClientFactory(SyncClientException syncClientException,
				Locator locator, Logger obmSyncLogger) {
			super(syncClientException, locator, obmSyncLogger);
		}
	}
	
	private LocatorService arquillianLocatorService(final URL baseURL) {
		return new LocatorService() {
			
			@Override
			public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
				return baseURL.toExternalForm();
			}
		};
	}

	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE) 
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils
				.buildWebArchive(CalendarBindingImplIntegrationTestModule.class)
				.addAsResource("sql/org/obm/sync/calendar/h2.sql", H2GuiceServletContextListener.INITIAL_DB_SCRIPT)
				.addClasses(CalendarBindingImplIntegrationTestModule.class);
	}
}