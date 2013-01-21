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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class EventRecurrenceTest {
		
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithoutExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		rec1.setEventExceptions(Sets.newHashSet(e1, e2));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(new Date());
		Assert.assertNull(exception);
	}
	
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		
		rec1.setEventExceptions(Sets.newHashSet(e1));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(e1.getRecurrenceId());
		Assert.assertNotNull(exception);
		Assert.assertEquals(e1, exception);
	}

	@Test
	public void testDailyIsRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		
		Assert.assertTrue(rec1.isRecurrent());
	}

	@Test
	public void testWeeklyIsRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.weekly);
		
		Assert.assertTrue(rec1.isRecurrent());
	}
	
	@Test
	public void testMonthlyByDayIsRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.monthlybyday);
		
		Assert.assertTrue(rec1.isRecurrent());
	}
	
	@Test
	public void testMonthlyByDateIsRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.monthlybydate);
		
		Assert.assertTrue(rec1.isRecurrent());
	}
	
	@Test
	public void testYearlyIsRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.yearly);
		
		Assert.assertTrue(rec1.isRecurrent());
	}
	
	@Test 
	public void testIsNotRecurrent() {
		EventRecurrence rec1 = getOneEventRecurrenceByKind(RecurrenceKind.none);
		
		Assert.assertFalse(rec1.isRecurrent());
	}
	
	private EventRecurrence getOneEventRecurrenceByKind(RecurrenceKind recurrenceKind) {
		EventRecurrence rec = new EventRecurrence();
		rec.setKind(recurrenceKind);
		
		return rec;
	}
	
	@Test
	public void testReplaceDeclinedEventExceptionByException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		Attendee firstAttendee = e1.getAttendees().get(0);
		firstAttendee.setParticipation(Participation.declined());	
		rec1.setEventExceptions(Sets.newHashSet(e1, e2));
		
		String attendeeWithDeclinedEventEmail = "email0@email.com";
		rec1.replaceUnattendedEventExceptionByException(attendeeWithDeclinedEventEmail);
		
		Assertions.assertThat(rec1.getEventExceptions()).containsOnly(e2);
		Assertions.assertThat(rec1.getExceptions()).containsOnly(e1.getRecurrenceId());
	}

	@Test
	public void testDontReplaceDeclinedEventExceptionByException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();

		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);

		Attendee firstAttendee = e1.getAttendees().get(0);
		firstAttendee.setParticipation(Participation.declined());
		rec1.setEventExceptions(Sets.newHashSet(e1, e2));

		EventRecurrence rec2 = getOneDailyEventRecurence();

		rec2.addEventException(e2);
		rec2.addException(e1.getStartDate());

		String attendeeWithDeclinedEventEmail = "email2@email.com";
		rec1.replaceUnattendedEventExceptionByException(attendeeWithDeclinedEventEmail);

		Assert.assertEquals(rec1, rec2);
	}
	
	@Test
	public void testHasAnyExceptionAtDateOnEventException() {
		Event exception = new Event();
		exception.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		exception.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Sets.newHashSet(exception));
		recurrence.setExceptions(Collections.<Date>emptyList());
		
		boolean exceptionFound = recurrence.hasAnyExceptionAtDate(DateUtils.date("2004-12-14T22:00:00Z"));
		Assertions.assertThat(exceptionFound).isTrue();
	}

	@Test
	public void testHasAnyExceptionAtDateOnDeletedException() {
		Date deletedException = DateUtils.date("2004-12-14T22:00:00Z");
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Lists.newArrayList(deletedException));
		recurrence.setEventExceptions(ImmutableSet.<Event>of());
		
		boolean exceptionFound = recurrence.hasAnyExceptionAtDate(DateUtils.date("2004-12-14T22:00:00Z"));
		Assertions.assertThat(exceptionFound).isTrue();
	}

	@Test
	public void testHasAnyExceptionAtDateNotFound() {
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Collections.<Date>emptyList());
		recurrence.setEventExceptions(ImmutableSet.<Event>of());
		
		boolean exceptionFound = recurrence.hasAnyExceptionAtDate(DateUtils.date("2004-12-14T22:00:00Z"));
		Assertions.assertThat(exceptionFound).isFalse();
	}

	@Test
	public void testHasDeletedException() {
		Date exceptionDeleted = DateUtils.date("2004-12-14T22:00:00Z");
		
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Lists.newArrayList(exceptionDeleted));
		recurrence.setEventExceptions(ImmutableSet.<Event>of());
		
		boolean exceptionFound = recurrence.hasException();
		Assertions.assertThat(exceptionFound).isTrue();
	}

	@Test
	public void testHasEventException() {
		Event eventException = new Event();
		eventException.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		eventException.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Collections.<Date>emptyList());
		recurrence.setEventExceptions(Sets.newHashSet(eventException));
		
		boolean exceptionFound = recurrence.hasEventException();
		Assertions.assertThat(exceptionFound).isTrue();
	}

	@Test
	public void testHasDeletedExceptionIsFalseWhenEventExceptions() {
		Event eventException = new Event();
		eventException.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		eventException.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));
		
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Collections.<Date>emptyList());
		recurrence.setEventExceptions(Sets.newHashSet(eventException));
		
		boolean exceptionFound = recurrence.hasException();
		Assertions.assertThat(exceptionFound).isFalse();
	}

	@Test
	public void testHasEventExceptionIsFalseWhenDeletedExceptions() {
		Date exceptionDeleted = DateUtils.date("2004-12-14T22:00:00Z");
		
		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setExceptions(Lists.newArrayList(exceptionDeleted));
		recurrence.setEventExceptions(ImmutableSet.<Event>of());
		
		boolean exceptionFound = recurrence.hasEventException();
		Assertions.assertThat(exceptionFound).isFalse();
	}

	@Test
	public void testAnonymizeWithoutPrivateMovedExceptions() {
		Date exceptionDeleted = DateUtils.date("2004-12-14T22:00:00Z");

		Event publicEventException1 = new Event();
		publicEventException1.setTitle("public event exception 1");
		publicEventException1.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		publicEventException1.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event publicEventException2 = new Event();
		publicEventException2.setTitle("public event exception 2");
		publicEventException2.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		publicEventException2.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Sets.newHashSet(publicEventException1,
				publicEventException2));
		recurrence.setExceptions(Lists.newArrayList(exceptionDeleted));

		Assertions.assertThat(recurrence.anonymizePrivateItems()).isEqualTo(recurrence);
	}

	@Test
	public void testAnonymizeWithPrivateMovedExceptions() {
		Date exceptionDeleted = DateUtils.date("2004-12-14T22:00:00Z");

		Event publicEventException1 = new Event();
		publicEventException1.setUid(new EventObmId(1));
		publicEventException1.setTitle("public event exception 1");
		publicEventException1.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		publicEventException1.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event publicEventException2 = new Event();
		publicEventException2.setUid(new EventObmId(2));
		publicEventException2.setTitle("public event exception 2");
		publicEventException2.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		publicEventException2.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event privateEventException1 = new Event();
		privateEventException1.setUid(new EventObmId(3));
		privateEventException1.setTitle("private event exception 1");
		privateEventException1.setPrivacy(EventPrivacy.PRIVATE);
		privateEventException1.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		privateEventException1.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event privateEventException2 = new Event();
		privateEventException2.setUid(new EventObmId(4));
		privateEventException2.setTitle("private event exception 2");
		privateEventException2.setPrivacy(EventPrivacy.PRIVATE);
		privateEventException2.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		privateEventException2.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event privateAnonymizedEventException1 = new Event();
		privateAnonymizedEventException1.setUid(new EventObmId(3));
		privateAnonymizedEventException1.setPrivacy(EventPrivacy.PRIVATE);
		privateAnonymizedEventException1.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		privateAnonymizedEventException1.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		Event privateAnonymizedEventException2 = new Event();
		privateAnonymizedEventException2.setUid(new EventObmId(4));
		privateAnonymizedEventException2.setPrivacy(EventPrivacy.PRIVATE);
		privateAnonymizedEventException2.setRecurrenceId(DateUtils.date("2004-12-14T22:00:00Z"));
		privateAnonymizedEventException2.setStartDate(DateUtils.date("2004-12-14T21:00:00Z"));

		EventRecurrence recurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Sets.newHashSet(publicEventException1,
				publicEventException2, privateEventException1, privateEventException2));
		recurrence.setExceptions(Lists.newArrayList(exceptionDeleted));

		EventRecurrence anonymizedRecurrence = getOneEventRecurrenceByKind(RecurrenceKind.daily);
		anonymizedRecurrence.setEventExceptions(Sets.newHashSet(publicEventException1,
				publicEventException2, privateAnonymizedEventException1,
				privateAnonymizedEventException2));
		anonymizedRecurrence.setExceptions(Lists.newArrayList(exceptionDeleted));

		Assertions.assertThat(recurrence.anonymizePrivateItems()).isEqualTo(anonymizedRecurrence);
	}

	private EventRecurrence getOneDailyEventRecurence() {
		EventRecurrence rec = new EventRecurrence();
		rec.setKind(RecurrenceKind.daily);
		rec.setFrequence(2);
		
		return rec;
	}
	
	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTime();
	}

	private Event createEventException(int id, int nbAttendees) {
		Event event = new Event();
		event.setAlert(10);
		event.setAllday(true);
		event.setAttendees( createAttendees(nbAttendees) );
		event.setCategory("category");
		event.setDescription("description");
		event.setExtId(new EventExtId("1"));
		event.setInternalEvent(true);
		event.setLocation("location");
		event.setOpacity(EventOpacity.OPAQUE);
		event.setOwner("owner");
		event.setOwnerDisplayName("owner displayname");
		event.setOwnerEmail("owner email");
		event.setPriority(1);
		event.setPrivacy(EventPrivacy.PRIVATE);
		event.setSequence(0);
		event.setTimeCreate(new Date());
		event.setTimeUpdate(new Date());
		event.setTimezoneName("timeZone");
		event.setTitle("title"+id);
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(id));
		
		Date d1 = getDate(2011, Calendar.DECEMBER, 20);
		event.setRecurrenceId(d1);
		event.setStartDate(d1);
		return event;
	}
	
	private List<Attendee> createAttendees(int count) {
		List<Attendee> attendees = new ArrayList<Attendee>();
		for (int i = 0; i < count; i++) {
			Attendee attendee = UserAttendee
					.builder()
					.canWriteOnCalendar(false)
					.displayName("DisplayName" + i)
					.email("email" + i + "@email.com")
					.percent(1)
					.participationRole(ParticipationRole.REQ)
					.participation(Participation.needsAction())
					.build();
			
			attendee.setOrganizer(isOrganizer(i));
			
			attendees.add(attendee);
		}
		return attendees;
	}
	
	private boolean isOrganizer(int id) {
		return id == 0 ? true : false;
	}

}
