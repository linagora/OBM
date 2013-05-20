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

import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class EventAlertHandlingIntegrationTest extends CalendarIntegrationTest {

	@Test
	@RunAsClient
	public void testCreateEventCreatesAlert() throws Exception {
		String calendar = "user1@domain.org";
		Event event = newEventWithAlert(calendar, 30);

		AccessToken token = loginClient.login(calendar, "user1");
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);
		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);

		assertThat(eventFromServer.getAlert()).isEqualTo(30);
	}

	@Test
	@RunAsClient
	public void testCreateEventInDelegationCreatesAlertForBothUsers() throws Exception {
		String user1Calendar = "user1@domain.org";
		Event event = newEventWithAlert(user1Calendar, 30);

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
		Event event = newEventWithAlert(calendar, 30);

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
		Event event = newEventWithAlert(user1Calendar, 30);

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

	private Event newEventWithAlert(String calendar, int alert) {
		Event event = new Event();
		EventExtId extId = new EventExtId(UUID.randomUUID().toString());

		event.setOwnerEmail(calendar);
		event.setExtId(extId);
		event.setTitle("Event_" + extId.serializeToString());
		event.setStartDate(date("2013-06-01T12:00:00"));
		event.setDuration(3600);
		event.addAttendee(UnidentifiedAttendee.builder().email(calendar).asOrganizer().build());
		event.setAlert(alert);

		return event;
	}

}
