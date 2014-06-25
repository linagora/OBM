/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.expect;

import javax.ws.rs.core.Response.Status;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.domain.dao.DomainDao;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.server.WebServer;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class DomainBasedSubResourceTest {

	@Rule public GuiceRule guiceRule = new GuiceRule(this, new TestImapArchiveModules.Simple());

	@Inject WebServer server;
	@Inject IMocksControl control;
	@Inject DomainDao domainDao;
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void getDomainConfigurationShouldReturnBadRequestOnInvalidUuid() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("21aeb670-f49e-428a-9d0c-f11f5feaa688");
		expect(domainDao.findDomainByUuid(domainId)).andReturn(ObmDomain.builder().uuid(domainId).build());
		control.replay();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.BAD_REQUEST.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/toto/configuration");
	}
	
	@Test
	public void getDomainConfigurationShouldReturnNotFoundOnAbsentDomain() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("c7dd9583-5057-4c0a-ac30-d284940420c8");
		expect(domainDao.findDomainByUuid(domainId)).andThrow(new DomainNotFoundException());
		control.replay();
		server.start();
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/c7dd9583-5057-4c0a-ac30-d284940420c8/configuration");
	}
	
}
