package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MailChanges {
	
	private final Set<Long> removed;
	private final Set<Long> updated;
	
	private Date lastSync;
	
	public MailChanges(Set<Long> removed, Set<Long> updated, Date lastSync) {
		this.removed = removed;
		this.updated = updated;
		this.lastSync = lastSync;
	}
	
	public MailChanges() {
		this(new HashSet<Long>(), new HashSet<Long>(), null);
	}
	
	public MailChanges(Date lastSync) {
		this(new HashSet<Long>(), new HashSet<Long>(), lastSync);
	}
	
	public MailChanges(Collection<Long> removed,
			Collection<Long> updated, Date lastSync) {
		
		this(lastSync);
		addRemoved(removed);
		addUpdated(updated);
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
