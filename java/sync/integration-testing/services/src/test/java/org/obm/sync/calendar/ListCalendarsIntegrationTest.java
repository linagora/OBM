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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
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

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
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
		CalendarInfo[] calendars = calendarClient.listCalendars(user1Token);

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
				makeTestUserCalendarInfo("t", true, false)
		);
	}

	@Test
	@RunAsClient
	public void testGetCalendarMetadata(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken user1Token = loginClient.login(USER1_EMAIL, "user1");
		CalendarInfo[] calendars = calendarClient.getCalendarMetadata(user1Token, new String[] {
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
		CalendarInfo[] calendars = calendarClient.getCalendarMetadata(user1Token, new String[] {
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
