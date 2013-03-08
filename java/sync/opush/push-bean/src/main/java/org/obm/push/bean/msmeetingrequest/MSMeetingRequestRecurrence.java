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
package org.obm.push.bean.msmeetingrequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class MSMeetingRequestRecurrence implements Serializable {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private MSMeetingRequestRecurrenceType type;
		private Integer interval;
		private Date until;
		private Integer occurrences;
		private Integer weekOfMonth;
		private Integer dayOfMonth;
		private List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek = Lists.newArrayList();
		private Integer monthOfYear;
	
		private Builder() {
		}
		
		public Builder type(MSMeetingRequestRecurrenceType type) {
			this.type = type;
			return this;
		}
		
		public Builder interval(Integer interval) {
			this.interval = interval;
			return this;
		}
		
		public Builder until(Date until) {
			this.until = until;
			return this;
		}
		
		public Builder occurrences(Integer occurrences) {
			this.occurrences = occurrences;
			return this;
		}
		
		public Builder weekOfMonth(Integer weekOfMonth) {
			this.weekOfMonth = weekOfMonth;
			return this;
		}
		
		public Builder dayOfMonth(Integer dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
			return this;
		}
		
		public Builder dayOfWeek(List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
			return this;
		}
		
		public Builder monthOfYear(Integer monthOfYear) {
			this.monthOfYear = monthOfYear;
			return this;
		}
		
		public MSMeetingRequestRecurrence build() {
			Preconditions.checkNotNull(type, "The field type is required");
			Preconditions.checkNotNull(interval, "The field interval is required");
			
			if (type == MSMeetingRequestRecurrenceType.WEEKLY 
					|| type == MSMeetingRequestRecurrenceType.YEARLY_NTH_DAY) {
				
				Preconditions.checkArgument(dayOfWeek != null && !dayOfWeek.isEmpty(), "The field dayOfWeek is required");
			}
			
			if (type == MSMeetingRequestRecurrenceType.YEARLY_NTH_DAY 
					|| type == MSMeetingRequestRecurrenceType.YEARLY) {
				
				Preconditions.checkNotNull(monthOfYear, "The field monthOfYear is required");
			}		
			
			return new MSMeetingRequestRecurrence(type, interval, until, 
					occurrences, weekOfMonth, dayOfMonth, dayOfWeek, monthOfYear);
		}
	}
	
	private static final long serialVersionUID = 5958868316016116823L;
	
	private final MSMeetingRequestRecurrenceType type;
	private final Integer interval;
	private final Date until;
	private final Integer occurrences;
	private final Integer weekOfMonth;
	private final Integer dayOfMonth;
	private final List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek;
	private final Integer monthOfYear;
	
	private MSMeetingRequestRecurrence(MSMeetingRequestRecurrenceType type, Integer interval, 
			Date until, Integer occurrences, Integer weekOfMonth, Integer dayOfMonth, 
			List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek, Integer monthOfYear) {
		
		super();
		this.type = type;
		this.interval = interval;
		this.until = until;
		this.occurrences = occurrences;
		this.weekOfMonth = weekOfMonth;
		this.dayOfMonth = dayOfMonth;
		this.dayOfWeek = dayOfWeek;
		this.monthOfYear = monthOfYear;
	}

	public MSMeetingRequestRecurrenceType getType() {
		return type;
	}

	public Integer getInterval() {
		return interval;
	}

	public Date getUntil() {
		return until;
	}

	public Integer getOccurrences() {
		return occurrences;
	}

	public Integer getWeekOfMonth() {
		return weekOfMonth;
	}

	public Integer getDayOfMonth() {
		return dayOfMonth;
	}

	public List<MSMeetingRequestRecurrenceDayOfWeek> getDayOfWeek() {
		return dayOfWeek;
	}

	public Integer getMonthOfYear() {
		return monthOfYear;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(type, interval, until, occurrences, 
				weekOfMonth, dayOfMonth, dayOfWeek, monthOfYear);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSMeetingRequestRecurrence) {
			MSMeetingRequestRecurrence that = (MSMeetingRequestRecurrence) object;
			return Objects.equal(this.type, that.type)
				&& Objects.equal(this.interval, that.interval)
				&& Objects.equal(this.until, that.until)
				&& Objects.equal(this.occurrences, that.occurrences)
				&& Objects.equal(this.weekOfMonth, that.weekOfMonth)
				&& Objects.equal(this.dayOfMonth, that.dayOfMonth)
				&& Objects.equal(this.dayOfWeek, that.dayOfWeek)
				&& Objects.equal(this.monthOfYear, that.monthOfYear);
		}
		return false;
	}
}
