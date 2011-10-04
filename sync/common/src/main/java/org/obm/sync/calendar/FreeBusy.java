package org.obm.sync.calendar;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FreeBusy {

	private String uid;
	private String owner;
	private Date start;
	private Date end;
	private Attendee att;
	private Set<FreeBusyInterval> freeBusyIntervals;

	public FreeBusy() {
		this.freeBusyIntervals = new HashSet<FreeBusyInterval>();
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

	public Attendee getAtt() {
		return att;
	}

	public void setAtt(Attendee att) {
		this.att = att;
	}

	public Set<FreeBusyInterval> getFreeBusyIntervals() {
		return freeBusyIntervals;
	}

	public void addFreeBusyInterval(FreeBusyInterval freeBusyInterval) {
		this.freeBusyIntervals.add(freeBusyInterval);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
