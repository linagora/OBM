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

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.sync.date.DateProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

@Singleton
public class SchedulingDatesService {

	private final DateProvider dateProvider;

	@Inject
	@VisibleForTesting SchedulingDatesService(DateProvider dateProvider) {
		this.dateProvider = dateProvider;
	}
	
	public DateTime nextTreatmentDate(SchedulingConfiguration schedulingConfiguration) {
		DateTime currentDateTime = new DateTime(dateProvider.getDate());
		DateTime currentDateWithScheduledTime = currentDateTime
				.withZone(DateTimeZone.UTC)
				.withHourOfDay(schedulingConfiguration.getHour())
				.withMinuteOfHour(schedulingConfiguration.getMinute())
				.withSecondOfMinute(0)
				.withMillisOfSecond(0);

		switch (schedulingConfiguration.getRepeatKind()) {
		case DAILY:
			return dailyNextTreatmentDate(currentDateTime, currentDateWithScheduledTime);
			
		case WEEKLY:
			return weeklyNextTreatmentDate(schedulingConfiguration, currentDateTime, currentDateWithScheduledTime);

		case MONTHLY:
			return monthlyNextTreatmentDate(schedulingConfiguration, currentDateTime, currentDateWithScheduledTime);
			
		case YEARLY:
			return yearlyNextTreatmentDate(schedulingConfiguration, currentDateTime, currentDateWithScheduledTime);
		
		default:
			throw new IllegalArgumentException("Unknown repeat kind: " + schedulingConfiguration.getRepeatKind());
		}
	}

	private DateTime dailyNextTreatmentDate(DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		if (currentDateWithScheduledTime.isAfter(currentDateTime)) {
			return currentDateWithScheduledTime;
		}
		return currentDateWithScheduledTime
				.plusDays(1);
	}

	private DateTime weeklyNextTreatmentDate(SchedulingConfiguration schedulingConfiguration, DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		DateTime dayOfWeek = currentDateWithScheduledTime
				.withDayOfWeek(schedulingConfiguration.getDayOfWeek().getSpecificationValue());
		if (dayOfWeek.isAfter(currentDateTime)) {
			return dayOfWeek;
		}
		return dayOfWeek
				.plusWeeks(1);
	}

	private DateTime monthlyNextTreatmentDate(SchedulingConfiguration schedulingConfiguration, DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		if (schedulingConfiguration.isLastDayOfMonth()) {
			return nextTreatmentDateOnLastDayOfMonth(currentDateTime, currentDateWithScheduledTime);
		}
		
		return nextTreatmentDateCommonDayOfMonth(schedulingConfiguration, currentDateTime, currentDateWithScheduledTime);
	}

	private DateTime nextTreatmentDateOnLastDayOfMonth(DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		DateTime dayOfMonth = lastDayOfMonth(currentDateWithScheduledTime);
		
		if (dayOfMonth.isAfter(currentDateTime)) {
			return dayOfMonth;
		}
		// currentDateTime is at the end of the month, but on a higher time -> next month 
		return lastDayOfMonth(currentDateWithScheduledTime.plusMonths(1));
	}

	private DateTime lastDayOfMonth(DateTime currentDateWithScheduledTime) {
		return currentDateWithScheduledTime
			.plusMonths(1)
			.withDayOfMonth(1)
			.minusDays(1);
	}

	private DateTime nextTreatmentDateCommonDayOfMonth(SchedulingConfiguration schedulingConfiguration,DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		DateTime dayOfMonth = currentDateWithScheduledTime
			.withDayOfMonth(schedulingConfiguration.getDayOfMonth().getDayIndex());
		
		if (dayOfMonth.isAfter(currentDateTime)) {
			return dayOfMonth;
		}
		return dayOfMonth
				.plusMonths(1);
	}

	private DateTime yearlyNextTreatmentDate(SchedulingConfiguration schedulingConfiguration, DateTime currentDateTime, DateTime currentDateWithScheduledTime) {
		DateTime dayOfYear = currentDateWithScheduledTime
			.withDayOfYear(schedulingConfiguration.getDayOfYear().getDayOfYear());
		if (dayOfYear.isAfter(currentDateTime)) {
			return dayOfYear;
		}
		return dayOfYear
				.plusYears(1);
	}
	
	public DateTime higherBoundary(DateTime treatmentDate, RepeatKind repeatKind) {
		return treatmentDate.minus(RepeatKind.toPeriod(repeatKind, 1))
				.withTime(23, 59, 59, 999);
	}
	
	public DateTime lowerBoundary(DateTime treatmentDate, RepeatKind repeatKind) {
		return treatmentDate.minus(RepeatKind.toPeriod(repeatKind, 2))
				.withTime(0, 0, 0, 0);
	}
}
