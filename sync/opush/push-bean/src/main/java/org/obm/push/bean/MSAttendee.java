package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class MSAttendee implements Serializable {
	
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
