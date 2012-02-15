package org.obm.push.bean;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;

public class MSEventBuilder {

	private Set<MSAttendee> attendees;
	private String organizerName;
	private String organizerEmail;
	private String location;
	private String subject;
	private EventObmId obmId;
	private EventExtId extId;
	private MSEventUid uid;
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
	private MSRecurrence recurrence;
	private List<MSEventException> exceptions;
	private TimeZone timeZone;

	public MSEventBuilder withAttendees(Set<MSAttendee> attendees) {
		this.attendees = attendees;
		return this;
	}

	public MSEventBuilder withOrganizerName(String organizerName) {
		this.organizerName = organizerName;
		return this;
	}

	public MSEventBuilder withOrganizerEmail(String organizerEmail) {
		this.organizerEmail = organizerEmail;
		return this;
	}

	public MSEventBuilder withLocation(String location) {
		this.location = location;
		return this;
	}

	public MSEventBuilder withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public MSEventBuilder withObmId(EventObmId obmId) {
		this.obmId = obmId;
		return this;
	}

	public MSEventBuilder withExtId(EventExtId extId) {
		this.extId = extId;
		return this;
	}

	public MSEventBuilder withUid(MSEventUid uid) {
		this.uid = uid;
		return this;
	}

	public MSEventBuilder withDescription(String description) {
		this.description = description;
		return this;
	}

	public MSEventBuilder withDtStamp(Date dtStamp) {
		this.dtStamp = dtStamp;
		return this;
	}

	public MSEventBuilder withEndTime(Date endTime) {
		this.endTime = endTime;
		return this;
	}

	public MSEventBuilder withStartTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public MSEventBuilder withAllDayEvent(Boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
		return this;
	}

	public MSEventBuilder withBusyStatus(CalendarBusyStatus busyStatus) {
		this.busyStatus = busyStatus;
		return this;
	}

	public MSEventBuilder withSensitivity(CalendarSensitivity sensitivity) {
		this.sensitivity = sensitivity;
		return this;
	}

	public MSEventBuilder withMeetingStatus(CalendarMeetingStatus meetingStatus) {
		this.meetingStatus = meetingStatus;
		return this;
	}

	public MSEventBuilder withReminder(Integer reminder) {
		this.reminder = reminder;
		return this;
	}

	public MSEventBuilder withCategories(List<String> categories) {
		this.categories = categories;
		return this;
	}

	public MSEventBuilder withRecurrence(MSRecurrence recurrence) {
		this.recurrence = recurrence;
		return this;
	}

	public MSEventBuilder withExceptions(List<MSEventException> exceptions) {
		this.exceptions = exceptions;
		return this;
	}

	public MSEventBuilder withTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	public MSEvent build(){
		MSEvent event = new MSEvent();
		event.setAllDayEvent(allDayEvent);
		event.setBusyStatus(busyStatus);
		event.setCategories(categories);
		event.setDescription(description);
		event.setDtStamp(dtStamp);
		event.setEndTime(endTime);
		event.setExceptions(exceptions);
		event.setExtId(extId);
		event.setLocation(location);
		event.setMeetingStatus(meetingStatus);
		event.setObmId(obmId);
		event.setOrganizerEmail(organizerEmail);
		event.setOrganizerName(organizerName);
		event.setRecurrence(recurrence);
		event.setReminder(reminder);
		event.setSensitivity(sensitivity);
		event.setStartTime(startTime);
		event.setSubject(subject);
		event.setTimeZone(timeZone);
		event.setUid(uid);
		event.getAttendees().clear();
		if (attendees != null){
			event.getAttendees().addAll(attendees);
		}
		return event;
	}
}
