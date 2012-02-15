package org.obm.push.bean;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.TimeZone;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.MSEventToObmEventConverter;
import org.obm.push.exception.IllegalMSEventRecurrenceException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.collect.Sets;

public class MSEventToObmEventConverterRecurrenceTest {

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

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyNeedDayOfMonthAndMonthOfYear() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeYearlyInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyIntervalIllegal() throws IllegalMSEventStateException {
		Integer yearlyIntervalShouldBeOne = 2;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(yearlyIntervalShouldBeOne);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();

		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeYearly() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.yearly);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test
	public void testConvertAttributeTypeYearlyUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeYearlyUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeYearlyOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(2);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer yearsNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addYearsToDate(msEventRecurrent.getStartTime(), yearsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeYearlyOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyUntilAndOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(2);
		recurrence.setUntil(date("2005-12-12T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyNDayNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyNDayIntervalIllegal() throws IllegalMSEventStateException {
		Integer yearlyIntervalShouldBeOne = 2;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(yearlyIntervalShouldBeOne);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();

		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeYearlyNDay() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.yearlybyday);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeYearlyNDayUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(2);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer yearsNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addYearsToDate(msEventRecurrent.getStartTime(), yearsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeYearlyNDayOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeYearlyNDayUntilAndOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.YEARLY_NDAY);
		recurrence.setDayOfMonth(1);
		recurrence.setMonthOfYear(1);
		recurrence.setInterval(1);
		recurrence.setOccurrences(2);
		recurrence.setUntil(date("2005-12-12T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeMonthlyInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(10);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyIntervalIllegal() throws IllegalMSEventStateException {
		Integer monthlyIntervalShouldLessThan = 100;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(monthlyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthly() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybydate);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyUntilAndOccurence() throws IllegalMSEventStateException {
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
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		recurrence.setOccurrences(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer monthsNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addMonthsToDate(msEventRecurrent.getStartTime(), monthsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeMonthlyOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyNDayNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(15);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyNDayIntervalIllegal() throws IllegalMSEventStateException {
		Integer monthlyIntervalShouldLessThan = 100;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(monthlyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDay() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybyday);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeMonthlyNDayUntilAndOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		recurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDayUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeMonthlyNDayUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		recurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer monthsNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addMonthsToDate(msEventRecurrent.getStartTime(), monthsNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setInterval(1);
		recurrence.setUntil(null);
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeMonthlyNDayOnNinthDayEachTwoMonth() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.MONTHLY_NDAY);
		recurrence.setUntil(null);
		recurrence.setOccurrences(null);
		recurrence.setInterval(2);
		recurrence.setDayOfMonth(9);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject").withRecurrence(recurrence).build();

		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEventRecurrent.getStartTime());
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.monthlybyday);
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getDays()).isEmpty();
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeWeeklyNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeWeeklyInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeWeeklyIntervalIllegal() throws IllegalMSEventStateException {
		Integer weeklyIntervalShouldLessThan = 100;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(weeklyIntervalShouldLessThan);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeWeeklyNeedDay() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(new HashSet<RecurrenceDayOfWeek>());
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeWeekly() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.weekly);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(daySetOf(RecurrenceDay.Friday));
	}

	@Test
	public void testConvertAttributeTypeWeeklyUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertAttributeTypeWeeklyUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeWeeklyOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setOccurrences(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer weeksNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addWeeksToDate(msEventRecurrent.getStartTime(), weeksNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeWeeklyOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeWeeklyUntilAndOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(1);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setOccurrences(3);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeWeeklyEachTwoMonday() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(2);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(daySetOf(RecurrenceDay.Monday));
	}

	@Test
	public void testConvertAttributeTypeWeeklyEachFourFridayAndSundayUntilOneYear() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.WEEKLY);
		recurrence.setInterval(4);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.FRIDAY, RecurrenceDayOfWeek.SUNDAY));
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(
				daySetOf(RecurrenceDay.Friday, RecurrenceDay.Sunday));
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeDailyNeedInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(Sets.newHashSet(RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}
	
	@Test
	public void testConvertAttributeTypeDailyInterval() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(Sets.newHashSet(RecurrenceDayOfWeek.FRIDAY));
		recurrence.setInterval(6);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
	}

	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeDailyIntervalIllegal() throws IllegalMSEventStateException {
		Integer dailyIntervalShouldLessThan = 1000;
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setInterval(dailyIntervalShouldLessThan);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	public void testConvertAttributeTypeDailyDefaultDays() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setInterval(6);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.daily);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(
				RecurrenceDay.Saturday, RecurrenceDay.Monday, RecurrenceDay.Tuesday, 
				RecurrenceDay.Wednesday, RecurrenceDay.Thursday, RecurrenceDay.Friday, RecurrenceDay.Sunday);
	}
	
	@Test
	public void testConvertAttributeTypeDaily() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.SATURDAY));
		recurrence.setInterval(5);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.isRecurrent()).isTrue();
		Assertions.assertThat(convertedRecurrence.getKind()).isEqualTo(RecurrenceKind.daily);
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(recurrence.getInterval());
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(daySetOf(RecurrenceDay.Saturday));
	}
	
	@Test
	public void testConvertAttributeTypeDailyUntil() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.SATURDAY));
		recurrence.setInterval(5);
		recurrence.setUntil(date("2005-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
	}
	
	@Test
	public void testConvertRecurrenceAttributeTypedDailyUntilNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.SATURDAY));
		recurrence.setInterval(5);
		recurrence.setUntil(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);
		
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}

	@Test
	public void testConvertAttributeTypeDailyOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		recurrence.setInterval(7);
		recurrence.setUntil(null);
		recurrence.setOccurrences(4);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		Integer daysNeededToContainsOccurrence = recurrence.getOccurrences()-1;
		Date untilDateExpected = addDaysToDate(msEventRecurrent.getStartTime(), daysNeededToContainsOccurrence);
		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(untilDateExpected);
	}

	@Test
	public void testConvertAttributeTypeDailyOccurenceNull() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		recurrence.setInterval(7);
		recurrence.setUntil(null);
		recurrence.setOccurrences(null);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
	}
	
	@Test(expected=IllegalMSEventRecurrenceException.class)
	public void testConvertAttributeTypeDailyUntilAndOccurence() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.MONDAY));
		recurrence.setInterval(7);
		recurrence.setUntil(date("2004-12-11T11:15:10Z"));
		recurrence.setOccurrences(3);
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		convertToOBMEvent(msEventRecurrent);
	}

	@Test
	public void testConvertAttributeTypeDailyEachWeekDay() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(
				RecurrenceDayOfWeek.MONDAY,
				RecurrenceDayOfWeek.TUESDAY,
				RecurrenceDayOfWeek.WEDNESDAY,
				RecurrenceDayOfWeek.THURSDAY,
				RecurrenceDayOfWeek.FRIDAY));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(1);
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(daySetOf(
				RecurrenceDay.Monday,
				RecurrenceDay.Tuesday,
				RecurrenceDay.Wednesday,
				RecurrenceDay.Thursday,
				RecurrenceDay.Friday));
	}

	@Test
	public void testConvertAttributeTypeDailyEachFiftyWednesdayForThreeYears() throws IllegalMSEventStateException {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setDayOfWeek(EnumSet.of(RecurrenceDayOfWeek.WEDNESDAY));
		recurrence.setInterval(50);
		recurrence.setUntil(date("2007-12-11T11:15:10Z"));
		MSEvent msEventRecurrent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(recurrence)
				.build();
		
		Event converted = convertToOBMEvent(msEventRecurrent);

		EventRecurrence convertedRecurrence = converted.getRecurrence();
		Assertions.assertThat(convertedRecurrence.getEnd()).isEqualTo(recurrence.getUntil());
		Assertions.assertThat(convertedRecurrence.getFrequence()).isEqualTo(50);
		Assertions.assertThat(convertedRecurrence.getDays()).containsOnly(daySetOf(RecurrenceDay.Wednesday));
	}

	private Calendar getInitializedCalendar(Date initTime) {
		Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		instance.setTime(initTime);
		return instance;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws IllegalMSEventStateException {
		return converter.convert(bs, null, msEvent, false);
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
	
	private Object[] daySetOf(RecurrenceDay... days) {
		return Sets.newHashSet(days).toArray();
	}
}
