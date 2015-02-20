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

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;


@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceListAllUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testListAllUserOnNonExistentDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/users");

		mocksControl.verify();
	}

	@Test
	public void testListAllUser() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.list(domain)).andReturn(fakeListOfUser());
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(expectedJsonSetOfUser())).
		when()
			.get("/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testListAllUserReturnNothing() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.list(domain)).andReturn(null);
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("[]")).
		when()
			.get("/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testListAllUserReturnEmptyList() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.list(domain)).andReturn(ImmutableList.<ObmUser>of());
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("[]")).
		when()
			.get("/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testListAllThrowError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(userDao.list(domain)).andThrow(new RuntimeException("bad things happen"));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.get("/users");
		
		mocksControl.verify();
	}

	private List<ObmUser> fakeListOfUser() {
		return ImmutableList.of(fakeUser(1), fakeUser(2));
	}
	
	private String expectedJsonSetOfUser() {
		return "[" +
					"{\"id\":\"ExtId1\",\"url\":\"/a3443822-bb58-4585-af72-543a287f7c0e/users/ExtId1\"}," +
					"{\"id\":\"ExtId2\",\"url\":\"/a3443822-bb58-4585-af72-543a287f7c0e/users/ExtId2\"}" +
				"]";
	}

	private ObmUser fakeUser(int id) {
		return ObmUser
				.builder()
				.login(UserLogin.valueOf("user" + id))
				.uid(id)
				.extId(UserExtId.builder().extId("ExtId" + id).build())
				.identity(UserIdentity.builder()
						.lastName("Lastname")
						.firstName("Firstname")
						.commonName("")
					.build())
				.domain(domain)
				.emails(UserEmails.builder()
					.addAddress("user" + id)
					.domain(domain)
					.build())
				.publicFreeBusy(true)
				.build();
	}
}
