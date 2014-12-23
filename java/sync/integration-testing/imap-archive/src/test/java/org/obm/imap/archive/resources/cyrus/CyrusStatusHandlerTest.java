/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.resources.cyrus;

import static com.jayway.restassured.RestAssured.given;
import static org.obm.imap.archive.DBData.admin;
import static org.obm.imap.archive.DBData.usera;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.icegreen.greenmail.util.GreenMail;

public class CyrusStatusHandlerTest {
	
	private ClientDriverRule driver = new ClientDriverRule();
	
	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.WithGreenmail(driver, new Provider<TemporaryFolder>() {

				@Override
				public TemporaryFolder get() {
					return temporaryFolder;
				}
				
			})))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));
	
	@Inject TemporaryFolder temporaryFolder;
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	@Inject GreenMail imapServer;

	@After
	public void tearDown() throws Exception {
		server.stop();
		imapServer.stop();
	}
	
	@Test
	public void testStatusIs200WhenImapIsUp() throws Exception {
		imapServer.setUser("cyrus", "cyrus");
		imapServer.start();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", admin.getLogin())
			.param("password", admin.getPassword())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsUpButCyrusUserNotFound() throws Exception {
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", admin.getLogin())
			.param("password", admin.getPassword())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsUpButLoginFails() throws Exception {
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", admin.getLogin())
			.param("password", admin.getPassword())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsDown() throws Exception {
		server.start();
		imapServer.stop();
		
		given()
			.port(server.getHttpPort())
			.param("login", admin.getLogin())
			.param("password", admin.getPassword())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/status");
	}
	
	@Test
	public void getPartitionShouldWork() throws Exception {
		imapServer.setUser("cyrus", "cyrus");
		imapServer.start();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("test_user", usera.getLogin())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/partition");
	}
	
	@Test
	public void getPartitionShouldThrowWhenImapIsDown() throws Exception {
		imapServer.stop();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("test_user", usera.getLogin())
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/cyrus/partition");
	}
}
