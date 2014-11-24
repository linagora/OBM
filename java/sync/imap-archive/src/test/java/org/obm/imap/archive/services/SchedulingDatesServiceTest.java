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


package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.sync.date.DateProvider;


public class SchedulingDatesServiceTest {
	
	private SchedulingDatesService schedulingDatesService;
	
	@Before
	public void setup() {
		schedulingDatesService = new SchedulingDatesService(new DateProvider() {
			
			@Override
			public Date getDate() {
				return DateTime.parse("2014-06-18T16:01:00.000Z").toDate();
			}
		});
	}
	
	@Test
	public void nextTreatmentDateShouldSameDayWhenDailyRepeatKindAndLowerTime() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("22:58"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-18T22:58:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldNextDayWhenDailyRepeatKindAndSameTime() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("16:01"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-19T16:01:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeDayPlusOneWhenDailyRepeatKindAndHigherTime() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.build())
				.time(LocalTime.parse("12:15"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-19T12:15:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextTuesdayNextWeekWhenWeeklyRepeatKindAndOtherDay() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.TUESDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-24T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeSameWednesdayNextWeekWhenWeeklyRepeatKindAndSameDay() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.WEDNESDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-18T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextSaturdaySameWeekWhenWeeklyRepeatKindAndSameWeek() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.WEEKLY)
					.dayOfWeek(DayOfWeek.SATURDAY)
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-21T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNext10NextMonthWhenMonthlyRepeatKindAndDayOfMonthPassed() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.of(10))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-07-10T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNext20SameMonthWhenMonthlyRepeatKindAndDayOfMonthInTheFuture() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.of(20))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-20T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextDaySameMonthWhenMonthlyRepeatKindAndDayOfMonthInTheFuture() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.last())
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-30T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeEndOfJulyWhenMonthlyRepeatKindAnd30DaysMonth() {
		SchedulingDatesService schedulingDatesService = new SchedulingDatesService(new DateProvider() {
					
					@Override
					public Date getDate() {
						return DateTime.parse("2014-07-02T16:01:00.000Z").toDate();
					}
				});
		
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.last())
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-07-31T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeEndOfFebruaryWhenMonthlyRepeatKindAnd28DaysMonth() {
		SchedulingDatesService schedulingDatesService = new SchedulingDatesService(new DateProvider() {
					
					@Override
					public Date getDate() {
						return DateTime.parse("2014-02-02T16:01:00.000Z").toDate();
					}
				});
		
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.last())
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-02-28T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeSameMonthSameDayWhenMonthlyRepeatKindAndLowerTime() {
		SchedulingDatesService schedulingDatesService = new SchedulingDatesService(new DateProvider() {
					
					@Override
					public Date getDate() {
						return DateTime.parse("2014-06-30T16:01:00.000Z").toDate();
					}
				});
		
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.last())
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-06-30T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextMonthWhenMonthlyRepeatKindAndHigherTime() {
		SchedulingDatesService schedulingDatesService = new SchedulingDatesService(new DateProvider() {
					
					@Override
					public Date getDate() {
						return DateTime.parse("2014-06-30T16:01:00.000Z").toDate();
					}
				});
		
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.MONTHLY)
					.dayOfMonth(DayOfMonth.last())
					.build())
				.time(LocalTime.parse("10:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-07-31T10:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextFebruary10NextYearWhenYearlyRepeatKindAndPassedDay() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.YEARLY)
					.dayOfYear(DayOfYear.of(41))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2015-02-10T23:59:00.000Z"));
	}
	
	@Test
	public void nextTreatmentDateShouldBeNextJully31NextYearWhenYearlyRepeatKindAndDayInTheFuture() {
		SchedulingConfiguration build = SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.YEARLY)
					.dayOfYear(DayOfYear.of(212))
					.build())
				.time(LocalTime.parse("23:59"))
				.build();
		
		DateTime nextTreatmentDate = schedulingDatesService.nextTreatmentDate(build);
		assertThat(nextTreatmentDate).isEqualTo(DateTime.parse("2014-07-31T23:59:00.000Z"));
	}
	
	@Test
	public void higherBoundaryShouldBePreviousDayWhenDailyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-06-17T23:59:59.999Z"));
	}
	
	@Test
	public void higherBoundaryShouldBePreviousWeekWhenWeeklyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.WEEKLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-06-11T23:59:59.999Z"));
	}
	
	@Test
	public void higherBoundaryShouldBePreviousMonthWhenMonthlyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.MONTHLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-05-18T23:59:59.999Z"));
	}
	
	@Test
	public void higherBoundaryShouldBePreviousYearWhenYearlyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.YEARLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2013-06-18T23:59:59.999Z"));
	}
	
	@Test
	public void LowerBoundaryShouldBe2DaysBeforeWhenDailyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.lowerBoundary(treatmentDate, RepeatKind.DAILY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-06-16T00:00:00.000Z"));
	}
	
	@Test
	public void LowerBoundaryShouldBe2WeeksBeforeWhenWeeklyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.lowerBoundary(treatmentDate, RepeatKind.WEEKLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-06-04T00:00:00.000Z"));
	}
	
	@Test
	public void LowerBoundaryShouldBe2MonthBeforeWhenMonthlyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.lowerBoundary(treatmentDate, RepeatKind.MONTHLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2014-04-18T00:00:00.000Z"));
	}
	
	@Test
	public void LowerBoundaryShouldBe2YearBeforeWhenYearlyRepeatKind() {
		DateTime treatmentDate = DateTime.parse("2014-06-18T16:01:00.000Z");
		DateTime higherBoundary = schedulingDatesService.lowerBoundary(treatmentDate, RepeatKind.YEARLY);
		assertThat(higherBoundary).isEqualTo(DateTime.parse("2012-06-18T00:00:00.000Z"));
	}
}
