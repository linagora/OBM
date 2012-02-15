package org.obm.push.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.MSEventToObmEventConverter;
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
	public void testConvertAttributeAllDayFalse() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(true)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test(expected=NullPointerException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=NullPointerException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNull() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(new ArrayList<String>())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNull() {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withCategories(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() {
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
	public void testConvertAttributeDtStampJustCreated() {
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
	public void testConvertAttributeDtStampAlreadyCreated() {
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
	public void testConvertAttributeDtStampNull() {
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
	public void testConvertExceptionAttributeDeletedTrue() {
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

		Date[] exceptions = convertedEvent.getRecurrence().getExceptions();
		Assertions.assertThat(exceptions).hasSize(1);
		Assertions.assertThat(exceptions[0]).isEqualTo(msEventException.getExceptionStartTime());
	}

	@Test
	public void testConvertExceptionAttributeDeletedFalse() {
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
		
		Date[] exceptions = convertedEvent.getRecurrence().getExceptions();
		Assertions.assertThat(exceptions).isEmpty();
	}

	private MSRecurrence simpleRecurrence(RecurrenceType type) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		return recurrence;
	}
	
	private Event convertToOBMEvent(MSEvent msEvent) {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}

	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) {
		return converter.convert(bs, editingEvent, msEvent, false);
	}
	
	private Date date(String date) {
		return DateUtils.date(date);
	}
}
