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
import static org.obm.DateUtils.dateUTC;

import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.filter.Slow;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.utils.DateUtils;
import org.obm.sync.H2GuiceServletContextListener;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncStaticConfigurationService.ObmSyncConfiguration;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class) @Slow
public class CalendarBindingImplIntegrationTest {
	
	@ArquillianResource URL baseURL;

	private LoginClient loginClient;
	private CalendarClient calendarClient;

	@Before
	public void setUp() {
		Logger logger = LoggerFactory.getLogger(getClass());
		ObmSyncConfiguration configuration = new ObmSyncConfiguration(new Configuration(), new Configuration.ObmSync());
		SyncClientException exceptionFactory = new SyncClientException();
		LocatorService locatorService = arquillianLocatorService();
		Locator locator = new Locator(configuration, locatorService) {};
		
		loginClient = new LoginClient("integration-testing", configuration, exceptionFactory, locator, logger) {};
		calendarClient = new CalendarClient(exceptionFactory, locator, logger) {};
	}

	private LocatorService arquillianLocatorService() {
		return new LocatorService() {
			
			@Override
			public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
				return baseURL.toExternalForm();
			}
		};
	}
	
	@Test @RunAsClient
	public void testImportICS() throws Exception {
		String calendar = "user1@domain.org";
		InputStream icsData = ClassLoader.getSystemClassLoader().getResourceAsStream("importICalendar.sample.ics");

		AccessToken accessToken = loginClient.login(calendar, "user1");
		int importCount = calendarClient.importICalendar(accessToken, calendar, IOUtils.toString(icsData), UUID.randomUUID().toString());
		EventChanges eventsInDB = calendarClient.getSync(accessToken, calendar, DateUtils.getEpochPlusOneSecondCalendar().getTime());
		
		UnidentifiedAttendee organizer = UnidentifiedAttendee.builder()
				.asOrganizer()
				.email("user1@domain.org")
				.displayName("Firstname Lastname")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.REQ)
				.build();

		Event event1 = new Event();
		event1.setTitle("event default");
		event1.setPrivacy(EventPrivacy.PUBLIC);
		event1.setDuration(3600);
		event1.setInternalEvent(true);
		event1.setOwner("user1");
		event1.setOwnerDisplayName("user1");
		event1.setOwnerEmail("user1@domain.org");
		event1.setDescription("");
		event1.setLocation("");
		event1.setCategory("");
		event1.setTimezoneName("Etc/GMT");
		event1.setPriority(0);
		event1.setExtId(new EventExtId("2e8de6deb053002a23c664e11c94dc65032452a779399e26bce4f61598a28709c8a1cc84eb01e4a4d00ebaa2491186186cfa0bc97787ecec4dbc7522123b31b7d3726dcde275e362"));
		event1.setTimeCreate(dateUTC("2013-04-07T12:09:37"));
		event1.setTimeUpdate(dateUTC("2013-04-07T12:09:37"));
		event1.setStartDate(dateUTC("2013-04-01T10:00:00"));
		event1.addAttendee(organizer);
		Event event2 = new Event();
		event2.setTitle("event public");
		event2.setPrivacy(EventPrivacy.PUBLIC);
		event2.setDuration(3600);
		event2.setInternalEvent(true);
		event2.setOwner("user1");
		event2.setOwnerDisplayName("user1");
		event2.setOwnerEmail("user1@domain.org");
		event2.setDescription("description");
		event2.setLocation("location");
		event2.setCategory("existing_category");
		event2.setTimezoneName("Etc/GMT");
		event2.setPriority(0);
		event2.setExtId(new EventExtId("3e8de6deb053002a23c664e11c94dc65032452a779399e26bce4f61598a28709c8a1cc84eb01e4a4d00ebaa2491186186cfa0bc97787ecec4dbc7522123b31b7d3726dcde275e362"));
		event2.setTimeCreate(dateUTC("2013-04-07T12:09:37"));
		event2.setTimeUpdate(dateUTC("2013-04-07T12:09:37"));
		event2.setStartDate(dateUTC("2013-04-02T08:00:00"));
		event2.addAttendee(organizer);
		Event event3 = new Event();
		event3.setTitle("event private");
		event3.setPrivacy(EventPrivacy.PRIVATE);
		event3.setDuration(3600);
		event3.setInternalEvent(true);
		event3.setOwner("user1");
		event3.setOwnerDisplayName("user1");
		event3.setOwnerEmail("user1@domain.org");
		event3.setDescription("");
		event3.setLocation("");
		event3.setCategory("");
		event3.setTimezoneName("Etc/GMT");
		event3.setPriority(0);
		event3.setExtId(new EventExtId("4e8de6deb053002a23c664e11c94dc65032452a779399e26bce4f61598a28709c8a1cc84eb01e4a4d00ebaa2491186186cfa0bc97787ecec4dbc7522123b31b7d3726dcde275e362"));
		event3.setTimeCreate(dateUTC("2013-04-07T12:09:37"));
		event3.setTimeUpdate(dateUTC("2013-04-07T12:09:37"));
		event3.setStartDate(dateUTC("2013-04-03T14:00:00"));
		event3.addAttendee(organizer);
		Event event4 = new Event();
		event4.setTitle("event confidential");
		event4.setPrivacy(EventPrivacy.CONFIDENTIAL);
		event4.setDuration(3600);
		event4.setInternalEvent(true);
		event4.setOwner("user1");
		event4.setOwnerDisplayName("user1");
		event4.setOwnerEmail("user1@domain.org");
		event4.setDescription("");
		event4.setLocation("");
		event4.setCategory("");
		event4.setPriority(0);
		event4.setTimezoneName("Etc/GMT");
		event4.setExtId(new EventExtId("5e8de6deb053002a23c664e11c94dc65032452a779399e26bce4f61598a28709c8a1cc84eb01e4a4d00ebaa2491186186cfa0bc97787ecec4dbc7522123b31b7d3726dcde275e362"));
		event4.setTimeCreate(dateUTC("2013-04-07T12:09:37"));
		event4.setTimeUpdate(dateUTC("2013-04-07T12:09:37"));
		event4.setStartDate(dateUTC("2013-04-04T08:00:00"));
		event4.addAttendee(organizer);
		
		assertThat(importCount).isEqualTo(4);
		assertThat(eventsInDB.getDeletedEvents()).isEmpty();
		assertThat(eventsInDB.getUpdated())
			.usingElementComparator(ignoreDatabaseElementsComparator())
			.containsOnly(event1, event2, event3, event4);
	}

	private Comparator<? super Event> ignoreDatabaseElementsComparator() {
		return new Comparator<Event>() {

			@Override
			public int compare(Event one, Event two) {
				boolean equalityIgnoringDatabaseElements = 
					Objects.equal(one.getTitle(), two.getTitle())
					&& Objects.equal(one.getDescription(), two.getDescription())
					&& Objects.equal(one.getExtId(), two.getExtId())
					&& Objects.equal(one.getPrivacy(), two.getPrivacy())
					&& Objects.equal(one.getMeetingStatus(), two.getMeetingStatus())
					&& Objects.equal(one.getOwner(), two.getOwner())
					&& Objects.equal(one.getOwnerDisplayName(), two.getOwnerDisplayName())
					&& Objects.equal(one.getOwnerEmail(), two.getOwnerEmail())
					&& Objects.equal(one.getCreatorDisplayName(), two.getCreatorDisplayName())
					&& Objects.equal(one.getCreatorEmail(), two.getCreatorEmail())
					&& Objects.equal(one.getLocation(), two.getLocation())
					&& Objects.equal(one.getStartDate(), two.getStartDate())
					&& Objects.equal(one.getDuration(), two.getDuration())
					&& Objects.equal(one.getAlert(), two.getAlert())
					&& Objects.equal(one.getCategory(), two.getCategory())
					&& Objects.equal(one.getPriority(), two.getPriority())
					&& Objects.equal(one.isAllday(), two.isAllday())
					&& Objects.equal(one.getAttendees(), two.getAttendees())
					&& Objects.equal(one.getRecurrence(), two.getRecurrence())
					&& Objects.equal(one.getType(), two.getType())
					&& Objects.equal(one.getOpacity(), two.getOpacity())
					&& Objects.equal(one.getEntityId(), two.getEntityId())
					&& Objects.equal(one.getTimezoneName(), two.getTimezoneName())
					&& Objects.equal(one.getRecurrenceId(), two.getRecurrenceId())
					&& Objects.equal(one.isInternalEvent(), two.isInternalEvent())
					&& Objects.equal(one.getSequence(), two.getSequence());
					
				if (equalityIgnoringDatabaseElements) {
					return 0;
				}
				return 1;
			}
		};
	}

	@Deployment
	public static WebArchive deployArchive() {
		return ObmSyncArchiveUtils
				.buildWebArchive(CalendarBindingImplIntegrationTestModule.class)
				.addAsResource("sql/org/obm/sync/calendar/h2.sql", H2GuiceServletContextListener.INITIAL_DB_SCRIPT)
				.addClasses(CalendarBindingImplIntegrationTestModule.class);
	}
	
}
