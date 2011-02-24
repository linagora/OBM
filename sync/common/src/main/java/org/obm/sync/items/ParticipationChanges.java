package org.obm.sync.items;

import java.util.List;

import org.obm.sync.calendar.Attendee;

public class ParticipationChanges {
	private int eventId;
	private String eventExtId;
	private List<Attendee> attendees;
	
	public ParticipationChanges() {
		super();
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getEventExtId() {
		return eventExtId;
	}

	public void setEventExtId(String eventExtId) {
		this.eventExtId = eventExtId;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}
	
}