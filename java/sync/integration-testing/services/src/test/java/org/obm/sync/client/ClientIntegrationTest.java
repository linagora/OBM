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
package org.obm.sync.client;

import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.sync.ObmSyncStaticConfigurationService.ObmSyncConfiguration;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarIntegrationTest;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class ClientIntegrationTest extends CalendarIntegrationTest {

	private LoginClient loginClientWithCookie;
	public DefaultHttpClient httpClient;

	@Before
	@Override
	public void setUp() {
		Logger logger = LoggerFactory.getLogger(CalendarIntegrationTest.class);
		ObmSyncConfiguration configuration = new ObmSyncConfiguration(new Configuration(), new Configuration.ObmSync());
		SyncClientException exceptionFactory = new SyncClientException();
		LocatorService locatorService = arquillianLocatorService();
		Locator locator = new Locator(configuration, locatorService) {};
		httpClient = new DefaultHttpClient();
		
		LoginClient.Factory loginClientFactory = new LoginClientFactory("integration-testing", configuration, exceptionFactory, locator, logger);
		loginClientWithCookie = loginClientFactory.create(httpClient);
	}

	protected LocatorService arquillianLocatorService() {
		return new LocatorService() {
			
			@Override
			public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
				return baseURL.toExternalForm();
			}
		};
	}
	
	@Test
	@RunAsClient
	public void testConnectionKeepsCookie() throws Exception {
		AccessToken login = loginClientWithCookie.login("user1@domain.org", "user1");
		
		loginClientWithCookie.logout(login);
	}

	private class LoginClientFactory extends LoginClient.Factory {

		@Override
		public LoginClient create(HttpClient httpClient) {
			return new LoginClientWithCookie(origin, configurationService, syncClientException, locator, obmSyncLogger, httpClient);
		}

		protected LoginClientFactory(String origin,
				ConfigurationService configurationService,
				SyncClientException syncClientException, Locator locator,
				Logger obmSyncLogger) {
			super(origin, configurationService, syncClientException, locator, obmSyncLogger);
		}
	}
	
	private class LoginClientWithCookie extends LoginClient {

		private LoginClientWithCookie(String origin,
				ConfigurationService configurationService,
				SyncClientException syncClientException, Locator locator,
				Logger obmSyncLogger, HttpClient httpClient) {

			super(origin, configurationService, syncClientException, locator, obmSyncLogger, httpClient);
		}

		@Override
		protected Document execute(AccessToken token, String action, Multimap<String, String> parameters) {
			setCookie(action);
			return super.execute(token, action, parameters);
		}

		@Override
		protected void executeVoid(AccessToken at, String action, Multimap<String, String> parameters) {
			assertCookie(action);
			super.executeVoid(at, action, parameters);
		}
		
		private void setCookie(String action) {
			if ("/login/doLogin".equals(action)) {
				CookieStore cookieStore = ((DefaultHttpClient) httpClient).getCookieStore();
				cookieStore.addCookie(new BasicClientCookie("abc", "123"));
				((DefaultHttpClient) httpClient).setCookieStore(cookieStore);
			}
		}

		private void assertCookie(String action) {
			if ("/login/doLogout".equals(action)) {
				boolean foundMine = false;
				for (Cookie cookie : ((DefaultHttpClient) httpClient).getCookieStore().getCookies()) {
					if (cookie.getName().equals("abc") && cookie.getValue().equals("123")) {
						foundMine = true;
						break;
					}
				}
				assertThat(foundMine).isTrue();
			}
		}
	}
}