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
package org.obm.provisioning.authentication;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.user.ObmUser;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserAuthorizingTest extends CommonDomainEndPointEnvTest {
	
	@Test
	public void testSubjectCannotAuthenticateWithNoDomain() {
		mocksControl.replay();
		
		given()
			.auth().basic("username", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/batches/1/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotPutWithWrongPassword() {
		expectSuccessfulAuthentication("username", "password");
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "wrongPassword").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.put("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotPatchWithWrongPassword() {
		expectSuccessfulAuthentication("username", "password");
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "wrongPassword").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.patch("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotGetWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:create, delete")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotPostWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read, delete")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.post("/batches/1/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotDeleteWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read, create")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.delete("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotPatchWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read, create")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.patch("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotPutWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read, create")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.put("/batches/1/users/1");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCanGetUserWithReadPermission() throws Exception {
		expectDomain();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read")));
		expect(userDao.getByExtIdWithGroups(userExtId("1"), domain)).andReturn(fakeUser());
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/users/1");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCanGetUserWithUrlWithLastSlash() throws Exception {
		expectDomain();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read")));
		expect(userDao.getByExtIdWithGroups(userExtId("1"), domain)).andReturn(fakeUser());
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/users/1/");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCanGetListOfUserWithReadPermission() throws Exception {
		expectDomain();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:read")));
		expect(userDao.list(domain)).andReturn(ImmutableList.<ObmUser>of());
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCanPostUserWithCreatePermission() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:create")));
		batchDao.addOperation(batch,
				operation(BatchEntityType.USER, "/batches/1/users", "", HttpVerb.POST,
						ImmutableMap.<String, String>of("domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1")));
		expectLastCall();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/batches/1/users");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCanDeleteUserWithDeletePermission() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:delete")));
		batchDao.addOperation(batch,
				operation(BatchEntityType.USER, "/batches/1/users/1", null, HttpVerb.DELETE,
						ImmutableMap.<String, String>of(
								"domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1")));
		expectLastCall();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCanPutUserWithUpdatePermission() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:update")));
		batchDao.addOperation(batch,
				operation(BatchEntityType.USER, "/batches/1/users/1", "", HttpVerb.PUT,
						ImmutableMap.<String, String>of(
								"domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1")));
		expectLastCall();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCanPatchUserWithUpdatePermission() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("users:patch")));
		batchDao.addOperation(batch,
				operation(BatchEntityType.USER, "/batches/1/users/1",
						"", HttpVerb.PATCH, ImmutableMap.<String, String>of(
								"domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1")));
		expectLastCall();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.patch("/batches/1/users/1");
		
		mocksControl.verify();
	}
}
