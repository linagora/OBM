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

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.TimeZone;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventBuilder;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.collect.Sets;


public class MSEventToObmEventConverterRecurrenceTest {

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
	public void testConvertDeletedExcetionWhenMightAndGMT() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("GMT");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T00:00:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-10T00:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyAndGMT() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("GMT");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T23:50:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-10T00:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenMightAndTurkeyWhenWinterTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("Turkey");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T00:00:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-09T22:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyAndTurkeyWhenWinterTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("Turkey");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T23:50:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-10T22:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenMightAndTurkeyWhenSummerTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("Turkey");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-05-10T00:00:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-05-09T21:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyAndTurkeyWhenSummerTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("Turkey");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-05-10T23:50:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-05-10T21:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenMightAndLosAngelesWhenWinterTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("America/Los_Angeles");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T00:00:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-09T08:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyAndLosAngelesWhenWinterTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("America/Los_Angeles");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-01-10T23:50:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-01-10T08:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenMightAndLosAngelesWhenSummerTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("America/Los_Angeles");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-05-10T00:00:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-05-09T07:00:00+00"));
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyAndLosAngelesWhenSummerTz() throws ConversionException {
		DateTimeZone timeZone = DateTimeZone.forID("America/Los_Angeles");
		EventRecurrence recurrence = new EventRecurrence();
		MSEventException msEventException = new MSEventException();
		msEventException.setExceptionStartTime(date("2004-05-10T23:50:00+00"));
		
		converter.addDeletedExceptionToRecurrence(recurrence, msEventException, timeZone);

		assertThat(recurrence.getExceptions()).containsOnly(date("2004-05-10T07:00:00+00"));
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyNeedDayOfMonthAndMonthOfYear() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeYearlyInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyIntervalIllegal() throws ConversionException {
		Integer yearlyIntervalShouldBeOne = 2;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(yearlyIntervalShouldBeOne);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();

		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeYearly() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.yearly);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test
	public void testConvertAttributeTypeYearlyUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeYearlyUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeYearlyOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(2);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer yearsNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addYearsToDate(msEventRecurrent.getStartTime(), yearsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeYearlyOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyUntilAndOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(2);
		msRecurrence.setUntil(date("2005-12-12T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyNDayNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyNDayIntervalIllegal() throws ConversionException {
		Integer yearlyIntervalShouldBeOne = 2;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(yearlyIntervalShouldBeOne);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();

		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeYearlyNDay() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.yearlybyday);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeYearlyNDayUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(2);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer yearsNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addYearsToDate(msEventRecurrent.getStartTime(), yearsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeYearlyNDayUntilAndOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.YEARLY_NDAY);
		msRecurrence.setDayOfMonth(1);
		msRecurrence.setMonthOfYear(1);
		msRecurrence.setInterval(1);
		msRecurrence.setOccurrences(2);
		msRecurrence.setUntil(date("2005-12-12T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeMonthlyNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeMonthlyInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(10);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeMonthlyIntervalIllegal() throws ConversionException {
		Integer monthlyIntervalShouldLessThan = 100;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(monthlyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthly() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybydate);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeMonthlyUntilAndOccurence() throws ConversionException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		recurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer monthsNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addMonthsToDate(msEventRecurrent.getStartTime(), monthsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeMonthlyOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyDayNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence eventRecurrence = converted.getRecurrence();
		assertThat(eventRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybyday);
		assertThat(eventRecurrence.getFrequence()).isEqualTo(1);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDayInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(15);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeMonthlyNDayIntervalIllegal() throws ConversionException {
		Integer monthlyIntervalShouldLessThan = 100;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(monthlyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDay() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybyday);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeMonthlyNDayUntilAndOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		msRecurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDayUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDayUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer monthsNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addMonthsToDate(msEventRecurrent.getStartTime(), monthsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setInterval(1);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOnNinthDayEachTwoMonth() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.MONTHLY_NDAY);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(null);
		msRecurrence.setInterval(2);
		msRecurrence.setDayOfMonth(9);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.withRecurrence(msRecurrence).build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEventRecurrent.getStartTime());
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybyday);
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeWeeklyNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeWeeklyInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeWeeklyIntervalIllegal() throws ConversionException {
		Integer weeklyIntervalShouldLessThan = 100;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(weeklyIntervalShouldLessThan);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeWeeklyNeedDay() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(new HashSet<RecurrenceDayOfWeek>());
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeWeekly() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.weekly);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(RecurrenceDay.Friday);
	}

	@Test
	public void testConvertAttributeTypeWeeklyUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeWeeklyUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeWeeklyOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setOccurrences(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer weeksNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addWeeksToDate(msEventRecurrent.getStartTime(), weeksNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeWeeklyOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeWeeklyUntilAndOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(1);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		msRecurrence.setOccurrences(3);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeWeeklyEachTwoMonday() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(2);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(RecurrenceDay.Monday);
	}

	@Test
	public void testConvertAttributeTypeWeeklyEachFourFridayAndSundayUntilOneYear() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.WEEKLY);
		msRecurrence.setInterval(4);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY, RecurrenceDayOfWeek.SUNDAY));
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(RecurrenceDay.Friday, RecurrenceDay.Sunday);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeDailyNeedInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setDayOfWeek(Sets.newHashSet(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeDailyInterval() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(6);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeDailyIntervalIllegal() throws ConversionException {
		Integer dailyIntervalShouldLessThan = 1000;
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(dailyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	public void testConvertAttributeTypeDailyDefaultDays() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(6);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.daily);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(
				RecurrenceDay.Saturday, RecurrenceDay.Monday, RecurrenceDay.Tuesday, 
				RecurrenceDay.Wednesday, RecurrenceDay.Thursday, RecurrenceDay.Friday, RecurrenceDay.Sunday);
	}
	
	@Test
	public void testConvertAttributeTypeDaily() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.daily);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(msRecurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test
	public void testConvertAttributeTypeDailyUntil() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(5);
		msRecurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(msRecurrence.getUntil());
	}
	
	@Test
	public void testConvertRecurrenceAttributeTypedDailyUntilNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(5);
		msRecurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeDailyOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(7);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(4);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer daysNeededToContainsOccurrence = msRecurrence.getOccurrences()-1;
		Date untilDateExpected = addDaysToDate(msEventRecurrent.getStartTime(), daysNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeDailyOccurenceNull() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(7);
		msRecurrence.setUntil(null);
		msRecurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeDailyUntilAndOccurence() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(7);
		msRecurrence.setUntil(date("2004-12-11T11:15:10Z"));
		msRecurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeTypeDailyAndRecurrenceDayOfWeek() throws ConversionException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setInterval(1);
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(msRecurrence)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	private Calendar getInitializedCalendar(Date initTime) {
		Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		instance.setTime(initTime);
		return instance;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws ConversionException {
		return converter.convert(user, null, msEvent, false);
	}

	private Date addDaysToDate(Date startTime, Integer days) {
		Calendar cal = getInitializedCalendar(startTime);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}
	
	private Date addWeeksToDate(Date startTime, Integer weeks) {
		Calendar cal = getInitializedCalendar(startTime);
		cal.add(Calendar.WEEK_OF_YEAR, weeks);
		return cal.getTime();
	}
	
	private Date addMonthsToDate(Date startTime, Integer months) {
		Calendar cal = getInitializedCalendar(startTime);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}
	
	private Date addYearsToDate(Date startTime, Integer years) {
		Calendar cal = getInitializedCalendar(startTime);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}
	
	private Date date(String date) {
		return DateUtils.date(date);
	}
}
