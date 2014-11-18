/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.sync.contact;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.obm.DateUtils;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.IntegrationTestUtils;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.dao.EntityId;
import org.obm.sync.utils.DisplayNameUtils;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.UserPassword;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientModule.class)
public class ContactBirthdayOrAnniversaryIntegrationTest extends ObmSyncIntegrationTest {

	@Inject
	private ArquillianLocatorService locatorService;
	@Inject
	private BookClient bookClient;
	@Inject
	private LoginClient loginClient;
	@Inject
	private CalendarClient calendarClient;

	private String calendar;
	private int addressBookId;
	private Contact contact;

	@Before
	public void setUp() {
		addressBookId = 1;
		calendar = "user1@domain.org";
		contact = IntegrationTestUtils.newContact();
	}

	@RunAsClient
	@Test
	public void testStoreContactWithNoBirthdayOrAnniversaryShouldNotCreateAnyEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));

		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events).isEmpty();
	}

	@RunAsClient
	@Test
	public void testStoreContactWithABirthdayShouldCreateOneEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date birthDate = DateUtils.date("1983-09-23T08:00:00Z");

		contact.setBirthday(birthDate);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events)
			.usingElementComparator(IntegrationTestUtils.ignoreDatabaseElementsComparator(true))
			.containsExactly(newBirthdayOrAnniversaryEvent("birthdayEvent", "user1", birthDate));
	}

	@RunAsClient
	@Test
	public void testStoreContactWithAnAnniversaryShouldCreateOneEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date anniversaryDate = DateUtils.date("1983-12-29T08:00:00Z");

		contact.setAnniversary(anniversaryDate);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events)
			.usingElementComparator(IntegrationTestUtils.ignoreDatabaseElementsComparator(true))
			.containsExactly(newBirthdayOrAnniversaryEvent("anniversaryEvent", "user1", anniversaryDate));
	}

	@RunAsClient
	@Test
	public void testStoreContactWithAnniversaryAndBirthdayShouldCreateTwoEvents(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date birthDate = DateUtils.date("1983-09-23T08:00:00Z"), anniversaryDate = DateUtils.date("1983-12-29T08:00:00Z");

		contact.setBirthday(birthDate);
		contact.setAnniversary(anniversaryDate);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events)
			.usingElementComparator(IntegrationTestUtils.ignoreDatabaseElementsComparator(true))
			.containsAll(ImmutableList.of(
					newBirthdayOrAnniversaryEvent("anniversaryEvent", "user1", anniversaryDate),
					newBirthdayOrAnniversaryEvent("birthdayEvent", "user1", birthDate)));
	}

	@RunAsClient
	@Test
	public void testStoreContactWithABirthdayShouldModifyExistingEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date birthDate = DateUtils.date("1983-09-23T08:00:00Z"), reBirthDate = DateUtils.date("1983-10-23T08:00:00Z");

		contact.setBirthday(birthDate);
		contact = bookClient.storeContact(token, addressBookId, contact, null);

		contact.setBirthday(reBirthDate);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events)
			.usingElementComparator(IntegrationTestUtils.ignoreDatabaseElementsComparator(true))
			.containsExactly(newBirthdayOrAnniversaryEvent("birthdayEvent", "user1", reBirthDate));
	}

	@RunAsClient
	@Test
	public void testStoreContactWithAnAnniversaryShouldModifyExistingEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date anniversaryDate = DateUtils.date("1983-12-29T08:00:00Z"), newAnniversaryDate = DateUtils.date("1983-11-29T08:00:00Z");

		contact.setAnniversary(anniversaryDate);
		contact = bookClient.storeContact(token, addressBookId, contact, null);

		contact.setAnniversary(newAnniversaryDate);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events)
			.usingElementComparator(IntegrationTestUtils.ignoreDatabaseElementsComparator(true))
			.containsExactly(newBirthdayOrAnniversaryEvent("anniversaryEvent", "user1", newAnniversaryDate));
	}

	@RunAsClient
	@Test
	public void testStoreContactWithoutAnniversaryShouldRemoveExistingEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date anniversaryDate = DateUtils.date("1983-12-29T08:00:00Z");

		contact.setAnniversary(anniversaryDate);
		contact = bookClient.storeContact(token, addressBookId, contact, null);

		contact.setAnniversary(null);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events).isEmpty();
	}

	@RunAsClient
	@Test
	public void testStoreContactWithoutABirthdayShouldRemoveExistingEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Date birthDate = DateUtils.date("1983-09-23T08:00:00Z");

		contact.setBirthday(birthDate);
		contact = bookClient.storeContact(token, addressBookId, contact, null);

		contact.setBirthday(null);
		bookClient.storeContact(token, addressBookId, contact, null);

		List<Event> allUser1Events = calendarClient.getAllEvents(token, calendar, EventType.VEVENT);

		assertThat(allUser1Events).isEmpty();
	}

	private Event newBirthdayOrAnniversaryEvent(String extId, String owner, Date startDate) {
		Event event = new Event();
		EventRecurrence rec = new EventRecurrence(RecurrenceKind.yearly);

		rec.setFrequence(1);
		rec.setEnd(null);

		event.setExtId(new EventExtId(extId));
		event.setTitle(DisplayNameUtils.getDisplayName(null, contact.getFirstname(), contact.getLastname()));
		event.setStartDate(startDate);
		event.setDuration(3600);
		event.setAllday(true);
		event.setRecurrence(rec);
		event.setPrivacy(EventPrivacy.PRIVATE);
		event.setPriority(1);
		event.setOwnerEmail(calendar);
		event.setInternalEvent(true);
		event.setDescription("");
		event.setLocation("");
		event.setOwner(owner);
		event.setOwnerDisplayName(owner);
		event.setCategory("");

		Attendee at = UserAttendee
				.builder()
				.email(calendar)
				.participationRole(ParticipationRole.CHAIR)
				.participation(Participation.accepted())
				.asOrganizer()
				.entityId(EntityId.valueOf(1))
				.build();

		event.addAttendee(at);

		return event;
	}

	@DeployForEachTests
	@Deployment(managed = false, name = ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils.createDeployment();
	}
}
