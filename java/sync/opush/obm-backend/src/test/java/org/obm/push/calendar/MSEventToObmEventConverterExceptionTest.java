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
package org.obm.push.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventBuilder;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventExceptionBuilder;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class MSEventToObmEventConverterExceptionTest {

	private MSEventToObmEventConverterImpl converter;

	private User user;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverterImpl();
		String mailbox = "user@domain";
	    user = User.Factory.create()
				.createUser(mailbox, mailbox, null);
	}

	@Test
	public void testConvertAttributeSubjectWhenSpecified() throws ConversionException {
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
		assertThat(exception.getTitle()).isEqualTo(msEventException.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectEmptyGetFromParent() throws ConversionException {
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
		assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectNull() throws ConversionException {
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
		assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeStartTimeOnly() throws ConversionException {
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

	@Test(expected=ConversionException.class)
	public void testConvertAttributeStartTimeNullOnly() throws ConversionException {
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

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeOnly() throws ConversionException {
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

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeNullOnly() throws ConversionException {
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

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws ConversionException {
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
	public void testConvertAttributeStartAndEndTime() throws ConversionException {
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
		assertThat(exception.getStartDate()).isEqualTo(msEventException.getStartTime());
		assertThat(exception.getEndDate()).isEqualTo(msEventException.getEndTime());
	}

	public void testConvertAttributeDtStampJustCreated() throws ConversionException {
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
		assertThat(exception.getTimeCreate()).isEqualTo(msEvent.getDtStamp());
		assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	public void testConvertAttributeDtStampAlreadyCreated() throws ConversionException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);

		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10"))
				.withEndTime(date("2004-12-12T11:15:10"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10"))
				.withDtStamp(date("2004-12-10T11:15:10"))
				.build();
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10"))
				.withEndTime(date("2004-12-12T11:15:10"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10"))
				.withRecurrence(simpleDailyRecurrence())
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);
		
		Event exception = exceptionOf(converted);
		assertThat(exception.getTimeCreate()).isEqualTo(previousDtStampDate);
		assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampCreatedByException() throws ConversionException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(null);
		editingEvent.setTimeUpdate(previousDtStampDate);

		Date dateException = date("2004-12-10T11:15:10");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10"))
				.withEndTime(date("2004-12-12T11:15:10"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10"))
				.withDtStamp(dateException)
				.build();
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10"))
				.withEndTime(date("2004-12-12T11:15:10"))
				.withSubject("Any Subject")
				.withDtStamp(null)
				.withRecurrence(simpleDailyRecurrence())
				.withExceptions(Lists.newArrayList(msEventException))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);
		
		Event exception = exceptionOf(converted);
		assertThat(exception.getTimeCreate()).isEqualTo(dateException);
		assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}
	
	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws ConversionException {
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
		assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws ConversionException {
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
		assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws ConversionException {
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
		assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws ConversionException {
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
		assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws ConversionException {
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
		assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsRequired() throws ConversionException {
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

	@Test(expected=ConversionException.class)
	public void testConvertAttributeMeetingStatusDontGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setMeetingStatus(null);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDeletedTrue() throws ConversionException {
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
				.withRecurrence(simpleDailyRecurrence())
				.withExceptions(Lists.newArrayList(msEventException))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.withTimeZone(DateTimeZone.UTC.toTimeZone())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		Set<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		assertThat(exceptions).hasSize(1);
		assertThat(exceptions).containsOnly(date("2004-10-11T00:00:00Z"));
		assertThat(eventExceptions).isEmpty();
	}

	@Test
	public void testConvertAttributeDeletedFalse() throws ConversionException {
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
		Set<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		assertThat(exceptions).isEmpty();
		assertThat(eventExceptions).hasSize(1);
		assertThat(Iterables.getOnlyElement(eventExceptions).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws ConversionException {
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
		assertThat(eventException.getStartDate()).isEqualTo(msEventException.getStartTime());
		assertThat(eventException.getEndDate()).isEqualTo(msEventException.getEndTime());
		assertThat(eventException.getDuration()).isEqualTo(getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws ConversionException {
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
		assertThat(eventException.getStartDate()).isEqualTo(msEventException.getStartTime());
		assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws ConversionException {
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
		assertThat(eventException.getStartDate()).isEqualTo(msEventException.getStartTime());
		assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testConvertAttributeAllDayFalse() throws ConversionException {
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
		assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws ConversionException {
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
		assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws ConversionException {
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
		assertThat(eventException.isAllday()).isFalse();
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws ConversionException {
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
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws ConversionException {
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
	public void testConvertAttributeAllDayNullGetFromParent() throws ConversionException {
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
		assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNullAndParentNullToo() throws ConversionException {
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
		assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNullGetFromParent() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}


	@Test
	public void testConvertAttributeBusyStatusNullAndParent() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusNullAndParentNullToo() throws ConversionException {
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
		assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws ConversionException {
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
		assertThat(exception.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws ConversionException {
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
		assertThat(exception.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNullGetFromParent() throws ConversionException {
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
		assertThat(exception.getCategory()).isEqualTo("category1");
	}
	
	@Test
	public void testConvertAttributeCategoryNullAndParentNullToo() throws ConversionException {
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
		assertThat(exception.getCategory()).isNull();
	}
	
	@Test
	public void testConvertAttributeCategoryBeyondThreeHundred() throws ConversionException {
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
		
		Event event = convertToOBMEvent(msEvent);
		assertThat(event.getCategory()).isNull();
	}
	
	@Test
	public void testConvertAttributeReminder() throws ConversionException {
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
		assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws ConversionException {
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
		assertThat(exception.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws ConversionException {
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
		assertThat(exception.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeReminderNullGetFromParent() throws ConversionException {
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
		assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderNotNullDontGetFromParent() throws ConversionException {
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
		assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}
	
	@Test
	public void testConvertAttributeSensitivityNormalKeepParentValue() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(
				MSEventToObmEventConverterImpl.SENSITIVITY_TO_PRIVACY.get(msEvent.getSensitivity()));
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidentialKeepParentValue() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.CONFIDENTIAL);
	}

	@Test
	public void testConvertAttributeSensitivityPersonalKeepParentValue() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(
				MSEventToObmEventConverterImpl.SENSITIVITY_TO_PRIVACY.get(msEvent.getSensitivity()));
	}

	@Test
	public void testConvertAttributeSensitivityPrivateKeepParentValue() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(
				MSEventToObmEventConverterImpl.SENSITIVITY_TO_PRIVACY.get(msEvent.getSensitivity()));
	}

	@Test
	public void testConvertAttributeSensitivityNullAndParentNullTooIsPublic() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}

	@Test
	public void testConvertAttributeSensitivityNullGetFromParent() throws ConversionException {
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
		assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.CONFIDENTIAL);
	}

	@Test
	public void testConvertAttributeLocation() throws ConversionException {
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
		assertThat(exception.getLocation()).isEqualTo(msEventException.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNullAndParentNullToo() throws ConversionException {
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
		assertThat(exception.getLocation()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocationNullGetFromParent() throws ConversionException {
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
		assertThat(exception.getLocation()).isEqualTo(msEvent.getLocation());
	}

	@Test
	public void testConvertWhenExistDeletedAndRegularExceptions() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEventException msEventExceptionDeleted = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-12T12:15:10Z"))
				.withSubject("Any Subject")
				.withDeleted(true)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventExceptionDeleted);

		Event converted = convertToOBMEvent(msEvent);
		Set<Event> convertedExceptions = converted.getRecurrence().getEventExceptions();
		Set<Date> convertedExceptionsDeleted = converted.getRecurrence().getExceptions();

		assertThat(convertedExceptions).hasSize(1);
		assertThat(Iterables.getOnlyElement(convertedExceptions).getRecurrenceId())
			.isEqualTo(msEventException.getExceptionStartTime());
		assertThat(convertedExceptionsDeleted).hasSize(1);
		assertThat(convertedExceptionsDeleted).containsOnly(date("2004-10-12T00:00:00Z"));
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertWhenExistDeletedAndRegularExceptionsAtSameDate() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEventException msEventExceptionDeleted = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(true)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventExceptionDeleted);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertWhenTwoEventExceptionWithDifferentSubjectAtSameDate() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEventException msEventException2 = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Another Subject")
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventException2);

		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertWhenTwoEventExceptionWithDifferentDtStampAtSameDate() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Subject")
				.withDeleted(false)
				.build();
		
		MSEventException msEventException2 = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-11-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Subject")
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventException2);

		convertToOBMEvent(msEvent);
	}

	
	@Test(expected=ConversionException.class)
	public void testConvertWhenExistRegularAndDeletedExceptionsAtSameDate() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		MSEventException msEventExceptionDeleted = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(true)
				.build();

		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventExceptionDeleted, msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertWhenExistRegularAndDeletedExceptionsAtSameDateReverseOrder() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		MSEventException msEventExceptionDeleted = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(true)
				.build();

		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventExceptionDeleted);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertWhenDuplicateDeletedExceptions() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		
		MSEventException msEventExceptionDeleted = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(true)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventExceptionDeleted, msEventExceptionDeleted);
		
		Event obmEvent = convertToOBMEvent(msEvent);
		
		assertThat(obmEvent.getRecurrence().getExceptions()).hasSize(1).containsOnly(date("2004-10-11T00:00:00Z"));
	}

	@Test
	public void testConvertWhenDuplicateRegularExceptions() throws ConversionException {
		Date exceptionRecurrenceId = date("2004-10-11T12:15:10Z");
		
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(exceptionRecurrenceId)
				.withSubject("Any Subject")
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException, msEventException);

		Event obmEvent = convertToOBMEvent(msEvent);
		
		Event expectedException = converter.convertEventException(user, null, obmEvent, msEventException, false);
		assertThat(obmEvent.getRecurrence().getEventExceptions()).hasSize(1).containsOnly(expectedException);
	}
	
	private MSEvent makeMSEventWithException(MSEventException... exceptions) {
		MSEvent msEvent = new MSEventBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleDailyRecurrence())
				.withExceptions(Lists.newArrayList(exceptions))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.withTimeZone(DateTimeZone.UTC.toTimeZone())
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.build();

		return msEvent;
	}
	
	private MSRecurrence simpleDailyRecurrence() {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(RecurrenceType.DAILY);
		return recurrence;
	}

	private Event exceptionOf(Event convertedEvent) {
		return Iterables.getOnlyElement(convertedEvent.getRecurrence().getEventExceptions());
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws ConversionException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}
	
	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws ConversionException {
		return converter.convert(user, editingEvent, msEvent, false);
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
		return DateUtils.minutesToSeconds(minutes);
	}
}
