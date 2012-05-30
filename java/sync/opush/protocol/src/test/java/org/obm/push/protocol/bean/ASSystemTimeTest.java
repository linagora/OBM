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
 * and its applicable Additional Terms for OBM aint with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.bean;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.protocol.bean.ASSystemTime.FromDateBuilder.JAVA_TO_MS_API_DAYOFWEEK_OFFSET;
import static org.obm.push.protocol.bean.ASSystemTime.FromDateBuilder.JAVA_TO_MS_API_MONTH_OFFSET;

import java.util.Calendar;
import java.util.Date;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.protocol.bean.ASSystemTime.FromDateBuilder;
import org.obm.push.utils.type.UnsignedShort;

public class ASSystemTimeTest {

	@RunWith(SlowFilterRunner.class)
	public static class TestBuilder {
		
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsYear() {
			new ASSystemTime.Builder()
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsMonth() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsDayOfWeek() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsDay() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsHour() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsMinute() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsSecond() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
	
		@Test(expected=NullPointerException.class)
		public void testThatBuilerNeedsMillis() {
			new ASSystemTime.Builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.build();
		}
		
		private UnsignedShort unsignedShort(int value) {
			return UnsignedShort.checkedCast(value);
		}
	}

	@RunWith(SlowFilterRunner.class)
	public static class TestFromDateBuilder {
			
		private FromDateBuilder systemTimeBuilder;
	
		@Before
		public void setUp() {
			systemTimeBuilder = new ASSystemTime.FromDateBuilder();
		}
		
		static enum SystemTimeSpecs {
			YEAR(1601, 30827),
			MONTH(1, 12),
			DAYOFWEEK(0, 6),
			WEEKOFMONTH(1, 5),
			HOUR(0, 23),
			MINUTE(0, 59),
			SECOND(0, 59),
			MILLISECOND(0, 999);
			
			final int min;
			final int max;
	
			private SystemTimeSpecs(int min, int max) {
				this.min = min;
				this.max = max;
			}
		}
		
		@Test
		public void testEncodeTimeZeroDate() {
			ASSystemTime sytemTime = systemTimeBuilder.date(new Date(0)).build();
			
			assertThat(sytemTime.getYear().getValue()).isEqualTo(1970);
			assertThat(sytemTime.getMonth().getValue()).isEqualTo(Calendar.JANUARY + JAVA_TO_MS_API_MONTH_OFFSET);
			assertThat(sytemTime.getDayOfWeek().getValue()).isEqualTo(Calendar.THURSDAY + JAVA_TO_MS_API_DAYOFWEEK_OFFSET);
			assertThat(sytemTime.getWeekOfMonth().getValue()).isEqualTo(1);
			assertThat(sytemTime.getHour().getValue()).isEqualTo(0);
			assertThat(sytemTime.getMinute().getValue()).isEqualTo(0);
			assertThat(sytemTime.getSecond().getValue()).isEqualTo(0);
			assertThat(sytemTime.getMilliseconds().getValue()).isEqualTo(0);
		}
	
		@Test
		public void testEncodeLastMillisOf2012Date() {
			ASSystemTime sytemTime = systemTimeBuilder.date(DateUtils.date("2012-12-31T23:59:59.999+00")).build();
			
			assertThat(sytemTime.getYear().getValue()).isEqualTo(2012);
			assertThat(sytemTime.getMonth().getValue()).isEqualTo(Calendar.DECEMBER + JAVA_TO_MS_API_MONTH_OFFSET);
			assertThat(sytemTime.getDayOfWeek().getValue()).isEqualTo(Calendar.MONDAY + JAVA_TO_MS_API_DAYOFWEEK_OFFSET);
			assertThat(sytemTime.getWeekOfMonth().getValue()).isEqualTo(5);
			assertThat(sytemTime.getHour().getValue()).isEqualTo(23);
			assertThat(sytemTime.getMinute().getValue()).isEqualTo(59);
			assertThat(sytemTime.getSecond().getValue()).isEqualTo(59);
			assertThat(sytemTime.getMilliseconds().getValue()).isEqualTo(999);
		}
	
		@Test
		public void testEncodeFirstMillisOf2015Date() {
			ASSystemTime sytemTime = systemTimeBuilder.date(DateUtils.date("2015-01-01T00:00:00.000+00")).build();
			
			assertThat(sytemTime.getYear().getValue()).isEqualTo(2015);
			assertThat(sytemTime.getMonth().getValue()).isEqualTo(Calendar.JANUARY + JAVA_TO_MS_API_MONTH_OFFSET);
			assertThat(sytemTime.getDayOfWeek().getValue()).isEqualTo(Calendar.THURSDAY + JAVA_TO_MS_API_DAYOFWEEK_OFFSET);
			assertThat(sytemTime.getWeekOfMonth().getValue()).isEqualTo(1);
			assertThat(sytemTime.getHour().getValue()).isEqualTo(0);
			assertThat(sytemTime.getMinute().getValue()).isEqualTo(0);
			assertThat(sytemTime.getSecond().getValue()).isEqualTo(0);
			assertThat(sytemTime.getMilliseconds().getValue()).isEqualTo(0);
		}
		
		@Test
		public void testMillisMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.MILLISECOND);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MILLISECOND.min));
		}
		
		@Test
		public void testMillisMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 999);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.MILLISECOND);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MILLISECOND.max));
		}
	
		@Test
		public void testSecondMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.SECOND);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MINUTE.min));
		}
		
		@Test
		public void testSecondMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 59);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.SECOND);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.SECOND.max));
		}
	
		@Test
		public void testMinuteMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MINUTE, 0);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.MINUTE);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MINUTE.min));
		}
		
		@Test
		public void testMinuteMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MINUTE, 59);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.MINUTE);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MINUTE.max));
		}
	
		@Test
		public void testHourMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.HOUR_OF_DAY);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.HOUR.min));
		}
		
		@Test
		public void testHourMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.HOUR_OF_DAY);
	
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.HOUR.max));
		}
	
		@Test
		public void testWeekOfMonthMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.WEEK_OF_MONTH, 1);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.WEEK_OF_MONTH);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.WEEKOFMONTH.min));
		}
		
		@Test
		public void testWeekOfMonthMax() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.date(DateUtils.date("2012-12-31T23:59:59.999+00"))
				.build();
				
			Assertions.assertThat(asSystemTime.getWeekOfMonth()).isEqualTo(unsignedShort(5));
		}
	
		@Test
		public void testDayOfWeekMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, 1);
			
			UnsignedShort fieldValue = systemTimeBuilder.dayOfWeekFieldAsBinary(calendar);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.DAYOFWEEK.min));
		}
		
		@Test
		public void testDayOfWeekMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, 7);
			
			UnsignedShort fieldValue = systemTimeBuilder.dayOfWeekFieldAsBinary(calendar);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.DAYOFWEEK.max));
		}
	
		@Test
		public void testMonthMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, 0);
			
			UnsignedShort fieldValue = systemTimeBuilder.monthFieldAsUnsignedShort(calendar);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MONTH.min));
		}
		
		@Test
		public void testMonthMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, 11);
			
			UnsignedShort fieldValue = systemTimeBuilder.monthFieldAsUnsignedShort(calendar);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.MONTH.max));
		}
	
		@Test
		public void testYearMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, SystemTimeSpecs.YEAR.min);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.YEAR);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.YEAR.min));
		}
		
		@Test
		public void testYearMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, SystemTimeSpecs.YEAR.max);
			
			UnsignedShort fieldValue = systemTimeBuilder.calendarFieldAsUnsignedShort(calendar, Calendar.YEAR);
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.YEAR.max));
		}

		@Test
		public void testOverridingYearWithNullKeepDateYear() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.date(DateUtils.date("2015-01-01T00:00:00.000+00"))
				.overridingYear(null)
				.build();
			
			Assertions.assertThat(asSystemTime.getYear()).isEqualTo(unsignedShort(2015));
		}
		
		@Test
		public void testOverridingYear() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.date(DateUtils.date("2015-01-01T00:00:00.000+00"))
				.overridingYear(unsignedShort(0))
				.build();
			
			Assertions.assertThat(asSystemTime.getYear()).isEqualTo(unsignedShort(0));
		}
		
		private UnsignedShort unsignedShort(int value) {
			return UnsignedShort.checkedCast(value);
		}
	}
}
