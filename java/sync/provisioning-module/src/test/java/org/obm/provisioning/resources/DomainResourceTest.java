/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.provisioning.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

import com.google.common.collect.ImmutableList;
import com.jayway.restassured.RestAssured;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class DomainResourceTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testEmptyList() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expect(domainDao.list()).andReturn(Collections.<ObmDomain> emptyList());
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(equalTo("[]")).
		when()
			.get("/domains");

		mocksControl.verify();
	}

	@Test
	public void testNullList() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expect(domainDao.list()).andReturn(null);
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(equalTo("[]")).
		when()
			.get("/domains");

		mocksControl.verify();
	}

	@Test
	public void testList() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expect(domainDao.list()).andReturn(ImmutableList.<ObmDomain> of(domain, ToolBox.getDefaultObmDomain()));
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(equalTo(
				"[" +
						"{" +
							"\"id\":\"a3443822-bb58-4585-af72-543a287f7c0e\"," +
							"\"url\":\"/domains/a3443822-bb58-4585-af72-543a287f7c0e\"" +
						"}," +
						"{" +
							"\"id\":\"ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6\"," +
							"\"url\":\"/domains/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6\"" +
						"}" +
				"]")).
		when()
			.get("/domains");

		mocksControl.verify();
	}

	@Test
	public void testListOnError() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expect(domainDao.list()).andThrow(new RuntimeException("foo"));
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
			.body(equalTo(
				"{" +
						"\"message\":\"foo\"," +
						"\"type\":\"java.lang.RuntimeException\"" +
				"}")).
		when()
			.get("/domains");
		
		mocksControl.verify();
	}

	@Test
	public void testGet() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expectDomain();
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(equalTo(
				"{" +
					"\"id\":\"a3443822-bb58-4585-af72-543a287f7c0e\"," +
					"\"name\":\"domain\"," +
					"\"label\":null," +
					"\"aliases\":[\"domain.com\"]" +
				"}")).
		when()
			.get("/domains/" + domain.getUuid().get());
		
		mocksControl.verify();
	}

	@Test
	public void testGetOnUnknownDomain() throws Exception {
		RestAssured.baseURI = baseUrl;
		expectSuccessfulAuthentication("user", "password");
		expectNoDomain();
		mocksControl.replay();

		given()
			.auth().basic("user@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/domains/" + domain.getUuid().get());

		mocksControl.verify();
	}

}
