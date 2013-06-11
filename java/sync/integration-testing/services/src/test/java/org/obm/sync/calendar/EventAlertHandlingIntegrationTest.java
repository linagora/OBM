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
import static org.obm.DateUtils.date;

import java.util.Date;
import java.util.List;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.items.EventChanges;

import com.google.common.collect.Iterables;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class EventAlertHandlingIntegrationTest extends ObmSyncIntegrationTest {

	@Test
	@RunAsClient
	public void testCreateEventCreatesAlert() throws Exception {
		String calendar = "user1@domain.org";
		Event event = newEventWithAlert(calendar, "1", 30);

		AccessToken token = loginClient.login(calendar, "user1");
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);
		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);

		assertThat(eventFromServer.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testCreateEventInDelegationCreatesAlertForBothUsers() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "2", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user2Token, user1Calendar, event, false, null);
		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, user1Calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, user1Calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testModifyEventModifiesAlert() throws Exception {
		String calendar = "user1@domain.org";
		Event event = newEventWithAlert(calendar, "3", 30);

		AccessToken token = loginClient.login(calendar, "user1");
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);

		event.setAlert(60);
		calendarClient.modifyEvent(token, calendar, event, false, false);

		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);

		assertThat(eventFromServer.getAlert()).isEqualTo(60);
	}

	@Test
	@RunAsClient
	public void testModifyEventInDelegationModifiesAlertForBothUsers() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "4", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		event.setAlert(60);
		calendarClient.modifyEvent(user2Token, user1Calendar, event, false, false);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, user1Calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, user1Calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(60);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(60);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetEventFromId() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "5", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, user1Calendar, eventObmId);
		Event eventFromServerAsUser2 = calendarClient.getEventFromId(user2Token, user1Calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSync() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "6", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date lastSync = calendarClient.getSync(user1Token, user1Calendar, null).getLastSync();

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSync(user1Token, user1Calendar, lastSync);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSync(user2Token, user1Calendar, lastSync);

		assertThat(Iterables.getFirst(eventChangesFromServerAsUser1.getUpdated(), null).getAlert()).isEqualTo(30);
		assertThat(Iterables.getFirst(eventChangesFromServerAsUser2.getUpdated(), null).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSyncWithSortedChanges() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "7", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date lastSync = calendarClient.getSync(user1Token, user1Calendar, null).getLastSync();

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSyncWithSortedChanges(user1Token, user1Calendar, lastSync, null);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSyncWithSortedChanges(user2Token, user1Calendar, lastSync, null);

		assertThat(Iterables.getFirst(eventChangesFromServerAsUser1.getUpdated(), null).getAlert()).isEqualTo(30);
		assertThat(Iterables.getFirst(eventChangesFromServerAsUser2.getUpdated(), null).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetEventFromExtId() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "8", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromExtId(user1Token, user1Calendar, event.getExtId());
		Event eventFromServerAsUser2 = calendarClient.getEventFromExtId(user2Token, user1Calendar, event.getExtId());

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetListEventsFromIntervalDate() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "9", 30, "2013-07-01T12:00:00Z");

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date start = date("2013-07-01T11:00:00Z"), end = date("2013-07-01T13:00:00Z");

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		List<Event> eventsFromServerAsUser1 = calendarClient.getListEventsFromIntervalDate(user1Token, user1Calendar, start, end);
		List<Event> eventsFromServerAsUser2 = calendarClient.getListEventsFromIntervalDate(user2Token, user1Calendar, start, end);

		assertThat(eventsFromServerAsUser1.get(0).getAlert()).isEqualTo(30);
		assertThat(eventsFromServerAsUser2.get(0).getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_ModifyEvent() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "10", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		event.setTitle("ModifiedTitle");

		Event eventFromServerAsUser2 = calendarClient.modifyEvent(user2Token, user1Calendar, event, false, false);

		assertThat(eventFromServerAsUser2.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testUserWithReadOnlyAccessDoesntInheritAlertFromEventOwner() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "11", 30);

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user3Token = loginClient.login("user3@domain.org", "user3");
		EventObmId eventObmId = calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		Event eventFromServerAsUser1 = calendarClient.getEventFromId(user1Token, user1Calendar, eventObmId);
		Event eventFromServerAsUser3 = calendarClient.getEventFromId(user3Token, user1Calendar, eventObmId);

		assertThat(eventFromServerAsUser1.getAlert()).isEqualTo(30);
		assertThat(eventFromServerAsUser3.getAlert()).isNull();
	}

	@Test
	@RunAsClient
	public void testUserWithDelegationInheritsAlertFromEventOwner_GetSyncEventDate() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, "12", 30, "2013-07-01T12:00:00Z");

		AccessToken user1Token = loginClient.login(user1Calendar, "user1");
		AccessToken user2Token = loginClient.login("user2@domain.org", "user2");
		Date start = date("2013-07-01T11:00:00Z");

		calendarClient.createEvent(user1Token, user1Calendar, event, false, null);

		EventChanges eventChangesFromServerAsUser1 = calendarClient.getSyncEventDate(user1Token, user1Calendar, start);
		EventChanges eventChangesFromServerAsUser2 = calendarClient.getSyncEventDate(user2Token, user1Calendar, start);

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

}
