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

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalTime;
import org.junit.Test;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.sync.base.EmailAddress;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;


public class DomainConfigurationTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullDomain() {
		DomainConfiguration.builder().domain(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenDomainIdIsNotProvided() {
		DomainConfiguration.builder().state(ConfigurationState.DISABLE).build();
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullState() {
		DomainConfiguration.builder().state(null);
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEnabledIsNotProvided() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build()).build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEnabledAndSchedulingConfigurationIsNotProvided() {
		DomainConfiguration.builder().state(ConfigurationState.ENABLE).domain(
				ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build()).build();
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullArchiveMainFolder() {
		DomainConfiguration.builder().archiveMainFolder(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenArchiveMainFolderIsNotProvided() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenArchiveMainFolderIsEmpty() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("")
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenArchiveMainFolderContainsSlash() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("ar/chive")
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenArchiveMainFolderContainsAt() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arch@ive")
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenExcludeFolderContainsSlash() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.excludedFolder("ex/cluded")
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenExcludeFolderContainsAt() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.excludedFolder("ex@cluded")
			.build();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenEnabledIsTrueAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.ENABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.archiveMainFolder("arChive")
					.excludedFolder("excluded")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenExcludedIsNotProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.ENABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.archiveMainFolder("arChive")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isNull();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenScopeUsersIsNotProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.ENABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.archiveMainFolder("arChive")
					.excludedFolder("excluded")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getScopeUsers()).isEmpty();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenMailingIsNotProvided() {
		ScopeUser expectedScopeUser = ScopeUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build();
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.ENABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.archiveMainFolder("arChive")
					.excludedFolder("excluded")
					.scopeUsers(ImmutableList.of(expectedScopeUser))
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getScopeUsers()).containsOnly(expectedScopeUser);
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenEnabledIsFalseAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.DISABLE)
					.archiveMainFolder("arChive")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.getState()).isEqualTo(ConfigurationState.DISABLE);
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenMoveEnabledIsTrueAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.DISABLE)
					.archiveMainFolder("arChive")
					.moveEnabled(true)
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.getState()).isEqualTo(ConfigurationState.DISABLE);
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.isMoveEnabled()).isTrue();
	}

	@Test
	public void builderShouldKeepUnusedConfigurationWhenProvidingDisabledFields() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.DISABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.archiveMainFolder("arChive")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
	}
	
	@Test
	public void defaultValues() {
		ObmDomainUuid domainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		SchedulingConfiguration schedulingConfiguration = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
						.dayOfMonth(DayOfMonth.last())
						.dayOfWeek(DayOfWeek.MONDAY)
						.dayOfYear(DayOfYear.of(1))
						.repeat(RepeatKind.MONTHLY)
						.build())
				.time(LocalTime.parse("0:0"))
				.build();
		
		ObmDomain domain = ObmDomain.builder().uuid(domainId).build();
		DomainConfiguration configuration = DomainConfiguration.DEFAULT_VALUES_BUILDER.domain(domain).build();
		assertThat(configuration.getState()).isEqualTo(ConfigurationState.DISABLE);
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getRepeatKind()).isEqualTo(schedulingConfiguration.getRepeatKind());
		assertThat(configuration.getDayOfMonth()).isEqualTo(schedulingConfiguration.getDayOfMonth());
		assertThat(configuration.getDayOfWeek()).isEqualTo(schedulingConfiguration.getDayOfWeek());
		assertThat(configuration.getDayOfYear()).isEqualTo(schedulingConfiguration.getDayOfYear());
		assertThat(configuration.getDomainId()).isEqualTo(domainId);
		assertThat(configuration.getHour()).isEqualTo(0);
		assertThat(configuration.getMinute()).isEqualTo(0);
		assertThat(configuration.getSchedulingConfiguration()).isEqualTo(schedulingConfiguration);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("ARCHIVE");
		assertThat(configuration.getExcludedFolder()).isNull();
		assertThat(configuration.isScopeUsersIncludes()).isFalse();
		assertThat(configuration.getScopeUsers()).isEmpty();
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
		assertThat(configuration.isMoveEnabled()).isFalse();
	}
	
	@Test
	public void fromDto() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		boolean expectedMoveEnabled = true;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.scopeUsersIncludes = true;
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
		domainConfigurationDto.mailingEmails = ImmutableList.of("usera@mydomain.org", "userb@mydomain.org");
		domainConfigurationDto.moveEnabled = expectedMoveEnabled;
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration configuration = DomainConfiguration.from(domainConfigurationDto, domain);
		assertThat(configuration.isEnabled()).isEqualTo(expectedEnabled);
		assertThat(configuration.getRepeatKind()).isEqualTo(expectedRepeatKind);
		assertThat(configuration.getDayOfMonth()).isEqualTo(expectedDayOfMonth);
		assertThat(configuration.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
		assertThat(configuration.getDayOfYear()).isEqualTo(expectedDayOfYear);
		assertThat(configuration.getDomainId()).isEqualTo(expectedDomainId);
		assertThat(configuration.getHour()).isEqualTo(expectedHour);
		assertThat(configuration.getMinute()).isEqualTo(expectedMinute);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.isScopeUsersIncludes()).isTrue();
		assertThat(configuration.getScopeUsers()).containsOnly(ScopeUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
		assertThat(configuration.getMailing()).isEqualTo(Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("usera@mydomain.org"), EmailAddress.loginAtDomain("userb@mydomain.org"))));
		assertThat(configuration.isMoveEnabled()).isEqualTo(expectedMoveEnabled);
	}
	
	@Test
	public void fromDtoShouldBuildWhenScopeUsersIsEmpty() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		boolean expectedMoveEnabled = false;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.mailingEmails = ImmutableList.of();
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of();
		domainConfigurationDto.moveEnabled = expectedMoveEnabled;
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration configuration = DomainConfiguration.from(domainConfigurationDto, domain);
		assertThat(configuration.getState()).isEqualTo(ConfigurationState.ENABLE);
		assertThat(configuration.isEnabled()).isEqualTo(expectedEnabled);
		assertThat(configuration.getRepeatKind()).isEqualTo(expectedRepeatKind);
		assertThat(configuration.getDayOfMonth()).isEqualTo(expectedDayOfMonth);
		assertThat(configuration.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
		assertThat(configuration.getDayOfYear()).isEqualTo(expectedDayOfYear);
		assertThat(configuration.getDomainId()).isEqualTo(expectedDomainId);
		assertThat(configuration.getHour()).isEqualTo(expectedHour);
		assertThat(configuration.getMinute()).isEqualTo(expectedMinute);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.isScopeUsersIncludes()).isFalse();
		assertThat(configuration.getScopeUsers()).isEmpty();
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
		assertThat(configuration.isMoveEnabled()).isEqualTo(expectedMoveEnabled);
	}
	
	@Test
	public void fromDtoShouldBuildWhenMoveEnabledIsFalse() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		boolean expectedMoveEnabled = false;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.mailingEmails = ImmutableList.of("usera@mydomain.org", "userb@mydomain.org");
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
		domainConfigurationDto.moveEnabled = expectedMoveEnabled;
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration configuration = DomainConfiguration.from(domainConfigurationDto, domain);
		assertThat(configuration.isEnabled()).isEqualTo(expectedEnabled);
		assertThat(configuration.getRepeatKind()).isEqualTo(expectedRepeatKind);
		assertThat(configuration.getDayOfMonth()).isEqualTo(expectedDayOfMonth);
		assertThat(configuration.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
		assertThat(configuration.getDayOfYear()).isEqualTo(expectedDayOfYear);
		assertThat(configuration.getDomainId()).isEqualTo(expectedDomainId);
		assertThat(configuration.getHour()).isEqualTo(expectedHour);
		assertThat(configuration.getMinute()).isEqualTo(expectedMinute);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getScopeUsers()).containsOnly(ScopeUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
		assertThat(configuration.getMailing()).isEqualTo(Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("usera@mydomain.org"), EmailAddress.loginAtDomain("userb@mydomain.org"))));
	}
	
	@Test(expected=NullPointerException.class)
	public void fromDtoShouldThrowWhenScopeUsersNull() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.scopeUserIdToLoginMap = null;
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration.from(domainConfigurationDto, domain);
	}
	
	@Test
	public void fromDtoShouldBuildWhenEmailAddressesIsEmpty() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
		domainConfigurationDto.mailingEmails = ImmutableList.of();
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration configuration = DomainConfiguration.from(domainConfigurationDto, domain);
		assertThat(configuration.isEnabled()).isEqualTo(expectedEnabled);
		assertThat(configuration.getRepeatKind()).isEqualTo(expectedRepeatKind);
		assertThat(configuration.getDayOfMonth()).isEqualTo(expectedDayOfMonth);
		assertThat(configuration.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
		assertThat(configuration.getDayOfYear()).isEqualTo(expectedDayOfYear);
		assertThat(configuration.getDomainId()).isEqualTo(expectedDomainId);
		assertThat(configuration.getHour()).isEqualTo(expectedHour);
		assertThat(configuration.getMinute()).isEqualTo(expectedMinute);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getScopeUsers()).containsOnly(ScopeUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void fromDtoShouldThrowWhenEmailAddressesIsNull() {
		ObmDomainUuid expectedDomainId = ObmDomainUuid.of("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		boolean expectedEnabled = true;
		RepeatKind expectedRepeatKind = RepeatKind.WEEKLY;
		DayOfWeek expectedDayOfWeek = DayOfWeek.TUESDAY;
		DayOfMonth expectedDayOfMonth = DayOfMonth.of(10);
		DayOfYear expectedDayOfYear = DayOfYear.of(100);
		Integer expectedHour = 11;
		Integer expectedMinute = 32;
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
		domainConfigurationDto.mailingEmails = null;
		
		ObmDomain domain = ObmDomain.builder().uuid(expectedDomainId).build();
		DomainConfiguration configuration = DomainConfiguration.from(domainConfigurationDto, domain);
		assertThat(configuration.isEnabled()).isEqualTo(expectedEnabled);
		assertThat(configuration.getRepeatKind()).isEqualTo(expectedRepeatKind);
		assertThat(configuration.getDayOfMonth()).isEqualTo(expectedDayOfMonth);
		assertThat(configuration.getDayOfWeek()).isEqualTo(expectedDayOfWeek);
		assertThat(configuration.getDayOfYear()).isEqualTo(expectedDayOfYear);
		assertThat(configuration.getDomainId()).isEqualTo(expectedDomainId);
		assertThat(configuration.getHour()).isEqualTo(expectedHour);
		assertThat(configuration.getMinute()).isEqualTo(expectedMinute);
		assertThat(configuration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getScopeUsers()).containsOnly(ScopeUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
	}
}
