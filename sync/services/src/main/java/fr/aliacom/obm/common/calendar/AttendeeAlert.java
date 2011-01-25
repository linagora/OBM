package fr.aliacom.obm.common.calendar;

import org.obm.sync.calendar.Attendee;

public class AttendeeAlert extends Attendee{
	
	private int entityId;
	private int alert;

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getAlert() {
		return alert;
	}

	public void setAlert(int alert) {
		this.alert = alert;
	}

}
