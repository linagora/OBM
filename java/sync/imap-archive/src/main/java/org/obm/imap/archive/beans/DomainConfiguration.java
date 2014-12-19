/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.imap.archive.beans;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalTime;
import org.obm.imap.archive.dto.DomainConfigurationDto;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;

public class DomainConfiguration {
	
	private static final String DEFAULT_ARCHIVE_MAIN_FOLDER = "ARCHIVE";
	
	public static final DomainConfiguration.Builder DEFAULT_VALUES_BUILDER = 
		builder()
			.state(ConfigurationState.DISABLE)
			.schedulingConfiguration(SchedulingConfiguration.DEFAULT_VALUES_BUILDER)
			.archiveMainFolder(DEFAULT_ARCHIVE_MAIN_FOLDER);

	public static DomainConfiguration from(DomainConfigurationDto configuration, ObmDomain domain) {
		return DomainConfiguration.builder()
				.domain(domain)
				.state(configuration.enabled ? ConfigurationState.ENABLE : ConfigurationState.DISABLE)
				.archiveMainFolder(configuration.archiveMainFolder)
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
				.excludedUsers(from(configuration.excludedUserIdToLoginMap))
				.mailing(Mailing.fromStrings(configuration.mailingEmails))
				.build();
	}
	
	private static List<ExcludedUser> from(Map<String, String> excludedUserIdToLoginMap) {
		Preconditions.checkNotNull(excludedUserIdToLoginMap);
		ImmutableList.Builder<ExcludedUser> builder = ImmutableList.builder();
		for (Entry<String, String> excludedUserIdToLogin : excludedUserIdToLoginMap.entrySet()) {
			builder.add(ExcludedUser.builder()
					.id(UserExtId.valueOf(excludedUserIdToLogin.getKey()))
					.login(excludedUserIdToLogin.getValue())
					.build());
		}
		return builder.build();
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ObmDomain domain;
		private ConfigurationState state;
		private String archiveMainFolder;
		private SchedulingConfiguration schedulingConfiguration;
		private String excludedFolder;
		private ImmutableList.Builder<ExcludedUser> excludedUsers;
		private Mailing mailing;
		
		private Builder() {
			excludedUsers = ImmutableList.builder();
		}
		
		public Builder domain(ObmDomain domain) {
			Preconditions.checkNotNull(domain);
			this.domain = domain;
			return this;
		}

		public Builder state(ConfigurationState state) {
			Preconditions.checkNotNull(state);
			this.state = state;
			return this;
		}
		
		public Builder archiveMainFolder(String archiveMainFolder) {
			Preconditions.checkNotNull(archiveMainFolder);
			this.archiveMainFolder = archiveMainFolder;
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
		
		public Builder excludedUsers(List<ExcludedUser> excludedUsers) {
			this.excludedUsers.addAll(excludedUsers);
			return this;
		}
		
		public Builder mailing(Mailing mailing) {
			this.mailing = mailing;
			return this;
		}
		
		public DomainConfiguration build() {
			Preconditions.checkState(domain != null);
			Preconditions.checkState(state != null);
			if (ConfigurationState.ENABLE == state) {
				Preconditions.checkState(schedulingConfiguration != null);
			}
			if (excludedFolder != null) {
				Preconditions.checkState(excludedFolder.contains("/") == false);
				Preconditions.checkState(excludedFolder.contains("@") == false);
			}
			Preconditions.checkState(!Strings.isNullOrEmpty(archiveMainFolder));
			Preconditions.checkState(archiveMainFolder.contains("/") == false);
			Preconditions.checkState(archiveMainFolder.contains("@") == false);
			
			return new DomainConfiguration(domain, state, schedulingConfiguration, archiveMainFolder, excludedFolder, excludedUsers.build(), Objects.firstNonNull(mailing, Mailing.empty()));
		}
	}
	
	private final ObmDomain domain;
	private final ConfigurationState state;
	private final SchedulingConfiguration schedulingConfiguration;
	private final String archiveMainFolder;
	private final String excludedFolder;
	private final List<ExcludedUser> excludedUsers;
	private final Mailing mailing;

	private DomainConfiguration(ObmDomain domain, ConfigurationState state, SchedulingConfiguration schedulingConfiguration, String archiveMainFolder, String excludedFolder, ImmutableList<ExcludedUser> excludedUsers, Mailing mailing) {
		this.domain = domain;
		this.state = state;
		this.schedulingConfiguration = schedulingConfiguration;
		this.archiveMainFolder = archiveMainFolder;
		this.excludedFolder = excludedFolder;
		this.excludedUsers = excludedUsers;
		this.mailing = mailing;
	}

	public ObmDomain getDomain() {
		return domain;
	}
	
	public ObmDomainUuid getDomainId() {
		return domain.getUuid();
	}
	
	public ConfigurationState getState() {
		return state;
	}
	
	public boolean isEnabled() {
		return ConfigurationState.ENABLE.equals(state);
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
	
	public String getArchiveMainFolder() {
		return archiveMainFolder;
	}

	public String getExcludedFolder() {
		return excludedFolder;
	}
	
	public List<ExcludedUser> getExcludedUsers() {
		return excludedUsers;
	}
	
	public Mailing getMailing() {
		return mailing;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(domain, state, schedulingConfiguration, archiveMainFolder, excludedFolder, excludedUsers, mailing);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof DomainConfiguration) {
			DomainConfiguration that = (DomainConfiguration) object;
			return Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.state, that.state)
				&& Objects.equal(this.schedulingConfiguration, that.schedulingConfiguration)
				&& Objects.equal(this.archiveMainFolder, that.archiveMainFolder)
				&& Objects.equal(this.excludedFolder, that.excludedFolder)
				&& Objects.equal(this.excludedUsers, that.excludedUsers)
				&& Objects.equal(this.mailing, that.mailing);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domain", domain)
			.add("state", state)
			.add("recurrence", schedulingConfiguration)
			.add("archiveMainFolder", archiveMainFolder)
			.add("excludedFolder", excludedFolder)
			.add("excludedUsers", excludedUsers)
			.add("mailing", mailing)
			.toString();
	}
}
