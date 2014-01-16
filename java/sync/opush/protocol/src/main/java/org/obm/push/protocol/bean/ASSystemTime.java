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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Weeks;
import org.obm.push.utils.type.UnsignedShort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ASSystemTime {

	public static final int JAVA_TO_MS_API_MONTH_OFFSET = 1;
	public static final int JAVA_TO_MS_API_DAYOFWEEK_OFFSET = -1;
	public static final int JAVA_TO_MS_API_WEEKOFMONTH_LAST = 5;
	private static final int MAX_HOUR = 23;
	
	private final UnsignedShort year;
	private final UnsignedShort month;
	private final UnsignedShort dayOfWeek;
	private final UnsignedShort weekOfMonth;
	private final UnsignedShort hour;
	private final UnsignedShort minute;
	private final UnsignedShort second;
	private final UnsignedShort milliseconds;

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private UnsignedShort year;
		private UnsignedShort month;
		private UnsignedShort dayOfWeek;
		private UnsignedShort weekOfMonth;
		private UnsignedShort hour;
		private UnsignedShort minute;
		private UnsignedShort second;
		private UnsignedShort milliseconds;

		private Builder() {
		}
		
		public Builder year(UnsignedShort year) {
			this.year = year;
			return this;
		}

		public Builder month(UnsignedShort month) {
			Preconditions.checkState(month.getValue() <= 12);
			this.month = month;
			return this;
		}

		public Builder dayOfWeek(UnsignedShort dayOfWeek) {
			Preconditions.checkState(dayOfWeek.getValue() <= 6);
			this.dayOfWeek = dayOfWeek;
			return this;
		}

		public Builder weekOfMonth(UnsignedShort weekOfMonth) {
			Preconditions.checkState(weekOfMonth.getValue() <= 5);
			this.weekOfMonth = weekOfMonth;
			return this;
		}

		public Builder hour(UnsignedShort hour) {
			Preconditions.checkState(hour.getValue() <= MAX_HOUR);
			this.hour = hour;
			return this;
		}

		public Builder minute(UnsignedShort minute) {
			Preconditions.checkState(minute.getValue() <= 59);
			this.minute = minute;
			return this;
		}

		public Builder second(UnsignedShort second) {
			Preconditions.checkState(second.getValue() <= 59);
			this.second = second;
			return this;
		}

		public Builder milliseconds(UnsignedShort milliseconds) {
			Preconditions.checkState(milliseconds.getValue() <= 999);
			this.milliseconds = milliseconds;
			return this;
		}

		public ASSystemTime build() {
			Preconditions.checkState(year != null);
			Preconditions.checkState(month != null);
			Preconditions.checkState(dayOfWeek != null);
			Preconditions.checkState(weekOfMonth != null);
			Preconditions.checkState(hour != null);
			Preconditions.checkState(minute != null);
			Preconditions.checkState(second != null);
			Preconditions.checkState(milliseconds != null);
			
			return new ASSystemTime(year, month, dayOfWeek,
					weekOfMonth, hour, minute, second, milliseconds);
		}
	}

	public static class FromDateBuilder {

		private DateTime dateTime;
		private UnsignedShort overridingYear;

		public FromDateBuilder dateTime(DateTime dateTime) {
			this.dateTime = dateTime;
			return this;
		}

		public FromDateBuilder overridingYear(UnsignedShort overridingYear) {
			this.overridingYear = overridingYear;
			return this;
		}

		public ASSystemTime build() {
			Preconditions.checkState(dateTime != null);

			UnsignedShort year = year(dateTime);
			UnsignedShort month = month(dateTime);
			UnsignedShort weekOfMonth = weekOfMonth(dateTime);
			UnsignedShort dayOfWeek = dayOfWeek(dateTime);
			UnsignedShort hour = hour(dateTime);
			UnsignedShort minute = minute(dateTime);
			UnsignedShort second = second(dateTime);
			UnsignedShort milliseconds = milliseconds(dateTime);
			
			return new Builder()
					.year(year).month(month).dayOfWeek(dayOfWeek).weekOfMonth(weekOfMonth)
					.hour(hour).minute(minute).second(second).milliseconds(milliseconds)
					.build();
		}
		
		@VisibleForTesting UnsignedShort year(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return Objects.firstNonNull(overridingYear, unsignedShortOf(dateTime.getYear()));
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort month(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(dateTime.getMonthOfYear());
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort weekOfMonth(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(Math.min(weekOfMonthAsUnsignedShort(dateTime), JAVA_TO_MS_API_WEEKOFMONTH_LAST));
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting int weekOfMonthAsUnsignedShort(DateTime dateTime) {
			DateTime firstDayOfMonth = new DateTime(dateTime).withDayOfMonth(1);
			Weeks weeksBetweenDateAndFirstDateOfMonth = Weeks.weeksBetween(firstDayOfMonth, dateTime);
			int weekOfMonth = weeksBetweenDateAndFirstDateOfMonth.getWeeks() + JAVA_TO_MS_API_MONTH_OFFSET;
			
			DateTime lastDayOfMonth = new DateTime(dateTime).plusMonths(1).withDayOfMonth(1).minusDays(1);
			Weeks weeksBetweenDateAndLastDateOfMonth = Weeks.weeksBetween(dateTime, lastDayOfMonth);
			if (weeksBetweenDateAndLastDateOfMonth.getWeeks() == 0) {
				weekOfMonth = JAVA_TO_MS_API_WEEKOFMONTH_LAST;
			}
			return weekOfMonth;
		}
		
		@VisibleForTesting UnsignedShort dayOfWeek(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(dateTime.getDayOfWeek() % 7);
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort hour(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC)) && dateTime.getHourOfDay() != 0) {
				int hourOfDay = dateTime.minusHours(1).getHourOfDay();
				return unsignedShortOf(Math.min(MAX_HOUR, hourOfDay+1));
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort minute(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(dateTime.getMinuteOfHour());
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort second(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(dateTime.getSecondOfMinute());
			}
			return unsignedShortOf(0);
		}
		
		@VisibleForTesting UnsignedShort milliseconds(DateTime dateTime) {
			if (!dateTime.isEqual(new DateTime(0, DateTimeZone.UTC))) {
				return unsignedShortOf(dateTime.getMillisOfSecond());
			}
			return unsignedShortOf(0);
		}
		
		private UnsignedShort unsignedShortOf(int value) {
			return UnsignedShort.checkedCast(value);
		}
		
	}
	
	private ASSystemTime(
			UnsignedShort year, UnsignedShort month, UnsignedShort dayOfWeek,
			UnsignedShort weekOfMonth, UnsignedShort hour, UnsignedShort minute,
			UnsignedShort second, UnsignedShort milliseconds) {
		
		this.year = year;
		this.month = month;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.milliseconds = milliseconds;
	}

	public UnsignedShort getYear() {
		return year;
	}

	public UnsignedShort getMonth() {
		return month;
	}

	public UnsignedShort getDayOfWeek() {
		return dayOfWeek;
	}

	public UnsignedShort getWeekOfMonth() {
		return weekOfMonth;
	}

	public UnsignedShort getHour() {
		return hour;
	}

	public UnsignedShort getMinute() {
		return minute;
	}

	public UnsignedShort getSecond() {
		return second;
	}

	public UnsignedShort getMilliseconds() {
		return milliseconds;
	}

	
	@Override
	public final int hashCode() {
		return Objects.hashCode(year, month, dayOfWeek, weekOfMonth, hour, minute, second, milliseconds);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof ASSystemTime) {
			ASSystemTime that = (ASSystemTime) object;
			return Objects.equal(this.year, that.year) &&
					Objects.equal(this.month, that.month) &&
					Objects.equal(this.dayOfWeek, that.dayOfWeek) &&
					Objects.equal(this.weekOfMonth, that.weekOfMonth) &&
					Objects.equal(this.hour, that.hour) &&
					Objects.equal(this.minute, that.minute) &&
					Objects.equal(this.second, that.second) &&
					Objects.equal(this.milliseconds, that.milliseconds);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s-%s-%s/%sT%s:%s:%s", 
				year, 
				month, 
				weekOfMonth, 
				dayOfWeek, 
				hour, 
				minute, 
				second);
	}
}
