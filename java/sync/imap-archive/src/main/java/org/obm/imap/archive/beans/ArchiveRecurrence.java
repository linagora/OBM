/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.beans;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ArchiveRecurrence {

	public static Builder builder() {
		return new Builder();
	}

	public static ArchiveRecurrence daily() {
		return builder().repeat(RepeatKind.DAILY).build();
	}
	
	public static class Builder {

		private RepeatKind repeat;
		private DayOfWeek dayOfWeek;
		private DayOfMonth dayOfMonth;
		private DayOfYear dayOfYear;
		
		private Builder() {
		}

		public Builder repeat(RepeatKind repeat) {
			Preconditions.checkNotNull(repeat);
			this.repeat = repeat;
			return this;
		}
		
		public Builder dayOfWeek(DayOfWeek dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
			return this;
		}
		
		public Builder dayOfMonth(DayOfMonth dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
			return this;
		}

		public Builder dayOfYear(DayOfYear dayOfYear) {
			this.dayOfYear = dayOfYear;
			return this;
		}
		
		public ArchiveRecurrence build() {
			Preconditions.checkState(repeat != null);
			switch (repeat) {
			case WEEKLY: {
				Preconditions.checkState(dayOfWeek != null);
				break;
			}
			case MONTHLY: {
				Preconditions.checkState(dayOfMonth != null);
				break;
			}
			case YEARLY: {
				Preconditions.checkState(dayOfYear != null);
				break;
			}
			case DAILY: {
				break;
			}
			}
			return new ArchiveRecurrence(repeat, dayOfWeek, dayOfMonth, dayOfYear);
		}
	}
	
	private final RepeatKind repeatKind;
	private final DayOfWeek dayOfWeek;
	private final DayOfMonth dayOfMonth;
	private final DayOfYear dayOfYear;
	
	private ArchiveRecurrence(RepeatKind repeat, DayOfWeek dayOfWeek, DayOfMonth dayOfMonth, DayOfYear dayOfYear) {
		this.repeatKind = repeat;
		this.dayOfWeek = dayOfWeek;
		this.dayOfMonth = dayOfMonth;
		this.dayOfYear = dayOfYear;
	}
	
	public RepeatKind getRepeatKind() {
		return repeatKind;
	}
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	
	public DayOfMonth getDayOfMonth() {
		return dayOfMonth;
	}
	
	public boolean isLastDayOfMonth() {
		return dayOfMonth.isLastDayOfMonth();
	}
	
	public DayOfYear getDayOfYear() {
		return dayOfYear;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(repeatKind, dayOfWeek, dayOfMonth, dayOfYear);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveRecurrence) {
			ArchiveRecurrence that = (ArchiveRecurrence) object;
			return Objects.equal(this.repeatKind, that.repeatKind)
				&& Objects.equal(this.dayOfWeek, that.dayOfWeek)
				&& Objects.equal(this.dayOfMonth, that.dayOfMonth)
				&& Objects.equal(this.dayOfYear, that.dayOfYear);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("repeatKind", repeatKind)
			.add("dayOfWeek", dayOfWeek)
			.add("dayOfMonth", dayOfMonth)
			.add("dayOfYear", dayOfYear)
			.toString();
	}
	
}
