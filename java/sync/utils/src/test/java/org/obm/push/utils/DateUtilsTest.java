/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.utils;

import java.util.Calendar;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
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
