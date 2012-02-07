/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.calendar;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Attendee {

	private ParticipationState state;
	private String email;
	private ParticipationRole participationRole;
	private String displayName;
	private int percent;
	private boolean organizer;
	private boolean obmUser;
	private boolean canWriteOnCalendar;
	
	public Attendee() {
		this.emailAlias = ImmutableList.of();
	}

	public Attendee(Attendee attendee) {
		this.state = attendee.state;
		this.email = attendee.email;
		this.emailAlias = Lists.newArrayList(attendee.emailAlias);
		this.displayName = attendee.displayName;
		this.percent = attendee.percent;
		this.organizer = attendee.organizer;
		this.obmUser = attendee.obmUser;
		this.canWriteOnCalendar = attendee.canWriteOnCalendar;
	}

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
