/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.resources.testing;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.core.Response.Status;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TestingResourceWhenVMArgumentTest {

	private ClientDriverRule driver = new ClientDriverRule();
	
	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TestRule() {
				
				@Override
				public Statement apply(final Statement base, Description description) {
					return new Statement() {

						@Override
						public void evaluate() throws Throwable {
							System.setProperty("testingMode", "true");
							
							try {
								base.evaluate();
							} finally  {
								System.setProperty("testingMode", "false");
							}
						}
					};
				}
			})
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.Simple(driver, new Provider<TemporaryFolder>() {

				@Override
				public TemporaryFolder get() {
					return temporaryFolder;
				}
				
			})))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));

	private @Inject TemporaryFolder temporaryFolder;
	private @Inject H2InMemoryDatabase db;
	private @Inject WebServer server;
	private @Inject IMocksControl control;

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void setDateShouldIncrement() throws Exception {
		control.replay();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.body("2014-08-27T12:18:00.000Z").
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.put("/imap-archive/testing/date");
		
		given()
			.port(server.getHttpPort()).
		expect()
			.body(equalTo("2014-08-27T12:18:00.000Z"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/testing/date");
		
		control.verify();
	}
	
	@Test
	public void setDateShouldDecrement() throws Exception {
		control.replay();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.body("2014-08-07T12:18:00.000Z").
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.put("/imap-archive/testing/date");
		
		given()
			.port(server.getHttpPort()).
		expect()
			.body(equalTo("2014-08-07T12:18:00.000Z"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/testing/date");
		
		control.verify();
	}
	
	@Test
	public void setDateShouldThrowWhenBadDuration() throws Exception {
		control.replay();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.body("badduration").
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.put("/imap-archive/testing/date");
		
		control.verify();
	}
}
