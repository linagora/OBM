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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.common.base.Objects;

public class MSEvent implements IApplicationData, MSEventCommon, Serializable {
	
	private static final long serialVersionUID = 1025020118283566465L;
	
	private String location;
	private String subject;
	private String description;
	private Date dtStamp;
	private Date endTime;
	private Date startTime;
	private Boolean allDayEvent;
	private CalendarBusyStatus busyStatus;
	private CalendarSensitivity sensitivity;
	private CalendarMeetingStatus meetingStatus;
	private Integer reminder;
	private List<String> categories;

	private final Set<MSAttendee> attendees;
	private String organizerName;
	private String organizerEmail;
	private MSEventUid uid;
	private Date created;
	private Date lastUpdate;
	private MSRecurrence recurrence;
	private List<MSEventException> exceptions;
	private TimeZone timeZone;
	private Integer obmSequence;
	private transient Set<String> attendeeEmails;
	
	public MSEvent(){
		this.attendees = new HashSet<MSAttendee>();
		this.attendeeEmails = new HashSet<String>();
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public String getOrganizerName() {
		return organizerName;
	}

	public void setOrganizerName(String organizerName) {
		this.organizerName = organizerName;
	}

	public String getOrganizerEmail() {
		return organizerEmail;
	}

	public void setOrganizerEmail(String organizerEmail) {
		this.organizerEmail = organizerEmail;
	}

	public Set<MSAttendee> getAttendees() {
		return attendees;
	}
	
	public void addAttendee(MSAttendee att) {
		if(!attendeeEmails.contains(att.getEmail())){
			attendees.add(att);
			attendeeEmails.add(att.getEmail());
		}
	}
	
	public MSRecurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(MSRecurrence recurrence) {
		this.recurrence = recurrence;
	}

	public List<MSEventException> getExceptions() {
		return Objects.firstNonNull(exceptions, Collections.<MSEventException>emptyList());
	}

	public void setExceptions(List<MSEventException> exceptions) {
		this.exceptions = exceptions;
	}

	@Override
	public PIMDataType getType() {
		return PIMDataType.CALENDAR;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Integer getObmSequence() {
		return obmSequence;
	}

	public void setObmSequence(Integer obmSequence) {
		this.obmSequence = obmSequence;
	}
	
	public MSEventUid getUid() {
		return uid;
	}
	
	public void setUid(MSEventUid uid) {
		this.uid = uid;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDtStamp() {
		return dtStamp;
	}

	public void setDtStamp(Date dtStamp) {
		this.dtStamp = dtStamp;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Boolean getAllDayEvent() {
		return allDayEvent;
	}

	public void setAllDayEvent(Boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
	}

	public CalendarBusyStatus getBusyStatus() {
		return busyStatus;
	}

	public void setBusyStatus(CalendarBusyStatus busyStatus) {
		this.busyStatus = busyStatus;
	}

	public CalendarSensitivity getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(CalendarSensitivity sensitivity) {
		this.sensitivity = sensitivity;
	}

	public CalendarMeetingStatus getMeetingStatus() {
		return meetingStatus;
	}

	public void setMeetingStatus(CalendarMeetingStatus meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	public Integer getReminder() {
		return reminder;
	}

	public void setReminder(Integer reminder) {
		this.reminder = reminder;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public Set<String> getAttendeeEmails() {
		return attendeeEmails;
	}

	public void setAttendeeEmails(Set<String> attendeeEmails) {
		this.attendeeEmails = attendeeEmails;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("attendees", attendees)
			.add("organizerName", organizerName)
			.add("organizerEmail", organizerEmail)
			.add("location", location)
			.add("subject", subject)
			.add("description", description)
			.add("dtStamp", dtStamp)
			.add("endTime", endTime)
			.add("startTime", startTime)
			.add("allDayEvent", allDayEvent)
			.add("busyStatus", busyStatus)
			.add("sensitivity", sensitivity)
			.add("meetingStatus", meetingStatus)
			.add("reminder", reminder)
			.add("categories", categories)
			.add("recurrence", recurrence)
			.add("exceptions", exceptions)
			.add("timeZone", timeZone)
			.add("obmSequence", obmSequence)
			.add("uid", uid)
			.toString();
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(attendees, organizerName, organizerEmail, location, 
				subject, description, dtStamp, 
				endTime, startTime, allDayEvent, busyStatus, sensitivity, meetingStatus, 
				reminder, categories, recurrence, exceptions, timeZone, obmSequence, uid);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEvent) {
			MSEvent that = (MSEvent) object;
			return Objects.equal(this.attendees, that.attendees)
				&& Objects.equal(this.organizerName, that.organizerName)
				&& Objects.equal(this.organizerEmail, that.organizerEmail)
				&& Objects.equal(this.location, that.location)
				&& Objects.equal(this.subject, that.subject)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.dtStamp, that.dtStamp)
				&& Objects.equal(this.endTime, that.endTime)
				&& Objects.equal(this.startTime, that.startTime)
				&& Objects.equal(this.allDayEvent, that.allDayEvent)
				&& Objects.equal(this.busyStatus, that.busyStatus)
				&& Objects.equal(this.sensitivity, that.sensitivity)
				&& Objects.equal(this.meetingStatus, that.meetingStatus)
				&& Objects.equal(this.reminder, that.reminder)
				&& Objects.equal(this.categories, that.categories)
				&& Objects.equal(this.recurrence, that.recurrence)
				&& Objects.equal(this.exceptions, that.exceptions)
				&& Objects.equal(this.timeZone, that.timeZone)
				&& Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.obmSequence, that.obmSequence);
		}
		return false;
	}
	
	
}
