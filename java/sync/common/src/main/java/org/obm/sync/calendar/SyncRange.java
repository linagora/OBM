package org.obm.sync.calendar;

import java.util.Date;

public class SyncRange {
	
	private Date before;
	private Date after;

	public SyncRange(Date before, Date after) {
		super();
		this.before = before;
		this.after = after != null ? after : new Date(0);
	}

	public Date getBefore() {
		return before;
	}

	public Date getAfter() {
		return after;
	}

}
