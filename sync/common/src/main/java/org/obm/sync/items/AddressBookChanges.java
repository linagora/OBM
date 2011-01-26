package org.obm.sync.items;

import java.util.List;
import java.util.Set;

import org.obm.sync.book.AddressBook;

public class AddressBookChanges {

	private List<AddressBook> updated; 
	private Set<Integer> removed;

	public void setUpdated(List<AddressBook> updated) {
		this.updated = updated;
	}
	
	public void setRemoved(Set<Integer> removed) {
		this.removed = removed;
	}
	
	public List<AddressBook> getUpdated() {
		return updated;
	}
	
	public Set<Integer> getRemoved() {
		return removed;
	}
}
