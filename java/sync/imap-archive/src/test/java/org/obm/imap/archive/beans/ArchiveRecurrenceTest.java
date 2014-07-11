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

import org.junit.Test;


public class ArchiveRecurrenceTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenRepeatKindIsNull() {
		ArchiveRecurrence.builder().repeat(null);
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenNoRepeatKind() {
		ArchiveRecurrence.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRepeatKindWeeklyButNoDayOfWeek() {
		ArchiveRecurrence.builder().repeat(RepeatKind.WEEKLY).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRepeatKindMonthlyButNoDayOfMonth() {
		ArchiveRecurrence.builder().repeat(RepeatKind.MONTHLY).build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRepeatKindYearlyButNoDayOfYear() {
		ArchiveRecurrence.builder().repeat(RepeatKind.YEARLY).build();
	}
	
	@Test
	public void builderShouldBuildObjectWhenRepeatKindDaily() {
		ArchiveRecurrence testee = ArchiveRecurrence.builder().repeat(RepeatKind.DAILY).build();
		assertThat(testee.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
	}
	
	@Test
	public void builderShouldBuildObjectWhenRepeatKindWeeklyAndDayOfWeek() {
		ArchiveRecurrence testee = ArchiveRecurrence.builder().repeat(RepeatKind.WEEKLY).dayOfWeek(DayOfWeek.FRIDAY).build();
		assertThat(testee.getRepeatKind()).isEqualTo(RepeatKind.WEEKLY);
		assertThat(testee.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
	}
	
	@Test
	public void builderShouldBuildObjectWhenRepeatKindMonthlyAndDayOfMonth() {
		ArchiveRecurrence testee = ArchiveRecurrence.builder().repeat(RepeatKind.MONTHLY).dayOfMonth(DayOfMonth.last()).build();
		assertThat(testee.getRepeatKind()).isEqualTo(RepeatKind.MONTHLY);
		assertThat(testee.getDayOfMonth()).isEqualTo(DayOfMonth.last());
	}
	
	@Test
	public void builderShouldBuildObjectWhenRepeatKindYearlyAndDayOfYear() {
		ArchiveRecurrence testee = ArchiveRecurrence.builder().repeat(RepeatKind.YEARLY).dayOfYear(DayOfYear.of(55)).build();
		assertThat(testee.getRepeatKind()).isEqualTo(RepeatKind.YEARLY);
		assertThat(testee.getDayOfYear()).isEqualTo(DayOfYear.of(55));
	}
	
	@Test
	public void builderShouldBuildObjectWhenUnusedFields() {
		ArchiveRecurrence testee = ArchiveRecurrence.builder().repeat(RepeatKind.DAILY)
				.dayOfYear(DayOfYear.of(55))
				.dayOfMonth(DayOfMonth.last())
				.dayOfWeek(DayOfWeek.MONDAY)
				.build();
		assertThat(testee.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(testee.getDayOfYear()).isEqualTo(DayOfYear.of(55));
		assertThat(testee.getDayOfMonth()).isEqualTo(DayOfMonth.last());
		assertThat(testee.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
	}
}
