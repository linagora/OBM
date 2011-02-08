package org.obm.sync.calendar;

import java.util.Date;

public class EventTimeUpdate {

	private String uid;
	private String extId;
	private Date recurrenceId;
	private Date timeUpdate;
	private Date date;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public Date getRecurrenceId() {
		return recurrenceId;
	}

	public void setRecurrenceId(Date recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Date timeUpdate) {
		this.timeUpdate = timeUpdate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
