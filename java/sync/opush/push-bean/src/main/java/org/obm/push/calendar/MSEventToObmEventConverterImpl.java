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
import org.obm.sync.calendar.EventMeetingStatus;
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
	public Event convert(User user, Event eventFromDB, MSEvent msEvent, boolean isObmInternalEvent)
			throws ConversionException {

		Event convertedEvent = convertMSEventToObmEvent(user, eventFromDB, msEvent, isObmInternalEvent);
		fillEventRecurrence(user, eventFromDB, msEvent, isObmInternalEvent, convertedEvent);

		return convertedEvent;
	}
	
	private Event convertMSEventToObmEvent(User user, Event eventFromDB, MSEvent msEvent, boolean isObmInternalEvent) 
			throws IllegalMSEventStateException {

		Event convertedEvent = new Event();
		convertedEvent.setExtId(msEvent.getExtId());
		convertedEvent.setUid(msEvent.getObmId());
		convertedEvent.setAttendees( getAttendees(eventFromDB, msEvent) );
		convertedEvent.setTimezoneName(convertTimeZone(msEvent));
		if (msEvent.getObmSequence() != null) {
			convertedEvent.setSequence(msEvent.getObmSequence());
		}
		
		assignOrganizer(user, convertedEvent, msEvent);
		fillEventProperties(user, convertedEvent, eventFromDB, msEvent, isObmInternalEvent);
		
		return convertedEvent;
	}
	
	private void fillEventCommonProperties(User user, Event convertedEvent, Event eventFromDB, MSEventCommon msEvent, 
			boolean isObmInternalEvent) throws IllegalMSEventStateException {
		
		assignOwner(user, convertedEvent, eventFromDB);
		convertedEvent.setInternalEvent(isObmInternalEvent);
		convertedEvent.setType(EventType.VEVENT);
		convertedEvent.setTimeUpdate(msEvent.getDtStamp());
		convertedEvent.setDuration(convertDuration(msEvent));
		convertedEvent.setStartDate(msEvent.getStartTime());
	}
	
	private void fillEventProperties(User user, Event convertedEvent, Event eventFromDB, MSEventCommon msEvent, 
			boolean isObmInternalEvent) throws IllegalMSEventStateException {
		
		fillEventCommonProperties(user, convertedEvent, eventFromDB, 
				msEvent, isObmInternalEvent);
		
		String title = convertSubjectToTitle(msEvent);
		convertedEvent.setTitle(title);
		
		String description = convertDescription(msEvent);
		convertedEvent.setDescription(description);
		
		String location = convertLocation(msEvent);
		convertedEvent.setLocation(location);
		
		boolean isAllDay = isAllDayEvent(msEvent);
		convertedEvent.setAllday(isAllDay);
		
		String category = convertCategories(msEvent);
		convertedEvent.setCategory(category);
		
		Integer reminder = convertReminder(msEvent);
		convertedEvent.setAlert(reminder);

		EventOpacity busyStatus = convertBusyStatusToOpacity(msEvent);
		convertedEvent.setOpacity(busyStatus);

		EventPrivacy eventPrivacy = convertSensitivityToPrivacy(msEvent);
		convertedEvent.setPrivacy(eventPrivacy);
		
		Date dtStamp = convertDtStamp(eventFromDB, msEvent);
		convertedEvent.setTimeCreate(dtStamp);
		
		EventMeetingStatus meetingStatus = convertMeetingStatus(msEvent);
		convertedEvent.setMeetingStatus(meetingStatus);
	}

	private void fillEventRecurrence(User user, Event eventFromDB, MSEvent msEvent, 
			boolean isObmInternalEvent, Event convertedEvent) throws IllegalMSEventStateException {
		
		if (msEvent.getRecurrence() != null) {
			
			EventRecurrence eventRecurrence = getRecurrence(msEvent);
			convertedEvent.setRecurrence(eventRecurrence);
			
			fillEventException(user, eventFromDB, msEvent, isObmInternalEvent, convertedEvent, eventRecurrence);
		}
	}

	private void fillEventException(User user, Event eventFromDB, MSEvent msEvent, boolean isObmInternalEvent, 
			Event convertedEvent, EventRecurrence eventRecurrence) throws IllegalMSEventStateException {
		
		if (msEvent.getExceptions() != null && !msEvent.getExceptions().isEmpty()) {
		
			for (MSEventException msEventException : msEvent.getExceptions()) {
				assertExceptionValidity(eventRecurrence, msEventException);
				if (msEventException.isDeleted()) {
					eventRecurrence.addException(msEventException.getExceptionStartTime());
				} else {
					Event obmEvent = convertEventException(user, eventFromDB, convertedEvent, 
							msEventException, isObmInternalEvent);

					obmEvent.setExtId(msEvent.getExtId());
					obmEvent.setUid(msEvent.getObmId());
					eventRecurrence.addEventException(obmEvent);
				}
			}
		}
	}
	
	private Event convertEventException(User user, Event eventFromDB, Event parentEvent, 
			MSEventException msEventException, boolean isObmInternalEvent) throws IllegalMSEventStateException {
		
		Event convertedEvent = new Event();
		fillEventExceptionProperties(user, convertedEvent, eventFromDB, parentEvent, msEventException, isObmInternalEvent);
		convertedEvent.setRecurrenceId(msEventException.getExceptionStartTime());
		return convertedEvent;
	}
	
	private void fillEventExceptionProperties(User user, Event convertedEvent, Event eventFromDB, Event parentEvent, 
			MSEventException msEvent, boolean isObmInternalEvent) throws IllegalMSEventStateException {
		
		fillEventCommonProperties(user, convertedEvent, eventFromDB, 
				msEvent, isObmInternalEvent);
		
		String title = convertSubjectToTitle(parentEvent, msEvent);
		convertedEvent.setTitle(title);
		
		String description = convertDescription(parentEvent, msEvent);
		convertedEvent.setDescription(description);
		
		String location = convertLocation(parentEvent, msEvent);
		convertedEvent.setLocation(location);
		
		boolean isAllDay = convertAllDay(parentEvent, msEvent);
		convertedEvent.setAllday(isAllDay);
		
		String category = convertCategories(parentEvent, msEvent);
		convertedEvent.setCategory(category);
		
		Integer reminder = convertReminder(parentEvent, msEvent);
		convertedEvent.setAlert(reminder);

		EventOpacity busyStatus = convertBusyStatusToOpacity(parentEvent, msEvent);
		convertedEvent.setOpacity(busyStatus);

		EventPrivacy eventPrivacy = convertSensitivityToPrivacy(parentEvent, msEvent);
		convertedEvent.setPrivacy(eventPrivacy);
		
		Date dtStamp = convertDtStamp(parentEvent, eventFromDB, msEvent);
		convertedEvent.setTimeCreate(dtStamp);
		
		EventMeetingStatus meetingStatus = convertMeetingStatus(parentEvent, msEvent);
		convertedEvent.setMeetingStatus(meetingStatus);
	}
	
	private String convertDescription(MSEventCommon msEvent) {
		if (!Strings.isNullOrEmpty(msEvent.getDescription())) {
			return msEvent.getDescription();
		} else {
			return null;
		}
	}
	
	private String convertDescription(Event parentEvent, MSEventCommon msEvent) {
		String description = convertDescription(msEvent);
		if (description == null) {
			if (!Strings.isNullOrEmpty(parentEvent.getDescription())) {
				description = parentEvent.getDescription();
			}
		}
		return description;
	}

	private ParticipationState getAttendeeState(Event eventFromBD, MSAttendee msAttendee) {
		if (eventFromBD != null) {
			Attendee attendee = eventFromBD.findAttendeeFromEmail(msAttendee.getEmail());
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
	
	private EventPrivacy convertSensitivityToPrivacy(MSEventCommon msEvent) {
		if (msEvent.getSensitivity() != null) {
			if (msEvent.getSensitivity() == CalendarSensitivity.NORMAL) {
				return EventPrivacy.PUBLIC;
			} else {
				return EventPrivacy.PRIVATE;
			}
		} else {
			return EventPrivacy.PUBLIC;
		}
	}

	private EventPrivacy convertSensitivityToPrivacy(Event parentEvent, MSEventCommon msEvent) {
		EventPrivacy eventPrivacy = EventPrivacy.PUBLIC;
		if (msEvent.getSensitivity() != null) {
			eventPrivacy = convertSensitivityToPrivacy(msEvent);
		} else {
			if (parentEvent.getPrivacy() != null) {
				eventPrivacy = parentEvent.getPrivacy();
			}
		}
		return eventPrivacy;
	}

	private String convertLocation(MSEventCommon msEvent) {
		if (!Strings.isNullOrEmpty(msEvent.getLocation())) {
			return msEvent.getLocation();
		} else {
			return null;
		}
	}
	
	private String convertLocation(Event parentEvent, MSEventCommon msEvent) {
		String location = convertLocation(msEvent);
		if (location == null) {
			if (!Strings.isNullOrEmpty(parentEvent.getLocation())) {
				location = parentEvent.getLocation();
			}
		}
		return location;
	}

	private Integer convertReminder(MSEventCommon msEvent) {
		if (msEvent.getReminder() != null) {
			return minutesToSeconds(msEvent.getReminder());
		} else {
			return null;
		}
	}
	
	private Integer convertReminder(Event parentEvent, MSEventCommon msEvent) {
		Integer reminder = convertReminder(msEvent);
		if (reminder == null) {
			reminder = parentEvent.getAlert();
		} 
		return reminder;
	}

	private String convertSubjectToTitle(MSEventCommon msEvent) throws IllegalMSEventStateException {
		if (!Strings.isNullOrEmpty(msEvent.getSubject())) {
			return msEvent.getSubject();
		} else {
			throw new IllegalMSEventStateException("Subject is required");
		}
	}
	
	private String convertSubjectToTitle(Event parentEvent, MSEventCommon msEvent) throws IllegalMSEventStateException {
		try {
			return convertSubjectToTitle(msEvent);
		} catch (IllegalMSEventStateException ex) {
			if (!Strings.isNullOrEmpty(parentEvent.getTitle())) {
				return parentEvent.getTitle();
			} else {
				throw ex;
			}
		}
	}

	private EventMeetingStatus convertMeetingStatus(MSEventCommon msEvent) throws IllegalMSEventExceptionStateException {
		if (msEvent.getMeetingStatus() != null) {
			return convertMeetingStatus(msEvent.getMeetingStatus());
		} else {
			throw new IllegalMSEventExceptionStateException("Exceptions.Exception.MeetingStatus is required");	
		}
	}
	
	private EventMeetingStatus convertMeetingStatus(Event parentEvent, MSEventCommon msEvent) {
		EventMeetingStatus eventMeetingStatus;
		try {
			eventMeetingStatus = convertMeetingStatus(msEvent);
		} catch (IllegalMSEventExceptionStateException e) {
			eventMeetingStatus = parentEvent.getMeetingStatus();
		}
		return eventMeetingStatus;
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

	private Date convertDtStamp(Event eventFromDB, MSEventCommon msEvent) {
		Date dtStamp = msEvent.getDtStamp(); 
		if (eventFromDB != null && eventFromDB.getTimeCreate() != null) {
			dtStamp = eventFromDB.getTimeCreate();
		}
		return dtStamp;
	}
	
	private Date convertDtStamp(Event eventFromDB, Event parentEvent, MSEventCommon msEvent) {
		Date dtStamp = msEvent.getDtStamp(); 
		if (eventFromDB != null && eventFromDB.getTimeCreate() != null) {
			dtStamp = eventFromDB.getTimeCreate();
		} else {
			if (parentEvent != null && parentEvent.getTimeCreate() != null) {
				dtStamp = parentEvent.getTimeCreate();
			}
		}
		return dtStamp;
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

	private List<Attendee> getAttendees(Event eventFromDB, MSEvent msEvent) throws IllegalMSEventExceptionStateException {
		List<Attendee> attendees = new LinkedList<Attendee>();
		if ( msEvent.getAttendees().isEmpty() && 
				(eventFromDB == null || eventFromDB.getAttendees().isEmpty()) ) {
			return attendees;
		}
		
		if (msEvent.getAttendees().isEmpty()) {
			attendees.addAll(eventFromDB.getAttendees());
		} else {
			for (MSAttendee msAttendee: msEvent.getAttendees()) {
				attendees.add( convertAttendee(eventFromDB, msEvent, msAttendee) );
			}
		}
		return attendees;
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
	
	private Attendee convertAttendee(Event eventFromDB, MSEvent msEvent, MSAttendee msAttendee) throws IllegalMSEventExceptionStateException {
		if (Strings.isNullOrEmpty(Strings.emptyToNull(msAttendee.getEmail()))) {
			throw new IllegalMSEventExceptionStateException("Attendees.Attendee.Email is required");
		}
		
		Attendee ret = new Attendee();
		ret.setEmail(msAttendee.getEmail());
		ret.setDisplayName(msAttendee.getName());
		ret.setParticipationRole( 
				getParticipationRole(msAttendee.getAttendeeType()) );
		
		ParticipationState status = getParticipationState( 
				getAttendeeState(eventFromDB, msAttendee) , msAttendee.getAttendeeStatus() );
		
		ret.setState(status);
		
		ret.setOrganizer( isOrganizer(msEvent, msAttendee) );
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

	private EventOpacity convertBusyStatusToOpacity(MSEventCommon msEvent) {
		EventOpacity eventOpacity = EventOpacity.OPAQUE;
		if (msEvent.getBusyStatus() != null) {
			if (msEvent.getBusyStatus() == CalendarBusyStatus.FREE) {
				eventOpacity = EventOpacity.TRANSPARENT;
			}
		}
		return eventOpacity;
	}

	private EventOpacity convertBusyStatusToOpacity(Event parentEvent, MSEventCommon msEvent) {
		EventOpacity eventOpacity = EventOpacity.OPAQUE;
		if (msEvent.getBusyStatus() != null) {
			eventOpacity = convertBusyStatusToOpacity(msEvent);
		} else {
			if (parentEvent.getOpacity() != null) {
				eventOpacity = parentEvent.getOpacity();
			}
		}
		return eventOpacity;
	}
	
	private String convertCategories(MSEventCommon msEvent) {
		if (msEvent.getCategories() != null && !msEvent.getCategories().isEmpty()) {
			try {
				assertEventCategoriesValidity(msEvent);
				return Iterables.getFirst(msEvent.getCategories(), null);
			} catch (IllegalMSEventStateException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private String convertCategories(Event parentEvent, MSEventCommon msEvent) {
		String category = convertCategories(msEvent);
		if (category == null) {
			category = parentEvent.getCategory();
		}
		return category;
	}

	private void assertEventCategoriesValidity(MSEventCommon event) throws IllegalMSEventStateException {
		if (event.getCategories().size() > EVENT_CATEGORIES_MAX) {
			String msg = String.format("Categories MUST NOT contain more than 300 elements, found:%d",
					event.getCategories().size());
			throw new IllegalMSEventStateException(msg);
		}
	}

	private boolean convertAllDay(Event parentEvent, MSEventCommon msEvent) {
		if (msEvent.getAllDayEvent() != null) {
			return msEvent.getAllDayEvent();
		} else {
			return parentEvent.isAllday();
		}
	}

	private boolean isAllDayEvent(MSEventCommon msEvent) {
		return Objects.firstNonNull(msEvent.getAllDayEvent(), false);
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
	}

	private void assertExceptionDoesntExistInRecurrence(EventRecurrence recurrence, MSEventException exception)
			throws IllegalMSEventExceptionStateException {
		if (recurrence.hasAnyExceptionAtDate(exception.getExceptionStartTime())) {
			throw new IllegalMSEventExceptionStateException("Try to add an already existing exception date");
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
