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

import java.util.List;

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

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceListAllUserTest extends CommonDomainEndPointEnvTest {

	@Test
	public void testListAllUserOnNonExistentDomain() throws Exception {
		expectNoDomain();
		mocksControl.replay();

		HttpResponse httpResponse = get("/users");

		mocksControl.verify();

		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testListAllUser() throws Exception {
		expectDomain();
		expect(userDao.list(domain)).andReturn(fakeListOfUser());
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
		assertThat(EntityUtils.toString(httpResponse.getEntity())).isEqualTo(expectedJsonSetOfUser());
	}
	
	@Test
	public void testListAllUserReturnNothing() throws Exception {
		expectDomain();
		expect(userDao.list(domain)).andReturn(null);
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(EntityUtils.toString(httpResponse.getEntity())).isEqualTo("[]");
	}
	
	@Test
	public void testListAllUserReturnEmptyList() throws Exception {
		expectDomain();
		expect(userDao.list(domain)).andReturn(ImmutableList.<ObmUser>of());
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(EntityUtils.toString(httpResponse.getEntity())).isEqualTo("[]");
	}
	
	@Test
	public void testListAllThrowError() throws Exception {
		expectDomain();
		expect(userDao.list(domain)).andThrow(new RuntimeException("bad things happen"));
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	private List<ObmUser> fakeListOfUser() {
		return ImmutableList.of(fakeUser(1), fakeUser(2));
	}
	
	private String expectedJsonSetOfUser() {
		return "[" +
					"{\"id\":\"ExtId1\",\"url\":\"/a3443822-bb58-4585-af72-543a287f7c0e/users/ExtId1\"}," +
					"{\"id\":\"ExtId2\",\"url\":\"/a3443822-bb58-4585-af72-543a287f7c0e/users/ExtId2\"}" +
				"]";
	}

	private ObmUser fakeUser(int id) {
		return ObmUser
				.builder()
				.login("user" + id)
				.uid(id)
				.extId(UserExtId.builder().extId("ExtId" + id).build())
				.lastName("Lastname")
				.firstName("Firstname")
				.commonName("")
				.domain(domain)
				.emailAndAliases("user" + id)
				.publicFreeBusy(true)
				.build();
	}
}
