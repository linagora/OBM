/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.provisioning.profile;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.domainUrl;

import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.provisioning.TestingProvisioningModule;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.RestAssured;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ProfileIntegrationTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new TestingProvisioningModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "dbInitialScript.sql"));

	@Inject private H2InMemoryDatabase db;
	@Inject private WebServer server;
	
	private URL baseURL;
	
	@Before
	public void init() throws Exception {
		server.start();
		baseURL = new URL("http", "localhost", server.getHttpPort(), "/");
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void testGetProfilesWhenDomainExists() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"[{\"id\":1,\"url\":\"/" + obmDomainUuid.get() + "/profiles/1\"}," +
				"{\"id\":2,\"url\":\"/" + obmDomainUuid.get() + "/profiles/2\"}," +
				"{\"id\":4,\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/profiles/4\"}]")).
		when()
			.get("/profiles/");
	}
	
	@Test
	public void testGetProfilesWhenDomainExistsButNoProfile() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("68936f0f-2bb5-447c-87f5-efcd46f58122");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"[{\"id\":4,\"url\":\"/68936f0f-2bb5-447c-87f5-efcd46f58122/profiles/4\"}]")).
		when()
			.get("/profiles/");
	}
	
	@Test
	public void testGetProfilesWhenDoNotDomainExists() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("99999999-9999-9999-9999-e50cfbfec5b6");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles/");
	}
	
	@Test
	public void testGetProfileNameWhenProfileExists() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{\"name\":\"admin\"}")).
		when()
			.get("/profiles/1");
	}
	
	@Test
	public void testGetProfileNameWhenProfileExistsInAnotherDomain() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles/1");
	}
	
	@Test
	public void testGetProfileNameWhenProfileDoNotExists() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles/1000");
	}
}
