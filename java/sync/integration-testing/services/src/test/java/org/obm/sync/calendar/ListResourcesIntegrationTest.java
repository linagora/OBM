/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.sync.IntegrationTestUtils.makeTestResourceInfo;

import java.net.URL;
import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.H2GuiceServletContextListener;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;

import com.google.inject.Inject;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientModule.class)
public class ListResourcesIntegrationTest extends ObmSyncIntegrationTest {

	private static final String USER1_EMAIL = "user1@domain.org";

	@Inject
	private ArquillianLocatorService locatorService;
	@Inject
	private CalendarClient calendarClient;
	@Inject
	private LoginClient loginClient;

	@Test
	@RunAsClient
	public void testListResources(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token);

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true),
				makeTestResourceInfo(2, "b", true, false),
				makeTestResourceInfo(3, "c", true, false),
				makeTestResourceInfo(4, "d", true, true),
				makeTestResourceInfo(5, "e", true, false),
				makeTestResourceInfo(6, "f", true, false),
				makeTestResourceInfo(7, "g", true, true),
				makeTestResourceInfo(8, "h", true, false),
				makeTestResourceInfo(9, "i", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testGetResourceMetadata(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.getResourceMetadata(user1Token, new String[] {
				"res-a@domain.org",
				"res-b@domain.org"
		});

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true),
				makeTestResourceInfo(2, "b", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithLimit(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, 2, 0);

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true),
				makeTestResourceInfo(2, "b", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithLimitAndOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, 2, 2);

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(3, "c", true, false),
				makeTestResourceInfo(4, "d", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesPagination(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, 3, 0);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true),
				makeTestResourceInfo(2, "b", true, false),
				makeTestResourceInfo(3, "c", true, false)
		);

		resources = calendarClient.listResources(user1Token, 3, 3);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(4, "d", true, true),
				makeTestResourceInfo(5, "e", true, false),
				makeTestResourceInfo(6, "f", true, false)
		);

		resources = calendarClient.listResources(user1Token, 3, 6);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(7, "g", true, true),
				makeTestResourceInfo(8, "h", true, false),
				makeTestResourceInfo(9, "i", true, false)
		);

		loginClient.logout(user1Token);
	}

	@Test(expected = ServerFault.class)
	@RunAsClient
	public void testListResourcesWithNegativeLimit(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");

		calendarClient.listResources(user1Token, -1, 0, null);
	}

	@Test(expected = ServerFault.class)
	@RunAsClient
	public void testListResourcesWithNegativeOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");

		calendarClient.listResources(user1Token, 10, -1, null);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithPatternMatchesName(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, null, 0, "resd");

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(4, "d", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithPatternMatchesNameICase(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, null, 0, "rESd");

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(4, "d", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithPatternMatchesDescription(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, null, 0, "description of resa");

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithPatternMatchesDescriptionICase(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, null, 0, "descRIPTION oF ReSa");

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(1, "a", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListResourcesWithPatternLimitAndOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, 2, 1, "res");

		loginClient.logout(user1Token);

		assertThat(resources).containsExactly(
				makeTestResourceInfo(2, "b", true, true),
				makeTestResourceInfo(3, "c", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithBadPattern(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<ResourceInfo> resources = calendarClient.listResources(user1Token, 2, 1, "iwontmatch");

		loginClient.logout(user1Token);

		assertThat(resources).isEmpty();
	}

	@DeployForEachTests
	@Deployment(managed = false, name = ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils
				.createDeployment()
				.addAsResource("sql/org/obm/sync/calendar/ListResourcesAdditionalDBScripts", H2GuiceServletContextListener.ADDITIONAL_DB_SCRIPTS_FILE)
				.addAsResource("sql/org/obm/sync/calendar/listresources/publicRights.sql")
				.addAsResource("sql/org/obm/sync/calendar/listresources/groupRights.sql")
				.addAsResource("sql/org/obm/sync/calendar/listresources/userRights.sql");
	}
}
