/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.sync.calendar.CalendarUtils.newEvent;

import java.net.URL;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.push.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class StoreEventIntegrationTest extends ObmSyncIntegrationTest {

	@Test
	@RunAsClient
	public void testStoreEventCreatesEventIfNotPresent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		Event event = newEvent(calendar, "user1", "testStoreEventCreatesEventIfNotPresent");

		AccessToken token = loginClient.login(calendar, "user1");

		calendarClient.storeEvent(token, calendar, event, false, null);

		Event eventFromServer = calendarClient.getEventFromExtId(token, calendar, event.getExtId());

		assertThat(eventFromServer).usingComparator(ignoreDatabaseElementsComparator()).isEqualTo(event);
	}

	@Test
	@RunAsClient
	public void testStoreEventModifiesEventIfPresent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		Event event = newEvent(calendar, "user1", "testStoreEventModifiesEventIfPresent");

		AccessToken token = loginClient.login(calendar, "user1");

		calendarClient.createEvent(token, calendar, event, false, null);
		event.setTitle("ModifiedTitle");
		calendarClient.storeEvent(token, calendar, event, false, null);

		Event eventFromServer = calendarClient.getEventFromExtId(token, calendar, event.getExtId());

		assertThat(eventFromServer).usingComparator(ignoreDatabaseElementsComparator()).isEqualTo(event);
	}

	@Test(expected = EventAlreadyExistException.class)
	@RunAsClient
	public void testCreateEventFailsIfPresent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		Event event = newEvent(calendar, "user1", "testCreateEventFailsIfPresent");

		AccessToken token = loginClient.login(calendar, "user1");

		calendarClient.createEvent(token, calendar, event, false, null);
		calendarClient.createEvent(token, calendar, event, false, null);
	}

	@Test
	@RunAsClient
	public void testClientIdCreateTwiceEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		Event event = newEvent(calendar, "user1", "firstExtIdOfCreateTwiceEvent");
		boolean notif = false;
		String clientId = "0123456789012345678901234567890123456789";

		AccessToken token = loginClient.login(calendar, "user1");

		calendarClient.createEvent(token, calendar, event, notif, clientId);
		Event newEventCreation = event.clone();
		newEventCreation.setExtId(new EventExtId("other ExtId"));
		calendarClient.createEvent(token, calendar, newEventCreation, notif, clientId);

		Event eventFromServer = calendarClient.getEventFromExtId(token, calendar, event.getExtId());
		Exception getEventWithSecondExtIdException = new Exception();
		try {
			calendarClient.getEventFromExtId(token, calendar, newEventCreation.getExtId());
		} catch (Exception e) {
			getEventWithSecondExtIdException = e;
		}
		assertThat(eventFromServer).usingComparator(ignoreDatabaseElementsComparator()).isEqualTo(event);
		assertThat(getEventWithSecondExtIdException).isInstanceOf(EventNotFoundException.class);
	}

	@Test
	@RunAsClient
	public void testClientIdCreateEventDeleteItThenCreateAgain(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		Event event = newEvent(calendar, "user1", "extIdOfCreateEventDeleteItThenCreateAgain");
		boolean notif = false;
		String clientId = "0123456789012345678901234567890123456788";

		AccessToken token = loginClient.login(calendar, "user1");

		EventObmId firstId = calendarClient.createEvent(token, calendar, event, notif, clientId);
		Event firstEvent = calendarClient.getEventFromId(token, calendar, firstId);
		calendarClient.removeEventById(token, calendar, firstId, 0, notif);
		EventObmId secondId = calendarClient.createEvent(token, calendar, event, notif, clientId);
		Event secondEvent = calendarClient.getEventFromId(token, calendar, secondId);

		assertThat(firstEvent.getObmId()).isNotNull();
		assertThat(secondEvent.getObmId()).isNotNull().isNotEqualTo(firstEvent.getObmId());
	}
}
