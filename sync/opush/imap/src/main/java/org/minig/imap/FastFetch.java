package org.minig.imap;

import java.util.Date;
import java.util.Set;

public class FastFetch {

	private final long uid;
	private final Date internalDate;
	private final Set<Flag> flags;
	
	public FastFetch(long uid, Date internalDate, Set<Flag> flags){
		this.uid = uid;
		this.internalDate = internalDate;
		this.flags = flags;
	}

	public long getUid() {
		return uid;
	}

	public Date getInternalDate() {
		return internalDate;
	}

	public Set<Flag> getFlags() {
		return flags;
	}
	
	public boolean isRead(){
		return flags != null && flags.contains(Flag.SEEN);
	}
	
}
