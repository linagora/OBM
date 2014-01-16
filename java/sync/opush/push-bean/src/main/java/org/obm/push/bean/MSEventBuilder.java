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
package org.obm.push.bean;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MSEventBuilder {

	private final MSEvent msEvent;

	public MSEventBuilder() {
		this.msEvent = new MSEvent();
	}
	
	public MSEventBuilder withAttendees(Set<MSAttendee> attendees) {
		if (attendees != null){
			this.msEvent.getAttendees().clear();
			this.msEvent.getAttendees().addAll(attendees);
		}
		return this;
	}

	public MSEventBuilder withOrganizerName(String organizerName) {
		this.msEvent.setOrganizerName(organizerName);
		return this;
	}

	public MSEventBuilder withOrganizerEmail(String organizerEmail) {
		this.msEvent.setOrganizerEmail(organizerEmail);
		return this;
	}

	public MSEventBuilder withLocation(String location) {
		this.msEvent.setLocation(location);
		return this;
	}

	public MSEventBuilder withSubject(String subject) {
		this.msEvent.setSubject(subject);
		return this;
	}

	public MSEventBuilder withUid(MSEventUid uid) {
		this.msEvent.setUid(uid);
		return this;
	}

	public MSEventBuilder withDescription(String description) {
		this.msEvent.setDescription(description);
		return this;
	}

	public MSEventBuilder withDtStamp(Date dtStamp) {
		this.msEvent.setDtStamp(dtStamp);
		return this;
	}

	public MSEventBuilder withEndTime(Date endTime) {
		this.msEvent.setEndTime(endTime);
		return this;
	}

	public MSEventBuilder withStartTime(Date startTime) {
		this.msEvent.setStartTime(startTime);
		return this;
	}

	public MSEventBuilder withAllDayEvent(Boolean allDayEvent) {
		this.msEvent.setAllDayEvent(allDayEvent);
		return this;
	}

	public MSEventBuilder withBusyStatus(CalendarBusyStatus busyStatus) {
		this.msEvent.setBusyStatus(busyStatus);
		return this;
	}

	public MSEventBuilder withSensitivity(CalendarSensitivity sensitivity) {
		this.msEvent.setSensitivity(sensitivity);
		return this;
	}

	public MSEventBuilder withMeetingStatus(CalendarMeetingStatus meetingStatus) {
		this.msEvent.setMeetingStatus(meetingStatus);
		return this;
	}

	public MSEventBuilder withReminder(Integer reminder) {
		this.msEvent.setReminder(reminder);
		return this;
	}

	public MSEventBuilder withCategories(List<String> categories) {
		this.msEvent.setCategories(categories);
		return this;
	}

	public MSEventBuilder withRecurrence(MSRecurrence recurrence) {
		this.msEvent.setRecurrence(recurrence);
		return this;
	}

	public MSEventBuilder withExceptions(List<MSEventException> exceptions) {
		this.msEvent.setExceptions(exceptions);
		return this;
	}

	public MSEventBuilder withTimeZone(TimeZone timeZone) {
		this.msEvent.setTimeZone(timeZone);
		return this;
	}

	public MSEvent build(){
		return msEvent;
	}
}
