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

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

import com.google.common.collect.ImmutableSet;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class ProfileAuthorizingTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testSubjectCannotAuthenticateWithNoDomain() {
		mocksControl.replay();
		
		given()
			.auth().basic("username", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/profiles");
		
		mocksControl.verify();
	}
	
	@Test
	public void testSubjectCannotGetProfilesWithWrongPassword() {
		expectSuccessfulAuthentication("username", "password");
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "wrongPassword").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/profiles");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCannotGetProfileWithWrongPassword() {
		expectSuccessfulAuthentication("username", "password");
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "wrongPassword").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/profiles/1");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCannotGetProfilesWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("batches:*"), domainAwarePerm("users:*")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/profiles");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCannotGetProfileWithWrongPermissions() throws Exception {
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("profiles:create")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/profiles/1");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCanGetProfilesWithReadPermission() throws Exception {
		expectDomain();
		expectProfiles();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username", ImmutableSet.of(domainAwarePerm("profiles:read")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/profiles");
		
		mocksControl.verify();
	}

	@Test
	public void testSubjectCanGetProfileWithReadPermission() throws Exception {
		expectProfile();
		expectDomain();
		expectSuccessfulAuthentication("username", "password");
		expectAuthorizingReturns("username",  ImmutableSet.of(domainAwarePerm("profiles:read")));
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/profiles/1");
		
		mocksControl.verify();
	}
}
