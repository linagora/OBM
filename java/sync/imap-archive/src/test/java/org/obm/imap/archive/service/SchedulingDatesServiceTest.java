/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.sync.date.DateProvider;


public class SchedulingDatesServiceTest {
	
	private SchedulingDatesService schedulingDatesService;
	
	@Before
	public void setup() {
		schedulingDatesService = new SchedulingDatesService(new DateProvider() {
			
			@Override
			public Date getDate() {
				return new DateTime(2014, 6, 18, 16, 1).toDate();
			}
		});
	}
	
	@Test
	public void nextTreatmentDateShouldSameDayWhenDailyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("22:58"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 18, 22, 58));
	}
	
	@Test
	public void nextTreatmentDateShouldNextDayWhenDailyRepeatKindSameTime() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("16:01"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 19, 16, 1));
	}
	
	@Test
	public void nextTreatmentDateShouldBeDayPlusOneWhenDailyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("12:15"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 19, 12, 15));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextTuesdayNextWeekWhenWeeklyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.TUESDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 24, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeSameWednesdayNextWeekWhenWeeklyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.WEDNESDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 18, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextSaturdaySameWeekWhenWeeklyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.SATURDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 21, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNext10NextMonthWhenMonthlyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.of(10))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 7, 10, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNext20SameMonthWhenMonthlyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.of(20))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 20, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextDaySameMonthWhenMonthlyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.of(-1))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 6, 30, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextFebruary10NextYearWhenYearlyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.YEARLY)
					.dayOfYear(DayOfYear.of(41))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2015, 2, 10, 23, 59));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextJully31NextYearWhenYearlyRepeatKind() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.YEARLY)
					.dayOfYear(DayOfYear.of(212))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(new DateTime(2014, 7, 31, 23, 59));
	}
}
