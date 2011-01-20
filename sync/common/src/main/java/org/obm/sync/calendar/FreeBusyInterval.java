package org.obm.sync.calendar;

import java.util.Date;

public class FreeBusyInterval {

	private Date start;
	private int duration;
	private Boolean allDay;

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(Boolean allDay) {
		this.allDay = allDay;
	}

}
