package org.obm.push.utils;

import java.util.Calendar;

import junit.framework.Assert;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class DateUtilsTest {

	@Test
	public void getGeneseDate() {
		Calendar currentGMTCalendar = DateUtils.getEpochPlusOneSecondCalendar();
		Assert.assertEquals(1970, currentGMTCalendar.get(Calendar.YEAR));
		Assert.assertEquals(0, currentGMTCalendar.get(Calendar.MONTH));
		Assert.assertEquals(1, currentGMTCalendar.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(0, currentGMTCalendar.get(Calendar.HOUR));
		Assert.assertEquals(0, currentGMTCalendar.get(Calendar.MINUTE));
		Assert.assertEquals(1, currentGMTCalendar.get(Calendar.SECOND));
	}
	
	@Test
	public void getMidnightCalendar() {
		Calendar currentGMTCalendar = DateUtils.getCurrentGMTCalendar();
		Calendar twoHoursAMCalendar = DateUtils.getMidnightCalendar();
		Assert.assertEquals(currentGMTCalendar.get(Calendar.YEAR), twoHoursAMCalendar.get(Calendar.YEAR));
		Assert.assertEquals(currentGMTCalendar.get(Calendar.MONTH), twoHoursAMCalendar.get(Calendar.MONTH));
		Assert.assertEquals(currentGMTCalendar.get(Calendar.DAY_OF_MONTH), twoHoursAMCalendar.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(0, twoHoursAMCalendar.get(Calendar.HOUR));
	}
	
	@Test
	public void testRealWeekWhenFirstDayNotInFirstWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 12, 7);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(1);
	}

	@Test
	public void testRealWeekWhenSecondDayIsInThirdWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 11);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(2);
	}

	@Test
	public void testRealWeekWhenFirstDayInFirstWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 1);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(1);
	}

	@Test
	public void testRealWeekWhenDayInSecondWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 8);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(2);
	}
	
	@Test
	public void testRealWeekWhenDayInLastWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 30);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(5);
	}
	
	@Test
	public void testRealWeekWhenRegular() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2012, 0, 18);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		Assertions.assertThat(weekWithoutStartShift).isEqualTo(3);
	}
}
