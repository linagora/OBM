package org.obm.push.utils;

import java.util.Calendar;

import junit.framework.Assert;

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
	
}
