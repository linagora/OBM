/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.mail;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.property.Clazz;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.obm.icalendar.ICalendar;
import org.obm.icalendar.ical4jwrapper.ICalendarEvent;
import org.obm.icalendar.ical4jwrapper.ICalendarMethod;
import org.obm.icalendar.ical4jwrapper.ICalendarRecur;
import org.obm.icalendar.ical4jwrapper.ICalendarTimeZone;
import org.obm.push.bean.MSEventExtId;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest.Builder;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestIntDBusyStatus;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceDayOfWeek;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestSensitivity;
import org.obm.push.mail.bean.Address;
import org.obm.push.utils.DateUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class ICalendarConverter {

	private static final int JAVA_TO_MS_API_MONTH_OFFSET = 1;
	
	private static final String X_OBM_ALL_DAY_ENABLED = "1";
	private static final String X_OBM_ALL_DAY = "X-OBM-ALL-DAY";
	private static final String X_MICROSOFT_CDO_INTENDEDSTATUS = "X-MICROSOFT-CDO-INTENDEDSTATUS";
	private static final Map<String, MSMeetingRequestRecurrenceDayOfWeek> RECUR_DAY_LIST = 
				new ImmutableMap.Builder<String, MSMeetingRequestRecurrenceDayOfWeek>()
			.put(WeekDay.SU.getDay(), MSMeetingRequestRecurrenceDayOfWeek.SUNDAY)
			.put(WeekDay.MO.getDay(), MSMeetingRequestRecurrenceDayOfWeek.MONDAY)
			.put(WeekDay.TU.getDay(), MSMeetingRequestRecurrenceDayOfWeek.TUESDAY)
			.put(WeekDay.WE.getDay(), MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY)
			.put(WeekDay.TH.getDay(), MSMeetingRequestRecurrenceDayOfWeek.THURSDAY)
			.put(WeekDay.FR.getDay(), MSMeetingRequestRecurrenceDayOfWeek.FRIDAY)
			.put(WeekDay.SA.getDay(), MSMeetingRequestRecurrenceDayOfWeek.SATURDAY).build();
	
	private static final Map<Clazz, MSMeetingRequestSensitivity> VEVENT_CLAZZ = 
			new ImmutableMap.Builder<Clazz, MSMeetingRequestSensitivity>()
		.put(Clazz.CONFIDENTIAL, MSMeetingRequestSensitivity.CONFIDENTIAL)
		.put(Clazz.PRIVATE, MSMeetingRequestSensitivity.PRIVATE)
		.put(Clazz.PUBLIC, MSMeetingRequestSensitivity.NORMAL).build();

	private static final Map<String, MSMeetingRequestRecurrenceType> RECUR_FREQUENCY = 
			new ImmutableMap.Builder<String, MSMeetingRequestRecurrenceType>()
		.put(Recur.DAILY, MSMeetingRequestRecurrenceType.DAILY)
		.put(Recur.WEEKLY, MSMeetingRequestRecurrenceType.WEEKLY)
		.put(Recur.MONTHLY, MSMeetingRequestRecurrenceType.MONTHLY)
		.put(Recur.YEARLY, MSMeetingRequestRecurrenceType.YEARLY).build();
	
	public MSMeetingRequest convertToMSMeetingRequest(ICalendar icalendar) {
		Preconditions.checkNotNull(icalendar, "ICalendar is null");
		
		Builder builder = MSMeetingRequest.builder();
		if (icalendar.hasEvent()) {
			ICalendarEvent iCalendarEvent = icalendar.getICalendarEvent();
			ICalendarMethod method = icalendar.getICalendarMethod();
			
			TimeZone timeZone = getTimeZone(icalendar.getICalendarTimeZone());
			fillMsMeetingRequestFromVEvent(iCalendarEvent, method, builder);
			
			if (iCalendarEvent.hasRecur()) {
				ICalendarRecur iCalendarRule = iCalendarEvent.recur();
				builder.recurrenceId(recurrenceId(iCalendarEvent));
				builder.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING);
				fillMsMeetingRequestFromRRule(iCalendarRule, iCalendarEvent, timeZone, builder);
			} else if (iCalendarEvent.recurrenceId() != null) {
				//FIXME OBMFULL-3610: MeetingResponse doesn't support accepting
				//Event Exception
				return null;
			}
			
			builder.timeZone(timeZone);
			return builder.build();
		}
		return null;
	}

	private TimeZone getTimeZone(ICalendarTimeZone iCalendarTimeZone) {
		if (iCalendarTimeZone != null) {
			return iCalendarTimeZone.getTimeZone();
		}
		return null;
	}
	
	private void fillMsMeetingRequestFromVEvent(ICalendarEvent iCalendarEvent, 
			ICalendarMethod method, Builder msMeetingRequestBuilder) {
		
		Date startDate = iCalendarEvent.startDate();
		Date endDate = endTime(iCalendarEvent);
		msMeetingRequestBuilder
			.startTime(startDate)
			.endTime(endDate)
			.allDayEvent(isAllDay(iCalendarEvent, startDate, endDate))
			.dtStamp(iCalendarEvent.dtStamp())
			.instanceType(MSMeetingRequestInstanceType.SINGLE)
			.location(iCalendarEvent.location())
			.organizer(iCalendarEvent.organizer())
			.reminder(reminder(iCalendarEvent))
			.responseRequested(responseRequested(method))
			.sensitivity(sensitivity(iCalendarEvent))
			.intDBusyStatus(transparency(iCalendarEvent))
			.msEventExtId(extId(iCalendarEvent.uid()));
	}
	
	@VisibleForTesting String organizer(String organizer, List<Address> fromList) {
		Address from = Iterables.getFirst(fromList, null);
		if (from != null) {
			return Objects.firstNonNull(Strings.emptyToNull(organizer), from.getMail());
		}
		return organizer;
	}

	@VisibleForTesting boolean responseRequested(ICalendarMethod method) {
		if (method != null) {
			return method == ICalendarMethod.REQUEST;
		}
		return true;
	}

	private Date endTime(ICalendarEvent iCalendarEvent) {
		Date endDate = iCalendarEvent.endDate();
		if (endDate == null 
				&& hasXObmAllDayProperty(iCalendarEvent)) {
			
			return new DateTime(iCalendarEvent.startDate().getTime()).plusDays(1).toDate();
		}
		return endDate;
	}

	private MSEventExtId extId(String uid) {
		if (Strings.isNullOrEmpty(uid)) {
			return new MSEventExtId(MSEventExtId.generateUid().toString());
		} else {
			return new MSEventExtId(uid);
		}
	}

	private Date recurrenceId(ICalendarEvent iCalendarEvent) {
		Date recurrenceId = iCalendarEvent.recurrenceId();
		if (recurrenceId == null) {
			return iCalendarEvent.startDate();
		}
		return recurrenceId;
	}
	
	private boolean isAllDay(ICalendarEvent iCalendarEvent, Date startDate, Date endDate) {
		if (startDate != null && endDate != null) {
			DateTime plusDays = new DateTime(startDate).plusDays(1);
			return plusDays.toDate().getTime() == endDate.getTime();
		} else {
			return hasXObmAllDayProperty(iCalendarEvent);
		}
	}

	private boolean hasXObmAllDayProperty(ICalendarEvent iCalendarEvent) {
		return X_OBM_ALL_DAY_ENABLED.equals(iCalendarEvent.property(X_OBM_ALL_DAY));
	}

	@VisibleForTesting Long reminder(ICalendarEvent iCalendarEvent) {
		Long firstAlarmInSeconds = iCalendarEvent.firstAlarmInSeconds();
		if (firstAlarmInSeconds != null && firstAlarmInSeconds <= 0) {
			return Duration.standardSeconds(Math.abs(firstAlarmInSeconds)).getStandardMinutes();
		}
		return null;
	}
	
	private MSMeetingRequestSensitivity sensitivity(ICalendarEvent iCalendarEvent) {
		Clazz clazz = iCalendarEvent.classification();
		if (clazz != null) {
			return VEVENT_CLAZZ.get(clazz);		
		} else {
			return MSMeetingRequestSensitivity.NORMAL;
		}
	}
	
	private MSMeetingRequestIntDBusyStatus transparency(ICalendarEvent iCalendarEvent) {
		String transparency = iCalendarEvent.transparency();
		if (transparency != null) {
			if (transparency.equalsIgnoreCase("OPAQUE") ) {
				return specialOpaqueValue(iCalendarEvent);	
			} else {
				return MSMeetingRequestIntDBusyStatus.FREE;
			}
		}
		return null;
	}

	private MSMeetingRequestIntDBusyStatus specialOpaqueValue(ICalendarEvent iCalendarEvent) {
		String value = iCalendarEvent.property(X_MICROSOFT_CDO_INTENDEDSTATUS);
		if (value != null) {
			if (value.equalsIgnoreCase("OOF")) {
				return MSMeetingRequestIntDBusyStatus.OUT_OF_OFFICE;
			}
			if (value.equalsIgnoreCase("TENTATIVE")) {
				return MSMeetingRequestIntDBusyStatus.TENTATIVE;
			}
		}
		return MSMeetingRequestIntDBusyStatus.BUSY;
	}
	
	private void fillMsMeetingRequestFromRRule(ICalendarRecur iCalendarRule, ICalendarEvent iCalendarEvent,
			TimeZone iCalendarTimeZone, Builder msMeetingRequestBuilder) {
		
		List<MSMeetingRequestRecurrence> meetingRequestRecurrences = Lists.newArrayList();
		
		MSMeetingRequestRecurrenceType frequency = frequency(iCalendarRule);
		List<MSMeetingRequestRecurrenceDayOfWeek> dayList = dayList(iCalendarRule);
		MSMeetingRequestRecurrenceType type = type(frequency, dayList);
		Integer dayOfMonth = dayOfMonth(iCalendarRule, frequency);
		
		meetingRequestRecurrences.add(
				MSMeetingRequestRecurrence.builder()
				.interval(iCalendarRule.interval())
				.until(iCalendarRule.until())
				.dayOfWeek(dayList)
				.type(type)
				.dayOfMonth(dayOfMonth)
				.monthOfYear(monthOfYear(type, iCalendarRule, iCalendarEvent, iCalendarTimeZone))
				.weekOfMonth(iCalendarRule.bySetPos())
				.occurrences(iCalendarRule.count())
				.build());
		msMeetingRequestBuilder.recurrences(meetingRequestRecurrences);
	}

	private Integer monthOfYear(MSMeetingRequestRecurrenceType type, ICalendarRecur iCalendarRule,
			ICalendarEvent iCalendarEvent, TimeZone iCalendarTimeZone) {

		if (type.isYearly()) {
			return Objects.firstNonNull(
					iCalendarRule.byMonth(),
					retrieveMonthFromStartTime(iCalendarEvent, iCalendarTimeZone));
		} else {
			return iCalendarRule.byMonth();
		}
	}

	@VisibleForTesting int retrieveMonthFromStartTime(ICalendarEvent iCalendarEvent, TimeZone iCalendarTimeZone) {
		Calendar calendar = Calendar.getInstance(Objects.firstNonNull(iCalendarTimeZone, DateUtils.getGMTTimeZone()));
		calendar.setTime(iCalendarEvent.startDate());
		return calendar.get(Calendar.MONTH) + JAVA_TO_MS_API_MONTH_OFFSET;
	}

	private MSMeetingRequestRecurrenceType type(MSMeetingRequestRecurrenceType frequency,
			List<MSMeetingRequestRecurrenceDayOfWeek> dayList) {

		boolean isByDay = dayList != null && !dayList.isEmpty();
		if (frequency == MSMeetingRequestRecurrenceType.MONTHLY && isByDay) {
			return MSMeetingRequestRecurrenceType.MONTHLY_NTH_DAY;
		}
		if (frequency == MSMeetingRequestRecurrenceType.YEARLY && isByDay) {
			return MSMeetingRequestRecurrenceType.YEARLY_NTH_DAY;
		}
		return frequency;
	}

	private Integer dayOfMonth(ICalendarRecur iCalendarRule, MSMeetingRequestRecurrenceType frequency) {
		Integer byMonthDay = iCalendarRule.byMonthDay();
		if (byMonthDay == null && (frequency == MSMeetingRequestRecurrenceType.MONTHLY 
				|| frequency == MSMeetingRequestRecurrenceType.YEARLY)) {
			
			Collection<WeekDay> dayList = iCalendarRule.dayList();
			if (dayList != null && !dayList.isEmpty() && dayList.size() == 1) {
				return Iterables.getOnlyElement(dayList).getOffset();
			}
		}
		return byMonthDay;
	}

	private MSMeetingRequestRecurrenceType frequency(ICalendarRecur iCalendarRule) {
		String frequency = iCalendarRule.frequency();
		return RECUR_FREQUENCY.get(frequency);
	}
	
	private List<MSMeetingRequestRecurrenceDayOfWeek> dayList(ICalendarRecur iCalendarRule) {
		List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeeks = Lists.newArrayList();
		Collection<WeekDay> dayList = iCalendarRule.dayList();
		if (dayList != null && !dayList.isEmpty()) {
			for (WeekDay weekDay: dayList) {
				dayOfWeeks.add(RECUR_DAY_LIST.get(weekDay.getDay()));
			}
		}
		return dayOfWeeks;
	}
}
