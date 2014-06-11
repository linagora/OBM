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

import java.util.UUID;

import org.joda.time.LocalTime;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;

import com.google.common.base.Preconditions;
import com.google.common.base.Objects;

public class DomainConfiguration {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private UUID domainId;
		private Boolean enabled;
		private ArchiveRecurrence recurrence;
		private LocalTime time;
		
		private Builder() {
		}
		
		public Builder domainId(UUID domainId) {
			Preconditions.checkNotNull(domainId);
			this.domainId = domainId;
			return this;
		}
		
		public Builder time(LocalTime time) {
			Preconditions.checkArgument(time.getMillisOfSecond() == 0 && time.getSecondOfMinute() == 0);
			this.time = time;
			return this;
		}

		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder recurrence(ArchiveRecurrence recurrence) {
			this.recurrence = recurrence;
			return this;
		}
		
		public DomainConfiguration build() {
			Preconditions.checkState(domainId != null);
			Preconditions.checkState(enabled != null);
			if (enabled) {
				Preconditions.checkState(time != null);
				Preconditions.checkState(recurrence != null);
			}
			return new DomainConfiguration(domainId, enabled, recurrence, time);
		}
	}
	
	private final UUID domainId;
	private final boolean enabled;
	private final ArchiveRecurrence recurrence;
	private final LocalTime time;

	private DomainConfiguration(UUID domainId, boolean enabled, ArchiveRecurrence recurrence, LocalTime time) {
		this.domainId = domainId;
		this.time = time;
		this.enabled = enabled;
		this.recurrence = recurrence;
	}
	
	public UUID getDomainId() {
		return domainId;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public RepeatKind getRepeatKind() {
		return recurrence != null ? recurrence.getRepeatKind() : null;
	}
	
	public DayOfWeek getDayOfWeek() {
		return recurrence != null ? recurrence.getDayOfWeek() : null;
	}
	
	public DayOfMonth getDayOfMonth() {
		return recurrence != null ? recurrence.getDayOfMonth() : null;
	}
	
	public DayOfYear getDayOfYear() {
		return recurrence != null ? recurrence.getDayOfYear() : null;
	}
	
	public ArchiveRecurrence getRecurrence() {
		return recurrence;
	}
	
	public LocalTime getTime() {
		return time;
	}
	
	public Integer getHour() {
		return time != null ? time.getHourOfDay() : null;
	}
	
	public Integer getMinute() {
		return time != null ? time.getMinuteOfHour() : null;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(domainId, enabled, recurrence, time);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof DomainConfiguration) {
			DomainConfiguration that = (DomainConfiguration) object;
			return Objects.equal(this.domainId, that.domainId)
				&& Objects.equal(this.enabled, that.enabled)
				&& Objects.equal(this.recurrence, that.recurrence)
				&& Objects.equal(this.time, that.time);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domainId", domainId)
			.add("enabled", enabled)
			.add("recurrence", recurrence)
			.add("time", time)
			.toString();
	}
}
