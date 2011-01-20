package org.obm.sync.calendar;

public final class DeletedEvent {
	
	private Integer id;
	private String extId;
	
	public DeletedEvent(int id, String extId) {
		this.id = id;
		this.extId = extId;
	}

	public Integer getId() {
		return id;
	}

	public String getExtId() {
		return extId;
	}

}
