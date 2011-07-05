package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MailChanges {
	
	private Set<Long> removed;
	private Set<Long> updated;
	private Date lastSync;
	
	public MailChanges(){
		this.removed = new HashSet<Long>();
		this.updated = new HashSet<Long>();
	}

	public Set<Long> getRemoved() {
		return removed;
	}

	public void addRemoved(Collection<Long> removed) {
		this.removed.addAll(removed);
	}

	public Set<Long> getUpdated() {
		return updated;
	}

	public void addUpdated(Collection<Long> updated) {
		this.updated.addAll(updated);
	}
	
	public void addUpdated(Long uid){
		this.updated.add(uid);
	}

	public void addRemoved(Long uid){
		this.removed.add(uid);
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
}
