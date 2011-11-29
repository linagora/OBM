package org.obm.sync.calendar;

import java.util.List;

import com.google.common.base.Objects;

public class Attendee {

	private ParticipationState state;
	private String email;
	private List<String> emailAlias;
	private ParticipationRole required;
	private String displayName;
	private int percent;
	private boolean organizer;
	private boolean obmUser;
	private boolean canWriteOnCalendar;
	
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
 
	public List<String> getEmailAlias() {
		return emailAlias;
	}
	
	public void setEmailAlias(List<String> emailAlias) {
		this.emailAlias = emailAlias;
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

	public Boolean isCanWriteOnCalendar() {
		return canWriteOnCalendar;
	}
	
	public void setCanWriteOnCalendar(Boolean b) {
		canWriteOnCalendar = b;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Attendee))
			return false;
		Attendee other = (Attendee) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equalsIgnoreCase(other.email))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass()).
				add("email", getEmail()).
				add("state", getState()).
				add("canWriteOnCalendar", isCanWriteOnCalendar()).toString();
	}
	
}