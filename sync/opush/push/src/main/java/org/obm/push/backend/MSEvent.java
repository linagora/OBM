package org.obm.push.backend;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.data.calendarenum.CalendarSensitivity;
import org.obm.push.store.IApplicationData;
import org.obm.push.store.PIMDataType;

public class MSEvent implements IApplicationData {
	
	private String organizerName;
	private String organizerEmail;
	private String location;
	private String subject;
	private String uID;
	private String description;
	private Date created;
	private Date lastUpdate;
	private Date dtStamp;
	private Date endTime;
	private Date startTime;
	private Boolean allDayEvent;
	private CalendarBusyStatus busyStatus;
	private CalendarSensitivity sensitivity;
	private CalendarMeetingStatus meetingStatus;
	private Integer reminder;
	private Set<MSAttendee> attendees;
	private List<String> categories;
	private Recurrence recurrence;
	private List<MSEvent> exceptions;
	private TimeZone timeZone;
	private Date exceptionStartTime;
	private boolean deletedException;
	private String obmUID;
	private Integer obmSequence;
	
	public MSEvent(){
		this.attendees = new HashSet<MSAttendee>();
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

	public String getUID() {
		return uID;
	}

	public void setUID(String uid) {
		uID = uid;
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

	public Set<MSAttendee> getAttendees() {
		return attendees;
	}
	
	public void addAttendee(MSAttendee att) {
		if(!attendees.contains(att)){
			attendees.add(att);
		}
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}

	public List<MSEvent> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<MSEvent> exceptions) {
		this.exceptions = exceptions;
	}

	public void setDeleted(boolean deleted) {
		this.deletedException = deleted;
	}

	public boolean isDeletedException() {
		return deletedException;
	}

	@Override
	public PIMDataType getType() {
		return PIMDataType.CALENDAR;
	}

	public Date getDtStamp() {
		if(dtStamp != null){
			return dtStamp;
		}
		return new Date(0);
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

	public Date getExceptionStartTime() {
		return exceptionStartTime;
	}

	public void setExceptionStartTime(Date exceptionStartTime) {
		this.exceptionStartTime = exceptionStartTime;
	}
	
	public String getObmUID() {
		return obmUID;
	}

	public void setObmUID(String obmUID) {
		this.obmUID = obmUID;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
