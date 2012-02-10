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
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.Recurrence;
import org.obm.push.bean.RecurrenceDayOfWeekUtils;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ObmEventToMsEventConverter {

	public MSEvent convert(Event e, MSEventUid uid, User user) {
		MSEvent mse = new MSEvent();

		fillEventCommonProperties(e, mse);
		
		mse.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		appendAttendeesAndOrganizer(e, mse, user);
		mse.setUid(uid);
		mse.setRecurrence(getRecurrence(e));
		mse.setExceptions(getException(e));
		mse.setExtId(e.getExtId());
		mse.setObmId(e.getObmId());
		mse.setObmSequence(e.getSequence());
		appendCreatedLastUpdate(mse, e);

		return mse;
	}

	private void fillEventCommonProperties(Event e, MSEventCommon mse) {
		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());
		mse.setLocation(e.getLocation());
		mse.setStartTime(e.getStartDate());
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.setTimeInMillis(e.getStartDate().getTime());
		c.add(Calendar.SECOND, e.getDuration());
		mse.setEndTime(c.getTime());
		
		mse.setAllDayEvent(e.isAllday());

		mse.setReminder(Objects.firstNonNull(e.getAlert(), 0) / 60);
		mse.setBusyStatus(busyStatus(e.getOpacity()));
		mse.setSensitivity(sensitivity(e.getPrivacy()));

	}

	private List<MSEventException> getException(Event event) {
		List<MSEventException> ret = new LinkedList<MSEventException>();
		if (!event.isRecurrent()) {
			return ret;
		}
		
		EventRecurrence recurrence = event.getRecurrence();
		for (Date excp : recurrence.getExceptions()) {
			MSEventException e = new MSEventException();
			e.setDeleted(true);
			e.setExceptionStartTime(excp);
			e.setStartTime(excp);
			e.setDtStamp(new Date());
			ret.add(e);
		}

		for (Event exception : recurrence.getEventExceptions()) {
			MSEventException e = convertException(exception);
			ret.add(e);
		}

		return ret;
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

	private void appendCreatedLastUpdate(MSEvent mse, Event e) {
		mse.setCreated(e.getTimeCreate() != null ? e.getTimeCreate() : new Date());
		mse.setLastUpdate(e.getTimeUpdate() != null ? e.getTimeUpdate() : new Date());
		mse.setDtStamp(mse.getLastUpdate());
	}

	@VisibleForTesting CalendarSensitivity sensitivity(EventPrivacy privacy) {
		Preconditions.checkNotNull(privacy);
		switch (privacy) {
		case PRIVATE:
			return CalendarSensitivity.PRIVATE;
		case PUBLIC:
			return CalendarSensitivity.NORMAL;
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

		msa.setAttendeeStatus(status(at.getState()));
		msa.setEmail(at.getEmail());
		msa.setName(at.getDisplayName());
		msa.setAttendeeType(participationRole(at.getParticipationRole()));

		return msa;
	}

	@VisibleForTesting AttendeeStatus status(ParticipationState state) {
		Preconditions.checkNotNull(state);
		switch (state) {
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
		Preconditions.checkNotNull(role);
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

	private Recurrence getRecurrence(Event event) {
		
		if (!event.isRecurrent()) {
			return null;
		}

		EventRecurrence recurrence = event.getRecurrence();
		Recurrence r = new Recurrence();
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
			r.setDayOfWeek(RecurrenceDayOfWeekUtils.fromRecurrenceDays(recurrence.getDays()));
			break;
		case yearly:
			r.setType(RecurrenceType.YEARLY);
			break;
		case none:
			r.setType(null);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(recurrence.getFrequence());

		return r;
	}

}
