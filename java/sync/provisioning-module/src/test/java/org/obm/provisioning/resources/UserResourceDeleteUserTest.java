/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2012  Linagora
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

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.dao.exceptions.DaoException;

import com.google.common.collect.ImmutableMap;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceDeleteUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testDeleteAUserWithTrueExpunge() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users/1", null, HttpVerb.DELETE,
						ImmutableMap.of(
								"expunge", "true", "domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1"))))
				.andReturn(batch);
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password")
			.parameter("expunge", true).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testDeleteAUserWithFalseExpunge() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users/1", null, HttpVerb.DELETE,
						ImmutableMap.of(
								"expunge", "false", "domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1"))))
				.andReturn(batch);
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password")
			.parameter("expunge", false).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/batches/1/users/1");
		
		mocksControl.verify();
	}
	
	@Test
	public void testDeleteAUserWithDefaultFalseExpunge() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users/1", null, HttpVerb.DELETE,
						ImmutableMap.<String, String>of(
								"domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1"))))
				.andReturn(batch);
		
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
	public void testDeleteAUserWithError() throws Exception {
		expectDomain();
		expectBatch();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users/1", null, HttpVerb.DELETE,
						ImmutableMap.<String, String>of(
								"domain", "a3443822-bb58-4585-af72-543a287f7c0e", "batchId", "1", "userId", "1"))))
				.andThrow(new DaoException());
		
		mocksControl.replay();
		
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.delete("/batches/1/users/1");
		
		mocksControl.verify();
	}
}
