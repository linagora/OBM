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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.hamcrest.Matchers.containsString;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;

import com.google.inject.Inject;
import com.jayway.restassured.http.ContentType;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class BatchResourceTest extends CommonDomainEndPointEnvTest {

	@Inject
	private BatchProcessor batchProcessor;
	@Inject
	private BatchTracker batchTracker;

	private static final Batch commitedBatch = Batch
			.builder()
			.id(batchId(13))
			.domain(domain)
			.status(BatchStatus.SUCCESS)
			.build();

	@Test
	public void testDeleteWithUnknownDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();
		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.delete("/batches/1");

		mocksControl.verify();
	}

	@Test
	public void testDeleteWithUnknownBatch() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		batchDao.delete(batchId(1));
		expectLastCall().andThrow(new BatchNotFoundException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.delete("/batches/1");

		mocksControl.verify();
	}

	@Test
	public void testDelete() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		batchDao.delete(batchId(1));
		expectLastCall();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/batches/1");

		mocksControl.verify();
	}

	@Test
	public void testCreate() throws Exception {
		Batch.Builder batchBuilder = Batch
				.builder()
				.status(BatchStatus.IDLE)
				.domain(domain);

		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.create(batchBuilder.build())).andReturn(batchBuilder.id(batchId(1)).build());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.CREATED.getStatusCode())
			.header("Location", baseUrl + '/' + domain.getUuid().get() + "/batches/1")
			.body(containsString(
				"{" +
					"\"id\":1" +
				"}")).
		when()
			.post("/batches");

		mocksControl.verify();
	}

	@Test
	public void testCreateWithUnknownDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.post("/batches");

		mocksControl.verify();
	}

	@Test
	public void testCreateOnError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.create(isA(Batch.class))).andThrow(new DaoException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.post("/batches");

		mocksControl.verify();
	}

	@Test
	public void testGetWithUnknownBatch() throws Exception {
		expectDomain();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.get(batchId(12), domain)).andThrow(new BatchNotFoundException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testGetWithUnknownDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
			expect()
				.statusCode(Status.NOT_FOUND.getStatusCode()).
			when()
				.get("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testGetOnError() throws Exception {
		expectDomain();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.get(batchId(12), domain)).andThrow(new DaoException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.get("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testGet() throws Exception {
		expectDomain();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.get(batchId(12), domain)).andReturn(batch);
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
						"{" +
							"\"id\":1," +
							"\"status\":\"IDLE\"," +
							"\"operationCount\":2," +
							"\"operationDone\":0," +
							"\"operations\":[" +
								"{" +
									"\"status\":\"IDLE\"," +
									"\"entityType\":\"USER\"," +
									"\"entity\":{\"id\":123456}," +
									"\"operation\":\"POST\"," +
									"\"error\":null" +
								"}," +
								"{" +
									"\"status\":\"IDLE\"," +
									"\"entityType\":\"USER\"," +
									"\"entity\":{}," +
									"\"operation\":\"PATCH\"," +
									"\"error\":null" +
								"}" +
							"]" +
						"}")).
		when()
			.get("/batches/12");


		mocksControl.verify();
	}

	@Test
	public void testGetWhenBatchIsRunning() throws Exception {
		expectDomain();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(batch);
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
						"{" +
							"\"id\":1," +
							"\"status\":\"IDLE\"," +
							"\"operationCount\":2," +
							"\"operationDone\":0," +
							"\"operations\":[" +
								"{" +
									"\"status\":\"IDLE\"," +
									"\"entityType\":\"USER\"," +
									"\"entity\":{\"id\":123456}," +
									"\"operation\":\"POST\"," +
									"\"error\":null" +
								"}," +
								"{" +
									"\"status\":\"IDLE\"," +
									"\"entityType\":\"USER\"," +
									"\"entity\":{}," +
									"\"operation\":\"PATCH\"," +
									"\"error\":null" +
								"}" +
							"]" +
						"}")).
		when()
			.get("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testCommitWithUnknownBatch() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12), domain)).andThrow(new BatchNotFoundException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.put("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testCommitWithAlreadyRunningBatch() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(batch);
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testCommitOnError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12), domain)).andReturn(batch);
		batchProcessor.process(batch);
		expectLastCall().andThrow(new ProcessingException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.put("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testCommit() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchTracker.getTrackedBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12), domain)).andReturn(batch);
		batchProcessor.process(batch);
		expectLastCall();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/batches/12");

		mocksControl.verify();
	}

	@Test
	public void testCommitBatchAlreadyCommited() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchTracker.getTrackedBatch(batchId(13))).andReturn(null);
		expect(batchDao.get(batchId(13), domain)).andReturn(commitedBatch);
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.content(containsString(  "{"
						+ "\"message\":\"Not commiting batch 13 in status SUCCESS.\","
						+ "\"type\":\"org.obm.provisioning.resources.BatchAlreadyCommitedException\""
					+ "}")).
		when()
			.put("/batches/13");

		mocksControl.verify();
	}

	@Test
	public void testAddOperationToAlreadyCommitedBatch() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(batchDao.get(batchId(13), domain)).andReturn(commitedBatch);
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password")
			.content(obmUserToJsonString()).contentType(ContentType.JSON).
		expect()
			.statusCode(Status.CONFLICT.getStatusCode())
			.content(containsString(  "{"
					+ "\"message\":\"Not adding operation to batch 13 in status SUCCESS.\","
					+ "\"type\":\"org.obm.provisioning.resources.BatchAlreadyCommitedException\""
				+ "}")).
		when()
			.post("/batches/13/users");

		mocksControl.verify();
	}
}
