package org.obm.provisioning;
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

import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(BatchResourceTest.Env.class)
public class BatchResourceTest {
	
	public static class Env extends ProvisioningService {
		
		public Env() {
		}

		@Override
		protected void configureServlets() {
			super.configureServlets();
		}
	
		@Provides @Singleton
		protected Server createServer() {
			Server server = new Server(0);
			Context root = new Context(server, "/", Context.SESSIONS);
			
			root.addFilter(GuiceFilter.class, "/*", 0);
			root.addServlet(DefaultServlet.class, "/*");
			
			return server;
		}
	}
	
	@Inject
	private Server server;
	
	protected String baseUrl;
	protected int serverPort;
	
	@Before
	public void setUp() throws Exception {
		server.start();
		serverPort = server.getConnectors()[0].getLocalPort();
		baseUrl = "http://localhost:" + serverPort + ProvisioningService.PROVISIONING_URL_PREFIX;
	}
	
	@Test
	public void test() throws Exception {
		HttpResponse httpResponse = get("/batches/12");
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(404);
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	protected HttpResponse get(String path) throws Exception {
		return createRequest(path).execute().returnResponse();
	}
	
	protected Request createRequest(String path) {
		return Request.Get(baseUrl + path);
	}
	
}
