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

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.dao.UserDao;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(CommonEndPointEnvTest.Env.class)
public class UserResourceGetUserTest extends CommonEndPointEnvTest {
	
	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private UserDao userDao;
	
	@Test
	public void testUnknownUrl() throws Exception {
		HttpResponse httpResponse = get("/users/a/b");
		assertThat(httpResponse.getStatusLine().getStatusCode())
			.isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	@Test
	public void testGetAUser() throws Exception {
		expect(userDao.getUser(1)).andReturn(fakeUser());
		
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/1");
		EntityUtils.consume(httpResponse.getEntity());
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
		assertThat(ContentType.get(httpResponse.getEntity()).getCharset()).isEqualTo(Charsets.UTF_8);
		assertThat(EntityUtils.toString(httpResponse.getEntity())).isEqualTo(expectedJsonUser());
		
	}
	
	@Test
	public void testGetNonExistingUser() throws Exception {
		expect(userDao.getUser(123)).andReturn(null);
		
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/123");
		
		mocksControl.verify();
		
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
	}
	
	@Test
	public void testGetUserThrowError() throws Exception {
		expect(userDao.getUser(1)).andThrow(new RuntimeException("bad things happen"));
		
		mocksControl.replay();
		
		HttpResponse httpResponse = get("/users/1");
		
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
				.firstName("Jésus")
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
	
	private String expectedJsonUser() {
		return  
				"{" +
				  "\"uid\":1," +
				  "\"entityId\":0," +
				  "\"login\":\"user1\"," +
				  "\"commonName\":\"John Doe\"," +
				  "\"lastName\":\"Doe\"," +
				  "\"firstName\":\"Jésus\"," +
				  "\"email\":\"mails@domain\"," +
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
				    "\"uuid\":null," +
				    "\"aliases\":[]," +
				    "\"names\":[\"domain\"]" +
				  "}," +
				  "\"publicFreeBusy\":false," +
				  "\"displayName\":\"John Doe\"" +
				"}";
	}
	
	
	@SuppressWarnings("unused")
	private String expectedJsonUserNotImplementYet() {
		String json =
					"{id: 1," +
					"login: user1," +
					"lastname: Doe," +
					//"profile: Utilisateurs," +
					"firstname: Jésus," +
					"commonname: John Doe," +
					"password: doe," +
					//"kind: kind," +
					"title: title," +
					"description: description," +
					//"company: company," +
					"service: service," +
					//"direction: direction," +
					"addresses: address1," +
					"town: town," +
					"zipcode: zipcode," +
					//"business_zipcode: business_zipcode," +
					//"country: country," +
					//"phones: phones," +
					"mobile: mobile," +
					//"faxes: faxes" +
					//"mail_quota: mail_quota," +
					//"mail_server: mail_server," +
					//"mails: mails," +
					"timecreate: timecreate," +
					"timeupdate: timeupdate," +
					//"groups: groups" +
					"}";
		
		return json;
	}
	
	protected HttpResponse get(String path) throws Exception {
		return createRequest(path).execute().returnResponse();
	}
	
	protected Request createRequest(String path) {
		return Request.Get(baseUrl + path);
	}
}
