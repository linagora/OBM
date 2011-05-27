package org.obm.sync.calendar;

import java.util.Date;

public class EventParticipationState {

	private String uid;
	private String title;
	private Date date;
	private ParticipationState state;
	private Integer alert;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public ParticipationState getState() {
		return state;
	}

	public void setState(ParticipationState state) {
		this.state = state;
	}

	public Integer getAlert() {
		return alert;
	}

	public void setAlert(Integer alert) {
		this.alert = alert;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
