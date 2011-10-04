package org.obm.sync.items;

import java.util.List;

import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;

public class ParticipationChanges {
	private EventObmId eventId;
	private EventExtId eventExtId;
	private List<Attendee> attendees;
	
	public ParticipationChanges() {
		super();
	}

	public EventObmId getEventId() {
		return eventId;
	}

	public void setEventId(EventObmId eventObmId) {
		this.eventId = eventObmId;
	}

	public EventExtId getEventExtId() {
		return eventExtId;
	}

	public void setEventExtId(EventExtId eventExtId) {
		this.eventExtId = eventExtId;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}
	
}