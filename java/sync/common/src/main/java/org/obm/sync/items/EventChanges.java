package org.obm.sync.items;

import java.util.Date;
import java.util.List;

import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;

public class EventChanges {

	private EventObmId[] removed;
	private EventExtId[] removedExtIds;
	private Event[] updated;
	private ParticipationChanges[] participationUpdated;
	private Date lastSync;

	public EventChanges() {
		removed = new EventObmId[0];
		updated = new Event[0];
		participationUpdated = new ParticipationChanges[0];
		removedExtIds = new EventExtId[0];
	}
	
	public EventObmId[] getRemoved() {
		return removed;
	}

	public void setDeletions(List<DeletedEvent> deletions) {
		this.removed = new EventObmId[deletions.size()];
		this.removedExtIds = new EventExtId[deletions.size()];
		int i = 0;
		for (DeletedEvent de : deletions) {
			removed[i] = de.getId();
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

	public EventExtId[] getRemovedExtIds() {
		return removedExtIds;
	}

	public ParticipationChanges[] getParticipationUpdated() {
		return participationUpdated;
	}
	
	public void setParticipationUpdated(ParticipationChanges[] participationUpdated) {
		this.participationUpdated = participationUpdated;
	}
	
}
