package org.obm.sync.items;

import java.util.List;
import java.util.Set;

import org.obm.sync.book.Contact;

public class ContactChanges {
	
	private List<Contact> updated;
	private Set<Integer> removed;

	public List<Contact> getUpdated() {
		return updated;
	}

	public void setUpdated(List<Contact> updated) {
		this.updated = updated;
	}

	public Set<Integer> getRemoved() {
		return removed;
	}

	public void setRemoved(Set<Integer> removed) {
		this.removed = removed;
	}
}