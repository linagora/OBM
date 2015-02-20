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
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(CommonDomainEndPointEnvTest.Env.class)
public class ProfileResourceTest extends CommonDomainEndPointEnvTest {

	@Inject
	private ProfileDao dao;

	@Test
	public void testGetProfilesWithEmptyList() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileEntries(domain.getUuid())).andReturn(Collections.<ProfileEntry> emptySet());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("[]")).
		when()
			.get("/profiles");

		mocksControl.verify();
	}

	@Test
	public void testGetProfiles() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileEntries(domain.getUuid())).andReturn(ImmutableSet.of(ProfileEntry
				.builder()
				.domainUuid(domain.getUuid())
				.id(1)
				.build()));
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
			"[" +
				"{" +
					"\"id\":1," +
					"\"url\":\"/" + domain.getUuid() + "/profiles/1\"" +
				"}" +
			"]")).
		when()
			.get("/profiles");

		mocksControl.verify();
	}

	@Test
	public void testGetProfilesOnError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileEntries(domain.getUuid())).andThrow(new DaoException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.get("/profiles");

		mocksControl.verify();
	}

	@Test
	public void testGetProfilesOnNonExistentDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles");

		mocksControl.verify();
	}

	@Test
	public void testGetProfile() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileName(domain.getUuid(), ProfileId.valueOf("123"))).andReturn(ProfileName.valueOf("profile1"));
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{\"name\":\"profile1\"}")).
		when()
			.get("/profiles/123");

		mocksControl.verify();
	}

	@Test
	public void testGetProfileOnNonExistentProfile() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileName(domain.getUuid(), ProfileId.valueOf("123"))).andThrow(new ProfileNotFoundException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles/123");

		mocksControl.verify();
	}

	@Test
	public void testGetProfileOnNonExistentDomain() throws Exception {
		expectNoDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/profiles/123");

		mocksControl.verify();
	}

	@Test
	public void testGetProfileOnError() throws Exception {
		expectDomain();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expect(dao.getProfileName(domain.getUuid(), ProfileId.valueOf("123"))).andThrow(new DaoException());
		mocksControl.replay();

		given()
			.auth().basic("username@domain", "password").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.get("/profiles/123");

		mocksControl.verify();
	}

}
