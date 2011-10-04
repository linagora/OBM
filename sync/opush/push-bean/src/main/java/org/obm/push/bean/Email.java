package org.obm.push.bean;

import java.util.Date;

import org.obm.push.utils.index.Indexed;

import com.google.common.base.Objects;

public class Email implements Indexed<Long> {

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

	@Override
	public Long getIndex() {
		return getUid();
	}
	
	public boolean isRead() {
		return read;
	}
	
	public Date getDate() {
		return date;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, read, date);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Email) {
			Email that = (Email) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.read, that.read)
				&& Objects.equal(this.date, that.date);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("read", read)
			.add("date", date)
			.toString();
	}
	
}
