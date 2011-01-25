package org.obm.sync.calendar;

public class Attendee {

	private ParticipationState state;
	private String email;
	private ParticipationRole required;
	private String displayName;
	private int percent;
	private boolean organizer;
	private boolean obmUser;

	public ParticipationState getState() {
		return state;
	}

	public void setState(ParticipationState state) {
		this.state = state;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ParticipationRole getRequired() {
		return required;
	}

	public void setRequired(ParticipationRole role) {
		this.required = role;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}
	
	public Boolean isOrganizer() {
		return organizer;
	}

	public void setOrganizer(boolean organizer) {
		this.organizer = organizer;
	}
	
	public boolean isObmUser() {
		return obmUser;
	}

	public void setObmUser(boolean obmUser) {
		this.obmUser = obmUser;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Attendee) {
			Attendee other = (Attendee) obj;
			if (email.equalsIgnoreCase(other.email)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return email.hashCode();
	}

}
