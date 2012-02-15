package org.obm.push.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.MSEventToObmEventConverter;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;

import com.google.common.collect.Lists;

public class MSEventToObmEventConverterTest {

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
	public void testConvertAttributeAllDayFalse() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(true)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test(expected=NullPointerException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=NullPointerException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(new ArrayList<String>())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() throws IllegalMSEventStateException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(Arrays.asList(tooMuchCategories))
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDtStampJustCreated() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(msEvent.getDtStamp());
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws IllegalMSEventStateException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(previousDtStampDate);
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withDtStamp(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isNull();
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isNull();
	}

	@Test
	public void testConvertAttributeDescription() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withDescription("any description")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isEqualTo(msEvent.getDescription());
	}
	
	@Test
	public void testConvertAttributeDescriptionNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withDescription(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isNull();
	}

	@Test
	public void testConvertAttributeTimezoneDefault() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withTimeZone(TimeZone.getDefault())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isEqualTo(TimeZone.getDefault().getID());
	}

	@Test
	public void testConvertAttributeTimezoneSpecific() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withTimeZone(TimeZone.getTimeZone("America/Tijuana"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isEqualTo("America/Tijuana");
	}
	
	@Test
	public void testConvertAttributeTimezoneNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withTimeZone(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isNull();
	}
	
	@Test
	public void testConvertExceptionAttributeDeletedTrue() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventException();
		msEventException.setStartTime(date("2004-12-11T11:15:10Z"));
		msEventException.setEndTime(date("2004-12-12T11:15:10Z"));
		msEventException.setExceptionStartTime(date("2004-10-11T11:15:10Z"));
		msEventException.setDeleted(true);

		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
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
	public void testConvertExceptionAttributeDeletedFalse() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventException();
		msEventException.setStartTime(date("2004-12-11T11:15:10Z"));
		msEventException.setEndTime(date("2004-12-12T11:15:10Z"));
		msEventException.setExceptionStartTime(date("2004-10-11T11:15:10Z"));
		msEventException.setDeleted(false);
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).isEmpty();
		Assertions.assertThat(eventExceptions).hasSize(1);
		Assertions.assertThat(eventExceptions.get(0).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	private MSRecurrence simpleRecurrence(RecurrenceType type) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		return recurrence;
	}
	
	private Event convertToOBMEvent(MSEvent msEvent) throws IllegalMSEventStateException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}

	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws IllegalMSEventStateException {
		return converter.convert(bs, editingEvent, msEvent, false);
	}
	
	private Date date(String date) {
		return DateUtils.date(date);
	}
}
