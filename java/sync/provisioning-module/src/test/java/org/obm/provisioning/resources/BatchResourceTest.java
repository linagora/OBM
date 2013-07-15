/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.BatchProcessor;

import com.google.inject.Inject;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class BatchResourceTest extends CommonDomainEndPointEnvTest {

	@Inject
	private BatchProcessor batchProcessor;

	@Test
	public void testDeleteWithUnknownDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse response = delete("/batches/1");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testDeleteWithUnknownBatch() throws Exception {
		expectDomain();
		batchDao.delete(batchId(1));
		expectLastCall().andThrow(new BatchNotFoundException());
		mocksControl.replay();

		HttpResponse response = delete("/batches/1");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testDelete() throws Exception {
		expectDomain();
		batchDao.delete(batchId(1));
		expectLastCall();
		mocksControl.replay();

		HttpResponse response = delete("/batches/1");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void testCreate() throws Exception {
		Batch.Builder batchBuilder = Batch
				.builder()
				.status(BatchStatus.IDLE)
				.domain(domain);

		expectDomain();
		expect(batchDao.create(batchBuilder.build())).andReturn(batchBuilder.id(batchId(1)).build());
		mocksControl.replay();

		HttpResponse response = post("/batches", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.CREATED.getStatusCode());
		assertThat(response.getFirstHeader("Location").getValue()).isEqualTo(baseUrl + '/' + domain.getUuid().get() + "/batches/1");
		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(
				"{" +
					"\"id\":1" +
				"}");
	}

	@Test
	public void testCreateWithUnknownDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse response = post("/batches", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testCreateOnError() throws Exception {
		expectDomain();
		expect(batchDao.create(isA(Batch.class))).andThrow(new DaoException());
		mocksControl.replay();

		HttpResponse response = post("/batches", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void testGetWithUnknownBatch() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andReturn(null);
		mocksControl.replay();

		HttpResponse response = get("/batches/12");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testGetWithUnknownDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse response = get("/batches/12");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testGetOnError() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andThrow(new DaoException());
		mocksControl.replay();

		HttpResponse response = get("/batches/12");

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void testGet() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andReturn(batch);
		mocksControl.replay();

		HttpResponse response = get("/batches/12");

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(
				"{" +
					"\"id\":1," +
					"\"status\":\"ERROR\"," +
					"\"operationCount\":2," +
					"\"operationDone\":1," +
					"\"operations\":[" +
						"{" +
							"\"status\":\"SUCCESS\"," +
							"\"entityType\":\"USER\"," +
							"\"entity\":{\"id\":123456}," +
							"\"operation\":\"POST\"," +
							"\"error\":null" +
						"}," +
						"{" +
							"\"status\":\"ERROR\"," +
							"\"entityType\":\"USER\"," +
							"\"entity\":{}," +
							"\"operation\":\"PATCH\"," +
							"\"error\":\"Invalid User\"" +
						"}" +
					"]" +
				"}");
	}

	@Test
	public void testGetWhenBatchIsRunning() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(batch);
		mocksControl.replay();

		HttpResponse response = get("/batches/12");

		mocksControl.verify();

		assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(
				"{" +
					"\"id\":1," +
					"\"status\":\"ERROR\"," +
					"\"operationCount\":2," +
					"\"operationDone\":1," +
					"\"operations\":[" +
						"{" +
							"\"status\":\"SUCCESS\"," +
							"\"entityType\":\"USER\"," +
							"\"entity\":{\"id\":123456}," +
							"\"operation\":\"POST\"," +
							"\"error\":null" +
						"}," +
						"{" +
							"\"status\":\"ERROR\"," +
							"\"entityType\":\"USER\"," +
							"\"entity\":{}," +
							"\"operation\":\"PATCH\"," +
							"\"error\":\"Invalid User\"" +
						"}" +
					"]" +
				"}");
	}

	@Test
	public void testCommitWithUnknownBatch() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andReturn(null);
		mocksControl.replay();

		HttpResponse response = put("/batches/12", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testCommitWithAlreadyRunningBatch() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(batch);
		mocksControl.replay();

		HttpResponse response = put("/batches/12", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void testCommitOnError() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andReturn(batch);
		batchProcessor.process(batch);
		expectLastCall().andThrow(new ProcessingException());
		mocksControl.replay();

		HttpResponse response = put("/batches/12", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void testCommit() throws Exception {
		expectDomain();
		expect(batchProcessor.getRunningBatch(batchId(12))).andReturn(null);
		expect(batchDao.get(batchId(12))).andReturn(batch);
		batchProcessor.process(batch);
		expectLastCall();
		mocksControl.replay();

		HttpResponse response = put("/batches/12", null);

		mocksControl.verify();

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
	}
}
