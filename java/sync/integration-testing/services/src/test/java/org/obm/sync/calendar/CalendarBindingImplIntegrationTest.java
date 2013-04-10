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
package org.obm.sync.calendar;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.StaticConfigurationService;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.filter.Slow;
import org.obm.push.arquillian.SlowGuiceArquillianRunner;
import org.obm.sync.ModuleUtils;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncStaticConfigurationService;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.common.user.ObmUser;

@Ignore("Need to initialize H2 database schema")
@RunWith(SlowGuiceArquillianRunner.class) @Slow
public class CalendarBindingImplIntegrationTest {

	@Test @RunAsClient
	public void testLoginRequest(@ArquillianResource URL baseURL) throws Exception {
		ObmUser defaultUser = ServicesToolBox.getSpecificObmUserFrom("user@domain", "first", "last");
		String calendar = defaultUser.getEmail();
		
		Response responseLogin = Request.Post(baseURL + "services/login/doLogin")
				.bodyForm(Form.form()
						.add("origin", "o-push")
						.add("login", calendar)
						.add("password", calendar)
						.build())
				.execute();
		
		assertThat(responseLogin.returnResponse().getStatusLine().getStatusCode())
			.isEqualTo(HttpServletResponse.SC_OK);
	}

	@Deployment
	public static WebArchive deployArchive() {
		JavaArchive wholeObmSyncArchive = ShrinkWrap
			.create(JavaArchive.class, "services-integration-testing-classes.jar")
			.addAsManifestResource("MANIFEST.MF")
			.addAsResource("bitronix-default-config.properties")
			.addAsResource("hornetq-configuration.xml")
			.addAsResource("hornetq-jms.xml")
			.addAsResource("ical4j.properties")
			.addAsResource("logback.xml")
			.addAsResource("Messages_en.properties")
			.addAsResource("Messages_fr.properties")
			.addAsResource("org")
			.addClasses(ObmSyncArchiveUtils.projectAnnotationsClasses())
			.addClasses(ObmSyncArchiveUtils.projectConfigurationClasses())
			.addClasses(ObmSyncArchiveUtils.projectDBCPClasses())
			.addClasses(ObmSyncArchiveUtils.projectICalendarClasses())
			.addClasses(ObmSyncArchiveUtils.projectMessageQueueClasses())
			.addClasses(ObmSyncArchiveUtils.projectUtilsClasses())
			.addClasses(ObmSyncArchiveUtils.projectLocatorClasses())
			.addClasses(ObmSyncArchiveUtils.projectServicesCommonClasses())
			.addClasses(ObmSyncArchiveUtils.projectCommonClasses());
		
		return ObmSyncArchiveUtils.buildWebArchive(CalendarBindingImplIntegrationTestModule.class)
				.addAsLibraries(projectDependencies())
				.addAsLibraries(wholeObmSyncArchive)
				.addClasses(
						CalendarBindingImplIntegrationTestModule.class,
						ModuleUtils.class,
						org.obm.Configuration.class,
						org.obm.ConfigurationModule.class,
						DatabaseConfigurationFixtureH2.class,
						StaticConfigurationService.class,
						ObmSyncStaticConfigurationService.class,
						TestTransactionConfiguration.class,
						H2DriverConfiguration.class);
	}

	private static File[] projectDependencies() {
		return filterObmDependencies(allObmSyncDependencies());
	}

	private static MavenResolvedArtifact[] allObmSyncDependencies() {
		return Maven.resolver()
			.offline()
			.loadPomFromFile("pom.xml")
			.importRuntimeDependencies()
			.asResolvedArtifact();
	}

	private static File[] filterObmDependencies(MavenResolvedArtifact[] allObmSyncDependencies) {
		return FluentIterable.from(Arrays.asList(
				allObmSyncDependencies))
				.filter(obmDependencyPredicate())
				.transform(artifactAsFile()).toArray(File.class);
	}

	private static Function<MavenResolvedArtifact, File> artifactAsFile() {
		return new Function<MavenResolvedArtifact, File>() {
			@Override
			public File apply(MavenResolvedArtifact input) {
				return input.asFile();
			}
		};
	}

	private static Predicate<MavenResolvedArtifact> obmDependencyPredicate() {
		return new Predicate<MavenResolvedArtifact>() {

			@Override
			public boolean apply(MavenResolvedArtifact input) {
				String groupId = input.getCoordinate().getGroupId();
				return !(groupId.startsWith("com.linagora") || groupId.startsWith("org.obm"));
			}
		};
	}
	
}
