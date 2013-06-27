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

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.dao.UserDao;
import org.obm.provisioning.dao.exceptions.DaoException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class UserResourceCreateUserTest extends CommonDomainEndPointEnvTest {
	
	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private UserDao userDao;
	
	@Test
	public void testCreateAUser() throws Exception {
		expectDomain();
		userDao.create(fakeUser());
		expectLastCall().once();
		
		mocksControl.replay();
		
		HttpResponse httpResponse = post("/users", obmUserToJson());
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.CREATED.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
	}
	
	@Test
	public void testCreateUserThrowError() throws Exception {
		expectDomain();
		userDao.create(fakeUser());
		expectLastCall().andThrow(new DaoException("bad things happen"));
		
		mocksControl.replay();
		
		HttpResponse httpResponse = post("/users", obmUserToJson());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}
	
	private ObmUser fakeUser() {
		return ObmUser.builder()
				.domain(domain)
				.uid(1)
				.login("user1")
				.lastName("Doe")
				//.profile("Utilisateurs")	// Not implemented yet in ObmUser
				.firstName("Jesus")
				.commonName("John Doe")
				//.kind("")					// Not implemented yet in ObmUser
				.title("title")
				.description("description")
				//.company("")				// Not implemented yet in ObmUser
				.service("service")
				//.direction()				// Not implemented yet in ObmUser
				.address1("address1")
				.address2("address2")
				.town("town")
				.zipCode("zipCode")
				//.business_zipcode()		// Not implemented yet in ObmUser
				//.country()				// Not implemented yet in ObmUser
				//.phones()					// Not implemented yet in ObmUser
				.mobile("mobile")
				//.faxes()					// Not implemented yet in ObmUser
				//.mail_quota()				// Not implemented yet in ObmUser
				//.mail_server()			// Not implemented yet in ObmUser
				.emailAndAliases("mails")
				.timeCreate(DateUtils.date("2013-06-11T14:00:00"))
				.timeUpdate(DateUtils.date("2013-06-11T15:00:00"))
				//.groups()					// Not implemented yet in ObmUser
				.build();
	}
	
	private StringEntity obmUserToJson() throws UnsupportedEncodingException {
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
		userToJson.setContentType(MediaType.APPLICATION_JSON);
		return  userToJson;
	}
}
