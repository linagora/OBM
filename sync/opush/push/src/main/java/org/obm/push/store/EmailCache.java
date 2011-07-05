package org.obm.push.store;

public class EmailCache {

	private long uid;
	private boolean read;
	
	public EmailCache(long uid, boolean read) {
		super();
		this.uid = uid;
		this.read = read;
	}

	public long getUid() {
		return uid;
	}

	public boolean isRead() {
		return read;
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
		EmailCache other = (EmailCache) obj;
		if (read != other.read)
			return false;
		if (uid != other.uid)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EmailCache [uid=" + uid + ", read=" + read + "]";
	}
}
