package org.obm.push.bean;


import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.MSEventToObmEventConverter;
import org.obm.push.exception.IllegalMSEventExceptionStateException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MSEventToObmEventConverterExceptionTest {

	private MSEventToObmEventConverter converter;

	private BackendSession bs;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverter();
		String mailbox = "user@domain";
		String password = "password";
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}

	@Test
	public void testConvertAttributeSubjectWhenSpecified() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("The subject of exception")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEventException.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectEmptyGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");

		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectNull() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeOnly() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeNullOnly() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeOnly() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullOnly() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeStartAndEndTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);

		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getStartDate()).isEqualTo(msEventException.getStartTime());
		Assertions.assertThat(exception.getEndDate()).isEqualTo(msEventException.getEndTime());
	}

	@Test
	public void testConvertAttributeDtStampJustCreated() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTimeCreate()).isEqualTo(msEventException.getDtStamp());
		Assertions.assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws IllegalMSEventStateException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);

		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTimeCreate()).isEqualTo(previousDtStampDate);
		Assertions.assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeDtStampInExceptionIsRequired() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeDtStampInExceptionDontGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setDtStamp(date("2004-12-10T12:15:10Z"));
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeMeetingStatusIsRequired() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeMeetingStatusDontGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDeletedTrue() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDeleted(true)
				.build();

		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).hasSize(1);
		Assertions.assertThat(exceptions).containsOnly(msEventException.getExceptionStartTime());
		Assertions.assertThat(eventExceptions).isEmpty();
	}

	@Test
	public void testConvertAttributeDeletedFalse() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).isEmpty();
		Assertions.assertThat(eventExceptions).hasSize(1);
		Assertions.assertThat(eventExceptions.get(0).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(msEventException.getStartTime());
		Assertions.assertThat(eventException.getEndDate()).isEqualTo(msEventException.getEndTime());
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testConvertAttributeAllDayFalse() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeAllDayNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(true);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNullAndParentNullToo() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(null);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayMakeEventStartMidnightAndFinishAtMidnight() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(null);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Date lateMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayLate(msEventException.getStartTime());
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getEndDate()).isEqualTo(lateMidnight);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(CalendarBusyStatus.FREE);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}


	@Test
	public void testConvertAttributeBusyStatusNullAndParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(CalendarBusyStatus.FREE);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusNullAndParentNullToo() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Collections.<String>emptyList())
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setCategories(Lists.newArrayList("category1", "category2"));
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isEqualTo("category1");
	}
	
	@Test
	public void testConvertAttributeCategoryNullAndParentNullToo() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setCategories(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isNull();
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() throws IllegalMSEventStateException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Arrays.asList(tooMuchCategories))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeReminder() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(150)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEventException.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(0)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeReminderNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(180);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEvent.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderNotNullDontGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(100)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEventException.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}
	
	@Test
	public void testConvertAttributeSensitivityNormal() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.NORMAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidential() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPersonal() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.PERSONAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPrivate() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.PRIVATE)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityNullAndParentNullTooIsPublic() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSensitivity(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}

	@Test
	public void testConvertAttributeSensitivityNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSensitivity(CalendarSensitivity.CONFIDENTIAL);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeLocation() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation("Any location")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isEqualTo(msEventException.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNullAndParentNullToo() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setLocation(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocationNullGetFromParent() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setLocation("Any location");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isEqualTo(msEvent.getLocation());
	}

	private MSEvent makeMSEventWithException(MSEventException exception) {
		MSEvent msEvent = new MSEventBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(exception))
				.build();

		return msEvent;
	}
	
	private MSRecurrence simpleRecurrence(RecurrenceType type) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		return recurrence;
	}

	private Event exceptionOf(Event convertedEvent) {
		return Iterables.getOnlyElement(convertedEvent.getRecurrence().getEventExceptions());
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws IllegalMSEventStateException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}
	
	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws IllegalMSEventStateException {
		return converter.convert(bs, editingEvent, msEvent, false);
	}
	
	private Date date(String date) {
		return org.obm.DateUtils.date(date);
	}

	private int getOneDayInSecond() {
		return (int) DateUtils.daysToSeconds(1);
	}

	private int getOneYearInSecond() {
		return (int) DateUtils.yearsToSeconds(1);
	}

	private int minuteToSecond(int minutes) {
		return (int) DateUtils.minutesToSeconds(minutes);
	}
}
