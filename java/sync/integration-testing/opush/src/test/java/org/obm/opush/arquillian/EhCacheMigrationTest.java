/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.opush.arquillian;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.arquillian.SlowGuiceArquillianRunner;
import org.obm.filter.Slow;
import org.obm.opush.env.arquillian.AuthenticatedArquillianModule;
import org.obm.opush.env.arquillian.OpushArchiveUtils;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;

@Slow
@RunWith(SlowGuiceArquillianRunner.class)
public class EhCacheMigrationTest {
	
	private CloseableHttpClient httpClient;

	@Before
	public void setup() {
		httpClient = HttpClientBuilder.create().build();
	}
	
	@After
	public void shutdown() throws IOException {
		httpClient.close();
	}

	@Test
	@RunAsClient
	public void testMigration(@ArquillianResource URL baseURL) throws Exception {
		StatusLine statusLine = httpClient.execute(AuthenticatedArquillianModule.post(baseURL, "Ping")).getStatusLine();
		assertThat(statusLine.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
	}

	@DeployForEachTests
	@Deployment
	public static WebArchive createDeployment() {
		return OpushArchiveUtils
				.buildWebArchive(EhCacheMigrationTestModule.class, EhCacheMigrationTestListener.class)
				.addClasses(EhCacheMigrationTestModule.class);
	}
}
