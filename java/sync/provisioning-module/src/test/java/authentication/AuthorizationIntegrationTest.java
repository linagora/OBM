/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package authentication;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class AuthorizationIntegrationTest extends CommonDomainEndPointEnvTest {
	
	@Test
	public void testSubjectIsAnonymous() throws Exception {
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authorization");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.UNAUTHORIZED.getStatusCode());
	}

	@Test
	public void testSubjectCannotAuthenticate() throws Exception {
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authentication", "user1", "password2");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.UNAUTHORIZED.getStatusCode());
	}
	
	@Test
	public void testSubjectCanAuthenticate() throws Exception {
		expectDomain();
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authentication", "user3", "password");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.OK.getStatusCode());
		assertThat(EntityUtils.toString(httpResponse.getEntity()))
			.isEqualTo("authenticated");
	}
	
	@Test
	public void testSubjectHasRoleAll() throws Exception {
		expectDomain();
		
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authorization", "user1", "password");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.OK.getStatusCode());
		assertThat(EntityUtils.toString(httpResponse.getEntity()))
			.isEqualTo("authorized");
	}
	
	@Ignore("Crash with a UnauthorizerException instead of a HTTPResponse Status.UNAUTHORIZED")
	@Test
	public void testSubjectHasNotRoleAll() throws Exception {
		expectDomain();
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authorization", "user2", "password");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.UNAUTHORIZED.getStatusCode());
	}
	
	@Test
	public void testSubjectHasRoleNotAll() throws Exception {
		expectDomain();
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/do/tests/on/authorization2", "user2", "password");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.OK.getStatusCode());
		assertThat(EntityUtils.toString(httpResponse.getEntity()))
			.isEqualTo("authorized");
	}
	
	private HttpResponse get(String path, String username, String password) throws Exception {
		return createGetRequestWithAuth(path, username, password).execute().returnResponse();
	}
	
	private Request createGetRequestWithAuth(String path, String username, String password) {
		final Request request = Request.Get(baseUrl + "/" + domain.getUuid().get() + path);
		request.addHeader(
				BasicScheme.authenticate(
						new UsernamePasswordCredentials(username, password), "UTF-8", false));
		return request;
	}
}
