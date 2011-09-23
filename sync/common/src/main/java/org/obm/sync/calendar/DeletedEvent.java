package org.obm.sync.calendar;

public final class DeletedEvent {
	
	private EventObmId id;
	private EventExtId extId;
	
	public DeletedEvent(EventObmId id, EventExtId extId) {
		this.id = id;
		this.extId = extId;
	}

	public EventObmId getId() {
		return id;
	}

	public EventExtId getExtId() {
		return extId;
	}

}
