package org.obm.push.bean;

import java.util.Date;

public class Email {

	private final long uid;
	private final boolean read;
	private final Date date;
	
	public Email(long uid, boolean read, Date date) {
		super();
		this.uid = uid;
		this.read = read;
		this.date = date;
	}

	public long getUid() {
		return uid;
	}

	public boolean isRead() {
		return read;
	}
	
	public Date getDate() {
		return date;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (read ? 1231 : 1237);
		result = prime * result + (int) (uid ^ (uid >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Email other = (Email) obj;
		if (read != other.read)
			return false;
		if (uid != other.uid)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Email [ uid = " + uid + ", read = " + read + " ]";
	}
	
}
