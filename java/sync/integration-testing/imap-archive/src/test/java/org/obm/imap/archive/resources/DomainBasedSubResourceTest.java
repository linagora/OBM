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
package org.obm.imap.archive.resources;

import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.jayway.restassured.RestAssured.given;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class DomainBasedSubResourceTest {

	private ClientDriverRule driver = new ClientDriverRule();
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new GuiceRule(this, new TestImapArchiveModules.Simple(driver, temporaryFolder)))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));
	
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	
	@Before
	public void setUp() throws Exception {
		new Expectations(driver)
			.expectTrustedLogin(ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309"));
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void getDomainConfigurationShouldReturnBadRequestOnInvalidUuid() {
		given()
			.port(server.getHttpPort())
			.param("login", "admin")
			.param("password", "trust3dToken")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.BAD_REQUEST.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/toto/configuration");
	}
	
	@Test
	public void getDomainConfigurationShouldReturnNotFoundOnAbsentDomain() {
		driver.addExpectation(
				onRequestTo("/obm-sync/provisioning/v1/domains/56077db7-ffdc-4e47-8fdd-40c69884bee6"),
				giveEmptyResponse().withStatus(Status.NOT_FOUND.getStatusCode())
				);
		given()
			.port(server.getHttpPort())
			.param("login", "admin")
			.param("password", "trust3dToken")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/56077db7-ffdc-4e47-8fdd-40c69884bee6/configuration");
	}
	
}
