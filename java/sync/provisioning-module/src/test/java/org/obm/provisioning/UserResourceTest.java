/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.dao.UserDao;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(UserResourceTest.Env.class)
public class UserResourceTest {
	public static class Env extends ProvisioningService {
		private IMocksControl mocksControl = createControl();
		
		public Env() {
		}

		@Override
		protected void configureServlets() {
			super.configureServlets();
			bind(IMocksControl.class).toInstance(mocksControl);
			bind(UserDao.class).toInstance(mocksControl.createMock(UserDao.class));
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
	private IMocksControl mocksControl;
	
	@Inject
	private UserDao userDao;
	
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
	public void testUnknownUrl() throws Exception {
		HttpResponse httpResponse = get("/users/a/b");
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(404);
	}
	
	@Test
	public void testGetAUser() throws Exception {
		expect(userDao.getUser(1)).andReturn(fakeUser());
		
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/1");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(200);
	}
	
	@Test
	public void testGetNonExistingUser() throws Exception {
		HttpResponse httpResponse = get("/users/123");
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(204);
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	private ObmUser fakeUser() {
		return ObmUser.builder()
				.domain(ObmDomain.builder().name("domain").id(1).build())
				.uid(1)
				.login("user1")
				.lastName("Doe")
				//.profile("Utilisateurs")	// Not implemented yet in ObmUser
				.firstName("Jésus")
				.commonName("John Doe")
				//.kind("")					// Not implemented yet in ObmUser
				.title("title")
				.description("description")
				//.company("")				// Not implemented yet in ObmUser
				.service("service")
				//.direction()				// Not implemented yet in ObmUser
				.address1("address1")
				.address2("address2")
				.town("town")
				.zipCode("zipCode")
				//.business_zipcode()		// Not implemented yet in ObmUser
				//.country()				// Not implemented yet in ObmUser
				//.phones()					// Not implemented yet in ObmUser
				.mobile("mobile")
				//.faxes()					// Not implemented yet in ObmUser
				//.mail_quota()				// Not implemented yet in ObmUser
				//.mail_server()			// Not implemented yet in ObmUser
				.emailAndAliases("mails")
				.timeCreate(DateUtils.date("2013-06-11T14:00:00"))
				.timeUpdate(DateUtils.date("2013-06-11T15:00:00"))
				//.groups()					// Not implemented yet in ObmUser
				.build();
				
				
	}
	
	private String expectedUser() {
		String json = 
				"[" +
					"{id: 1," +
					"login: user1," +
					"lastname: Doe," +
					"profile: Utilisateurs," +
					"firstname: John," +
					"commonname: John Doe," +
					"password: doe," +
					"kind: kind," +
					"title: title," +
					"description: description," +
					"company: company," +
					"service: service," +
					"direction: direction," +
					"addresses: addresse," +
					"town: town," +
					"zipcode: zipcode," +
					"business_zipcode: business_zipcode," +
					"country: country," +
					"phones: phones," +
					"mobile: mobile," +
					"faxes: faxes" +
					"mail_quota: mail_quota," +
					"mail_server: mail_server," +
					"mails: mails," +
					"timecreate: timecreate," +
					"timeupdate: timeupdate," +
					"groups: groups}" +
				"]";
		
		return json;
	}
	
	protected HttpResponse get(String path) throws Exception {
		return createRequest(path).execute().returnResponse();
	}
	
	protected Request createRequest(String path) {
		return Request.Get(baseUrl + path);
	}
}
