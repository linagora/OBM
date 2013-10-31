/* ***** BEGIN LICENSE BLOCK *****
 * 
<<<<<<< HEAD
 * Copyright (C) 2011-2012  Linagora
=======
 * Copyright (C) 2013 Linagora
>>>>>>> dbb8bd3... - [OBMFULL-5147] write test for Http Post body params encoding
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
package org.obm.sync.client.impl;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;


@RunWith(SlowFilterRunner.class)
public class AbstractClientImplTest {

	private TestServlet testServlet;
	private ImmutableMultimap<String, String> params;
	private IMocksControl control;
	private SyncClientException syncClientException;
	private Logger logger;
	private CloseableHttpClient httpClient;
	private Locator locator;
	private TestClient client;
	private int serverPort;
	private AccessToken at;
	private Server server;
	
	@Before
	public void setup() throws Exception {
		control = createControl();
		syncClientException = control.createMock(SyncClientException.class);
		logger = control.createMock(Logger.class);
		locator = control.createMock(Locator.class);
		httpClient = HttpClientBuilder.create().build();
		client = new TestClient(syncClientException, logger, httpClient);
		
		testServlet = new TestServlet();
		server = new Server(0);
		Context root = new Context(server, "/", Context.SESSIONS);
		root.addServlet(new ServletHolder(testServlet), "/*");
		server.start();
		serverPort = server.getConnectors()[0].getLocalPort();

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		at = ToolBox.mockAccessToken(control);
		expect(locator.backendUrl(defaultObmUser.getLogin() + "@" + defaultObmUser.getDomain().getName())).andReturn("http://localhost:" + serverPort + "/test");
	}

	@After
	public void shutdown() throws Exception {
		server.stop();
		httpClient.close();
	}
	
	@Test
	public void testBodyForm() {
		control.replay();
		ImmutableMultimap<String, String> sentParams = ImmutableMultimap.of("foo", "bar");
		client.callServlet(at, sentParams);
		control.verify();
		assertThat(params).isEqualTo(sentParams);
	}

	@Test
	public void testBodyFormMultipleValues() {
		control.replay();
		ImmutableMultimap<String, String> sentParams = ImmutableMultimap.of("foo", "bar", "foo", "taz");
		client.callServlet(at, sentParams);
		control.verify();
		assertThat(params).isEqualTo(sentParams);
	}
	
	@Test
	public void testBodyFormValueEncoding() {
		control.replay();
		ImmutableMultimap<String, String> sentParams = ImmutableMultimap.of("key", "élément");
		client.callServlet(at, sentParams);
		control.verify();
		assertThat(params).isEqualTo(sentParams);
	}
	
	@Test
	public void testBodyFormKeyEncoding() {
		control.replay();
		ImmutableMultimap<String, String> sentParams = ImmutableMultimap.of("clé", "element");
		client.callServlet(at, sentParams);
		control.verify();
		assertThat(params).isEqualTo(sentParams);
	}
	
	@Test
	public void testBodyFormBothEncoding() {
		control.replay();
		ImmutableMultimap<String, String> sentParams = ImmutableMultimap.of("clé", "élément");
		client.callServlet(at, sentParams);
		control.verify();
		assertThat(params).isEqualTo(sentParams);
	}
	
	private final class TestClient extends AbstractClientImpl {
		private TestClient(
				SyncClientException exceptionFactory, Logger obmSyncLogger,
				HttpClient httpClient) {
			super(exceptionFactory, obmSyncLogger, httpClient);
		}

		@Override
		protected Locator getLocator() {
			return locator;
		}
		
		public void callServlet(AccessToken at, Multimap<String, String> params) {
			executeVoid(at, "/test", params);
		}
	}

	private class TestServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			Builder<String, String> builder = ImmutableMultimap.<String, String>builder();
			Enumeration<String> names = req.getParameterNames();
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				builder.putAll(name, req.getParameterValues(name));
			}
			params = builder.build();
		}
	}
	
}
