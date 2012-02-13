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

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeekUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

/**
 * Convert events between OBM-Sync object model & Microsoft object model
 */
public class EventConverter {

	private EventRecurrence getRecurrence(MSEvent msev) {
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
			or.setKind(RecurrenceKind.yearly);
			cal.setTimeInMillis(startDate.getTime());
			cal.set(Calendar.DAY_OF_MONTH, pr.getDayOfMonth());
			cal.set(Calendar.MONTH, pr.getMonthOfYear() - 1);
			msev.setStartTime(cal.getTime());
			or.setFrequence(1);
			multiply = Calendar.YEAR;
			break;
		case YEARLY_NDAY:
			or.setKind(RecurrenceKind.yearly);
			multiply = Calendar.YEAR;
			break;
		}

		// interval
		if (pr.getInterval() != null) {
			or.setFrequence(pr.getInterval());
		}

		// occurence or end date
		Date endDate = null;
		if (pr.getOccurrences() != null && pr.getOccurrences() > 0) {
			cal.setTimeInMillis(startDate.getTime());
			cal.add(multiply, pr.getOccurrences() - 1);
			endDate = new Date(cal.getTimeInMillis());
		} else {
			endDate = pr.getUntil();
		}
		or.setEnd(endDate);

		return or;
	}

	public Event convert(BackendSession bs, Event oldEvent, MSEvent event, Boolean isObmInternalEvent) {
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
					if (!excep.isDeletedException()) {
						
						Event obmEvent = convertEventException(bs, oldEvent, e, excep, isObmInternalEvent);
						obmEvent.setExtId(extId);
						obmEvent.setUid(obmId);
						
						r.addEventException(obmEvent);
					} else {
						r.addException(excep.getExceptionStartTime());
					}
				}
			}
		}

		return e;
	}

	private Event convertEventException(BackendSession bs, Event oldEvent, Event parentEvent, MSEventException data, boolean isObmInternalEvent) {
		Event e = convertEventCommon(bs, oldEvent, parentEvent, data, isObmInternalEvent);
		e.setRecurrenceId(data.getExceptionStartTime());
		return e;
	}
	
	private Event convertEventCommon(BackendSession bs, Event oldEvent, Event parentEvent, MSEventCommon data, boolean isObmInternalEvent) {
		Event e = new Event();
		defineOwner(bs, e, oldEvent);
		e.setInternalEvent(isObmInternalEvent);
		e.setType(EventType.VEVENT);
		
		if (parentEvent != null && parentEvent.getTitle() != null && !parentEvent.getTitle().isEmpty()) {
			e.setTitle(parentEvent.getTitle());
		} else {
			e.setTitle(data.getSubject());
		}
		
		if (parentEvent != null && parentEvent.getDescription() != null && !parentEvent.getDescription().isEmpty()) {
			e.setDescription(parentEvent.getDescription());
		} else {
			e.setDescription(data.getDescription());
		}
		
		e.setLocation(data.getLocation());
		e.setStartDate(data.getStartTime());
		
		int duration = (int) (data.getEndTime().getTime() - data.getStartTime().getTime()) / 1000;
		e.setDuration(duration);
		e.setAllday(data.getAllDayEvent() != null ? data.getAllDayEvent() : false);
		
		if (data.getReminder() != null && data.getReminder() > 0) {
			e.setAlert(data.getReminder() * 60);
		}

		if (data.getBusyStatus() == null) {
			if (parentEvent != null) {
				e.setOpacity(parentEvent.getOpacity());
			}
		} else {
			e.setOpacity(opacity(data.getBusyStatus()));
		}

		if (data.getSensitivity() == null && parentEvent != null) {
			e.setPrivacy(parentEvent.getPrivacy());
		} else {
			e.setPrivacy(privacy(oldEvent, data.getSensitivity()));
		}
		
		return e;
	}
	
	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertEventOne(BackendSession bs, Event oldEvent, Event parentEvent, MSEvent data, boolean isObmInternalEvent) {
		Event e = convertEventCommon(bs, oldEvent, parentEvent, data, isObmInternalEvent);
		e.setAttendees( getAttendees(oldEvent, parentEvent, data) );
		defineOrganizer(e, data, bs);
		return e;
	}

	private void defineOwner(BackendSession bs, Event e, Event oldEvent) {
		if (oldEvent != null) {
			e.setOwnerEmail(oldEvent.getOwnerEmail());
		} else{
			e.setOwnerEmail(bs.getCredentials().getUser().getEmail());
		}
	}

	private List<Attendee> getAttendees(Event oldEvent, Event parentEvent, MSEvent data) {
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
				e.getAttendees().add( getOrganizer(bs.getCredentials().getUser().getEmail(), null) );
			}	
		}
	}
	
	private Attendee convertAttendee(Event oldEvent, MSEvent event, MSAttendee at) {
		Attendee ret = new Attendee();
		ret.setEmail(at.getEmail());
		ret.setDisplayName(at.getName());
		ret.setParticipationRole(ParticipationRole.REQ);
		
		ParticipationState status = getParticipationState( 
				getAttendeeState(oldEvent, at) , at.getAttendeeStatus());
		ret.setState(status);
		
		ret.setOrganizer( isOrganizer(event, at) );
		return ret;
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
		switch (sensitivity) {
		case CONFIDENTIAL:
		case PERSONAL:
		case PRIVATE:
			return EventPrivacy.PRIVATE;
		case NORMAL:
		default:
			return EventPrivacy.PUBLIC;
		}

	}

	private EventOpacity opacity(CalendarBusyStatus busyStatus) {
		switch (busyStatus) {
		case FREE:
			return EventOpacity.TRANSPARENT;
		default:
			return EventOpacity.OPAQUE;
		}
	}

	public static ParticipationState getParticipationState(ParticipationState oldParticipationState, AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
		case RESPONSE_UNKNOWN:
		case TENTATIVE:
			return ParticipationState.NEEDSACTION;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}
	
	public static boolean isInternalEvent(Event event, boolean defaultValue){
		return event != null ? event.isInternalEvent() : defaultValue;
	}
	
}
