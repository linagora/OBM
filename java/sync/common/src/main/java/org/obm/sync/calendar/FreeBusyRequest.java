package org.obm.sync.calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * BEGIN:VCALENDAR PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
 * VERSION:2.0 METHOD:REQUEST BEGIN:VFREEBUSY DTSTAMP:20091029T082350Z
 * ORGANIZER:mailto:adrien@test.tlse.lng DTSTART:20091011T220000Z
 * DTEND:20091027T230000Z UID:11b66915-d01c-4562-b35d-fe222770a95c
 * ATTENDEE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:
 * mailto:adrien@test.tlse.lng END:VFREEBUSY END:VCALENDAR
 * 
 * @author adrienp
 * 
 */
public class FreeBusyRequest {

	private String uid;
	private String owner;
	private Date start;
	private Date end;
	private List<Attendee> attendees;

	public FreeBusyRequest() {
		attendees = new LinkedList<Attendee>();

	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	public void addAttendee(Attendee att) {
		this.attendees.add(att);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
