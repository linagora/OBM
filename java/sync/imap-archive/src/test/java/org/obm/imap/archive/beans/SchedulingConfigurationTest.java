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


public class SchedulingConfigurationTest {

	@Test(expected=IllegalArgumentException.class)
	public void builderShouldThrowWhenTimeHasSeconds() {
		SchedulingConfiguration.builder().time(LocalTime.parse("12:22:23")).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderShouldThrowWhenTimeHasMillis() {
		SchedulingConfiguration.builder().time(LocalTime.parse("12:22:00.552")).build();
	}

	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRepeatKindIsNotProvided() {
		SchedulingConfiguration.builder().time(LocalTime.parse("10:22")).build();
	}
	
	@Test
	public void builderShouldBuildConfigurationWhenRequiredFieldsAreProvided() {
		SchedulingConfiguration configuration = 
				SchedulingConfiguration.builder()
					.time(LocalTime.parse("13:23"))
					.recurrence(ArchiveRecurrence.daily())
					.build();
		assertThat(configuration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(configuration.getHour()).isEqualTo(13);
		assertThat(configuration.getMinute()).isEqualTo(23);
	}
	
	@Test
	public void defaultValues() {
		ArchiveRecurrence archiveRecurrence = ArchiveRecurrence.builder()
			.dayOfMonth(DayOfMonth.last())
			.dayOfWeek(DayOfWeek.MONDAY)
			.dayOfYear(DayOfYear.of(1))
			.repeat(RepeatKind.MONTHLY)
			.build();
		
		SchedulingConfiguration configuration = SchedulingConfiguration.DEFAULT_VALUES_BUILDER;
		assertThat(configuration.getRepeatKind()).isEqualTo(archiveRecurrence.getRepeatKind());
		assertThat(configuration.getDayOfMonth()).isEqualTo(archiveRecurrence.getDayOfMonth());
		assertThat(configuration.getDayOfWeek()).isEqualTo(archiveRecurrence.getDayOfWeek());
		assertThat(configuration.getDayOfYear()).isEqualTo(archiveRecurrence.getDayOfYear());
		assertThat(configuration.getHour()).isEqualTo(0);
		assertThat(configuration.getMinute()).isEqualTo(0);
		assertThat(configuration.getRecurrence()).isEqualTo(archiveRecurrence);
	}
}
