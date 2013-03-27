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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.Duration;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

public class DateUtils {

	public static TimeZone getGMTTimeZone() {
		return TimeZone.getTimeZone("GMT");
	}
	
	
	public static Calendar getCurrentGMTCalendar() {
		return Calendar.getInstance(getGMTTimeZone());
	}

	public static Calendar getEpochCalendar() {
		Calendar calendar = getCurrentGMTCalendar();
		calendar.setTimeInMillis(0);
		return calendar;
	}
	
	public static Calendar getEpochCalendar(TimeZone timeZone) {
		if (timeZone == null) {
			return getEpochCalendar();
		}
		
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(0);
		return calendar;
	}
	
	public static Calendar getEpochPlusOneSecondCalendar() {
		Calendar calendar = getEpochCalendar();
		//We don't use zero timestamp to avoid broken handling of MYSQL
		calendar.set(Calendar.SECOND, 1);
		return calendar;
	}
	
	public static Date getCurrentDate() {
		return getCurrentGMTCalendar().getTime();
	}
	
	public static Calendar getMidnightCalendar() {
		Calendar calendar = getCurrentGMTCalendar();
		setTimeToZero(calendar);
		return calendar;
	}
	
	public static Date getOneDayLater(Date date) {
		Calendar cal = getOneDayLaterCalendar(date);
		return cal.getTime();
	}

	private static Calendar getOneDayLaterCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, 1);
		return cal;
	}

	private static void setTimeToZero(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	public static int getWeekOfCurrentDayWithoutStartShift(Calendar cal) {
		int eventStartDay = cal.get(Calendar.DAY_OF_MONTH);
		int eventWeekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
		int eventRealWeekOfMonth = getRealWeekOfDay(eventStartDay);
		return adjustWeekOfMonth(eventWeekOfMonth, eventRealWeekOfMonth);
	}

	private static int adjustWeekOfMonth(int eventWeekOfMonth, int eventRealWeekOfMonth) {
		if (eventWeekOfMonth > 1 && eventWeekOfMonth > eventRealWeekOfMonth) {
			return eventWeekOfMonth - 1;
		}
		return eventWeekOfMonth;
	}
	
	private static int getRealWeekOfDay(int startDay) {
		return (int) Math.ceil(startDay / 7d);
	}

	public static int minutesToSeconds(long minutes) {
		return Ints.checkedCast( 
				Duration.standardMinutes(minutes).getStandardSeconds() );
	}

	public static long daysToSeconds(long days) {
		return Duration.standardDays(days).getStandardSeconds();
	}

	public static long yearsToSeconds(long years) {
		return daysToSeconds(years * 365);
	}
	
	public static Timestamp toTimestamp(Date date) {
		return new Timestamp(date.getTime()) ;
	}

	public static boolean isValidTimeZoneIdentifier(String tzId) {
		return Iterables.contains(Arrays.asList(TimeZone.getAvailableIDs()), tzId);
	}
}
