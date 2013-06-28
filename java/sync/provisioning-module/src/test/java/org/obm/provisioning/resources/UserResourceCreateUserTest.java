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

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
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
public class UserResourceCreateUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testCreateAUser() throws Exception {
		expectDomain();
		expectBatch();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users", obmUserToJsonString(), HttpVerb.POST, ImmutableMap.<String, String>of())))
				.andReturn(batch);
		
		mocksControl.replay();
		
		HttpResponse httpResponse = post("/batches/1/users", obmUserToJson());
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
	}
	
	@Test
	public void testCreateUserThrowError() throws Exception {
		expectDomain();
		expectBatch();
		expect(batchDao.addOperation(batch.getId(),
				operation(BatchEntityType.USER, "/batches/1/users", obmUserToJsonString(), HttpVerb.POST, ImmutableMap.<String, String>of())))
				.andThrow(new DaoException());
		
		mocksControl.replay();
		
		HttpResponse httpResponse = post("/batches/1/users", obmUserToJson());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}
}
