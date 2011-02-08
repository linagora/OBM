package org.obm.sync.calendar;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;


public class EventTest {

	@Test
	public void testIsEventInThePast1970() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 0, 0);
		event.setDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneDayLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		event.setDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneHourLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1);
		event.setDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneSecondLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -1);
		event.setDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneMinuteMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		event.setDate(calendar.getTime());
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneYearMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		event.setDate(calendar.getTime());
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastStartInThePastEndInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -10);
		event.setDate(calendar.getTime());
		event.setDuration(3600);
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneOccurrenceInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		event.setDate(calendar.getTime());
		event.setDuration(3600);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		calendar.add(Calendar.MONTH, 1);
		recurrence.setEnd(calendar.getTime());
		event.setRecurrence(recurrence);
		Assert.assertFalse(event.isEventInThePast());
	}
}
