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

import static org.easymock.EasyMock.expectLastCall;
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
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import fr.aliacom.obm.common.user.UserExtId;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceDeleteUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testDeleteAUserWithTrueExpunge() throws Exception {
		expectDomain();
		userDao.delete(1, true);
		expectLastCall().once();
		
		mocksControl.replay();
		
		HttpResponse httpResponse = delete("/users/1?expunge=true");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
	}
	
	@Test
	public void testDeleteAUserWithFalseExpunge() throws Exception {
		expectDomain();
		userDao.delete(1, false);
		expectLastCall().once();
		
		mocksControl.replay();
		
		HttpResponse httpResponse = delete("/users/1?expunge=false");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
	}
	
	@Test
	public void testDeleteAUserWithDefaultFalseExpunge() throws Exception {
		expectDomain();
		userDao.delete(1, false);
		expectLastCall().once();
		
		mocksControl.replay();
		
		HttpResponse httpResponse = delete("/users/1");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
	}
	
	@Test
	public void testDeleteNonExistingUser() throws Exception {
		expectDomain();
		userDao.delete(1, false);
		expectLastCall().andThrow(new UserNotFoundException(new UserExtId("1")));

		mocksControl.replay();
		
		HttpResponse httpResponse = delete("/users/1?expunge=false");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testPutUserThrowError() throws Exception {
		expectDomain();
		userDao.delete(1, false);
		expectLastCall().andThrow(new RuntimeException("bad things happen"));
		
		mocksControl.replay();
		
		HttpResponse httpResponse = delete("/users/1");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}
}
