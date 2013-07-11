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
package org.obm.sync.items;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import fr.aliacom.obm.ToolBox;

public class EventChangesTest {

	private Date lastSync;

	private DeletedEvent deletedEvent1;
	private DeletedEvent deletedEvent2;

	private ParticipationChanges participationChanges1;
	private ParticipationChanges participationChanges2;

	@Before
	public void setUp() {
		lastSync = DateUtils.date("2012-04-14T14:00:00");
		deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted_event_1").build();
		deletedEvent2 = DeletedEvent.builder().eventObmId(2).eventExtId("deleted_event_2").build();

		Attendee attendee1 = ContactAttendee.builder().email("attendee1@email.com").build();
		Attendee attendee2 = ContactAttendee.builder().email("attendee2@email.com").build();
		participationChanges1 = ParticipationChanges.builder()
				.eventExtId("participation_changes_1")
				.eventObmId(3)
				.recurrenceId("recurrence_id_1")
				.attendees(Sets.newHashSet(attendee1, attendee2))
				.build();

		Attendee attendee3 = ContactAttendee.builder().email("attendee3@email.com").build();
		Attendee attendee4 = ContactAttendee.builder().email("attendee4@email.com").build();
		participationChanges2 = ParticipationChanges.builder()
				.eventExtId("participation_changes_2")
				.eventObmId(4)
				.recurrenceId("recurrence_id_2")
				.attendees(Sets.newHashSet(attendee3, attendee4))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void buildNull() {
		EventChanges.builder().build();
	}

	@Test(expected=IllegalStateException.class)
	public void buildWithNullLastSync() {
		EventChanges.builder()
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(
						Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(new Event()))
				.build();
	}

	@Test(expected=NullPointerException.class)
	public void buildWithNullDeletedEvent() {
		EventChanges.builder()
			.deletes(null)
			.lastSync(lastSync)
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void buildWithNullUpdatedEvent() {
		EventChanges.builder()
			.updates(null)
			.lastSync(lastSync)
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void buildWithNullParticipationChanges() {
		EventChanges.builder()
			.participationChanges(null)
			.lastSync(lastSync)
			.build();
	}

	@Test
	public void buildParticipationChangesWithOnlyLastSync() {
		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.build();

		assertThat(eventChanges.getLastSync()).isEqualTo(lastSync);
		assertThat(eventChanges.getDeletedEvents()).isNotNull();
		assertThat(eventChanges.getParticipationUpdated()).isNotNull();
		assertThat(eventChanges.getUpdated()).isNotNull();
	}

	@Test
	public void buildWithAllFields() {
		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");

		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(
						Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(publicEvent))
				.build();

		assertThat(eventChanges.getLastSync()).isEqualTo(lastSync);
		assertThat(eventChanges.getDeletedEvents()).isEqualTo(ImmutableSet.of(deletedEvent1, deletedEvent2));
		assertThat(eventChanges.getParticipationUpdated()).isEqualTo(
				Sets.newHashSet(participationChanges1, participationChanges2));
		assertThat(eventChanges.getUpdated()).isEqualTo(Sets.newHashSet(publicEvent));
	}

	@Test
	public void testAnonymize() {

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");

		Event privateEvent = new Event();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateEvent.setExtId(new EventExtId("private_event"));
		privateEvent.setTitle("private event");

		EventChanges eventChanges = EventChanges.builder()
									.lastSync(lastSync)
									.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
									.participationChanges(
											Sets.newHashSet(participationChanges1, participationChanges2))
									.updates(Sets.newHashSet(publicEvent, privateEvent))
									.build();

		Event privateAnonymizedEvent = new Event();
		privateAnonymizedEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateAnonymizedEvent.setAnonymized(true);
		privateAnonymizedEvent.setExtId(new EventExtId("private_event"));

		EventChanges expectedChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(
						Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(publicEvent, privateAnonymizedEvent))
				.build();

		assertThat(eventChanges.anonymizePrivateItems(ToolBox.getDefaultObmUser())).isEqualTo(expectedChanges);
	}

	@Test
	public void testMoveConfidentialEventsToRemovedEvents() {

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");

		Event privateEvent = new Event();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateEvent.setExtId(new EventExtId("private_event"));
		privateEvent.setTitle("private event");

		Event shouldNotMoveConfidentialEvent = new Event();
		shouldNotMoveConfidentialEvent.setUid(new EventObmId(3));
		shouldNotMoveConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		shouldNotMoveConfidentialEvent.setExtId(new EventExtId("confidential_event"));
		shouldNotMoveConfidentialEvent.addAttendee(ContactAttendee.builder().email("attendee1@email.com").build());

		Event shouldMoveConfidentialEvent = new Event();
		shouldMoveConfidentialEvent.setUid(new EventObmId(4));
		shouldMoveConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		shouldMoveConfidentialEvent.setExtId(new EventExtId("confidential_event2"));

		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(
						publicEvent, privateEvent, shouldMoveConfidentialEvent, shouldNotMoveConfidentialEvent))
				.build();

		DeletedEvent confidentialEventToDeletedEvent =
				DeletedEvent.builder().eventObmId(4).eventExtId("confidential_event2").build();

		EventChanges expectedChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2, confidentialEventToDeletedEvent))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(publicEvent, privateEvent, shouldNotMoveConfidentialEvent))
				.build();

		EventChanges resultChanges = eventChanges.moveConfidentialEventsToRemovedEvents("attendee1@email.com");
		assertThat(resultChanges).isEqualTo(expectedChanges);
	}

	@Test
	public void testMoveAllConfidentialEventsToRemovedEvents() {

		Event shouldMoveConfidentialEvent1 = new Event();
		shouldMoveConfidentialEvent1.setUid(new EventObmId(3));
		shouldMoveConfidentialEvent1.setPrivacy(EventPrivacy.CONFIDENTIAL);
		shouldMoveConfidentialEvent1.setExtId(new EventExtId("confidential_event"));
		shouldMoveConfidentialEvent1.addAttendee(ContactAttendee.builder().email("attendee2@email.com").build());

		Event shouldMoveConfidentialEvent2 = new Event();
		shouldMoveConfidentialEvent2.setUid(new EventObmId(4));
		shouldMoveConfidentialEvent2.setPrivacy(EventPrivacy.CONFIDENTIAL);
		shouldMoveConfidentialEvent2.setExtId(new EventExtId("confidential_event2"));

		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(shouldMoveConfidentialEvent2, shouldMoveConfidentialEvent1))
				.build();

		DeletedEvent confidentialEventToDeletedEvent1 =
				DeletedEvent.builder().eventObmId(3).eventExtId("confidential_event").build();
		DeletedEvent confidentialEventToDeletedEvent2 =
				DeletedEvent.builder().eventObmId(4).eventExtId("confidential_event2").build();

		EventChanges expectedChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(
						deletedEvent1, deletedEvent2,
						confidentialEventToDeletedEvent1, confidentialEventToDeletedEvent2))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.build();

		EventChanges resultChanges = eventChanges.moveConfidentialEventsToRemovedEvents("attendee1@email.com");
		assertThat(resultChanges.getUpdated()).isEmpty();
		assertThat(resultChanges).isEqualTo(expectedChanges);
	}

	@Test
	public void testWithoutConfidentialEvents() {

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");
		final ContactAttendee attendee1 = ContactAttendee.builder().email("attendee1@email.com").build();
		publicEvent.addAttendee(attendee1);

		Event privateEvent = new Event();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateEvent.setExtId(new EventExtId("private_event"));
		privateEvent.setTitle("private event");
		publicEvent.addAttendee(attendee1);

		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(publicEvent, privateEvent))
				.build();

		EventChanges expectedChanges = EventChanges.builder()
				.lastSync(lastSync)
				.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
				.participationChanges(Sets.newHashSet(participationChanges1, participationChanges2))
				.updates(Sets.newHashSet(publicEvent, privateEvent))
				.build();

		EventChanges resultChanges = eventChanges.moveConfidentialEventsToRemovedEvents("attendee1@email.com");
		assertThat(resultChanges).isEqualTo(expectedChanges);
	}
}
