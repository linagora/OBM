package org.obm.sync.items;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.book.Contact;

public class ContactChangesResponse {

	private ContactChanges changes;
	private Date lastSync;

	public ContactChangesResponse() {
		changes = new ContactChanges();
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public List<Contact> getUpdated() {
		return changes.getUpdated();
	}

	public void setUpdated(List<Contact> updated) {
		this.changes.setUpdated(updated);
	}

	public Set<Integer> getRemoved() {
		return changes.getRemoved();
	}

	public void setRemoved(Set<Integer> removed) {
		this.changes.setRemoved(removed);
	}

	public void setChanges(ContactChanges changes) {
		this.changes = changes;
	}

	public ContactChanges getChanges() {
		return changes;
	}
}
