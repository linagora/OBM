/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
import static org.obm.sync.calendar.CalendarUtils.makeCalendarInfo;
import static org.obm.sync.calendar.CalendarUtils.makeTestUserCalendarInfo;

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
public class ListCalendarsIntegrationTest extends ObmSyncIntegrationTest {

	private static final String USER1_EMAIL = "user1@domain.org";

	@Inject
	private ArquillianLocatorService locatorService;
	@Inject
	private CalendarClient calendarClient;
	@Inject
	private LoginClient loginClient;

	@Test
	@RunAsClient
	public void testListCalendars(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token);

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("user1", "Firstname", "Lastname", true, true),
				makeTestUserCalendarInfo("a", true, true),
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false),
				makeTestUserCalendarInfo("d", true, false),
				makeTestUserCalendarInfo("e", true, false),
				makeTestUserCalendarInfo("f", true, false),
				makeTestUserCalendarInfo("g", true, false),
				makeTestUserCalendarInfo("h", true, false),
				makeTestUserCalendarInfo("i", true, false),
				makeTestUserCalendarInfo("j", true, false),
				makeTestUserCalendarInfo("k", true, true),
				makeTestUserCalendarInfo("l", true, true),
				makeTestUserCalendarInfo("m", true, false),
				makeTestUserCalendarInfo("n", true, false),
				makeTestUserCalendarInfo("o", true, false),
				makeTestUserCalendarInfo("p", true, true),
				makeTestUserCalendarInfo("q", true, true),
				makeTestUserCalendarInfo("r", true, false),
				makeTestUserCalendarInfo("s", true, false),
				makeTestUserCalendarInfo("t", true, false),
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithLimit(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, 5, 0);

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("user1", "Firstname", "Lastname", true, true),
				makeTestUserCalendarInfo("a", true, true),
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false),
				makeTestUserCalendarInfo("d", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithLimitAndOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, 2, 2);

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsPagination(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, 5, 0);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("user1", "Firstname", "Lastname", true, true),
				makeTestUserCalendarInfo("a", true, true),
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false),
				makeTestUserCalendarInfo("d", true, false)
		);

		calendars = calendarClient.listCalendars(user1Token, 5, 5);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("e", true, false),
				makeTestUserCalendarInfo("f", true, false),
				makeTestUserCalendarInfo("g", true, false),
				makeTestUserCalendarInfo("h", true, false),
				makeTestUserCalendarInfo("i", true, false)
		);

		calendars = calendarClient.listCalendars(user1Token, 5, 10);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("j", true, false),
				makeTestUserCalendarInfo("k", true, true),
				makeTestUserCalendarInfo("l", true, true),
				makeTestUserCalendarInfo("m", true, false),
				makeTestUserCalendarInfo("n", true, false)
		);

		loginClient.logout(user1Token);
	}

	@Test
	@RunAsClient
	public void testGetCalendarMetadata(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.getCalendarMetadata(user1Token, new String[] {
				"usera@domain.org",
				"userb@domain.org",
				"userc@domain.org"
		});

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("a", true, true),
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testGetCalendarMetadataOnOwnCalendar(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.getCalendarMetadata(user1Token, new String[] {
				USER1_EMAIL,
				"userc@domain.org"
		});

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("user1", "Firstname", "Lastname", true, true),
				makeTestUserCalendarInfo("c", true, false)
		);
	}

	@RunAsClient
	@Test(expected = ServerFault.class)
	public void testGetCalendarMetadataWithNoCalendars(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");

		calendarClient.getCalendarMetadata(user1Token, new String[] {});
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesLogin(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "testuser");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesLoginICase(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "TESTUS");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesLastname(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "Lastname_r");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("r", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesLastnameICase(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "TeSTlasT");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesFirstname(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "TestFirs");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternMatchesFirstnameICase(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "testfirST");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser1", "Test", "User", true, false),
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false),
				makeCalendarInfo("testuser4", "Test", "User", true, false),
				makeCalendarInfo("testuser5", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternWhenMatchingOwnCalendarOnly(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "user1");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("user1", "Firstname", "Lastname", true, true)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternWhenAlsoMatchingOwnCalendar(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login("userc@domain.org", "userc");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, 3, 0, "Firstname");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeTestUserCalendarInfo("a", true, true),
				makeTestUserCalendarInfo("b", true, true),
				makeTestUserCalendarInfo("c", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithPatternLimitAndOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, 2, 1, "testuser");

		loginClient.logout(user1Token);

		assertThat(calendars).containsExactly(
				makeCalendarInfo("testuser2", "Test", "User", true, false),
				makeCalendarInfo("testuser3", "Test", "User", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testListCalendarsWithBadPattern(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		Collection<CalendarInfo> calendars = calendarClient.listCalendars(user1Token, null, 0, "iwontmatch");

		loginClient.logout(user1Token);

		assertThat(calendars).isEmpty();
	}

	@Test(expected = ServerFault.class)
	@RunAsClient
	public void testListCalendarsWithNegativeLimit(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");

		calendarClient.listCalendars(user1Token, -1, 0, null);
	}

	@Test(expected = ServerFault.class)
	@RunAsClient
	public void testListCalendarsWithNegativeOffset(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");

		calendarClient.listCalendars(user1Token, 10, -1, null);
	}

	@DeployForEachTests
	@Deployment(managed = false, name = ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils
				.createDeployment()
				.addAsResource("sql/org/obm/sync/calendar/ListCalendarsAdditionalDBScripts", H2GuiceServletContextListener.ADDITIONAL_DB_SCRIPTS_FILE)
				.addAsResource("sql/org/obm/sync/calendar/listcalendars/publicRights.sql")
				.addAsResource("sql/org/obm/sync/calendar/listcalendars/groupRights.sql")
				.addAsResource("sql/org/obm/sync/calendar/listcalendars/userRights.sql");
	}
}
