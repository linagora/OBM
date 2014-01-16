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
import java.util.Date;
import java.util.List;
import com.google.common.base.Objects;

public class MSEventException implements MSEventCommon, Serializable {

	private static final long serialVersionUID = 7845798798936574053L;
	
	protected String location;
	protected String subject;
	protected String description;
	protected Date dtStamp;
	protected Date endTime;
	protected Date startTime;
	protected Boolean allDayEvent;
	protected CalendarBusyStatus busyStatus;
	protected CalendarSensitivity sensitivity;
	protected CalendarMeetingStatus meetingStatus;
	protected Integer reminder;
	protected List<String> categories;
	private boolean deleted;
	private Date exceptionStartTime;
	
	public MSEventException() {
		super();
		this.deleted = false;
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getExceptionStartTime() {
		return exceptionStartTime;
	}

	public void setExceptionStartTime(Date exceptionStartTime) {
		this.exceptionStartTime = exceptionStartTime;
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

	@Override
	public final int hashCode(){
		return Objects.hashCode(location, subject, description, dtStamp, 
				endTime, startTime, allDayEvent, busyStatus, sensitivity, 
				meetingStatus, reminder, categories, deleted, exceptionStartTime);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEventException) {
			MSEventException that = (MSEventException) object;
			return Objects.equal(this.location, that.location)
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
				&& Objects.equal(this.deleted, that.deleted)
				&& Objects.equal(this.exceptionStartTime, that.exceptionStartTime);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
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
			.add("deleted", deleted)
			.add("exceptionStartTime", exceptionStartTime)
			.toString();
	}
	
}
