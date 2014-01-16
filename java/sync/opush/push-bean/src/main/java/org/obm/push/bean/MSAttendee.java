/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class MSAttendee implements Serializable {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private final MSAttendee attendee;
		
		private Builder() {
			super();
			attendee = new MSAttendee();
		}
		
		public Builder withEmail(String email) {
			attendee.email = email;
			return this;
		}
		
		public Builder withName(String name) {
			attendee.name = name;
			return this;
		}
		
		public Builder withStatus(AttendeeStatus status) {
			attendee.attendeeStatus = status;
			return this;
		}
		
		public Builder withType(AttendeeType type) {
			attendee.attendeeType = type;
			return this;
		}
		
		public MSAttendee build() {
			return attendee;
		}
	}
	
	private static final long serialVersionUID = 6378209444148071792L;
	
	private String email;
	private String name;
	private AttendeeStatus attendeeStatus;
	private AttendeeType attendeeType;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public AttendeeStatus getAttendeeStatus() {
		return attendeeStatus;
	}
	
	public void setAttendeeStatus(AttendeeStatus attendeeStatus) {
		this.attendeeStatus = attendeeStatus;
	}
	
	public AttendeeType getAttendeeType() {
		return attendeeType;
	}
	
	public void setAttendeeType(AttendeeType attendeeType) {
		this.attendeeType = attendeeType;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(email, name, attendeeStatus, attendeeType);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAttendee) {
			MSAttendee that = (MSAttendee) object;
			return Objects.equal(this.email, that.email)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.attendeeStatus, that.attendeeStatus)
				&& Objects.equal(this.attendeeType, that.attendeeType);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("email", email)
			.add("name", name)
			.add("attendeeStatus", attendeeStatus)
			.add("attendeeType", attendeeType)
			.toString();
	}
	
}
