/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.locator;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.locator.server.LocatorServer;

import com.google.inject.Inject;
import com.google.inject.Injector;

@RunWith(GuiceRunner.class)
@GuiceModule(TestLocatorModule.class)
public class StartupTest implements H2TestClass {

	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "db-schema.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	@Inject Injector injector;
	
	private LocatorServer locatorServer;
	private String webAppUrl;
	
	@Before
	public void setUp() throws Exception {
		locatorServer = new LocatorServerLauncher().start(injector);
		locatorServer.start();
		webAppUrl = "http://localhost:" + locatorServer.getPort() + "/obm-locator/";
	}

	@After
	public void tearDown() throws Exception {
		locatorServer.stop();
		db.closeConnections();
	}
	
	@Test
	@RunAsClient
	public void testStartup() throws Exception {
		Response result = Request.Get(webAppUrl + "location/host/sync/obm_sync/login@test-domain").execute();
		assertThat(result.returnContent().asString()).isEqualTo("12.23.34.45\n");
	}

}
