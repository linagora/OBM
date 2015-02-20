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
package org.obm.provisioning.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.containsString;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;


@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceGetUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testUnknownUrl() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/users/a/b");

		mocksControl.verify();
	}

	@Test
	public void testGetAUser() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.getByExtIdWithGroups(userExtId("1"), domain)).andReturn(fakeUser());
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(obmUserToJsonString())).
		when()
			.get("/users/1");
		
		mocksControl.verify();
	}

	@Test
	public void testGetAUserOnNonExistentDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/users/1");

		mocksControl.verify();
	}
	
	@Test
	public void testGetNonExistingUser() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.getByExtIdWithGroups(userExtId("123"), domain)).andReturn(null);
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.get("/users/123");

		mocksControl.verify();
	}
	
	@Test
	public void testGetUserThrowError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.getByExtIdWithGroups(userExtId("123"), domain)).andThrow(new RuntimeException("bad things happen"));
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.get("/users/123");

		mocksControl.verify();
	}

}
