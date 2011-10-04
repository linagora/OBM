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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allDay == null) ? 0 : allDay.hashCode());
		result = prime * result + duration;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FreeBusyInterval other = (FreeBusyInterval) obj;
		if (allDay == null) {
			if (other.allDay != null)
				return false;
		} else if (!allDay.equals(other.allDay))
			return false;
		if (duration != other.duration)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
	
}
