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
package org.obm.push.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class DateUtilsTest {
	
	@Test
	public void getGeneseDate() {
		Calendar currentGMTCalendar = DateUtils.getEpochPlusOneSecondCalendar();
		assertThat(currentGMTCalendar.get(Calendar.YEAR)).isEqualTo(1970);
		assertThat(currentGMTCalendar.get(Calendar.MONTH)).isEqualTo(0);
		assertThat(currentGMTCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
		assertThat(currentGMTCalendar.get(Calendar.HOUR)).isEqualTo(0);
		assertThat(currentGMTCalendar.get(Calendar.MINUTE)).isEqualTo(0);
		assertThat(currentGMTCalendar.get(Calendar.SECOND)).isEqualTo(1);
	}
	
	@Test
	public void getMidnightCalendar() {
		Calendar currentGMTCalendar = DateUtils.getCurrentGMTCalendar();
		Calendar twoHoursAMCalendar = DateUtils.getMidnightCalendar();
		int year = currentGMTCalendar.get(Calendar.YEAR);
		assertThat(twoHoursAMCalendar.get(Calendar.YEAR)).isEqualTo(year);
		int month = currentGMTCalendar.get(Calendar.MONDAY);
		assertThat(twoHoursAMCalendar.get(Calendar.MONTH)).isEqualTo(month);
		int dayOfMonth = currentGMTCalendar.get(Calendar.DAY_OF_MONTH);
		assertThat(twoHoursAMCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(dayOfMonth);
		assertThat(twoHoursAMCalendar.get(Calendar.HOUR)).isEqualTo(0);
	}
	
	@Test
	public void testRealWeekWhenFirstDayNotInFirstWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 12, 7);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(1);
	}

	@Test
	public void testRealWeekWhenSecondDayIsInThirdWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 11);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(2);
	}

	@Test
	public void testRealWeekWhenFirstDayInFirstWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 1);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(1);
	}

	@Test
	public void testRealWeekWhenDayInSecondWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 8);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(2);
	}
	
	@Test
	public void testRealWeekWhenDayInLastWeek() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2011, 11, 30);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(5);
	}
	
	@Test
	public void testRealWeekWhenRegular() {
		Calendar calendar = DateUtils.getCurrentGMTCalendar();
		calendar.set(2012, 0, 18);
		int weekWithoutStartShift = DateUtils.getWeekOfCurrentDayWithoutStartShift(calendar);
		assertThat(weekWithoutStartShift).isEqualTo(3);
	}
	
	@Test
	public void testGetEpochCalendar() {
		Calendar calendar = DateUtils.getEpochCalendar();
		assertThat(calendar).isNotNull();
		assertThat(calendar.getTimeInMillis()).isEqualTo(0);
	}
	
	@Test
	public void testGetEpochCalendarWithNullParameter() {
		Calendar calendar = DateUtils.getEpochCalendar(null);
		assertThat(calendar).isNotNull();
		assertThat(calendar.getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT"));
		assertThat(calendar.getTimeInMillis()).isEqualTo(0);
	}
	
	@Test
	public void testGetEpochCalendarWithSpecifiedTimeZone() {
		TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
		Calendar calendar = DateUtils.getEpochCalendar(timeZone);
		assertThat(calendar).isNotNull();
		assertThat(calendar.getTimeZone()).isEqualTo(timeZone);
		assertThat(calendar.getTimeInMillis()).isEqualTo(0);
	}
	
	@Test
	public void testToTimestamp() {
		long currentTimeMillis = System.currentTimeMillis();
		Date date = new Date(currentTimeMillis);
		
		Timestamp timestamp = DateUtils.toTimestamp(date);
		assertThat(timestamp.getTime()).isEqualTo(currentTimeMillis);
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithNullId() {
		assertThat(DateUtils.isValidTimeZoneIdentifier(null)).isFalse();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithEmptyId() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("")).isFalse();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithWhitespaceOnlyId() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("   ")).isFalse();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithUnknownId() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("ThisIsNotAValidTimezoneIdentifier")).isFalse();
	}

	@Test
	public void testIsValidTimeZoneIdentifier() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("UTC")).isTrue();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithEtcGMT() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("Etc/GMT")).isTrue();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithEuropeParis() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("Europe/Paris")).isTrue();
	}
	
	@Test
	public void testIsValidTimeZoneIdentifierWithAmericaGuadeloupe() {
		assertThat(DateUtils.isValidTimeZoneIdentifier("America/Guadeloupe")).isTrue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDateWithEmptyString() {
		DateUtils.date("");
	}

	@Test
	public void testDateWithNull() {
		Date before = new Date();

		assertThat(DateUtils.date(null)).isAfterOrEqualsTo(before);
	}

	@Test
	public void testDate() {
		assertThat(DateUtils.date("2013-07-05T12:13:14")).isEqualTo(new GregorianCalendar(2013, 6, 5, 12, 13, 14).getTime());
	}

	@Test
	public void testDateWithZSuffix() {
		GregorianCalendar calendar = new GregorianCalendar(2013, 6, 5, 14, 13, 14);

		calendar.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		assertThat(DateUtils.date("2013-07-05T12:13:14Z")).isEqualTo(calendar.getTime());
	}

	@Test
	public void testDateWithNoSeconds() {
		assertThat(DateUtils.date("2013-07-05T12:13")).isEqualTo(new GregorianCalendar(2013, 6, 5, 12, 13, 0).getTime());
	}

	@Test
	public void testDateWithNoMinutes() {
		assertThat(DateUtils.date("2013-07-05T12")).isEqualTo(new GregorianCalendar(2013, 6, 5, 12, 0, 0).getTime());
	}

	@Test
	public void testDateWithNoTime() {
		assertThat(DateUtils.date("2013-07-05")).isEqualTo(new GregorianCalendar(2013, 6, 5, 0, 0, 0).getTime());
	}

	@Test
	public void testDateWithYearMonth() {
		assertThat(DateUtils.date("2013-07")).isEqualTo(new GregorianCalendar(2013, 6, 1, 0, 0, 0).getTime());
	}

	@Test
	public void testDateWithYear() {
		assertThat(DateUtils.date("2013")).isEqualTo(new GregorianCalendar(2013, 0, 1, 0, 0, 0).getTime());
	}

	@Test
	public void testDateWithTimeOffsetWhenEuropeParisTZ() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
		gregorianCalendar.set(2013, 6, 5, 10, 0, 0);
		gregorianCalendar.set(Calendar.MILLISECOND, 0);
		assertThat(DateUtils.date("2013-07-05T12:00:00+0400")).isEqualTo(gregorianCalendar.getTime());
	}

	@Test
	public void testDateWithNegativeTimeOffsetWhenEuropeParisTZ() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
		gregorianCalendar.set(2013, 6, 5, 18, 0, 0);
		gregorianCalendar.set(Calendar.MILLISECOND, 0);
		assertThat(DateUtils.date("2013-07-05T12:00:00-0400")).isEqualTo(gregorianCalendar.getTime());
	}

	@Test
	public void testDateWithTimeOffsetWhenUtcTZ() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		gregorianCalendar.set(2013, 6, 5, 8, 0, 0);
		gregorianCalendar.set(Calendar.MILLISECOND, 0);
		assertThat(DateUtils.date("2013-07-05T12:00:00+0400")).isEqualTo(gregorianCalendar.getTime());
	}

	@Test
	public void testDateWithNegativeTimeOffsetWhenUtcTZ() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		gregorianCalendar.set(2013, 6, 5, 16, 0, 0);
		gregorianCalendar.set(Calendar.MILLISECOND, 0);
		assertThat(DateUtils.date("2013-07-05T12:00:00-0400")).isEqualTo(gregorianCalendar.getTime());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDateUTCWithEmptyString() {
		DateUtils.dateUTC("");
	}

	@Test
	public void testDateUTCWithNull() {
		Date before = new Date();

		assertThat(DateUtils.dateUTC(null)).isAfterOrEqualsTo(before);
	}

	@Test
	public void testDateUTC() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		gregorianCalendar.set(2013, 6, 5, 12, 13, 14);
		gregorianCalendar.set(Calendar.MILLISECOND, 0);
		assertThat(DateUtils.dateUTC("2013-07-05T12:13:14Z")).isEqualTo(gregorianCalendar.getTime());
	}
}
