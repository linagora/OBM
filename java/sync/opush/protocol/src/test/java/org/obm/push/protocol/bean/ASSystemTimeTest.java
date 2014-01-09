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
 * and its applicable Additional Terms for OBM aint with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.bean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.protocol.bean.ASSystemTime.FromDateBuilder;
import org.obm.push.utils.type.UnsignedShort;

public class ASSystemTimeTest {

	public static class TestBuilder {
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsYear() {
			ASSystemTime.builder()
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsMonth() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsDayOfWeek() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsDay() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsHour() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsMinute() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsSecond() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
	
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerNeedsMillis() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerMonthRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(40))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerDayOfWeekRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(8))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerWeekOfMonthRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(6))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerHourRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(25))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerMinuteRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(61))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerSecondRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(61))
				.milliseconds(unsignedShort(0))
				.build();
		}
		
		@Test(expected=IllegalStateException.class)
		public void testThatBuilerMillisecondsRange() {
			ASSystemTime.builder()
				.year(unsignedShort(0))
				.month(unsignedShort(0))
				.dayOfWeek(unsignedShort(0))
				.weekOfMonth(unsignedShort(0))
				.hour(unsignedShort(0))
				.minute(unsignedShort(0))
				.second(unsignedShort(0))
				.milliseconds(unsignedShort(9999))
				.build();
		}
		
		private UnsignedShort unsignedShort(int value) {
			return UnsignedShort.checkedCast(value);
		}
	}

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
			ASSystemTime asSystemTime = systemTimeBuilder.dateTime(new DateTime(0, DateTimeZone.UTC)).build();
			
			assertThat(asSystemTime.getYear().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getMonth().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getDayOfWeek().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getWeekOfMonth().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getHour().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getMinute().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getSecond().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getMilliseconds().getValue()).isEqualTo(0);
		}
	
		@Test
		public void testEncodeLastMillisOf2012Date() {
			ASSystemTime asSystemTime = systemTimeBuilder.dateTime(new DateTime(DateTime.parse("2012-12-31T23:59:59.999+00"), DateTimeZone.UTC)).build();
			
			assertThat(asSystemTime.getYear().getValue()).isEqualTo(2012);
			assertThat(asSystemTime.getMonth().getValue()).isEqualTo(12);
			assertThat(asSystemTime.getDayOfWeek().getValue()).isEqualTo(1);
			assertThat(asSystemTime.getWeekOfMonth().getValue()).isEqualTo(5);
			assertThat(asSystemTime.getHour().getValue()).isEqualTo(23);
			assertThat(asSystemTime.getMinute().getValue()).isEqualTo(59);
			assertThat(asSystemTime.getSecond().getValue()).isEqualTo(59);
			assertThat(asSystemTime.getMilliseconds().getValue()).isEqualTo(999);
		}
	
		@Test
		public void testEncodeFirstMillisOf2015Date() {
			ASSystemTime asSystemTime = systemTimeBuilder.dateTime(new DateTime(DateTime.parse("2015-01-01T00:00:00.000+00"), DateTimeZone.UTC)).build();
			
			assertThat(asSystemTime.getYear().getValue()).isEqualTo(2015);
			assertThat(asSystemTime.getMonth().getValue()).isEqualTo(1);
			assertThat(asSystemTime.getDayOfWeek().getValue()).isEqualTo(4);
			assertThat(asSystemTime.getWeekOfMonth().getValue()).isEqualTo(1);
			assertThat(asSystemTime.getHour().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getMinute().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getSecond().getValue()).isEqualTo(0);
			assertThat(asSystemTime.getMilliseconds().getValue()).isEqualTo(0);
		}
		
		@Test
		public void testWeekOfMonthMax() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.dateTime(DateTime.parse("2012-12-31T23:59:59.999+00"))
				.build();
				
			assertThat(asSystemTime.getWeekOfMonth()).isEqualTo(unsignedShort(5));
		}
	
		@Test
		public void testDayOfWeekMin() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, 1);
			
			UnsignedShort fieldValue = systemTimeBuilder.dayOfWeek(new DateTime(calendar.getTimeInMillis()));
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.DAYOFWEEK.min));
		}
		
		@Test
		public void testDayOfWeekMax() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK, 7);
			
			UnsignedShort fieldValue = systemTimeBuilder.dayOfWeek(new DateTime(calendar.getTimeInMillis()));
			
			assertThat(fieldValue).isEqualTo(unsignedShort(SystemTimeSpecs.DAYOFWEEK.max));
		}
	
	
		@Test
		public void testOverridingYearWithNullKeepDateYear() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.dateTime(DateTime.parse("2015-01-01T00:00:00.000+00"))
				.overridingYear(null)
				.build();
			
			assertThat(asSystemTime.getYear()).isEqualTo(unsignedShort(2015));
		}
		
		@Test
		public void testOverridingYear() {
			ASSystemTime asSystemTime = systemTimeBuilder
				.dateTime(DateTime.parse("2015-01-01T00:00:00.000+00"))
				.overridingYear(unsignedShort(0))
				.build();
			
			assertThat(asSystemTime.getYear()).isEqualTo(unsignedShort(0));
		}
		
		@Test
		public void testWeekOfMonth() {
			ASSystemTime asSystemTime = systemTimeBuilder
					.dateTime(DateTime.parse("2015-01-01T00:00:00.000+00"))
					.overridingYear(unsignedShort(0))
					.build();

			assertThat(asSystemTime.getWeekOfMonth()).isEqualTo(unsignedShort(1));
		}
		
		@Test
		public void testWeekOfMonthNext() {
			ASSystemTime asSystemTime = systemTimeBuilder
					.dateTime(DateTime.parse("2015-01-08T00:00:00.000+00"))
					.overridingYear(unsignedShort(0))
					.build();

			assertThat(asSystemTime.getWeekOfMonth()).isEqualTo(unsignedShort(2));
		}
		
		@Test
		public void testWeekOfMonthLast() {
			ASSystemTime asSystemTime = systemTimeBuilder
					.dateTime(DateTime.parse("2015-01-31T00:00:00.000+00"))
					.overridingYear(unsignedShort(0))
					.build();

			assertThat(asSystemTime.getWeekOfMonth()).isEqualTo(unsignedShort(5));
		}
		
		private UnsignedShort unsignedShort(int value) {
			return UnsignedShort.checkedCast(value);
		}
	}
}
