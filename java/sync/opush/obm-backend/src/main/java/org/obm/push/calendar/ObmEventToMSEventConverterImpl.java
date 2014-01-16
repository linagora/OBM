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
package org.obm.push.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.push.RecurrenceDayOfWeekConverter;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;


@Singleton
public class ObmEventToMSEventConverterImpl implements ObmEventToMSEventConverter {

	public static final AttendeeType RFC5545_DEFAULT_ATTENDEE_ROLE = AttendeeType.REQUIRED;
	
	@Override
	public MSEvent convert(Event e, MSEventUid uid, User user) {
		MSEvent mse = new MSEvent();

		fillEventCommonProperties(e, mse);
		
		mse.setTimeZone(TimeZone.getTimeZone(e.getTimezoneName()));
		appendAttendeesAndOrganizer(e, mse, user);
		mse.setUid(uid);
		mse.setRecurrence(getRecurrence(e));
		mse.setExceptions(getException(e));
		mse.setObmSequence(e.getSequence());
		appendDtStamp(mse, e);
		return mse;
	}

	private void fillEventCommonProperties(Event e, MSEventCommon mse) {
		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());
		mse.setLocation(e.getLocation());
		setStartTimeVersusAllDayProperty(e, mse);

		Date endtTime = endTime(mse.getStartTime().getTime(), e.getDuration());
		mse.setEndTime(endtTime);
		
		mse.setAllDayEvent(e.isAllday());

		mse.setReminder(reminder(e));
		mse.setBusyStatus(busyStatus(e.getOpacity()));
		mse.setSensitivity(sensitivity(e.getPrivacy()));

		mse.setCategories(category(e));
		mse.setMeetingStatus(CalendarMeetingStatus.IS_A_MEETING);
	}

	private void setStartTimeVersusAllDayProperty(Event event, MSEventCommon mse) {
		if(event.isAllday()) {
			DateTime startMidnight = new DateTime(event.getStartDate()).withTimeAtStartOfDay();
			mse.setStartTime(startMidnight.toDate());
		} else {
			mse.setStartTime(event.getStartDate());
		}
	}

	private Date endTime(long startTime, int duration) {
		if (duration >= 0) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.setTimeInMillis(startTime);
			c.add(Calendar.SECOND, duration);
			return c.getTime();
		}
		throw new IllegalArgumentException("Duration must be a positive value");
	}
	
	private List<String> category(Event e) {
		List<String> categories = Lists.newArrayList();
		if (e.getCategory() != null) {
			categories.add(e.getCategory());
		}
		return categories;
	}

	private Integer reminder(Event e) {
		Integer alert = e.getAlert();
		if (alert == null) {
			return null;
		} else {
			return alert / 60;
		}
	}

	private List<MSEventException> getException(Event event) {
		List<MSEventException> ret = Lists.newArrayList();
		if (!event.isRecurrent()) {
			return ret;
		}
		
		EventRecurrence recurrence = event.getRecurrence();
		for (Date excp : recurrence.getExceptions()) {
			MSEventException e = deletionException(event, excp);
			ret.add(e);
		}

		for (Event exception : recurrence.getEventExceptions()) {
			MSEventException e = convertException(exception);
			ret.add(e);
		}
		return ret;
	}

	@VisibleForTesting MSEventException deletionException(Event event, Date excp) {
		MSEventException e = new MSEventException();
		e.setDeleted(true);
		e.setExceptionStartTime(buildOccurrenceDateTime(event, excp).toDate());
		return e;
	}

	private DateTime buildOccurrenceDateTime(Event event, Date excp) {
		DateTimeZone eventTimeZone = DateTimeZone.forID(event.getTimezoneName());
		DateTime tzEventDataTime = new DateTime(event.getStartDate(), eventTimeZone);
		
		return new DateTime(excp, DateTimeZone.UTC)
				.withZone(eventTimeZone)
				.withMillisOfDay(tzEventDataTime.getMillisOfDay())
				.withZone(DateTimeZone.UTC);
	}

	private MSEventException convertException(Event exception) {
		MSEventException msEventException = new MSEventException();
		fillEventCommonProperties(exception, msEventException);
		msEventException.setExceptionStartTime(exception.getRecurrenceId());

		return msEventException;
	}
	
	private void appendAttendeesAndOrganizer(Event e, MSEvent mse, User user) {
		String userEmail = user.getEmail();
		boolean hasOrganizer = false;
		for (Attendee at: e.getAttendees()) {
			if (at.isOrganizer()) {
				hasOrganizer = true;
				appendOrganizer(mse, at);
			} 
			if (!hasOrganizer && userEmail.equalsIgnoreCase(at.getEmail())) {
				appendOrganizer(mse, at);
			}
			mse.addAttendee(convertAttendee(at));
		}
	}

	private void appendOrganizer(MSEvent mse, Attendee at) {
		mse.setOrganizerName(at.getDisplayName());
		mse.setOrganizerEmail(at.getEmail());		
	}

	private void appendDtStamp(MSEvent mse, Event e) {
		mse.setDtStamp(Objects.firstNonNull(e.getTimeUpdate(), new Date()));
	}

	@VisibleForTesting CalendarSensitivity sensitivity(EventPrivacy privacy) {
		Preconditions.checkNotNull(privacy);
		switch (privacy) {
		case PUBLIC:
			return CalendarSensitivity.NORMAL;
		case PRIVATE:
			return CalendarSensitivity.PRIVATE;
		case CONFIDENTIAL:
			return CalendarSensitivity.CONFIDENTIAL;
		}
		throw new IllegalArgumentException("EventPrivacy " + privacy + " can't be converted to MSEvent property");
	}
	
	@VisibleForTesting CalendarBusyStatus busyStatus(EventOpacity opacity) {
		Preconditions.checkNotNull(opacity);
		switch (opacity) {
		case TRANSPARENT:
			return CalendarBusyStatus.FREE;
		case OPAQUE:
			return CalendarBusyStatus.BUSY;
		}
		throw new IllegalArgumentException("EventOpacity " + opacity + " can't be converted to MSEvent property");
	}
	
	private MSAttendee convertAttendee(Attendee at) {
		MSAttendee msa = new MSAttendee();

		msa.setAttendeeStatus(status(at.getParticipation()));
		msa.setEmail(at.getEmail());
		msa.setName(at.getDisplayName());
		msa.setAttendeeType(participationRole(at.getParticipationRole()));

		return msa;
	}

	@VisibleForTesting AttendeeStatus status(Participation participation) {
		Preconditions.checkNotNull(participation);
		switch (participation.getState()) {
		case DECLINED:
			return AttendeeStatus.DECLINE;
		case NEEDSACTION:
			return AttendeeStatus.NOT_RESPONDED;
		case TENTATIVE:
			return AttendeeStatus.TENTATIVE;
		case ACCEPTED:
			return AttendeeStatus.ACCEPT;
		default:
		case COMPLETED:
		case DELEGATED:
		case INPROGRESS:
			return AttendeeStatus.RESPONSE_UNKNOWN;
		}
	}

	@VisibleForTesting AttendeeType participationRole(ParticipationRole role) {
		if (role == null) {
			return RFC5545_DEFAULT_ATTENDEE_ROLE; 
		}
		switch (role) {
		case REQ:
		case CHAIR:
			return AttendeeType.REQUIRED;
		case NON:
		case OPT:
			return AttendeeType.OPTIONAL;
		}
		throw new IllegalArgumentException("ParticipationRole " + role + " can't be converted to MSEvent property");
	}

	private MSRecurrence getRecurrence(Event event) {
		
		if (!event.isRecurrent()) {
			return null;
		}

		EventRecurrence recurrence = event.getRecurrence();
		MSRecurrence r = new MSRecurrence();
		switch (recurrence.getKind()) {
		case daily:
			r.setType(RecurrenceType.DAILY);
			break;
		case monthlybydate:
			r.setType(RecurrenceType.MONTHLY);
			break;
		case monthlybyday:
			r.setType(RecurrenceType.MONTHLY_NDAY);
			break;
		case weekly:
			r.setType(RecurrenceType.WEEKLY);
			r.setDayOfWeek(RecurrenceDayOfWeekConverter.fromRecurrenceDays(recurrence.getDays()));
			break;
		case yearly:
			r.setType(RecurrenceType.YEARLY);
			break;
		case yearlybyday:
			r.setType(RecurrenceType.YEARLY_NDAY);
			break;
		case none:
			r.setType(null);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(getInterval(recurrence));

		return r;
	}

	private int getInterval(EventRecurrence recurrence) {
		if (recurrence.frequencyIsSpecified()) {
			return recurrence.getFrequence();
		} else {
			return ACTIVESYNC_DEFAULT_FREQUENCY;
		}
	}

}
