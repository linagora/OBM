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
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenExcludeFolderContainsSlash() {
		DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.excludedFolder("exclud/ed")
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
					.excludedFolder("excluded")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
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
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getExcludedFolder()).isNull();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenExcludedUsersIsNotProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.ENABLE)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.excludedFolder("excluded")
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).isEmpty();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenMailingIsNotProvided() {
		ExcludedUser expectedExcludedUser = ExcludedUser.builder()
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
					.excludedFolder("excluded")
					.excludedUsers(ImmutableList.of(expectedExcludedUser))
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).containsOnly(expectedExcludedUser);
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenEnabledIsFalseAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build())
					.state(ConfigurationState.DISABLE)
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.getState()).isEqualTo(ConfigurationState.DISABLE);
		assertThat(configuration.isEnabled()).isFalse();
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
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
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
		assertThat(configuration.getExcludedFolder()).isNull();
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
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
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = expectedDomainId.getUUID();
		domainConfigurationDto.enabled = expectedEnabled;
		domainConfigurationDto.repeatKind = expectedRepeatKind.toString();
		domainConfigurationDto.dayOfWeek = expectedDayOfWeek.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = expectedDayOfMonth.getDayIndex();
		domainConfigurationDto.dayOfYear = expectedDayOfYear.getDayOfYear();
		domainConfigurationDto.hour = expectedHour;
		domainConfigurationDto.minute = expectedMinute;
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.excludedUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
		domainConfigurationDto.mailingEmails = ImmutableList.of("usera@mydomain.org", "userb@mydomain.org");
		
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
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).containsOnly(ExcludedUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
		assertThat(configuration.getMailing()).isEqualTo(Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("usera@mydomain.org"), EmailAddress.loginAtDomain("userb@mydomain.org"))));
	}
	
	@Test
	public void fromDtoShouldBuildWhenExcludedUsersIsEmpty() {
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
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.excludedUserIdToLoginMap = ImmutableMap.of();
		domainConfigurationDto.mailingEmails = ImmutableList.of();
		
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
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).isEmpty();
		assertThat(configuration.getMailing().getEmailAddresses()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void fromDtoShouldThrowWhenExcludedUsersNull() {
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
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.excludedUserIdToLoginMap = null;
		
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
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.excludedUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
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
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).containsOnly(ExcludedUser.builder()
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
		domainConfigurationDto.excludedFolder = "excluded";
		domainConfigurationDto.excludedUserIdToLoginMap = ImmutableMap.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "usera");
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
		assertThat(configuration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(configuration.getExcludedUsers()).containsOnly(ExcludedUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build());
	}
}
