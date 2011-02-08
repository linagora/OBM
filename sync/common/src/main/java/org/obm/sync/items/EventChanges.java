package org.obm.sync.items;

import java.util.Date;
import java.util.List;

import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;

public class EventChanges {

	private String[] removed;
	private Event[] updated;
	private Date lastSync;
	private String[] removedExtIds;

	public String[] getRemoved() {
		return removed;
	}

	public void setDeletions(List<DeletedEvent> deletions) {
		this.removed = new String[deletions.size()];
		this.removedExtIds = new String[deletions.size()];
		int i = 0;
		for (DeletedEvent de : deletions) {
			removed[i] = de.getId().toString();
			removedExtIds[i] = de.getExtId();
			i++;
		}
	}

	public Event[] getUpdated() {
		return updated;
	}

	public void setUpdated(Event[] updated) {
		this.updated = updated;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public String[] getRemovedExtIds() {
		return removedExtIds;
	}

}
