/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeekUtils;
import org.obm.push.calendar.EventConverter;
import org.obm.push.exception.IllegalMSEventExceptionStateException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.exception.IllegalMSEventRecurrenceException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;

@Singleton
public class MSEventToObmEventConverter {

	private static final int EVENT_ALLDAY_DURATION_IN_MS = 24 * 3600;
	private static final int EVENT_CATEGORIES_MAX = 300;

	public Event convert(BackendSession bs, Event oldEvent, MSEvent event, Boolean isObmInternalEvent) 
			throws org.obm.push.exception.IllegalMSEventStateException {
		EventExtId extId = event.getExtId();
		EventObmId obmId = event.getObmId();
		
		Event e = convertEventOne(bs, oldEvent, null, event, isObmInternalEvent);
		e.setExtId(extId);
		e.setUid(obmId);
		
		if(event.getObmSequence() != null){
			e.setSequence(event.getObmSequence());
		}
		
		if (event.getRecurrence() != null) {
			EventRecurrence r = getRecurrence(event);
			e.setRecurrence(r);
			if (event.getExceptions() != null && !event.getExceptions().isEmpty()) {
				for (MSEventException excep : event.getExceptions()) {
					assertExceptionValidity(excep);
					if (excep.isDeleted()) {
						r.addException(excep.getExceptionStartTime());
					} else {
						Event obmEvent = convertEventException(bs, oldEvent, e, excep, isObmInternalEvent);
						obmEvent.setExtId(extId);
						obmEvent.setUid(obmId);
						
						r.addEventException(obmEvent);
					}
				}
			}
		}

		return e;
	}

	private void fillEventCommonProperties(BackendSession bs, Event converted, Event oldEvent, Event parentEvent, 
			MSEventCommon data, boolean isObmInternalEvent) throws org.obm.push.exception.IllegalMSEventStateException {
		
		defineOwner(bs, converted, oldEvent);
		converted.setInternalEvent(isObmInternalEvent);
		converted.setType(EventType.VEVENT);
		
		convertSubject(parentEvent, data, converted);
		
		if (parentEvent != null && !Strings.isNullOrEmpty(parentEvent.getDescription())) {
			converted.setDescription(parentEvent.getDescription());
		} else {
			converted.setDescription(data.getDescription());
		}
		
		convertLocation(parentEvent, data, converted);
		converted.setStartDate(data.getStartTime());
		convertDtStamp(data, converted, oldEvent);
		
		convertDurationAttribute(data, converted);
		convertAllDayAttribute(parentEvent, data, converted);
		convertCategories(parentEvent, data, converted);
		convertMeetingStatus(data, converted);

		convertReminder(parentEvent, data, converted);

		convertBusyStatus(parentEvent, data, converted);

		if (data.getSensitivity() == null && parentEvent != null) {
			converted.setPrivacy(parentEvent.getPrivacy());
		} else {
			converted.setPrivacy(privacy(oldEvent, data.getSensitivity()));
		}
		
	}
	
	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertEventOne(BackendSession bs, Event oldEvent, Event parentEvent, MSEvent data, boolean isObmInternalEvent) 
			throws org.obm.push.exception.IllegalMSEventStateException {
		
		Event e = new Event();
		fillEventCommonProperties(bs, e, oldEvent, null, data, isObmInternalEvent);
		e.setAttendees( getAttendees(oldEvent, parentEvent, data) );
		defineOrganizer(e, data, bs);
		convertTimeZone(data, e);
		return e;
	}

	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertEventException(BackendSession bs, Event oldEvent, Event parentEvent, 
			MSEventException data, boolean isObmInternalEvent) throws org.obm.push.exception.IllegalMSEventStateException {
		
		Event e = new Event();
		fillEventCommonProperties(bs, e, oldEvent, parentEvent, data, isObmInternalEvent);
		e.setRecurrenceId(data.getExceptionStartTime());
		return e;
	}

	private void defineOwner(BackendSession bs, Event e, Event oldEvent) {
		if (oldEvent != null) {
			e.setOwnerEmail(oldEvent.getOwnerEmail());
		} else{
			e.setOwnerEmail(bs.getCredentials().getUser().getEmail());
		}
	}

	private ParticipationState getAttendeeState(Event oldEvent, MSAttendee at) {
		if (oldEvent != null) {
			Attendee attendee = oldEvent.findAttendeeFromEmail(at.getEmail());
			if (attendee != null) {
				return attendee.getState();
			}
		}
		return ParticipationState.NEEDSACTION;
	}

	private boolean isOrganizer(MSEvent event, MSAttendee at) {
		if(at.getEmail() != null  && at.getEmail().equals(event.getOrganizerEmail())){
			return true;
		} else if(at.getName() != null  && at.getName().equals(event.getOrganizerName())){
			return true;
		}
		return false;
	}
	
	private Attendee getOrganizer(String email, String displayName) {
		Attendee att = new Attendee();
		att.setEmail(email);
		att.setDisplayName(displayName);
		att.setState(ParticipationState.ACCEPTED);
		att.setParticipationRole(ParticipationRole.REQ);
		att.setOrganizer(true);
		return att;
	}	
	
	private EventPrivacy privacy(Event oldEvent, CalendarSensitivity sensitivity) {
		if (sensitivity == null) {
			return oldEvent != null ? oldEvent.getPrivacy() : EventPrivacy.PUBLIC;
		}

		if (sensitivity == CalendarSensitivity.NORMAL) {
			return EventPrivacy.PUBLIC;
		} else {
			return EventPrivacy.PRIVATE;
		}
	}

	private void convertLocation(Event parentEvent, MSEventCommon data, Event converted) {
		if (!Strings.isNullOrEmpty(data.getLocation())) {
			converted.setLocation(data.getLocation());
		} else if (parentEvent != null) {
			converted.setLocation(parentEvent.getLocation());
		}
	}

	private void convertReminder(Event parentEvent, MSEventCommon data, Event converted) {
		if (data.getReminder() != null) {
			converted.setAlert(data.getReminder() * 60);
		} else if (parentEvent != null) {
			converted.setAlert(parentEvent.getAlert());
		}
	}

	private void convertSubject(Event parentEvent, MSEventCommon data, Event converted) throws IllegalMSEventStateException {
		if (!Strings.isNullOrEmpty(data.getSubject())) {
			converted.setTitle(data.getSubject());
		} else if (parentEvent != null && !Strings.isNullOrEmpty(parentEvent.getTitle())) {
			converted.setTitle(parentEvent.getTitle());
		} else {
			throw new IllegalMSEventStateException("Subject is required");
		}
	}

	private void convertMeetingStatus(MSEventCommon data, Event converted) {
		if (data.getMeetingStatus() == null) {
			converted.setMeetingStatus(null);
		} else {
			converted.setMeetingStatus(getMeetingStatus(data.getMeetingStatus()));
		}
	}

	private EventMeetingStatus getMeetingStatus(CalendarMeetingStatus meetingStatus) {
		switch (meetingStatus) {
		case IS_A_MEETING:
			return EventMeetingStatus.IS_A_MEETING;
		case IS_NOT_A_MEETING:
			return EventMeetingStatus.IS_NOT_A_MEETING;
		case MEETING_IS_CANCELED:
			return EventMeetingStatus.MEETING_IS_CANCELED;
		case MEETING_IS_CANCELED_AND_RECEIVED:
			return EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED;
		case MEETING_RECEIVED:
			return EventMeetingStatus.MEETING_RECEIVED;
		default:
			return EventMeetingStatus.IS_A_MEETING;
		}
	}

	private void convertDtStamp(MSEventCommon data, Event converted, Event oldEvent) {
		converted.setTimeUpdate(data.getDtStamp());
		if (oldEvent != null) {
			converted.setTimeCreate(oldEvent.getTimeCreate());
		}
		if (converted.getTimeCreate()==null) {
			converted.setTimeCreate(data.getDtStamp());
		}
	}

	private EventRecurrence getRecurrence(MSEvent msev) throws IllegalMSEventRecurrenceException {
		Date startDate = msev.getStartTime();
		MSRecurrence pr = msev.getRecurrence();
		EventRecurrence or = new EventRecurrence();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		int multiply = 0;
		switch (pr.getType()) {
		case DAILY:
			or.setKind(RecurrenceKind.daily);
			or.setDays(RecurrenceDayOfWeekUtils.toRecurrenceDays(pr.getDayOfWeek()));
			multiply = Calendar.DAY_OF_MONTH;
			break;
		case MONTHLY:
			or.setKind(RecurrenceKind.monthlybydate);
			multiply = Calendar.MONTH;
			break;
		case MONTHLY_NDAY:
			or.setKind(RecurrenceKind.monthlybyday);
			multiply = Calendar.MONTH;
			break;
		case WEEKLY:
			or.setKind(RecurrenceKind.weekly);
			or.setDays(RecurrenceDayOfWeekUtils.toRecurrenceDays(pr.getDayOfWeek()));
			multiply = Calendar.WEEK_OF_YEAR;
			break;
		case YEARLY:
			if (pr.getDayOfMonth() == null || pr.getMonthOfYear() == null) {
				throw new IllegalMSEventRecurrenceException("Yearly recurrence need DayOfMonth and MonthOfYear attributes");
			}
			or.setKind(RecurrenceKind.yearly);
			cal.setTimeInMillis(startDate.getTime());
			cal.set(Calendar.DAY_OF_MONTH, pr.getDayOfMonth());
			cal.set(Calendar.MONTH, pr.getMonthOfYear() - 1);
			msev.setStartTime(cal.getTime());
			startDate = msev.getStartTime();
			multiply = Calendar.YEAR;
			break;
		case YEARLY_NDAY:
			or.setKind(RecurrenceKind.yearlybyday);
			multiply = Calendar.YEAR;
			break;
		}

		// interval
		convertRecurrenceInterval(pr, or);

		// occurence or end date
		Date endDate = null;
		boolean hasOccurences = pr.hasOccurences();
		if (hasOccurences && pr.getUntil() != null) {
			throw new IllegalMSEventRecurrenceException("Recurrence cannot has Until AND Occurences");
		} else if (hasOccurences) {
			cal.setTimeInMillis(startDate.getTime());
			cal.add(multiply, pr.getOccurrences() - 1);
			endDate = new Date(cal.getTimeInMillis());
		} else {
			endDate = pr.getUntil();
		}
		or.setEnd(endDate);

		return or;
	}
	
	private void convertRecurrenceInterval(MSRecurrence from, EventRecurrence to) throws IllegalMSEventRecurrenceException {
		Integer interval = from.getInterval();
		from.getType().validIntervalOrException(interval);
		to.setFrequence(interval);
	}

	
	private List<Attendee> getAttendees(Event oldEvent, Event parentEvent, MSEvent data)
			throws IllegalMSEventExceptionStateException {
		List<Attendee> ret = new LinkedList<Attendee>();
		if (parentEvent != null && data.getAttendees().isEmpty()) {
			// copy parent attendees. CalendarBackend ensured parentEvent has attendees.
			ret.addAll(parentEvent.getAttendees());
		} else {
			for (MSAttendee at: data.getAttendees()) {
				ret.add( convertAttendee(oldEvent, data, at) );
			}
		}
		return ret;
	}
	
	private void defineOrganizer(Event e, MSEvent data, BackendSession bs) {
		if (e.findOrganizer() == null) {
			if (data.getOrganizerEmail() != null) {
				Attendee attendee = getOrganizer(data.getOrganizerEmail(), data.getOrganizerName());
				e.getAttendees().add(attendee);
			} else {
				e.getAttendees().add( getOrganizer(bs.getCredentials().getUser().getEmail(), data.getOrganizerName()) );
			}	
		}
	}

	private void convertTimeZone(MSEvent from, Event to) {
		if (from.getTimeZone() != null) {
			to.setTimezoneName(from.getTimeZone().getID());
		} else {
			to.setTimezoneName(null);
		}
	}
	
	private Attendee convertAttendee(Event oldEvent, MSEvent event, MSAttendee at) throws IllegalMSEventExceptionStateException {
		if (Strings.isNullOrEmpty(Strings.emptyToNull(at.getEmail()))) {
			throw new IllegalMSEventExceptionStateException("Attendees.Attendee.Email is required");
		}
		Attendee ret = new Attendee();
		ret.setEmail(at.getEmail());
		ret.setDisplayName(at.getName());
		ret.setParticipationRole(getParticipationRole(at.getAttendeeType()));
		
		ParticipationState status = EventConverter.getParticipationState( 
				getAttendeeState(oldEvent, at) , at.getAttendeeStatus());
		ret.setState(status);
		
		ret.setOrganizer( isOrganizer(event, at) );
		return ret;
	}

	public ParticipationRole getParticipationRole(AttendeeType attendeeType) {
		if (attendeeType == null) {
			return ParticipationRole.NON;
		}
		
		switch (attendeeType) {
		case OPTIONAL:
			return ParticipationRole.OPT;
		case REQUIRED:
			return ParticipationRole.REQ;
		case RESOURCE:
			return ParticipationRole.CHAIR;
		default:
			return ParticipationRole.NON;
		}
	}

	private void convertBusyStatus(Event parentEvent, MSEventCommon from, Event to) {
		if (from.getBusyStatus() != null) {
			convertBusyStatus(from, to);
		} else if (parentEvent != null) {
			to.setOpacity(parentEvent.getOpacity());
		}
	}
	
	private void convertBusyStatus(MSEventCommon from, Event to) {
		if (from.getBusyStatus() == CalendarBusyStatus.FREE) {
			to.setOpacity(EventOpacity.TRANSPARENT);
		} else {
			to.setOpacity(EventOpacity.OPAQUE);
		}
	}
	
	private void convertCategories(Event parentEvent, MSEventCommon from, Event to) throws IllegalMSEventStateException {
		if (eventHasCategories(from)) {
			convertCategories(from, to);
		} else if (parentEvent != null) {
			to.setCategory(parentEvent.getCategory());
		}
	}
	
	private void convertCategories(MSEventCommon from, Event to) throws IllegalMSEventStateException {
		checkEventCategoriesValidity(from);
		to.setCategory(Iterables.getFirst(from.getCategories(), null));
	}

	private void checkEventCategoriesValidity(MSEventCommon event) throws IllegalMSEventStateException {
		if (event.getCategories().size() > EVENT_CATEGORIES_MAX) {
			String msg = String.format("Categories MUST NOT contain more than 300 elements, found:%d",
					event.getCategories().size());
			throw new IllegalMSEventStateException(msg);
		}
	}

	private boolean eventHasCategories(MSEventCommon event) {
		return event.getCategories() != null && !event.getCategories().isEmpty();
	}

	private void convertAllDayAttribute(Event parentEvent, MSEventCommon from, Event to) {
		if (from.getAllDayEvent() != null) {
			to.setAllday(isAllDayEvent(from));
		} else if (parentEvent != null) {
			to.setAllday(parentEvent.isAllday());
		}
		convertStartTimeByAllDay(to);
	}

	private void convertStartTimeByAllDay(Event event) {
		if (event.isAllday()) {
			event.setStartDate(DateUtils.getMidnightOfDayEarly(event.getStartDate()));
		}
	}

	private Boolean isAllDayEvent(MSEventCommon from) {
		return Objects.firstNonNull(from.getAllDayEvent(), false);
	}

	private void convertDurationAttribute(MSEventCommon data, Event converted) throws IllegalMSEventStateException {
		if (isAllDayEvent(data)) {
			converted.setDuration(EVENT_ALLDAY_DURATION_IN_MS);
		} else {
			convertDurationAttributeByStartTime(data, converted);
		}
	}
	
	private void convertDurationAttributeByStartTime(MSEventCommon data, Event converted) throws IllegalMSEventStateException {
		checkEventTimesValidity(data);
		
		int duration = (int) ((data.getEndTime().getTime() - data.getStartTime().getTime()) / 1000);
		converted.setDuration(duration);
	}
	
	private void checkEventTimesValidity(MSEventCommon event) throws IllegalMSEventStateException {
		if (!eventHasAStartTime(event)) {
			throw new IllegalMSEventStateException("StartTime is required");
		} else if (!isAllDayEvent(event) && !eventHasAEndTime(event)) {
			throw new IllegalMSEventStateException("If not AllDayEvent then EndTime is required");
		}
	}

	private boolean eventHasAStartTime(MSEventCommon event) {
		return event.getStartTime() != null;
	}
	
	private boolean eventHasAEndTime(MSEventCommon event) {
		return event.getEndTime() != null;
	}

	private void assertExceptionValidity(MSEventException exception) throws IllegalMSEventExceptionStateException {
		assertExceptionStartTime(exception);
		assertExceptionDtStamp(exception);
		assertExceptionMeetingStatus(exception);
	}

	private void assertExceptionMeetingStatus(MSEventException exception) throws IllegalMSEventExceptionStateException {
		if (exception.getMeetingStatus() == null) {
			throw new IllegalMSEventExceptionStateException("Exceptions.Exception.MeetingStatus is required");
		}
	}

	private void assertExceptionDtStamp(MSEventException exception) throws IllegalMSEventExceptionStateException {
		if (exception.getDtStamp() == null) {
			throw new IllegalMSEventExceptionStateException("Exceptions.Exception.DtStamp is required");
		}
	}

	private void assertExceptionStartTime(MSEventException exception) throws IllegalMSEventExceptionStateException {
		if (exception.getExceptionStartTime() == null) {
			throw new IllegalMSEventExceptionStateException("Exceptions.Exception.ExceptionStartTime is required");
		}
	}
}
