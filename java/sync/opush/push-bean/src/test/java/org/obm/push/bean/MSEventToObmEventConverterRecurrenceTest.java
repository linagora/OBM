package org.obm.push.bean;

import java.util.Calendar;
import java.util.Date;
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
import org.obm.sync.calendar.RecurrenceKind;

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
		Assertions.assertThat(convertedRecurrence.getDays()).isNull();
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
		Assertions.assertThat(convertedRecurrence.getDays()).isNull();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getReadableRepeatDays()).isEmpty();
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
		Assertions.assertThat(convertedRecurrence.getDays()).isNull();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getReadableRepeatDays()).isEmpty();
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
		Assertions.assertThat(convertedRecurrence.getDays()).isNull();
		Assertions.assertThat(convertedRecurrence.getEnd()).isNull();
		Assertions.assertThat(convertedRecurrence.getReadableRepeatDays()).isEmpty();
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
		Assertions.assertThat(convertedRecurrence.getDays()).isNull();
		Assertions.assertThat(convertedRecurrence.getReadableRepeatDays()).isEmpty();
	}

	private Calendar getInitializedCalendar(Date initTime) {
		Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		instance.setTime(initTime);
		return instance;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws IllegalMSEventStateException {
		return converter.convert(bs, null, msEvent, false);
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
