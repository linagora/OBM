package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.bean.Email;

public class MailChanges {
	
	private final Set<Email> removed;
	private final Set<Email> updated;
	
	private Date lastSync;
	
	public MailChanges(Set<Email> removed, Set<Email> updated, Date lastSync) {
		this.removed = removed;
		this.updated = updated;
		this.lastSync = lastSync;
	}
	
	public MailChanges() {
		this(new HashSet<Email>(), new HashSet<Email>(), null);
	}
	
	public MailChanges(Date lastSync) {
		this(new HashSet<Email>(), new HashSet<Email>(), lastSync);
	}
	
	public MailChanges(Collection<Email> removed, Collection<Email> updated, Date lastSync) {
		this(lastSync);
		addRemoved(removed);
		addUpdated(updated);
	}

	public Set<Email> getRemoved() {
		return removed;
	}

	public void addRemoved(Collection<Email> removed) {
		this.removed.addAll(removed);
	}

	public Set<Email> getUpdated() {
		return updated;
	}

	public void addUpdated(Collection<Email> updated) {
		this.updated.addAll(updated);
	}
	
	public void addUpdated(Email uid){
		this.updated.add(uid);
	}

	public void addRemoved(Email uid){
		this.removed.add(uid);
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	public Collection<Long> getRemovedToLong() {
		return EmailFactory.listUIDFromEmail(getRemoved());
	}
	
	public Collection<Long> getUpdatedToLong() {
		return EmailFactory.listUIDFromEmail(getUpdated());
	}
	
}
