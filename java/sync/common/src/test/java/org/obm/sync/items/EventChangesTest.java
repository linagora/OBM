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
package org.obm.sync.items;

import java.util.Calendar;
import java.util.Date;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.RecurrenceId;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class EventChangesTest {

	@Test
	public void testAnonymize() {
		Date lastSync = new DateTime(2012, Calendar.APRIL, 25, 14, 0).toDate();

		Attendee attendee1 = ContactAttendee.builder().email("attendee1@email.com").build();
		Attendee attendee2 = ContactAttendee.builder().email("attendee2@email.com").build();
		Attendee attendee3 = ContactAttendee.builder().email("attendee3@email.com").build();
		Attendee attendee4 = ContactAttendee.builder().email("attendee4@email.com").build();

		DeletedEvent deletedEvent1 = new DeletedEvent(new EventObmId(1), new EventExtId(
				"deleted_event_1"));
		DeletedEvent deletedEvent2 = new DeletedEvent(new EventObmId(2), new EventExtId(
				"deleted_event_2"));

		ParticipationChanges participationChanges1 = new ParticipationChanges();
		participationChanges1.setEventId(new EventObmId(3));
		participationChanges1.setEventExtId(new EventExtId("participation_changes_1"));
		participationChanges1.setRecurrenceId(new RecurrenceId("recurrence_id_1"));
		participationChanges1.setAttendees(Lists.newArrayList(attendee1, attendee2));

		ParticipationChanges participationChanges2 = new ParticipationChanges();
		participationChanges2.setEventId(new EventObmId(4));
		participationChanges2.setEventExtId(new EventExtId("participation_changes_2"));
		participationChanges2.setRecurrenceId(new RecurrenceId("recurrence_id_2"));
		participationChanges2.setAttendees(Lists.newArrayList(attendee3, attendee4));

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");

		Event privateEvent = new Event();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateEvent.setExtId(new EventExtId("private_event"));
		privateEvent.setTitle("private event");

		Event privateAnonymizedEvent = new Event();
		privateAnonymizedEvent.setPrivacy(EventPrivacy.PRIVATE);
		privateAnonymizedEvent.setExtId(new EventExtId("private_event"));

		EventChanges changes = new EventChanges();
		changes.setLastSync(lastSync);
		changes.setDeletedEvents(ImmutableSet.of(deletedEvent1, deletedEvent2));
		changes.setParticipationUpdated(Lists.newArrayList(participationChanges1,
				participationChanges2));
		changes.setUpdated(Lists.newArrayList(publicEvent, privateEvent));

		EventChanges expectedChanges = new EventChanges();
		expectedChanges.setLastSync(lastSync);
		expectedChanges.setDeletedEvents(ImmutableSet.of(deletedEvent1, deletedEvent2));
		expectedChanges.setParticipationUpdated(Lists.newArrayList(participationChanges1,
				participationChanges2));
		expectedChanges.setUpdated(Lists.newArrayList(publicEvent, privateAnonymizedEvent));

		Assertions.assertThat(changes.anonymizePrivateItems()).isEqualTo(expectedChanges);
	}
}
