package org.obm.caldav.server.share.filter;

import java.util.Date;

public class TimeRange {
	private Date start;
	private Date end;

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean isEmpty() {
		if (start == null && end == null) {
			return true;
		} else {
			return false;
		}
	}

}
