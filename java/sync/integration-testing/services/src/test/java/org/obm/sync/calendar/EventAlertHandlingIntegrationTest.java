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
package org.obm.sync.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.items.EventChanges;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientModule.class)
public class EventAlertHandlingIntegrationTest extends ObmSyncIntegrationTest {

	@Inject ArquillianLocatorService locatorService;
	@Inject CalendarClient calendarClient;
	@Inject BookClient bookClient;
	@Inject LoginClient loginClient;
	
	private String calendar;

	@Before
	public void setUp() {
		calendar = "user1@domain.org";
	}
	
	@Test
	@RunAsClient
	public void testCreateEventCreatesAlert(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, "user1");
		
		Event event = newEventWithAlert(calendar, "1", 30);
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);
		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);

		assertThat(eventFromServer.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testCreateEventInDelegationCreatesAlertForBothUsers(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "2", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user2Token, calendar, event, false, null);
		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testModifyEventModifiesAlert(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, "user1");

		Event event = newEventWithAlert(calendar, "3", 30);
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);

		event.setAlert(60);
		calendarClient.modifyEvent(token, calendar, event, false, false);

		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);

		assertThat(eventFromServer.getAlert()).isEqualTo(60);
	}

	@Test
	@RunAsClient
	public void testModifyEventInDelegationModifiesAlertForBothUsers(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "4", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, calendar, event, false, null);

		event.setAlert(60);
		calendarClient.modifyEvent(user2Token, calendar, event, false, false);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(60);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(60);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetEventFromId(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "5", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSync(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "6", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date lastSync = calendarClient.getSync(user1Token, calendar, null).getLastSync();

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSync(user1Token, calendar, lastSync);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSync(user2Token, calendar, lastSync);

		assertThat(Iterables.getFirst(eventChangesFromServerAsUser1.getUpdated(), null).getAlert()).isEqualTo(30);
		assertThat(Iterables.getFirst(eventChangesFromServerAsUser2.getUpdated(), null).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSyncWithSortedChanges(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "7", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date lastSync = calendarClient.getSync(user1Token, calendar, null).getLastSync();

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSyncWithSortedChanges(user1Token, calendar, lastSync, null);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSyncWithSortedChanges(user2Token, calendar, lastSync, null);

		assertThat(Iterables.getFirst(eventChangesFromServerAsUser1.getUpdated(), null).getAlert()).isEqualTo(30);
		assertThat(Iterables.getFirst(eventChangesFromServerAsUser2.getUpdated(), null).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetEventFromExtId(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "8", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromExtId(user1Token, calendar, event.getExtId());
		Event eventFromServerAsUser2 = calendarClient.getEventFromExtId(user2Token, calendar, event.getExtId());

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetListEventsFromIntervalDate(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "9", 30, "2013-07-01T12:00:00Z");
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date start = date("2013-07-01T11:00:00Z"), end = date("2013-07-01T13:00:00Z");

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		List<Event> eventsFromServerAsUser1 = calendarClient.getListEventsFromIntervalDate(user1Token, calendar, start, end);
		List<Event> eventsFromServerAsUser2 = calendarClient.getListEventsFromIntervalDate(user2Token, calendar, start, end);

		assertThat(eventsFromServerAsUser1.get(0).getAlert()).isEqualTo(30);
		assertThat(eventsFromServerAsUser2.get(0).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_ModifyEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "10", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		event.setTitle("ModifiedTitle");

		Event eventFromServerAsUser2 = calendarClient.modifyEvent(user2Token, calendar, event, false, false);

		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithReadOnlyAccessDoesntInheritAlertFromEventOwner(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "11", 30);
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user3Token = loginClient.login("user3@domain.org", "user3");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, calendar, eventObmId);
		Event eventFromServerAsUser3 = calendarClient.getEventFromId(user3Token, calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser3.getAlert()).isNull();
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSyncEventDate(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		Event event = newEventWithAlert(calendar, "12", 30, "2013-07-01T12:00:00Z");
		AccessToken user1Token = loginClient.login(calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date start = date("2013-07-01T11:00:00Z");

		calendarClient.createEvent(user1Token, calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSyncEventDate(user1Token, calendar, start);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSyncEventDate(user2Token, calendar, start);

		assertThat(Iterables.getFirst(eventChangesFromServerAsUser1.getUpdated(), null).getAlert()).isEqualTo(30);
		assertThat(Iterables.getFirst(eventChangesFromServerAsUser2.getUpdated(), null).getAlert()).isEqualTo(30);
	}

	private Event newEventWithAlert(String calendar, String extIdString, int alert) {
		return newEventWithAlert(calendar, extIdString, alert, "2013-06-01T12:00:00");
	}

	private Event newEventWithAlert(String calendar, String extIdString, int alert, String date) {
		Event event = new Event();
		EventExtId extId = new EventExtId(extIdString);

		event.setOwnerEmail(calendar);
		event.setExtId(extId);
		event.setTitle("Event_" + extIdString);
		event.setStartDate(date(date));
		event.setDuration(3600);
		event.addAttendee(UnidentifiedAttendee.builder().email(calendar).asOrganizer().build());
		event.setAlert(alert);

		return event;
	}
	
	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils.createDeployment();
	}
}
