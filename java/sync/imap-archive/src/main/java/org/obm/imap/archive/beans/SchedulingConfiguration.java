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

import org.joda.time.LocalTime;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.dto.DomainConfigurationDto;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class SchedulingConfiguration {
	
	public static final SchedulingConfiguration DEFAULT_VALUES_BUILDER = 
		builder()
			.time(LocalTime.MIDNIGHT)
			.recurrence(ArchiveRecurrence.builder()
					.dayOfMonth(DayOfMonth.last())
					.dayOfWeek(DayOfWeek.MONDAY)
					.dayOfYear(DayOfYear.of(1))
					.repeat(RepeatKind.MONTHLY)
					.build())
			.build();

	public static SchedulingConfiguration from(DomainConfigurationDto configuration) {
		return SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
						.repeat(RepeatKind.valueOf(configuration.repeatKind))
						.dayOfWeek(DayOfWeek.fromSpecificationValue(configuration.dayOfWeek))
						.dayOfMonth(DayOfMonth.of(configuration.dayOfMonth))
						.dayOfYear(DayOfYear.of(configuration.dayOfYear))
						.build())
				.time(LocalTime.parse(configuration.hour + ":" + configuration.minute))
				.build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ArchiveRecurrence recurrence;
		private LocalTime time;
		
		private Builder() {
		}
		
		public Builder time(LocalTime time) {
			Preconditions.checkArgument(time.getMillisOfSecond() == 0 && time.getSecondOfMinute() == 0);
			this.time = time;
			return this;
		}

		public Builder recurrence(ArchiveRecurrence recurrence) {
			this.recurrence = recurrence;
			return this;
		}
		
		public SchedulingConfiguration build() {
			Preconditions.checkState(time != null);
			Preconditions.checkState(recurrence != null);
			return new SchedulingConfiguration(recurrence, time);
		}
	}
	
	private final ArchiveRecurrence recurrence;
	private final LocalTime time;

	private SchedulingConfiguration(ArchiveRecurrence recurrence, LocalTime time) {
		this.time = time;
		this.recurrence = recurrence;
	}
	
	public RepeatKind getRepeatKind() {
		return recurrence.getRepeatKind();
	}
	
	public DayOfWeek getDayOfWeek() {
		return recurrence.getDayOfWeek();
	}
	
	public DayOfMonth getDayOfMonth() {
		return recurrence.getDayOfMonth();
	}
	
	public Boolean isLastDayOfMonth() {
		return recurrence.isLastDayOfMonth();
	}
	
	public DayOfYear getDayOfYear() {
		return recurrence.getDayOfYear();
	}
	
	public ArchiveRecurrence getRecurrence() {
		return recurrence;
	}
	
	public LocalTime getTime() {
		return time;
	}
	
	public Integer getHour() {
		return time.getHourOfDay();
	}
	
	public Integer getMinute() {
		return time.getMinuteOfHour();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(recurrence, time);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof SchedulingConfiguration) {
			SchedulingConfiguration that = (SchedulingConfiguration) object;
			return Objects.equal(this.recurrence, that.recurrence)
				&& Objects.equal(this.time, that.time);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("recurrence", recurrence)
			.add("time", time)
			.toString();
	}
}
