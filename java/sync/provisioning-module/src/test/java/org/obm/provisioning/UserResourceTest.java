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
package org.obm.provisioning;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceTest extends CommonDomainEndPointEnvTest {
	
	@Test
	public void testGetWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		HttpResponse httpResponse = get("/batches/1/users/a/b");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testPostWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		HttpResponse httpResponse = post("/batches/1/users/a/b", null);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testPostConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		final StringEntity userToJson = invalidMediaTypeEntity();
		HttpResponse httpResponse = post("/batches/1/users", userToJson);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
	}
	
	@Test
	public void testPutWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		HttpResponse httpResponse = put("/batches/1/users/a/b", null);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testPutConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		final StringEntity userToJson = invalidMediaTypeEntity();
		HttpResponse httpResponse = put("/batches/1/users/1", userToJson);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
	}
	
	@Test
	public void testPatchWithUnknownUrl() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		HttpResponse httpResponse = patch("/batches/1/users/a/b", null);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testPatchConsumeInvalidData() throws Exception {
		expectDomain();
		expectBatch();
		mocksControl.replay();

		final StringEntity userToJson = invalidMediaTypeEntity();
		HttpResponse httpResponse = patch("/batches/1/users/1", userToJson);

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
	}
	
	@Test
	public void testdeleteWithUnknownUrl() throws Exception {
		expectDomain();
		mocksControl.replay();

		HttpResponse httpResponse = delete("/batches/1/userss/1");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	private StringEntity invalidMediaTypeEntity()
			throws UnsupportedEncodingException {
		final StringEntity userToJson = new StringEntity(
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
				"}");
		userToJson.setContentType(MediaType.TEXT_PLAIN);
		return userToJson;
	}
}
