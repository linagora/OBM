package org.obm.sync.items;

import java.util.Date;

import org.obm.sync.user.User;

public class UserChanges {

	private int[] removed;
	private User[] updated;
	private Date lastSync;

	public int[] getRemoved() {
		return removed;
	}

	public void setRemoved(int[] removed) {
		this.removed = removed;
	}

	public User[] getUpdated() {
		return updated;
	}

	public void setUpdated(User[] updated) {
		this.updated = updated;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

}
