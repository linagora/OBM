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


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceGetUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testGetAUser() throws Exception {
		expectDomain();
		expect(userDao.get(1)).andReturn(fakeUser());
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/1");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
		assertThat(EntityUtils.toString(httpResponse.getEntity())).isEqualTo(obmUserToJsonString());
	}

	@Test
	public void testGetAUserOnNonExistentDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse httpResponse = get("/users/1");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testGetNonExistingUser() throws Exception {
		expectDomain();
		expect(userDao.get(123)).andReturn(null);
		mocksControl.replay();

		HttpResponse httpResponse = get("/users/123");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
	}
	
	@Test
	public void testGetUserThrowError() throws Exception {
		expectDomain();
		expect(userDao.get(1)).andThrow(new RuntimeException("bad things happen"));
		mocksControl.replay();

		HttpResponse httpResponse = get("/users/1");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}
}
