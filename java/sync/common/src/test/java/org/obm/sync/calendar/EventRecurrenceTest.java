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
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EventRecurrenceTest {
	
	/*@Test
	public void testGetEventExceptionWithAttributeChangesWithoutChanges() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e2));
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertTrue(list.isEmpty());
	}
	
	@Test
	public void testGetEventExceptionWithAttributeChangesWithoutEventChanges() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		Event e3 = e2.clone();
		e3.setTitle("Modif"+e3.getTitle());
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e3));
		
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertEquals(1, list.size());
	}
	
	@Test
	public void testGetEventExceptionWithAttributeChangesWithNewEvent() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		Event e3 =createEventException(3, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e2, e3));
		
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertEquals(1, list.size());
	}*/
	
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithoutExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(new Date());
		Assert.assertNull(exception);
	}
	
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(e1.getRecurrenceId());
		Assert.assertNotNull(exception);
		Assert.assertEquals(e1, exception);
	}
	
	@Test
	public void testGetReadableRepeatDays() {
		EventRecurrence rec1 = new EventRecurrence();
		
		rec1.setDays("0101100");
		EnumSet<RecurrenceDay> repeatDays = rec1.getReadableRepeatDays();
		EnumSet<RecurrenceDay> expectedEnumSetOfDays = EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Wednesday, RecurrenceDay.Thursday);
		Assert.assertEquals(repeatDays, expectedEnumSetOfDays);
	}
	
	@Test
	public void testGetReadableRepeatDaysWithAllZeroDays() {
		EventRecurrence rec1 = new EventRecurrence();
		
		rec1.setDays("0000000");
		EnumSet<RecurrenceDay> repeatDays = rec1.getReadableRepeatDays();
		Assert.assertTrue(repeatDays.isEmpty());
	}

	@Test
	public void testGetReadableRepeatDaysWithNullRepeatDays() {
		EventRecurrence rec1 = new EventRecurrence();
		
		EnumSet<RecurrenceDay> repeatDays = rec1.getReadableRepeatDays();
		Assert.assertTrue(repeatDays.isEmpty());
	}

	@Test
	public void testGetReadableRepeatDaysWithEmptyRepeatDays() {
		EventRecurrence rec1 = new EventRecurrence();
		
		EnumSet<RecurrenceDay> repeatDays = rec1.getReadableRepeatDays();
		Assert.assertTrue(repeatDays.isEmpty());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testsetDaysWithMoreDaysThanEnumSize() {
		EventRecurrence rec1 = new EventRecurrence();
		rec1.setDays("00000000");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testsetDaysWithLessDaysThanEnumSize() {
		EventRecurrence rec1 = new EventRecurrence();
		rec1.setDays("000000");		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetDaysWithIllegalCharacter() {
		EventRecurrence rec1 = new EventRecurrence();
		rec1.setDays("0000200");			
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
		firstAttendee.setState(ParticipationState.DECLINED);	
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		String attendeeWithDeclinedEventEmail = "email0@email.com";
		rec1.replaceDeclinedEventExceptionByException(attendeeWithDeclinedEventEmail);
		
		Assertions.assertThat(rec1.getEventExceptions()).containsExactly(e2);
		Assertions.assertThat(rec1.getExceptions()).containsOnly(e1.getRecurrenceId());
	}
	
	@Test
	public void testDontReplaceDeclinedEventExceptionByException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		Attendee firstAttendee = e1.getAttendees().get(0);
		firstAttendee.setState(ParticipationState.DECLINED);
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		EventRecurrence rec2 = rec1.clone();
		
		String attendeeWithDeclinedEventEmail = "email3@email.com";
		rec1.replaceDeclinedEventExceptionByException(attendeeWithDeclinedEventEmail);
		
		Assert.assertEquals(rec1, rec2);
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
		event.setCompletion(new Date());
		event.setDescription("description");
		event.setDomain("domain");
		event.setDuration(10);
		event.setEntityId(1);
		event.setExtId(new EventExtId("1"));
		event.setInternalEvent(true);
		event.setLocation("location");
		event.setOpacity(EventOpacity.OPAQUE);
		event.setOwner("owner");
		event.setOwnerDisplayName("owner displayname");
		event.setOwnerEmail("owner email");
		event.setPercent(1);
		event.setPriority(1);
		event.setPrivacy(1);
		event.setSequence(0);
		event.setTimeCreate(new Date());
		event.setTimeUpdate(new Date());
		event.setTimezoneName("timeZone");
		event.setTitle("title"+id);
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(id));
		
		Date d1 = getDate(2011, Calendar.DECEMBER, 20);
		event.setRecurrenceId(d1);
		event.setDate(d1);
		return event;
	}
	
	private List<Attendee> createAttendees(int count) {
		List<Attendee> attendees = new ArrayList<Attendee>();
		for (int i = 0; i < count; i++) {
			Attendee attendee = new Attendee();
			attendee.setCanWriteOnCalendar(false);
			attendee.setDisplayName("DisplayName" + i);
			attendee.setEmail("email" + i + "@email.com");
			attendee.setObmUser(true);
			attendee.setOrganizer(isOrganizer(i));
			attendee.setPercent(1);
			attendee.setRequired(ParticipationRole.REQ);
			attendee.setState(ParticipationState.NEEDSACTION);
			attendees.add(attendee);
		}
		return attendees;
	}
	
	private boolean isOrganizer(int id) {
		return id == 0 ? true : false;
	}

}
