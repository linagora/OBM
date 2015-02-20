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

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceTest extends CommonDomainEndPointEnvTest {
	
	@Test
	public void testGetWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/batches/1/users/a/b");

		mocksControl.verify();
	}
	
	@Test
	public void testPostWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.post("/batches/1/users/a/b");

		mocksControl.verify();
	}
	
	@Test
	public void testPostConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()).
		when()
			.post("/batches/1/users");

		mocksControl.verify();
	}
	
	@Test
	public void testPutWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.put("/batches/1/users/1/a/b");

		mocksControl.verify();
	}
	
	@Test
	public void testPutConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()).
		when()
			.put("/batches/1/users/1");

		mocksControl.verify();
	}
	
	@Test
	public void testPatchWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.patch("/batches/1/users/1/a/b");

		mocksControl.verify();
	}
	
	@Test
	public void testPatchConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()).
		when()
			.patch("/batches/1/users/1");

		mocksControl.verify();
	}
	
	@Test
	public void testDeleteWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password")
			.body(invalidMediaTypeEntity()).
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.delete("/batches/1/users/1/a/b");

		mocksControl.verify();
	}
	
	private String invalidMediaTypeEntity() {
		return 
				"{" +
				  "\"uid\":1," +
				  "\"entityId\":0," +
				  "\"login\":\"user1\"," +
				  "\"commonName\":\"John Doe\"," +
				  "\"lastName\":\"Doe\"," +
				  "\"firstName\":\"Jesus\"," +
				  "\"email\":\"mails\"," +
				  "\"emailAlias\":[]," +
				  "\"address1\":\"address1\"," +
				  "\"address2\":\"address2\"," +
				  "\"address3\":null," +
				  "\"expresspostal\":null," +
				  "\"homePhone\":null," +
				  "\"mobile\":\"mobile\"," +
				  "\"service\":\"service\"," +
				  "\"title\":\"title\"," +
				  "\"town\":\"town\"," +
				  "\"workFax\":null," +
				  "\"workPhone\":null," +
				  "\"zipCode\":\"zipCode\"," +
				  "\"description\":\"description\"," +
				  "\"timeCreate\":\"2013-06-11T12:00:00.000+0000\"," +
				  "\"timeUpdate\":\"2013-06-11T13:00:00.000+0000\"," +
				  "\"createdBy\":null," +
				  "\"updatedBy\":null," +
				  "\"domain\":{" +
				    "\"id\":1," +
				    "\"name\":\"domain\"," +
				    "\"uuid\":\"a3443822-bb58-4585-af72-543a287f7c0e\"," +
				    "\"aliases\":[]" +
				  "}," +
				  "\"publicFreeBusy\":false" +
				"}";
	}
}
