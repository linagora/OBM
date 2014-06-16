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

import static com.jayway.restassured.RestAssured.given;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.guice.GuiceRule;
import org.obm.jersey.injection.GuiceContainer;
import org.obm.server.EmbeddedServerModule;
import org.obm.server.ServerConfiguration;
import org.obm.server.WebServer;

import com.google.inject.servlet.ServletModule;

public class TwoWayInjection {

	@Rule public GuiceRule guiceRule = new GuiceRule(this, new GuiceModule()); 
	
	@Inject WebServer server;
	
	@Before
	public void setUp() throws Exception {
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void acceptedHandlerShouldReturnacceptedAnd69() {
		given()
			.port(server.getHttpPort())
		.expect()
			.statusCode(Status.ACCEPTED.getStatusCode())
			.body(Matchers.equalTo("69"))
		.when()
			.get("/accepted");
	}
	
	@Test
	public void acceptedHandlerShouldReturn70AtSecondCall() {
		given()
			.port(server.getHttpPort())
		.when()
			.get("/accepted");

		given()
			.port(server.getHttpPort())
		.expect()
			.statusCode(Status.ACCEPTED.getStatusCode())
			.body(Matchers.equalTo("70"))
		.when()
			.get("/accepted");
	}
	
	public static class GuiceModule extends ServletModule {
		
		@Override
		protected void configureServlets() {
			install(new EmbeddedServerModule(ServerConfiguration.defaultConfiguration()));
			bind(Servlet.class);
			bind(Integer.class).toInstance(69);
			serve("/*").with(GuiceContainer.class);
		}
	}
	
	@Path("/accepted")
	public static class Servlet {
		
		@Inject Service service;
		
		@GET
		public Response accepted() {
			return Response.accepted(service.value()).build();
		}
		
	}
	
	@Singleton
	public static class Service {
		
		@Inject Integer value;
		
		public Integer value() {
			return value++;
		}
		
	}
	
}
