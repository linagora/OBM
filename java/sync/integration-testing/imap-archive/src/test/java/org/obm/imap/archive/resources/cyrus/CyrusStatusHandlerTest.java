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
package org.obm.imap.archive.resources.cyrus;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.expect;

import javax.ws.rs.core.Response.Status;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.domain.dao.UserSystemDao;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

import fr.aliacom.obm.common.system.ObmSystemUser;

public class CyrusStatusHandlerTest {
	
	@Rule public GuiceRule guiceRule = new GuiceRule(this, new TestImapArchiveModules.WithGreenmail());

	@Inject WebServer server;
	@Inject GreenMail imapServer;
	@Inject UserSystemDao userSystemDao;
	@Inject IMocksControl control;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		imapServer.stop();
	}
	
	@Test
	public void testStatusIs200WhenImapIsUp() throws Exception {
		imapServer.setUser("cyrus", "cyrus");
		expect(userSystemDao.getByLogin("cyrus")).andReturn(ObmSystemUser.builder().login("cyrus").password("cyrus").id(12).build());
		control.replay();
		imapServer.start();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsUpButCyrusUserNotFound() throws Exception {
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsUpButLoginFails() throws Exception {
		server.start();
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/cyrus/status");
	}
	
	@Test
	public void testStatusIs503WhenImapIsDown() throws Exception {
		server.start();
		
		imapServer.stop();
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/cyrus/status");
	}
}
