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
		return displayName ;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attendee other = (Attendee) obj;
		if (email != null && email.equalsIgnoreCase(other.email)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((email == null) ? 0 : email.toLowerCase().hashCode());
		result = prime * result + (obmUser ? 1231 : 1237);
		result = prime * result + (organizer ? 1231 : 1237);
		result = prime * result + percent;
		result = prime * result
				+ ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

}
