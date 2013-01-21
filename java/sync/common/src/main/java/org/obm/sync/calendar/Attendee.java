/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.calendar;


import java.io.Serializable;

import com.google.common.base.Objects;

public abstract class Attendee implements Cloneable, Serializable {
	
	public abstract static class Builder<T extends Attendee> {
		
		private final T attendee;
		
		protected Builder() {
			attendee = createInstance();
		}
		
		public Builder<T> participation(Participation participation) {
			attendee.participation = participation;
			return this;
		}
		
		public Builder<T> email(String email) {
			attendee.email = email;
			return this;
		}
		
		public Builder<T> participationRole(ParticipationRole role) {
			attendee.participationRole = role;
			return this;
		}
		
		public Builder<T> displayName(String name) {
			attendee.displayName = name;
			return this;
		}
		
		public Builder<T> percent(int percent) {
			attendee.percent = percent;
			return this;
		}
		
		public Builder<T> canWriteOnCalendar(boolean canWriteOnCalendar) {
			attendee.canWriteOnCalendar = canWriteOnCalendar;
			return this;
		}
		
		public Builder<T> asOrganizer() {
			attendee.organizer = true;
			return this;
		}
		
		public Builder<T> asAttendee() {
			attendee.organizer = false;
			return this;
		}
		
		public T build() {
			return attendee;
		}
		
		protected abstract T createInstance();
	}
	
	private Participation participation;
	private String email;
	private ParticipationRole participationRole;
	private String displayName;
	private int percent;
	private boolean organizer;
	private boolean canWriteOnCalendar;
	
	public Attendee() {
	}

	public Attendee(Attendee attendee) {
		this.participation = attendee.participation;
		this.email = attendee.email;
		this.displayName = attendee.displayName;
		this.percent = attendee.percent;
		this.organizer = attendee.organizer;
		this.canWriteOnCalendar = attendee.canWriteOnCalendar;
		this.participationRole = attendee.participationRole;
	}
	
	public abstract Attendee clone();
	
	public abstract CalendarUserType getCalendarUserType();
	
	public boolean isObmUser() {
		return false;
	}

	public Participation getParticipation() {
		return participation;
	}

	public void setParticipation(Participation participation) {
		this.participation = participation;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public ParticipationRole getParticipationRole() {
		return participationRole;
	}

	public void setParticipationRole(ParticipationRole role) {
		this.participationRole = role;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Attendee)) {
			return false;
		}
		if (!obj.getClass().equals(getClass())) {
			return false;
		}
		
		Attendee other = (Attendee) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equalsIgnoreCase(other.email)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass()).
				add("email", getEmail()).
				add("participation", getParticipation()).
				add("canWriteOnCalendar", isCanWriteOnCalendar()).toString();
	}
	
}
