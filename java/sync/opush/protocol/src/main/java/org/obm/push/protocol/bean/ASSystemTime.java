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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.obm.push.utils.type.UnsignedShort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ASSystemTime {

	private final UnsignedShort year;
	private final UnsignedShort month;
	private final UnsignedShort dayOfWeek;
	private final UnsignedShort weekOfMonth;
	private final UnsignedShort hour;
	private final UnsignedShort minute;
	private final UnsignedShort second;
	private final UnsignedShort milliseconds;

	public static class Builder {

		private UnsignedShort year;
		private UnsignedShort month;
		private UnsignedShort dayOfWeek;
		private UnsignedShort weekOfMonth;
		private UnsignedShort hour;
		private UnsignedShort minute;
		private UnsignedShort second;
		private UnsignedShort milliseconds;

		public Builder year(UnsignedShort year) {
			this.year = year;
			return this;
		}

		public Builder month(UnsignedShort month) {
			this.month = month;
			return this;
		}

		public Builder dayOfWeek(UnsignedShort dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
			return this;
		}

		public Builder weekOfMonth(UnsignedShort weekOfMonth) {
			this.weekOfMonth = weekOfMonth;
			return this;
		}

		public Builder hour(UnsignedShort hour) {
			this.hour = hour;
			return this;
		}

		public Builder minute(UnsignedShort minute) {
			this.minute = minute;
			return this;
		}

		public Builder second(UnsignedShort second) {
			this.second = second;
			return this;
		}

		public Builder milliseconds(UnsignedShort milliseconds) {
			this.milliseconds = milliseconds;
			return this;
		}

		public ASSystemTime build() {
			Preconditions.checkNotNull(year);
			Preconditions.checkNotNull(month);
			Preconditions.checkNotNull(dayOfWeek);
			Preconditions.checkNotNull(weekOfMonth);
			Preconditions.checkNotNull(hour);
			Preconditions.checkNotNull(minute);
			Preconditions.checkNotNull(second);
			Preconditions.checkNotNull(milliseconds);
			
			return new ASSystemTime(year, month, dayOfWeek,
					weekOfMonth, hour, minute, second, milliseconds);
		}
	}

	public static class FromDateBuilder {

		@VisibleForTesting static final int JAVA_TO_MS_API_MONTH_OFFSET = 1;
		@VisibleForTesting static final int JAVA_TO_MS_API_DAYOFWEEK_OFFSET = -1;
		@VisibleForTesting static final int JAVA_TO_MS_API_WEEKOFMONTH_LAST = 5;

		private Date date;
		private UnsignedShort overridingYear;

		public FromDateBuilder date(Date date) {
			this.date = date;
			return this;
		}

		public FromDateBuilder overridingYear(UnsignedShort overridingYear) {
			this.overridingYear = overridingYear;
			return this;
		}

		public ASSystemTime build() {
			Preconditions.checkNotNull(date);

			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			calendar.setTime(date);
			
			UnsignedShort month = monthFieldAsUnsignedShort(calendar);
			UnsignedShort weekOfMonth = calendarFieldAsUnsignedShort(calendar, Calendar.WEEK_OF_MONTH);
			UnsignedShort dayOfWeek = dayOfWeekFieldAsBinary(calendar);
			UnsignedShort hour = calendarFieldAsUnsignedShort(calendar, Calendar.HOUR_OF_DAY);
			UnsignedShort minute = calendarFieldAsUnsignedShort(calendar, Calendar.MINUTE);
			UnsignedShort second = calendarFieldAsUnsignedShort(calendar, Calendar.SECOND);
			UnsignedShort milliseconds = calendarFieldAsUnsignedShort(calendar, Calendar.MILLISECOND);
			
			UnsignedShort year = overridingYear;
			if (year == null) {
				year = calendarFieldAsUnsignedShort(calendar, Calendar.YEAR);
			}

			if (weekOfMonth.getValue() > JAVA_TO_MS_API_WEEKOFMONTH_LAST) {
				weekOfMonth = UnsignedShort.checkedCast(JAVA_TO_MS_API_WEEKOFMONTH_LAST);
			}
			
			return new Builder()
					.year(year).month(month).dayOfWeek(dayOfWeek).weekOfMonth(weekOfMonth)
					.hour(hour).minute(minute).second(second).milliseconds(milliseconds)
					.build();
		}
		
		@VisibleForTesting UnsignedShort calendarFieldAsUnsignedShort(Calendar calendar, int calendarField) {
			int calendarFieldValue = calendar.get(calendarField);
			return unsignedShortOf(calendarFieldValue);
		}
		
		@VisibleForTesting UnsignedShort monthFieldAsUnsignedShort(Calendar calendar) {
			int monthFieldValue = calendar.get(Calendar.MONTH) + JAVA_TO_MS_API_MONTH_OFFSET;
			return unsignedShortOf(monthFieldValue);
		}
		
		@VisibleForTesting UnsignedShort dayOfWeekFieldAsBinary(Calendar calendar) {
			int dayFieldValue = calendar.get(Calendar.DAY_OF_WEEK) + JAVA_TO_MS_API_DAYOFWEEK_OFFSET;
			return unsignedShortOf(dayFieldValue);
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
	public int hashCode() {
		return Objects.hashCode(year, month, dayOfWeek, weekOfMonth, hour, minute, second, milliseconds);
	}
	
	@Override
	public boolean equals(Object object) {
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
}
