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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.sync.calendar.Participation.State;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.user.Users;


public class EventTest {

	@Test
	public void testIsEventInThePast1970() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 0, 0);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isTrue();
	}

	@Test
	public void testIsEventInThePastOneDayLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isTrue();
	}

	@Test
	public void testIsEventInThePastOneHourLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isTrue();
	}

	@Test
	public void testIsEventInThePastOneSecondLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -1);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isTrue();
	}

	@Test
	public void testIsEventInThePastOneMinuteMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isFalse();
	}

	@Test
	public void testIsEventInThePastOneYearMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		event.setStartDate(calendar.getTime());
		assertThat(event.isEventInThePast()).isFalse();
	}

	@Test
	public void testIsEventInThePastStartInThePastEndInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -10);
		event.setStartDate(calendar.getTime());
		event.setDuration(3600);
		assertThat(event.isEventInThePast()).isFalse();
	}

	@Test
	public void testIsEventInThePastOneOccurrenceInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		event.setStartDate(calendar.getTime());
		event.setDuration(3600);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		calendar.add(Calendar.MONTH, 1);
		recurrence.setEnd(calendar.getTime());
		event.setRecurrence(recurrence);
		assertThat(event.isEventInThePast()).isFalse();
	}

	@Test
	public void testModifiedSinceNullLastsync() {
		Event event = new Event();
		event.setTimeCreate(new Date());
		boolean modified = event.modifiedSince(null);
		assertThat(modified).isTrue();
	}

	@Test
	public void testModifiedSinceZeroLastsync() {
		Event event = new Event();
		event.setTimeCreate(new Date());
		boolean modified = event.modifiedSince(new Date(0));
		assertThat(modified).isTrue();
	}

	@Test
	public void testModifiedSinceLastsyncBeforeTimecreate() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, -1);
		boolean modified = event.modifiedSince(calendar.getTime());
		assertThat(modified).isTrue();
	}

	@Test
	public void testModifiedLastSyncAfterTimecreateBeforeTimeupdate() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 2);
		event.setTimeUpdate(calendar.getTime());
		calendar.add(Calendar.MONTH, -1);
		boolean modified = event.modifiedSince(calendar.getTime());
		assertThat(modified).isTrue();
	}

	@Test
	public void testModifiedSinceLastsyncAfterTimecreateAfterTimeupdate() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		event.setTimeUpdate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		boolean modified = event.modifiedSince(calendar.getTime());
		assertThat(modified).isFalse();
	}

	@Test
	public void testModifiedSinceLastsyncAfterTimeCreate() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		boolean modified = event.modifiedSince(calendar.getTime());
		assertThat(modified).isFalse();
	}

	@Test
	public void testModifiedSinceLastSyncEqualsTimecreate() {
		Event event = new Event();

		Calendar calendar = Calendar.getInstance();
		Date timecreate = calendar.getTime();
		event.setTimeCreate(timecreate);

		assertThat(event.modifiedSince(timecreate)).isTrue();
	}

	@Test
	public void testModifiedSinceLastSyncEqualsTimeupdate() {
		Event event = new Event();

		Calendar calendar = Calendar.getInstance();
		Date timecreate = calendar.getTime();
		event.setTimeCreate(timecreate);

		calendar.add(Calendar.MONTH, 1);
		Date timeupdate = calendar.getTime();
		event.setTimeUpdate(timeupdate);

		assertThat(event.modifiedSince(timeupdate)).isTrue();
	}

	@Test
	public void testRecurrentEventHasAModifiedException(){
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		Date dateStart = calendar.getTime();
		Calendar calendarUpdate = calendar;
		calendarUpdate.add(Calendar.HOUR, 1);
		event.setTimeCreate(dateStart);
		event.setStartDate(dateStart);

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);

		HashSet<Event> eventExceptions = Sets.newHashSet();
		Event exception = event.clone();
		exception.setTimeCreate(dateStart);
		exception.setTimeUpdate(calendarUpdate.getTime());
		eventExceptions.add(exception);

		recurrence.setEventExceptions(eventExceptions);
		event.setRecurrence(recurrence);

		assertThat(event.exceptionModifiedSince(dateStart)).isTrue();
	}

	@Test
	public void testRecurrentEventHasNotAnyModifiedException(){
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		Date dateStart = calendar.getTime();
		Calendar calendarUpdate = calendar;
		calendarUpdate.add(Calendar.HOUR, 1);
		event.setTimeCreate(dateStart);
		event.setStartDate(dateStart);

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		event.setRecurrence(recurrence);

		assertThat(event.exceptionModifiedSince(dateStart)).isFalse();
	}

	@Test
	public void testEventClone() {
		Event newEvent = createOneEvent(5);
		Event clone = newEvent.clone();
		assertThat(newEvent).isEqualTo(clone);
	}

	@Test
	public void testModifiedEventClone() {
		Event newEvent = createOneEvent(5);
		Event clone = newEvent.clone();
		clone.setDescription("event updated");
		assertThat(clone).isNotSameAs(newEvent);
	}

	@Test
	public void testEventRecurrenceClone() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event eventCloned = newEvent.clone();
		assertThat(newEvent).isEqualTo(eventCloned);
	}

	@Test
	public void testModifiedEventRecurrenceClone() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		recurrence.setFrequence(10);
		newEvent.setRecurrence(recurrence);

		Event eventCloned = newEvent.clone();
		EventRecurrence recurrenceCloned = eventCloned.getRecurrence();
		recurrenceCloned.setFrequence(25);

		assertThat(newEvent).isNotSameAs(eventCloned);
	}

	@Test
	public void testHasNoImportantChangesEvent() {
		Event event = createOneEvent(5);
		assertThat(event.hasImportantChanges(event)).isFalse();
	}

	@Test
	public void testGetEventExceptionsWithImportantChangesWithNullRecurrence() {
		Event before = createOneEvent(5);
		Event after = before.clone();
		after.setRecurrence(createDailyRecurrenceUntil(new DateTime(before.getStartDate()).plusDays(3).toDate()));
		assertThat(after.getEventExceptionsWithImportantChanges(before)).isEmpty();
	}

	@Test
	public void testGetEventExceptionsWithImportantChangesWithNullInBeforeRecurrenceAndEventExceptionAfter() {
		Event before = createOneEvent(5);
		Event after = before.clone();
		after.setRecurrence(createDailyRecurrenceUntil(new DateTime(before.getStartDate()).plusDays(3).toDate()));
		Event secondOccurrence = after.getOccurrence(new DateTime(before.getStartDate()).plusDays(1).toDate());
		secondOccurrence.setLocation("new location");
		after.addEventException(secondOccurrence);
		assertThat(after.getEventExceptionsWithImportantChanges(before)).containsSequence(secondOccurrence);
	}

	@Test
	public void testHasImportantChangesEventRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event updateEvent = newEvent.clone();
		updateEvent.setDescription("Date updated");
		Calendar calendarUpdate = Calendar.getInstance();
		calendarUpdate.add(Calendar.HOUR, 4);
		updateEvent.setStartDate(calendarUpdate.getTime());

		assertThat(updateEvent.hasImportantChanges(newEvent)).isTrue();
	}

	@Test
	public void testHasNotImportantChangesEventRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event updateEvent = newEvent.clone();
		updateEvent.setDescription("there are not important changes");
		assertThat(updateEvent.hasImportantChanges(newEvent)).isFalse();
	}

	@Test
	public void testHasNotImportantChangesEventAdddingAttendees() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event updateEvent = newEvent.clone();
		updateEvent.setAttendees(createAttendees(8));

		assertThat(updateEvent.hasImportantChanges(newEvent)).isFalse();
	}

	@Test
	public void testHasImportantChangesEventWithAddingAttendeesAndUpdatedLocation() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event updateEvent = newEvent.clone();
		updateEvent.setAttendees(createAttendees(6));
		updateEvent.setLocation("to Paris");

		assertThat(updateEvent.hasImportantChanges(newEvent)).isTrue();
	}

	@Test
	public void testHasImportantChangesEventWithUpdatedRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(newEvent.getStartDate());
		newEvent.setRecurrence(recurrence);

		Event updateEvent = newEvent.clone();
		EventRecurrence updateRecurrence = updateEvent.getRecurrence();

		Calendar calendarUpdate = Calendar.getInstance();
		calendarUpdate.add(Calendar.HOUR, 4);
		updateRecurrence.setEnd(calendarUpdate.getTime());

		assertThat(updateEvent.hasImportantChanges(newEvent)).isTrue();
	}

	@Test
	public void testModifiedEventExceptionHasImportantChanges() {
		Event before = createOneEvent(5);

		DateTime recurrenceEnd = new DateTime(before.getStartDate()).plusDays(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(recurrenceEnd.toDate());
		before.setRecurrence(recurrence);

		DateTime dateTime = new DateTime(before.getStartDate());
		Event secondOccurrence = before.getOccurrence(dateTime.plusDays(1).toDate());

		DateTime secondOccurrenceRecurrenceId = new DateTime(secondOccurrence.getStartDate());
		secondOccurrence.setStartDate(secondOccurrenceRecurrenceId.plusHours(10).toDate());

		recurrence.getEventExceptions().add(secondOccurrence);

		Event after = before.clone();
		EventRecurrence updateRecurrence = after.getRecurrence();

		Event firstException = Iterables.getOnlyElement(updateRecurrence.getEventExceptions());
		firstException.setStartDate(new DateTime(firstException.getStartDate()).plusHours(4).toDate());

		assertThat(after.hasImportantChanges(before)).isTrue();
	}

	@Test
	public void testHasImportantChangesWithModifiedDescriptionEventException() {
		Event before = createOneEvent(5);

		DateTime recurrenceEnd = new DateTime(before.getStartDate()).plusDays(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(recurrenceEnd.toDate());
		before.setRecurrence(recurrence);

		DateTime dateTime = new DateTime(before.getStartDate());
		Event secondOccurrence = before.getOccurrence(dateTime.plusDays(1).toDate());

		DateTime secondOccurrenceRecurrenceId = new DateTime(secondOccurrence.getStartDate());
		secondOccurrence.setStartDate(secondOccurrenceRecurrenceId.plusHours(10).toDate());

		recurrence.getEventExceptions().add(secondOccurrence);

		Event after = before.clone();
		EventRecurrence updateRecurrence = after.getRecurrence();

		Event firstException = Iterables.getOnlyElement(updateRecurrence.getEventExceptions());
		firstException.setDescription("my very different description");

		assertThat(after.hasImportantChanges(before)).isFalse();
	}


	@Test
	public void testModifiedEventExceptionHasImportantChangesExceptedEventException() {
		Event before = createOneEvent(5);

		DateTime recurrenceEnd = new DateTime(before.getStartDate()).plusDays(5);
		EventRecurrence recurrence = createDailyRecurrenceUntil(recurrenceEnd.toDate());
		before.setRecurrence(recurrence);

		DateTime dateTime = new DateTime(before.getStartDate());
		Event secondOccurrence = before.getOccurrence(dateTime.plusDays(1).toDate());

		DateTime secondOccurrenceRecurrenceId = new DateTime(secondOccurrence.getStartDate());
		secondOccurrence.setStartDate(secondOccurrenceRecurrenceId.plusHours(10).toDate());

		recurrence.getEventExceptions().add(secondOccurrence);

		Event after = before.clone();
		EventRecurrence updateRecurrence = after.getRecurrence();

		Event firstException = Iterables.getOnlyElement(updateRecurrence.getEventExceptions());
		firstException.setStartDate(new DateTime(firstException.getStartDate()).plusHours(4).toDate());

		assertThat(after.hasImportantChangesExceptedEventException(before)).isFalse();
	}

	@Test
	public void testHasNotImportantChangesEventWithNullImportantParameter() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation(null);
		Event updateEvent = newEvent.clone();
		assertThat(updateEvent.hasImportantChanges(newEvent)).isFalse();
	}

	@Test
	public void testHasNotImportantChangesEventWithEmptyImportantChange() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation("");
		Event updateEvent = newEvent.clone();
		assertThat(updateEvent.hasImportantChanges(newEvent)).isFalse();
	}

	@Test
	public void testHasImportantChangesEventWithNullImportantChangeToEventCloned() {
		Event newEvent = createOneEvent(5);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation(null);
		assertThat(updateEvent.hasImportantChanges(newEvent)).isTrue();
	}

	@Test
	public void testHasImportantChangesEventWithEmptyImportantChangeToEventCloned() {
		Event newEvent = createOneEvent(5);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation("");
		assertThat(updateEvent.hasImportantChanges(newEvent)).isTrue();
	}

	@Test
	public void testHasNotImportantChangesEventCompareEmptyAndNullImportantChange() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation(null);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation("");
		assertThat(updateEvent.hasImportantChanges(newEvent)).isFalse();
	}

	@Test
	public void testHasChangesOnAllEventAttributesExceptedExceptionWithoutChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();

		boolean change = ev1.hasChangesExceptedEventException(ev2);

		assertThat(change).isFalse();
	}

	@Test
	public void testHasChangesOnAllEventAttributesExceptedExceptionWithChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();

		ev2.setTitle("Change: "+ev2.getTitle());

		boolean change = ev1.hasChangesExceptedEventException(ev2);

		assertThat(change).isTrue();
	}

	@Test
	public void testHasImportantChangesExceptedEventExceptionWithoutChanges() {
		Event before = createOneEvent(3);
		Event after = before.clone();

		EventRecurrence recurrence = createDailyRecurrenceUntil(before.getStartDate());
		after.setRecurrence(recurrence);

		assertThat(after.hasImportantChangesExceptedEventException(before)).isTrue();
	}

	@Test
	public void testHasImportantChangesExceptedEventExceptionWithChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();

		ev2.setLocation("a new location");

		boolean change = ev1.hasImportantChangesExceptedEventException(ev2);

		assertThat(change).isTrue();
	}

	@Test
	public void getOccurrenceWithoutException(){
		Event ev1 = createOneEvent(3);
		Calendar cal = Calendar.getInstance();
		cal.setTime(ev1.getStartDate());
		cal.set(2012, 12, 12);
		Date recurrenceId = cal.getTime();


		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setFrequence(1);
		ev1.setRecurrence(recurrence);

		Event instance = ev1.getOccurrence(recurrenceId);

		assertThat(instance).isNotNull();
		assertThat(recurrenceId).isEqualTo(instance.getStartDate());
		assertThat(recurrenceId).isEqualTo(instance.getRecurrenceId());
		assertThat(RecurrenceKind.none).isEqualTo(instance.getRecurrence().getKind());
	}

	@Test
	public void getOccurrenceWithException(){
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 12, 12);
		Date recurrenceId = cal.getTime();

		Event ev1 = createOneEvent(3);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setFrequence(1);
		Event exception = createOneEvent(2);
		exception.setRecurrenceId(recurrenceId);
		recurrence.addEventException(exception);
		ev1.setRecurrence(recurrence);

		Event instance = ev1.getOccurrence(recurrenceId);

		assertThat(instance).isNotNull();
		assertThat(exception).isEqualTo(instance);
	}

	@Test
	public void testIsNotRecurrentForNullEventRecurrence(){
		Event ev1 = createOneEvent(1);
		assertThat(ev1.isRecurrent()).isFalse();
	}

	@Test
	public void testAddEventException() {
		Event parent = new Event();
		parent.setRecurrence(new EventRecurrence());
		Event eventException = createOneEvent(1);
		parent.addEventException(eventException);

		EventRecurrence parentEventRecurrence = parent.getRecurrence();
		assertThat(parentEventRecurrence.getEventExceptions()).containsOnly(eventException);
	}

	@Test
	public void testGetNegativeExceptionsChangesWithNullEvent() {
		Date exceptionDate = new Date();
		Event ev1 = createEventWithNegativeExceptions(new Date(), 1, exceptionDate);
		Event ev2 = null;
		Collection<Date> difference = ev1.getNegativeExceptionsChanges(ev2);
		assertThat(difference).containsOnly(exceptionDate);
	}

	@Test
	public void testGetNegativeExceptionsChangesWithNonRecurrentEvent() {
		Event ev1 = createOneEvent(1);
		Event ev2 = null;
		Collection<Date> difference = ev1.getNegativeExceptionsChanges(ev2);
		assertThat(difference).isEmpty();
	}

	@Test
	public void testGetNegativeExceptionsChangesWithIdenticalEvents() {
		Date eventDate = new Date();
		Date exceptionDate = new Date();
		Event ev1 = createEventWithNegativeExceptions(eventDate, 1, exceptionDate);
		Event ev2 = createEventWithNegativeExceptions(eventDate, 2, exceptionDate);
		Collection<Date> difference = ev1.getNegativeExceptionsChanges(ev2);
		assertThat(difference).isEmpty();
	}

	@Test
	public void testGetNegativeExceptionsChangesWithDifferentEvents() {
		Date eventDate = new Date();
		Date exceptionDate1 = new DateTime(eventDate).plusDays(2).toDate();
		Date exceptionDate2 = new DateTime(eventDate).plusMonths(1).toDate();
		Event ev1 = createEventWithNegativeExceptions(eventDate, 1, exceptionDate1, exceptionDate2);
		Event ev2 = createEventWithNegativeExceptions(eventDate, 2, exceptionDate1);
		Collection<Date> difference = ev1.getNegativeExceptionsChanges(ev2);
		assertThat(difference).containsOnly(exceptionDate2);
	}

	private Event createEventWithNegativeExceptions(Date eventDate, int sequence, Date... negativeExceptions) {
		Event event = new Event();
		event.setStartDate(eventDate);
		event.setSequence(sequence);
		event.setExtId(new EventExtId("extId"));

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setExceptions(Arrays.asList(negativeExceptions));
		
		event.setRecurrence(recurrence);
		return event;
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAlertNegativeInteger() {
		Event event = new Event();
		event.setAlert(-10);
	}

	@Test
	public void testAlertNullInteger() {
		Event event = new Event();
		event.setAlert(null);
		assertThat(event.getAlert()).isNull();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeDuration() {
		Event event = new Event();
		event.setDuration(-10);
	}

	@Test
	public void testLastsFullDaysWhenOneDayInSeconds() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY);
	    assertThat(event.lastsFullDays()).isTrue();
	}

	@Test
	public void testLastsFullDaysWhenTwoDaysInSeconds() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY * 2);
	    assertThat(event.lastsFullDays()).isTrue();
	}

	@Test
	public void testLastsFullDaysWhenNotMultipleOfSecondsInADay() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY * 2 + 10);
	    assertThat(event.lastsFullDays()).isFalse();
	}

	@Test
	public void testLastsFullDaysDayWhenZero() {
	    Event event = new Event();
	    event.setDuration(0);
	    assertThat(event.lastsFullDays()).isFalse();
	}

	@Test
	public void testDurationInFullDaysWhenZero() {
	    Event event = new Event();
	    event.setDuration(10);
	    assertThat(event.durationInFullDays()).isEqualTo(Event.SECONDS_IN_A_DAY);
	}

	@Test
	public void testDurationInFullDaysWhenAFewSeconds() {
	    Event event = new Event();
	    event.setDuration(10);
	    assertThat(event.durationInFullDays()).isEqualTo(Event.SECONDS_IN_A_DAY);
	}

	@Test
	public void testDurationInFullDaysWhenOneDayAndAFewSeconds() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY + 10);
	    assertThat(event.durationInFullDays()).isEqualTo(Event.SECONDS_IN_A_DAY * 2);
	}

	@Test
	public void testGetDurationAllDayWhenZero() {
	    Event event = new Event();
	    event.setDuration(0);
	    event.setAllday(true);
	    assertThat(event.getDuration()).isEqualTo(Event.SECONDS_IN_A_DAY);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Test
	public void testGetDurationAllDayWhenAFewSeconds() {
	    Event event = new Event();
	    event.setDuration(10);
	    event.setAllday(true);
	    assertThat(event.durationInFullDays()).isEqualTo(Event.SECONDS_IN_A_DAY);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Test
	public void testGetDurationAllDayWhenOneDay() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY);
	    event.setAllday(true);
	    assertThat(event.getDuration()).isEqualTo(Event.SECONDS_IN_A_DAY);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Test
	public void testGetDurationAllDayWhenOneDayAndAFewSeconds() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY + 10);
	    event.setAllday(true);
	    assertThat(event.getDuration()).isEqualTo(Event.SECONDS_IN_A_DAY * 2);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Test
	public void testGetDurationAllDayWhenTwoDays() {
	    Event event = new Event();
	    event.setDuration(Event.SECONDS_IN_A_DAY * 2);
	    event.setAllday(true);
	    assertThat(event.getDuration()).isEqualTo(Event.SECONDS_IN_A_DAY * 2);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Test
	public void testDurationThenAllDay() {
	    Event event = new Event();
	    event.setDuration(10);
	    event.setAllday(true);
	    assertThat(event.getDuration()).isEqualTo(Event.SECONDS_IN_A_DAY);
	    assertThat(event.isAllday()).isEqualTo(true);
	}

	@Ignore("We don't have anyway to enforce this without a complete rework of DB schema")
	@Test(expected=IllegalStateException.class)
	public void testAllDayThenDuration() {
		Event event = new Event();
		event.setAllday(true);
		event.setDuration(10);
	}

	@Test
	public void testDurationThenAllDayThenNotAllDay() {
		Event event = new Event();
		event.setDuration(10);
		event.setAllday(true);
		event.setAllday(false);
		assertThat(event.getDuration()).isEqualTo(10);
		assertThat(event.isAllday()).isEqualTo(false);
	}

	@Test
	public void testEndDateAllDay() {
		Event event = new Event();
		event.setAllday(true);
		event.setStartDate(date("2004-12-11T11:15:10"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-12T11:15:10"));
	}

	@Test
	public void testEndDateNullStartDateAndAllDay() {
		Event event = new Event();
		event.setAllday(true);
		assertThat(event.getEndDate()).isNull();
	}

	@Test
	public void testEndDateNullStartDate() {
		Event event = new Event();
		event.setDuration(10);
		assertThat(event.getEndDate()).isNull();
	}

	@Test
	public void testEndDate() {
		Event event = new Event();
		event.setDuration(120);
		event.setStartDate(date("2004-12-11T11:15:10Z"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-11T11:17:10Z"));
	}

	@Test
	public void testEndDateUndefinedDuration() {
		Event event = new Event();
		event.setStartDate(date("2004-12-11T11:15:10Z"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-11T11:15:10Z"));
	}

	private Event createOneEvent(int nbAttendees) {
		Date currentDate = new Date();
		Event event = new Event();
		event.setAlert(10);
		event.setAllday(true);
		event.setAttendees( createAttendees(nbAttendees) );
		event.setCategory("category");
		event.setStartDate(currentDate);
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
		event.setRecurrenceId(currentDate);
		event.setTimeCreate(currentDate);
		event.setTimeUpdate(currentDate);
		event.setTimezoneName("timeZone");
		event.setTitle("title");
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(1));
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
		if (id == 0) {
			return true;
		} else {
			return false;
		}
	}

	private EventRecurrence createDailyRecurrenceUntil(Date endDate) {
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setDays(new RecurrenceDays(RecurrenceDay.Sunday, RecurrenceDay.Monday,
				RecurrenceDay.Thursday, RecurrenceDay.Friday, RecurrenceDay.Saturday));
		recurrence.setEnd(endDate);
		recurrence.setFrequence(1);
		return recurrence;
	}

	@Test
	public void modifyDescriptionOnEventException() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());

		DateTime recurrenceEndDate = new DateTime(before.getStartDate()).plusDays(3);
		EventRecurrence eventRecurrence = createDailyRecurrenceUntil(recurrenceEndDate.toDate());
		before.setRecurrence(eventRecurrence);

		Event after = before.clone();

		Event eventException = before.getOccurrence(recurrenceStartDate.plusDays(1).toDate());
		eventException.setDescription("my description");
		after.addEventException(eventException);

		List<Event> changes = after.getEventExceptionsWithImportantChanges(before);

		assertThat(changes).isEmpty();
	}

	@Test
	public void testHasImportantChangeWithAddedRecurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());

		Event after = before.clone();
		after.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		assertThat(after.hasImportantChanges(before)).isTrue();
	}

	@Test
	public void testHasImportantChangeWithRemovedRecurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());

		Event after = before.clone();

		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		assertThat(after.hasImportantChanges(before)).isTrue();
	}

	@Test
	public void testHasImportantChangeWithRemovedOccurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());
		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		Event after = before.clone();
		after.addException(recurrenceStartDate.plusDays(1).toDate());

		assertThat(after.hasImportantChanges(before)).isFalse();
	}

	@Test
	public void testHasImportantChangeExceptedEventExceptionWithRemovedOccurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());
		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		Event after = before.clone();
		after.addException(recurrenceStartDate.plusDays(1).toDate());

		assertThat(after.hasImportantChangesExceptedEventException(before)).isFalse();
	}

	@Test
	public void testHasImportantChangeWithRemovedExceptionalOccurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());
		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		Event after = before.clone();
		Date secondOccurrenceDate = recurrenceStartDate.plusDays(1).toDate();

		before.addEventException(before.getOccurrence(secondOccurrenceDate));
		after.addException(secondOccurrenceDate);

		assertThat(after.hasImportantChanges(before)).isFalse();
	}

	@Test
	public void testHasImportantChangeExceptedEventExceptionWithRemovedExceptionalOccurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());
		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		Event after = before.clone();
		Date secondOccurrenceDate = recurrenceStartDate.plusDays(1).toDate();

		before.addEventException(before.getOccurrence(secondOccurrenceDate));
		after.addException(secondOccurrenceDate);

		assertThat(after.hasImportantChangesExceptedEventException(before)).isFalse();
	}

	@Test
	public void testGetEventExceptionsWithImportantChangesWithRemovedExceptionalOccurrence() {
		Event before = new Event();
		DateTime recurrenceStartDate = new DateTime(2012, Calendar.FEBRUARY, 23, 14, 0);
		before.setStartDate(recurrenceStartDate.toDate());
		before.setRecurrence(createDailyRecurrenceUntil(recurrenceStartDate.plusDays(4).toDate()));

		Event after = before.clone();
		Date secondOccurrenceDate = recurrenceStartDate.plusDays(1).toDate();

		before.addEventException(before.getOccurrence(secondOccurrenceDate));
		after.addException(secondOccurrenceDate);

		assertThat(after.getEventExceptionsWithImportantChanges(before)).isEmpty();
	}

	@Test
	public void testGetDeletedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());

		List<Event> deletedEventExceptions = after.getDeletedEventExceptions(before);
		assertThat(deletedEventExceptions ).containsOnly(eexp2);
	}

	@Test
	public void testEmptyGetDeletedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		after.addEventException(eexp2.clone());

		List<Event> deletedEventExceptions = after.getDeletedEventExceptions(before);
		assertThat(deletedEventExceptions ).isEmpty();
	}

	@Test
	public void testGetAddedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		after.addEventException(eexp2.clone());

		List<Event> addedEventExceptions = after.getAddedEventExceptions(before);
		assertThat(addedEventExceptions ).containsOnly(eexp2);
	}

	@Test
	public void testEmptyGetAddedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		after.addEventException(eexp2.clone());

		List<Event> addedEventExceptions = after.getAddedEventExceptions(before);
		assertThat(addedEventExceptions ).isEmpty();
	}

	@Test
	public void testGetModifiedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		Event eexp3 = eexp2.clone();
		eexp3.setDescription("a new description");
		after.addEventException(eexp3);

		List<Event> modifiedEventExceptions = after.getModifiedEventExceptions(before);
		assertThat(modifiedEventExceptions ).containsOnly(eexp3);
	}

	@Test
	public void testDeletedAndAddedNotInGetModifiedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event deletedEventException = new Event();
		deletedEventException.setUid(new EventObmId(3));
		deletedEventException.setRecurrenceId(new Date(3));

		Event addedEventException = new Event();
		addedEventException.setUid(new EventObmId(4));
		addedEventException.setRecurrenceId(new Date(4));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);
		before.addEventException(deletedEventException);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		Event modifiedEventException = eexp2.clone();
		modifiedEventException.setDescription("a new description");
		after.addEventException(modifiedEventException);
		after.addEventException(addedEventException.clone());

		List<Event> modifiedEventExceptions = after.getModifiedEventExceptions(before);
		assertThat(modifiedEventExceptions ).containsOnly(modifiedEventException);
	}

	@Test
	public void TestEmptyGetModifiedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		after.setRecurrence(new EventRecurrence());
		after.addEventException(eexp1.clone());
		after.addEventException(eexp2.clone());

		List<Event> modifiedEventExceptions = after.getModifiedEventExceptions(before);
		assertThat(modifiedEventExceptions ).isEmpty();
	}

	@Test
	public void testGetNegativeExceptionsChangesDoesntContainDeletedEventExceptions() {
		Event eexp1 = new Event();
		eexp1.setUid(new EventObmId(1));
		eexp1.setRecurrenceId(new Date(1));

		Event eexp2 = new Event();
		eexp2.setUid(new EventObmId(2));
		eexp2.setRecurrenceId(new Date(2));

		Event before = new Event();
		before.setRecurrence(new EventRecurrence());
		before.addEventException(eexp1);
		before.addEventException(eexp2);

		Event after = new Event();
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		after.setRecurrence(recurrence);
		after.addEventException(eexp1.clone());
		Date addedException = new Date(1);
		after.addException(addedException);

		Collection<Date> deletedExceptions = after.getNegativeExceptionsChanges(before);
		assertThat(deletedExceptions).containsOnly(addedException);
	}

	@Test
	public void testExtIdIsPropagatedToEventExceptions() {
		String extIdString = "parent-extid";
		Event event = new Event();
		event.addEventException(new Event());
		event.setExtId(new EventExtId(extIdString));
		assertThat(event.getExtId().getExtId()).isEqualTo(extIdString);
		Event eventException = Iterables.getOnlyElement(event.getEventsExceptions());
		assertThat(eventException.getExtId().getExtId()).isEqualTo(extIdString);
	}

	private Event createNonRecurrentEventWithMostFields() {
		Event event = new Event();
		event.setTitle("my event");
		event.setDescription("description");
		event.setUid(new EventObmId(2));
		event.setExtId(new EventExtId("my_event"));
		event.setOwner("owner");
		event.setOwnerDisplayName("owner display name");
		event.setOwnerEmail("owner@email.com");
		event.setCreatorDisplayName("creator");
		event.setCreatorEmail("creator@email.com");
		event.setLocation("location");
		event.setStartDate(new DateTime(2012, Calendar.APRIL, 25, 14, 0).toDate());
		event.setDuration(3660);
		event.setAlert(3);
		event.setCategory("category");
		event.setPriority(2);
		event.setAllday(false);
		event.setAnonymized(false);
		event.setType(EventType.VEVENT);
		event.setOpacity(EventOpacity.TRANSPARENT);
		event.setTimeCreate(new DateTime(2012, Calendar.APRIL, 24, 14, 0).toDate());
		event.setTimeUpdate(new DateTime(2012, Calendar.APRIL, 24, 18, 0).toDate());
		event.setTimezoneName("timezone");
		event.setInternalEvent(false);
		event.setSequence(2);

		Attendee att1 = UserAttendee.builder().email("att1@email.com").build();
		Attendee att2 = UserAttendee.builder().email("att2@email.com").build();

		event.addAttendee(att1);
		event.addAttendee(att2);

		return event;
	}

	private Event createPrivateAnonymizedEvent() {
		Event event = new Event();
		event.setPrivacy(EventPrivacy.PRIVATE);
		event.setAnonymized(true);
		event.setUid(new EventObmId(2));
		event.setExtId(new EventExtId("my_event"));
		event.setOwner("owner");
		event.setOwnerDisplayName("owner display name");
		event.setOwnerEmail("owner@email.com");
		event.setCreatorDisplayName("creator");
		event.setCreatorEmail("creator@email.com");
		event.setStartDate(new DateTime(2012, Calendar.APRIL, 25, 14, 0).toDate());
		event.setDuration(3660);
		event.setPriority(2);
		event.setAllday(false);
		event.setType(EventType.VEVENT);
		event.setOpacity(EventOpacity.TRANSPARENT);
		event.setTimeCreate(new DateTime(2012, Calendar.APRIL, 24, 14, 0).toDate());
		event.setTimeUpdate(new DateTime(2012, Calendar.APRIL, 24, 18, 0).toDate());
		event.setTimezoneName("timezone");
		event.setInternalEvent(false);
		event.setSequence(2);

		return event;
	}

	@Test
	public void testAnonymizePublicEvent() {
		Event publicEvent = createNonRecurrentEventWithMostFields();

		assertThat(publicEvent.anonymizePrivateItems(Users.userAtLinagora)).isEqualTo(publicEvent);
	}

	@Test
	public void testAnonymizeConfidentialEvent() {
		Event confidentialEvent = createNonRecurrentEventWithMostFields();
		confidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);

		assertThat(confidentialEvent.anonymizePrivateItems(Users.userAtLinagora)).isEqualTo(confidentialEvent);
	}

	@Test
	public void testAnonymizePrivateEvent() {
		Event privateEvent = createNonRecurrentEventWithMostFields();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);

		Event privateAnonymizedEvent = createPrivateAnonymizedEvent();

		assertThat(privateEvent.anonymizePrivateItems(Users.userAtLinagora)).isEqualTo(privateAnonymizedEvent);
	}

	@Test
	public void testIsAnonymizedEvent() {
		Event privateEvent = createNonRecurrentEventWithMostFields();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);

		assertThat(privateEvent.anonymizePrivateItems(Users.userAtLinagora).isAnonymized()).isTrue();
	}

	@Test
	public void testAnonymizePublicRecurrentEvent() {
		Event publicEvent = createNonRecurrentEventWithMostFields();

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);

		Event exception = createNonRecurrentEventWithMostFields();
		exception.setRecurrenceId(new DateTime(2012, Calendar.MAY, 24, 14, 0).toDate());

		recurrence.setEventExceptions(Sets.newHashSet(exception));

		publicEvent.setRecurrence(recurrence);

		assertThat(publicEvent.anonymizePrivateItems(Users.userAtLinagora)).isEqualTo(publicEvent);
	}

	@Test
	public void testAnonymizePrivateRecurrentEvent() {
		Event privateEvent = createNonRecurrentEventWithMostFields();
		privateEvent.setPrivacy(EventPrivacy.PRIVATE);

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);

		Event exception = createNonRecurrentEventWithMostFields();
		exception.setPrivacy(EventPrivacy.PRIVATE);
		exception.setRecurrenceId(new DateTime(2012, Calendar.MAY, 24, 14, 0).toDate());

		recurrence.setEventExceptions(Sets.newHashSet(exception));
		privateEvent.setRecurrence(recurrence);

		Event privateAnonymizedEvent = createPrivateAnonymizedEvent();

		EventRecurrence anonymizedRecurrence = new EventRecurrence();
		anonymizedRecurrence.setKind(RecurrenceKind.daily);

		Event anonymizedException = createPrivateAnonymizedEvent();
		anonymizedException.setRecurrenceId(new DateTime(2012, Calendar.MAY, 24, 14, 0).toDate());

		anonymizedRecurrence.setEventExceptions(Sets.newHashSet(anonymizedException));

		privateAnonymizedEvent.setRecurrence(anonymizedRecurrence);

		assertThat(privateEvent.anonymizePrivateItems(Users.userAtLinagora)).isEqualTo(privateAnonymizedEvent);
	}

	@Test
	public void testDeclinedAttendeeWithoutDelegationEndsUpInNeedsAction() {
		Event event = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = event.getAttendees();
		Attendee att1 = Iterables.getFirst(attendees, null);
		att1.setParticipation(Participation.declined());
		att1.setCanWriteOnCalendar(false);
		event.updateParticipation();

		assertThat(att1.getParticipation()).isEqualTo(Participation.needsAction());
	}

	public void testAcceptedAttendeeWithoutDelegationEndsUpInNeedsAction() {
		Event event = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = event.getAttendees();
		Attendee att1 = Iterables.getFirst(attendees, null);
		att1.setParticipation(Participation.accepted());
		att1.setCanWriteOnCalendar(false);
		event.updateParticipation();

		assertThat(att1.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test
	public void testDeclinedAttendeeWithDelegationEndsUpAutoAccepted() {
		Event event = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = event.getAttendees();
		Attendee att1 = Iterables.getFirst(attendees, null);
		att1.setParticipation(Participation.declined());
		att1.setCanWriteOnCalendar(true);
		event.updateParticipation();

		assertThat(att1.getParticipation()).isEqualTo(Participation.accepted());
	}

	@Test
	public void testNeedsActionAttendeeWithDelegationEndsUpAutoAccepted() {
		Event event = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = event.getAttendees();
		Attendee att1 = Iterables.getFirst(attendees, null);
		att1.setParticipation(Participation.needsAction());
		att1.setCanWriteOnCalendar(true);
		event.updateParticipation();

		assertThat(att1.getParticipation()).isEqualTo(Participation.accepted());
	}

	@Test
	public void testTitleIsTruncated() {
		Event event = new Event();
		String title = "abcdefghijklmnopqrstuvwxyz" + // 1
						"abcdefghijklmnopqrstuvwxyz" +// 2
						"abcdefghijklmnopqrstuvwxyz" +// 3
						"abcdefghijklmnopqrstuvwxyz" +// 4
						"abcdefghijklmnopqrstuvwxyz" +// 5
						"abcdefghijklmnopqrstuvwxyz" +// 6
						"abcdefghijklmnopqrstuvwxyz" +// 7
						"abcdefghijklmnopqrstuvwxyz" +// 8
						"abcdefghijklmnopqrstuvwxyz" +// 9
						"abcdefghijklmnopqrstuv"// 10
						;
		event.setTitle(title);
		assertThat(event.getTitle().length()).isEqualTo(Event.DATABASE_TITLE_MAX_LENGTH);
	}

	@Test
	public void testNullTitle() {
		Event event = new Event();
		String title = null;
		event.setTitle(title);
		assertThat(event.getTitle()).isNull();
	}

	@Test
	public void testChangeAttendeesParticipation() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = publicEvent.getAttendees();

		for (Attendee attendee : attendees){
			attendee.setParticipation(Participation.accepted());
		}
		publicEvent.updateParticipation();

		List<Attendee> updatedAttendees = publicEvent.getAttendees();
		for (Attendee upAtt : updatedAttendees){
			assertThat(upAtt.getParticipation()).isEqualTo(Participation.needsAction());
		}
	}

	@Test
	public void resetAttendeeCommentWhenEventIsUpdated() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		List<Attendee> attendees = publicEvent.getAttendees();

		for (Attendee attendee : attendees){
			attendee.setParticipation(
					Participation.builder()
						.state(State.DECLINED)
						.comment("a random comment")
						.build());
		}

		publicEvent.updateParticipation();

		List<Attendee> updatedAttendees = publicEvent.getAttendees();
		for (Attendee updatedAttendee : updatedAttendees){
			assertThat(updatedAttendee.getParticipation().getComment()).isEqualTo(Comment.EMPTY);
		}
	}

	@Test
	public void testAddOrReplaceAttendeeWhenNoAttendee() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.setAttendees(Lists.<Attendee>newArrayList());

		Attendee newAttendee = UserAttendee.builder().email("user@domain").build();

		publicEvent.addOrReplaceAttendee("user@domain", newAttendee);

		assertThat(publicEvent.getAttendees()).containsOnly(newAttendee);
	}

	@Test
	public void testAddOrReplaceAttendeeWhenSameAttendee() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.setAttendees(ImmutableList.of(UserAttendee.builder().email("user@domain").build()));

		Attendee newAttendee = UserAttendee.builder().email("user@domain").build();

		publicEvent.addOrReplaceAttendee("user@domain", newAttendee);

		assertThat(publicEvent.getAttendees()).containsOnly(newAttendee);
	}

	@Test
	public void testAddOrReplaceAttendeeWhenSameAttendeeOtherCase() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.setAttendees(ImmutableList.of(UserAttendee.builder().email("user@domain").build()));

		Attendee newAttendee = UserAttendee.builder().email("user@domain").build();

		publicEvent.addOrReplaceAttendee("User@domain", newAttendee);

		assertThat(publicEvent.getAttendees()).containsOnly(newAttendee);
	}

	@Test
	public void testAddOrReplaceAttendeeWhenSameAttendeeReplacingOtherEmail() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.setAttendees(ImmutableList.of(UserAttendee.builder().email("user@domain").build()));

		Attendee newAttendee = UserAttendee.builder().email("user@domain").build();

		publicEvent.addOrReplaceAttendee("otheruser@domain", newAttendee);

		assertThat(publicEvent.getAttendees()).containsOnly(
				UserAttendee.builder().email("user@domain").build(),
				newAttendee);
	}

	@Test(expected=NullPointerException.class)
	public void testAddOrReplaceAttendeeWhenNullAddess() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.addAttendee(UserAttendee.builder().email("user@domain").build());

		Attendee newAttendee = UserAttendee.builder().email("user@domain").build();

		publicEvent.addOrReplaceAttendee(null, newAttendee);
	}

	@Test(expected=NullPointerException.class)
	public void testAddOrReplaceAttendeeWhenNullAttendee() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		publicEvent.addAttendee(UserAttendee.builder().email("user@domain").build());

		publicEvent.addOrReplaceAttendee("user@domain", null);
	}

	@Test
	public void testFindOwner() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		Attendee ownerAttendee = UserAttendee.builder().email("owner@email.com").asOrganizer().build();
		publicEvent.addAttendee(ownerAttendee);
		assertThat(publicEvent.findOwner()).isEqualTo(ownerAttendee);
	}

	@Test
	public void testNullFindOwner() {
		Event publicEvent = createNonRecurrentEventWithMostFields();
		assertThat(publicEvent.findOwner()).isNull();
	}

	@Test
	public void testFindAttendeeFromEmail() {
		String email = "user@obm.com";
		Event event = createNonRecurrentEventWithMostFields();
		Attendee organizer = UserAttendee.builder().email(email).asOrganizer().build();

		event.addAttendee(organizer);
		event.addAttendee(UserAttendee.builder().email("user2@obm.com").build());

		assertThat(event.findAttendeeFromEmail(email)).isEqualTo(organizer);
	}

	@Test
	public void testFindAttendeeFromEmailWithNullEmail() {
		Event event = createNonRecurrentEventWithMostFields();

		event.addAttendee(UserAttendee.builder().email("user@obm.com").asOrganizer().build());
		event.addAttendee(UserAttendee.builder().email("user2@obm.com").build());

		assertThat(event.findAttendeeFromEmail(null)).isNull();
	}

	@Test
	public void testFindAttendeeFromEmailWhenFirstAttendeeHasNoEmail() {
		String email = "user@obm.com";
		Event event = createNonRecurrentEventWithMostFields();
		UserAttendee organizer = UserAttendee.builder().email(email).asOrganizer().build();

		event.addAttendee(ContactAttendee.builder().build());
		event.addAttendee(organizer);
		event.addAttendee(UserAttendee.builder().email("user2@obm.com").build());

		assertThat(event.findAttendeeFromEmail(email)).isEqualTo(organizer);
	}

	@Test(expected=NullPointerException.class)
	public void testWithOrganizerIfNoneOnNullOrganizer() {
		new Event().withOrganizerIfNone(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithOrganizerIfNoneOnNotOrganizerAttendee() {
		Attendee organizer = ContactAttendee.builder().asAttendee().email("organizer@domain").build();
		new Event().withOrganizerIfNone(organizer);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithOrganizerIfNoneOnNullEmailOrganizer() {
		Attendee organizer = ContactAttendee.builder().asOrganizer().email(null).build();
		new Event().withOrganizerIfNone(organizer);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithOrganizerIfNoneOnEmptyEmailOrganizer() {
		Attendee organizer = ContactAttendee.builder().asOrganizer().email("").build();
		new Event().withOrganizerIfNone(organizer);
	}

	@Test
	public void testWithOrganizerIfNoneOnEventWithOrgnanizer() {
		Attendee organizer = ContactAttendee.builder().asOrganizer().email("organizer@domain").build();
		Attendee initialOrganizer = ContactAttendee.builder().asOrganizer().email("initialOrganizer@domain").build();
		Event event = new Event();
		event.addAttendee(initialOrganizer);

		assertThat(event.withOrganizerIfNone(organizer).findOrganizer()).isEqualTo(initialOrganizer);
	}

	@Test
	public void testWithOrganizerIfNoneOnEventWithAttendeeButNoOrgnanizer() {
		Attendee organizer = ContactAttendee.builder().asOrganizer().email("organizer@domain").build();
		Attendee attendee = ContactAttendee.builder().asAttendee().email("notorganizer@domain").build();
		Event event = new Event();
		event.addAttendee(attendee);

		assertThat(event.withOrganizerIfNone(organizer).findOrganizer()).isEqualTo(organizer);
	}

	@Test
	public void testWithOrganizerIfNoneOnEventWithAttendeeWithSameEmail() {
		Attendee organizer = ContactAttendee.builder().asOrganizer().email("will_be_organizer@domain").build();
		Attendee attendee = ContactAttendee.builder().asAttendee().email("will_be_organizer@domain").build();
		Event event = new Event();
		event.addAttendee(attendee);

		Attendee newEventOrganizer = event.withOrganizerIfNone(organizer).findOrganizer();

		assertThat(newEventOrganizer).isEqualTo(organizer).isEqualToComparingFieldByField(organizer);
	}
}
