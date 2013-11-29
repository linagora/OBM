/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import java.net.URL;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.obm.Configuration;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.module.LoggerModule;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.sync.ObmSyncStaticConfigurationService.ObmSyncConfiguration;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class ServicesClientModule extends AbstractModule {

	protected ObmSyncConfiguration configuration;
	protected CloseableHttpClient httpClient;
	protected SyncClientAssert exceptionFactory;
	protected Logger logger;
	protected BasicCookieStore cookieStore;

	@Override
	protected void configure() {
		logger = LoggerFactory.getLogger(ObmSyncIntegrationTest.class);
		configuration = new ObmSyncConfiguration(new Configuration(), new Configuration.ObmSync());
		exceptionFactory = new SyncClientAssert();
		cookieStore = new BasicCookieStore();
		httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
		
		bind(String.class).annotatedWith(Names.named("origin")).toInstance("integration-testing");
		bind(Logger.class).annotatedWith(Names.named(LoggerModule.OBM_SYNC)).toInstance(logger);
		bind(ConfigurationService.class).toInstance(configuration);
		bind(CloseableHttpClient.class).toInstance(httpClient);
		bind(LocatorService.class).to(ArquillianLocatorService.class);
	}

	@Provides
	CookiesFromClient createCookies(Locator locator) {
		return new CookiesFromClient(exceptionFactory, locator, cookieStore, logger, httpClient);
	}
	
	@Provides @Singleton
	BookClient createBookClient(BookClient.Factory bookClientFactory) {
		return bookClientFactory.create(httpClient);
	}
	
	@Provides @Singleton
	CalendarClient createCalendarClient(CalendarClient.Factory calendarClientFactory) {
		return calendarClientFactory.create(httpClient);
	}

	@Provides @Singleton
	LoginClient createLoginClient(LoginClient.Factory loginClientFactory) {
		return loginClientFactory.create(httpClient);
	}
	
	@Singleton
	public static class ArquillianLocatorService implements LocatorService {
		
		private URL baseUrl;
		
		public void configure(URL baseUrl) {
			this.baseUrl = baseUrl;
		}
		
		@Override
		public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
			Preconditions.checkState(baseUrl != null, "Your test must configure the locator first");
			return baseUrl.toExternalForm();
		}
	}
	
	public static class CookiesFromClient extends AbstractClientImpl {

		private final Locator locator;
		private final CookieStore cookieStore;

		public CookiesFromClient(SyncClientAssert exceptionFactory, Locator locator,
				CookieStore cookieStore, Logger obmSyncLogger, HttpClient httpClient) {
			super(exceptionFactory, obmSyncLogger, httpClient);
			this.locator = locator;
			this.cookieStore = cookieStore;
		}

		@Override
		protected Locator getLocator() {
			return locator;
		}
		
		public String getSid() {
			return FluentIterable.from(getCookies())
				.firstMatch(new Predicate<Cookie>() {

					@Override
					public boolean apply(Cookie input) {
						return input.getName().equals("JSESSIONID");
					}
				})
				.get()
				.getValue();
		}

		private List<Cookie> getCookies() {
			return cookieStore.getCookies();
		}
	}
}
