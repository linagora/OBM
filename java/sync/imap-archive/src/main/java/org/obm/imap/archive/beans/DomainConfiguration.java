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
import org.obm.imap.archive.dto.DomainConfigurationDto;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class DomainConfiguration {
	
	public static final DomainConfiguration.Builder DEFAULT_VALUES_BUILDER = 
		builder()
			.enabled(false)
			.schedulingConfiguration(SchedulingConfiguration.DEFAULT_VALUES_BUILDER);

	public static DomainConfiguration from(DomainConfigurationDto configuration, ObmDomain domain) {
		return DomainConfiguration.builder()
				.domain(domain)
				.enabled(configuration.enabled)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.valueOf(configuration.repeatKind))
							.dayOfWeek(DayOfWeek.fromSpecificationValue(configuration.dayOfWeek))
							.dayOfMonth(DayOfMonth.of(configuration.dayOfMonth))
							.dayOfYear(DayOfYear.of(configuration.dayOfYear))
							.build())
						.time(LocalTime.parse(configuration.hour + ":" + configuration.minute))
						.build())
				.excludedFolder(configuration.excludedFolder)
				.build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ObmDomain domain;
		private Boolean enabled;
		private SchedulingConfiguration schedulingConfiguration;
		private String excludedFolder;
		
		private Builder() {
		}
		
		public Builder domain(ObmDomain domain) {
			Preconditions.checkNotNull(domain);
			this.domain = domain;
			return this;
		}

		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder schedulingConfiguration(SchedulingConfiguration schedulingConfiguration) {
			this.schedulingConfiguration = schedulingConfiguration;
			return this;
		}
		
		public Builder excludedFolder(String excludedFolder) {
			this.excludedFolder = excludedFolder;
			return this;
		}
		
		public DomainConfiguration build() {
			Preconditions.checkState(domain != null);
			Preconditions.checkState(enabled != null);
			if (enabled) {
				Preconditions.checkState(schedulingConfiguration != null);
			}
			if (excludedFolder != null) {
				Preconditions.checkState(excludedFolder.contains("/") == false);
				Preconditions.checkState(excludedFolder.contains("@") == false);
			}
			return new DomainConfiguration(domain, enabled, schedulingConfiguration, excludedFolder);
		}
	}
	
	private final ObmDomain domain;
	private final boolean enabled;
	private final SchedulingConfiguration schedulingConfiguration;
	private final String excludedFolder;

	private DomainConfiguration(ObmDomain domain, boolean enabled, SchedulingConfiguration schedulingConfiguration, String excludedFolder) {
		this.domain = domain;
		this.enabled = enabled;
		this.schedulingConfiguration = schedulingConfiguration;
		this.excludedFolder = excludedFolder;
	}

	public ObmDomain getDomain() {
		return domain;
	}
	
	public ObmDomainUuid getDomainId() {
		return domain.getUuid();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public RepeatKind getRepeatKind() {
		return schedulingConfiguration != null ? schedulingConfiguration.getRepeatKind() : null;
	}
	
	public DayOfWeek getDayOfWeek() {
		return schedulingConfiguration != null ? schedulingConfiguration.getDayOfWeek() : null;
	}
	
	public DayOfMonth getDayOfMonth() {
		return schedulingConfiguration != null ? schedulingConfiguration.getDayOfMonth() : null;
	}
	
	public Boolean isLastDayOfMonth() {
		return schedulingConfiguration != null ? schedulingConfiguration.isLastDayOfMonth() : null;
	}
	
	public DayOfYear getDayOfYear() {
		return schedulingConfiguration != null ? schedulingConfiguration.getDayOfYear() : null;
	}
	
	public SchedulingConfiguration getSchedulingConfiguration() {
		return schedulingConfiguration;
	}
	
	public LocalTime getTime() {
		return schedulingConfiguration != null ? schedulingConfiguration.getTime() : null;
	}
	
	public Integer getHour() {
		return schedulingConfiguration != null ? schedulingConfiguration.getHour() : null;
	}
	
	public Integer getMinute() {
		return schedulingConfiguration != null ? schedulingConfiguration.getMinute() : null;
	}

	public String getExcludedFolder() {
		return excludedFolder;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(domain, enabled, schedulingConfiguration, excludedFolder);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof DomainConfiguration) {
			DomainConfiguration that = (DomainConfiguration) object;
			return Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.enabled, that.enabled)
				&& Objects.equal(this.schedulingConfiguration, that.schedulingConfiguration)
				&& Objects.equal(this.excludedFolder, that.excludedFolder);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domain", domain)
			.add("enabled", enabled)
			.add("recurrence", schedulingConfiguration)
			.add("excludedFolder", excludedFolder)
			.toString();
	}
}
