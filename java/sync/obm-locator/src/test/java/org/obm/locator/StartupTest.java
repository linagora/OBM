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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.arquillian.ArtifactFilters;
import org.obm.arquillian.GuiceWebXmlDescriptor;
import org.obm.arquillian.SlowGuiceArquillianRunner;
import org.obm.filter.Slow;


@Slow
@RunWith(SlowGuiceArquillianRunner.class)
public class StartupTest {

	@Test
	@RunAsClient
	public void testStartup(@ArquillianResource URL webAppUrl) throws URISyntaxException, ClientProtocolException, IOException {
		Response result = Request.Get(new URIBuilder(webAppUrl.toURI())
						.setPath(webAppUrl.getPath() + "location/host/sync/obm_sync/login@test-domain")
						.build())
				.execute();
		assertThat(result.returnContent().asString()).isEqualTo("12.23.34.45\n");
	}

	@Deployment
	public static WebArchive buildWar() {
		return ShrinkWrap.create(WebArchive.class)
			.addAsResource("db-schema.sql", H2GuiceServletContextListener.INITIAL_DB_SCRIPT)
			.addPackages(true, "org.obm.locator")
			.addAsWebInfResource(GuiceWebXmlDescriptor.webXml(ArquillianLocatorModule.class, H2GuiceServletContextListener.class), "web.xml")
			.addAsLibraries(
					ArtifactFilters.filterObmDependencies(Maven
					.resolver()
					.offline()
					.loadPomFromClassLoaderResource("pom.xml")
					.importRuntimeDependencies(new AcceptScopesStrategy(ScopeType.PROVIDED, ScopeType.COMPILE))
					.asResolvedArtifact()));
	}
	
}
