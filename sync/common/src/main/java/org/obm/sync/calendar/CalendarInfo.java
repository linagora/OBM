package org.obm.sync.calendar;

public class CalendarInfo {

	private String uid;
	private String firstname;
	private String lastname;
	private String mail;
	private boolean read;
	private boolean write;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstName) {
		this.firstname = firstName;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastName) {
		this.lastname = lastName;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean equals(Object o) {
		if (!(o instanceof CalendarInfo)) {
			return false;
		}
		CalendarInfo oi = (CalendarInfo) o;
		return uid.equals(oi.uid);
	}

	public int hashCode() {
		return uid.hashCode();
	}

}
