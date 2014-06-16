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

import java.util.UUID;

import org.joda.time.LocalTime;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;


public class DomainConfigurationTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenNullDomainId() {
		DomainConfiguration.builder().domainId(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenDomainIdIsNotProvided() {
		DomainConfiguration.builder().enabled(false).build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEnabledIsNotProvided() {
		DomainConfiguration.builder().domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEnabledAndTimeIsNotProvided() {
		DomainConfiguration.builder().enabled(true).domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderShouldThrowWhenTimeHasSeconds() {
		DomainConfiguration.builder().time(LocalTime.parse("12:22:23")).domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderShouldThrowWhenTimeHasMillis() {
		DomainConfiguration.builder().time(LocalTime.parse("12:22:00.552")).domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
	}

	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEnabledAndRepeatKindIsNotProvided() {
		DomainConfiguration.builder().time(LocalTime.parse("10:22")).domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenEnabledIsTrueAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"))
					.enabled(true)
					.time(LocalTime.parse("13:23"))
					.recurrence(ArchiveRecurrence.daily())
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isTrue();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenEnabledIsFalseAndRequiredFieldsAreProvided() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"))
					.enabled(false)
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isFalse();
	}

	@Test
	public void builderShouldKeepUnusedConfigurationWhenProvidingDisabledFields() {
		DomainConfiguration configuration = 
				DomainConfiguration.builder()
					.domainId(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"))
					.enabled(false)
					.time(LocalTime.parse("13:23"))
					.recurrence(ArchiveRecurrence.daily())
					.build();
		assertThat(configuration.getDomainId()).isEqualTo(UUID.fromString("e953d0ab-7053-4f84-b83a-abfe479d3888"));
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
	}
	
	@Test
	public void defaultValues() {
		UUID domainId = UUID.fromString("85bd08f7-d5a4-4b19-a37a-a738113e1d0a");
		ArchiveRecurrence archiveRecurrence = ArchiveRecurrence.builder()
			.dayOfMonth(DayOfMonth.last())
			.dayOfWeek(DayOfWeek.MONDAY)
			.dayOfYear(DayOfYear.of(1))
			.repeat(RepeatKind.MONTHLY)
			.build();
		
		DomainConfiguration configuration = DomainConfiguration.DEFAULT_VALUES_BUILDER.domainId(domainId).build();
		assertThat(configuration.isEnabled()).isFalse();
		assertThat(configuration.getRepeatKind()).isEqualTo(archiveRecurrence.getRepeatKind());
		assertThat(configuration.getDayOfMonth()).isEqualTo(archiveRecurrence.getDayOfMonth());
		assertThat(configuration.getDayOfWeek()).isEqualTo(archiveRecurrence.getDayOfWeek());
		assertThat(configuration.getDayOfYear()).isEqualTo(archiveRecurrence.getDayOfYear());
		assertThat(configuration.getDomainId()).isEqualTo(domainId);
		assertThat(configuration.getHour()).isEqualTo(0);
		assertThat(configuration.getMinute()).isEqualTo(0);
		assertThat(configuration.getRecurrence()).isEqualTo(archiveRecurrence);
	}
}
