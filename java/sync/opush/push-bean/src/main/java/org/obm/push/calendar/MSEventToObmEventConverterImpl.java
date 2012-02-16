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
package org.obm.push.calendar;

import static org.obm.push.utils.DateUtils.minutesToSeconds;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceDayOfWeekConverter;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.IllegalMSEventExceptionStateException;
import org.obm.push.exception.IllegalMSEventRecurrenceException;
import org.obm.push.exception.IllegalMSEventStateException;
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
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;

@Singleton
public class MSEventToObmEventConverterImpl implements MSEventToObmEventConverter {

	private static final int EVENT_ALLDAY_DURATION_IN_MS = 24 * 3600;
	private static final int EVENT_CATEGORIES_MAX = 300;

	@Override
	public Event convert(User user, Event oldEvent, MSEvent event, boolean isObmInternalEvent) 
			throws ConversionException {

		EventExtId extId = event.getExtId();
		EventObmId obmId = event.getObmId();
		
		Event e = convertMSEventToObmEvent(user, oldEvent, null, event, isObmInternalEvent);
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
					assertExceptionValidity(r, excep);
					if (excep.isDeleted()) {
						r.addException(excep.getExceptionStartTime());
					} else {
						Event obmEvent = convertEventException(user, oldEvent, e, excep, isObmInternalEvent);
						obmEvent.setExtId(extId);
						obmEvent.setUid(obmId);
						
						r.addEventException(obmEvent);
					}
				}
			}
		}

		return e;
	}

	private void fillEventCommonProperties(User user, Event converted, Event oldEvent, Event parentEvent, 
			MSEventCommon msEvent, boolean isObmInternalEvent) throws org.obm.push.exception.IllegalMSEventStateException {
		
		assignOwner(user, converted, oldEvent);
		converted.setInternalEvent(isObmInternalEvent);
		converted.setType(EventType.VEVENT);
		
		converted.setTitle(convertSubject(parentEvent, msEvent));
		
		if (parentEvent != null && !Strings.isNullOrEmpty(parentEvent.getDescription())) {
			converted.setDescription(parentEvent.getDescription());
		} else {
			converted.setDescription(msEvent.getDescription());
		}
		
		converted.setLocation(convertLocation(parentEvent, msEvent));
		
		converted.setTimeUpdate(msEvent.getDtStamp());
		converted.setTimeCreate(convertDtStamp(msEvent, oldEvent));
		
		converted.setDuration(convertDuration(msEvent));
		converted.setAllday(convertAllDay(parentEvent, msEvent));
		converted.setStartDate(msEvent.getStartTime());
		converted.setCategory(convertCategories(parentEvent, msEvent));
		converted.setMeetingStatus(convertMeetingStatus(msEvent));
		
		converted.setAlert(convertReminder(parentEvent, msEvent));

		converted.setOpacity(convertBusyStatus(parentEvent, msEvent));

		if (msEvent.getSensitivity() == null && parentEvent != null) {
			converted.setPrivacy(parentEvent.getPrivacy());
		} else {
			converted.setPrivacy(privacy(oldEvent, msEvent.getSensitivity()));
		}
		
	}
	
	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertMSEventToObmEvent(User user, Event oldEvent, Event parentEvent, MSEvent data, boolean isObmInternalEvent) 
			throws org.obm.push.exception.IllegalMSEventStateException {

		Event e = new Event();
		fillEventCommonProperties(user, e, oldEvent, null, data, isObmInternalEvent);
		e.setAttendees( getAttendees(oldEvent, parentEvent, data) );
		e.setTimezoneName(convertTimeZone(data));
		assignOrganizer(user, e, data);
		return e;
	}

	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertEventException(User user, Event oldEvent, Event parentEvent, 
			MSEventException data, boolean isObmInternalEvent) throws org.obm.push.exception.IllegalMSEventStateException {
		
		Event e = new Event();
		fillEventCommonProperties(user, e, oldEvent, parentEvent, data, isObmInternalEvent);
		e.setRecurrenceId(data.getExceptionStartTime());
		return e;
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

	private String convertLocation(Event parentEvent, MSEventCommon data) {
		if (!Strings.isNullOrEmpty(data.getLocation())) {
			return data.getLocation();
		} else if (parentEvent != null) {
			return parentEvent.getLocation();
		} else {
			return null;
		}
	}

	private Integer convertReminder(Event parentEvent, MSEventCommon data) {
		if (data.getReminder() != null) {
			return (int) minutesToSeconds(data.getReminder());
		} else if (parentEvent != null) {
			return parentEvent.getAlert();
		} else {
			return null;
		}
	}

	private String convertSubject(Event parentEvent, MSEventCommon data) throws IllegalMSEventStateException {
		if (!Strings.isNullOrEmpty(data.getSubject())) {
			return data.getSubject();
		} else if (parentEvent != null && !Strings.isNullOrEmpty(parentEvent.getTitle())) {
			return parentEvent.getTitle();
		} else {
			throw new IllegalMSEventStateException("Subject is required");
		}
	}

	private EventMeetingStatus convertMeetingStatus(MSEventCommon data) {
		if (data.getMeetingStatus() != null) {
			return convertMeetingStatus(data.getMeetingStatus());
		} else {
			return null;
		}
	}

	private EventMeetingStatus convertMeetingStatus(CalendarMeetingStatus meetingStatus) {
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

	private Date convertDtStamp(MSEventCommon data, Event oldEvent) {
		if (oldEvent != null && oldEvent.getTimeCreate() != null) {
			return oldEvent.getTimeCreate();
		} else {
			return data.getDtStamp();
		}
	}

	private EventRecurrence getRecurrence(MSEvent msEvent) throws IllegalMSEventRecurrenceException {
		MSRecurrence msEventRecurrence = msEvent.getRecurrence();
		EventRecurrence convertedRecurrence = convertRecurrenceType(msEventRecurrence);
		convertedRecurrence.setFrequence(convertRecurrenceInterval(msEventRecurrence));
		convertedRecurrence.setEnd(calculateNewRecurrenceEndDate(msEvent));
		return convertedRecurrence;
	}

	private EventRecurrence convertRecurrenceType(MSRecurrence msEventRecurrence) throws IllegalMSEventRecurrenceException {
		EventRecurrence convertedRecurrence = new EventRecurrence();
		switch (msEventRecurrence.getType()) {
		case DAILY:
			convertedRecurrence.setKind(RecurrenceKind.daily);
			convertedRecurrence.setDays(dailyToDays(msEventRecurrence.getDayOfWeek()));
			break;
		case MONTHLY:
			convertedRecurrence.setKind(RecurrenceKind.monthlybydate);
			break;
		case MONTHLY_NDAY:
			convertedRecurrence.setKind(RecurrenceKind.monthlybyday);
			break;
		case WEEKLY:
			convertedRecurrence.setKind(RecurrenceKind.weekly);
			if (msEventRecurrence.getDayOfWeek() == null || msEventRecurrence.getDayOfWeek().isEmpty()) {
				throw new IllegalMSEventRecurrenceException("Weekly recurrence need DayOfWeek attribute");
			}
			convertedRecurrence.setDays(RecurrenceDayOfWeekConverter.toRecurrenceDays(msEventRecurrence.getDayOfWeek()));
			convertedRecurrence.setKind(RecurrenceKind.weekly);
			break;
		case YEARLY:
			convertedRecurrence.setKind(RecurrenceKind.yearly);
			break;
		case YEARLY_NDAY:
			convertedRecurrence.setKind(RecurrenceKind.yearlybyday);
			break;
		}
		return convertedRecurrence;
	}
	
	private Date calculateNewRecurrenceEndDate(MSEvent msEvent) throws IllegalMSEventRecurrenceException {
		MSRecurrence msEventRecurrence = msEvent.getRecurrence();
		boolean hasOccurences = msEventRecurrence.hasOccurences();

		if (hasOccurences && msEventRecurrence.getUntil() != null) {
			throw new IllegalMSEventRecurrenceException("Recurrence cannot has Until AND Occurences");
		} else if (hasOccurences) {
			moveRecurrentEventStartDate(msEvent);
			return calculateRecurrenceEndDateByOccurences(msEventRecurrence, msEvent.getStartTime());
		} else {
			return msEventRecurrence.getUntil();
		}
	}

	private Date calculateRecurrenceEndDateByOccurences(MSRecurrence msEventRecurrence, Date startDate)
			throws IllegalMSEventRecurrenceException {
		
		int multiplyField = findEndTimeMultiplyCalendarField(msEventRecurrence);
		Calendar cal = eventCalendarInstance();
		cal.setTimeInMillis(startDate.getTime());
		cal.add(multiplyField, msEventRecurrence.getOccurrences() - 1);
		return new Date(cal.getTimeInMillis());
	}

	private RecurrenceDays dailyToDays(Set<RecurrenceDayOfWeek> daysOfWeek) {
        if (daysOfWeek != null) {
                return RecurrenceDayOfWeekConverter.toRecurrenceDays(daysOfWeek);
        } else {
                return RecurrenceDays.ALL_DAYS;
        }
	}
	
	private void moveRecurrentEventStartDate(MSEvent msEvent) throws IllegalMSEventRecurrenceException {
		if (msEvent.getRecurrence().getType() == RecurrenceType.YEARLY) {
			moveRecurrentYearlyEventStartDate(msEvent);
		}
	}

	private void moveRecurrentYearlyEventStartDate(MSEvent msEvent) throws IllegalMSEventRecurrenceException {
		MSRecurrence msEventRecurrence = msEvent.getRecurrence();
		if (msEventRecurrence.getDayOfMonth() == null || msEventRecurrence.getMonthOfYear() == null) {
			throw new IllegalMSEventRecurrenceException("Yearly recurrence need DayOfMonth and MonthOfYear attributes");
		}
		Calendar cal = eventCalendarInstance();
		cal.setTimeInMillis(msEvent.getStartTime().getTime());
		cal.set(Calendar.DAY_OF_MONTH, msEventRecurrence.getDayOfMonth());
		cal.set(Calendar.MONTH, msEventRecurrence.getMonthOfYear() - 1);
		msEvent.setStartTime(cal.getTime());
	}

	private int findEndTimeMultiplyCalendarField(MSRecurrence msEventRecurrence) throws IllegalMSEventRecurrenceException {
		Preconditions.checkNotNull(msEventRecurrence.getType(), "Recurrence type should not be null");
		
		switch (msEventRecurrence.getType()) {
		case DAILY:
			return Calendar.DAY_OF_MONTH;
		case MONTHLY:
			return Calendar.MONTH;
		case MONTHLY_NDAY:
			return Calendar.MONTH;
		case WEEKLY:
			return Calendar.WEEK_OF_YEAR;
		case YEARLY:
			return Calendar.YEAR;
		case YEARLY_NDAY:
			return Calendar.YEAR;
		}
		throw new IllegalMSEventRecurrenceException(String.format(
				"The recurrence type cannot be found, value:{%s}", String.valueOf(msEventRecurrence.getType())));
	}

	private Integer convertRecurrenceInterval(MSRecurrence from) throws IllegalMSEventRecurrenceException {
		Integer interval = from.getInterval();
		from.getType().validIntervalOrException(interval);
		return interval;
	}

	
	private void assignOwner(User user, Event e, Event oldEvent) {
		if (oldEvent != null) {
			e.setOwnerEmail(oldEvent.getOwnerEmail());
		} else{
			e.setOwnerEmail(user.getEmail());
		}
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
	
	private void assignOrganizer(User user, Event e, MSEvent data) {
		if (e.findOrganizer() == null) {
			if (data.getOrganizerEmail() != null) {
				Attendee attendee = getOrganizer(data.getOrganizerEmail(), data.getOrganizerName());
				e.getAttendees().add(attendee);
			} else {
				e.getAttendees().add( getOrganizer(user.getEmail(), data.getOrganizerName()) );
			}	
		}
	}

	private String convertTimeZone(MSEvent from) {
		if (from.getTimeZone() != null) {
			return from.getTimeZone().getID();
		} else {
			return null;
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
		
		ParticipationState status = getParticipationState(getAttendeeState(oldEvent, at) , at.getAttendeeStatus());
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

	private EventOpacity convertBusyStatus(Event parentEvent, MSEventCommon from) {
		if (from.getBusyStatus() != null) {
			return convertBusyStatus(from);
		} else if (parentEvent != null) {
			return parentEvent.getOpacity();
		} else {
			return EventOpacity.OPAQUE;
		}
	}
	
	private EventOpacity convertBusyStatus(MSEventCommon from) {
		if (from.getBusyStatus() == CalendarBusyStatus.FREE) {
			return EventOpacity.TRANSPARENT;
		} else {
			return EventOpacity.OPAQUE;
		}
	}
	
	private String convertCategories(Event parentEvent, MSEventCommon from) throws IllegalMSEventStateException {
		if (eventHasCategories(from)) {
			return convertCategories(from);
		} else if (parentEvent != null) {
			return parentEvent.getCategory();
		} else {
			return null;
		}
	}
	
	private String convertCategories(MSEventCommon from) throws IllegalMSEventStateException {
		assertEventCategoriesValidity(from);
		return Iterables.getFirst(from.getCategories(), null);
	}

	private void assertEventCategoriesValidity(MSEventCommon event) throws IllegalMSEventStateException {
		if (event.getCategories().size() > EVENT_CATEGORIES_MAX) {
			String msg = String.format("Categories MUST NOT contain more than 300 elements, found:%d",
					event.getCategories().size());
			throw new IllegalMSEventStateException(msg);
		}
	}

	private boolean eventHasCategories(MSEventCommon event) {
		return event.getCategories() != null && !event.getCategories().isEmpty();
	}

	private boolean convertAllDay(Event parentEvent, MSEventCommon from) {
		if (from.getAllDayEvent() != null) {
			return isAllDayEvent(from);
		} else if (parentEvent != null) {
			return parentEvent.isAllday();
		} else {
			return false;
		}
	}

	private Boolean isAllDayEvent(MSEventCommon from) {
		return Objects.firstNonNull(from.getAllDayEvent(), false);
	}

	private int convertDuration(MSEventCommon data) throws IllegalMSEventStateException {
		if (isAllDayEvent(data)) {
			return EVENT_ALLDAY_DURATION_IN_MS;
		} else {
			return convertDurationAttributeByStartTime(data);
		}
	}
	
	private int convertDurationAttributeByStartTime(MSEventCommon data) throws IllegalMSEventStateException {
		assertEventTimesValidity(data);
		
		int duration = (int) ((data.getEndTime().getTime() - data.getStartTime().getTime()) / 1000);
		return duration;
	}
	
	private void assertEventTimesValidity(MSEventCommon event) throws IllegalMSEventStateException {
		if (!eventHasStartTime(event)) {
			throw new IllegalMSEventStateException("StartTime is required");
		} else if (!isAllDayEvent(event) && !eventHasEndTime(event)) {
			throw new IllegalMSEventStateException("If not AllDayEvent then EndTime is required");
		}
	}

	private boolean eventHasStartTime(MSEventCommon event) {
		return event.getStartTime() != null;
	}
	
	private boolean eventHasEndTime(MSEventCommon event) {
		return event.getEndTime() != null;
	}

	private Calendar eventCalendarInstance() {
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	private void assertExceptionValidity(EventRecurrence recurrence, MSEventException exception)
			throws IllegalMSEventExceptionStateException {
		assertExceptionDoesntExistInRecurrence(recurrence, exception);
		assertExceptionStartTime(exception);
		assertExceptionDtStamp(exception);
		assertExceptionMeetingStatus(exception);
	}

	private void assertExceptionDoesntExistInRecurrence(EventRecurrence recurrence, MSEventException exception)
			throws IllegalMSEventExceptionStateException {
		if (recurrence.hasAnyExceptionAtDate(exception.getExceptionStartTime())) {
			throw new IllegalMSEventExceptionStateException("Try to add an already existing exception date");
		}
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
	
	@Override
	public ParticipationState getParticipationState(ParticipationState oldParticipationState, AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
		case RESPONSE_UNKNOWN:
			return ParticipationState.NEEDSACTION;
		case TENTATIVE:
			return ParticipationState.TENTATIVE;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}

	@Override
	public boolean isInternalEvent(Event event, boolean defaultValue){
		return event != null ? event.isInternalEvent() : defaultValue;
	}
}
