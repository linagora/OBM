package org.obm.sync.items;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;

public class AddressBookChangesResponse {

	private ContactChanges contactChanges;
	private FolderChanges booksChanges;
	private Date lastSync;

	public AddressBookChangesResponse() {
		contactChanges = new ContactChanges();
		booksChanges = new FolderChanges();
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public List<Contact> getUpdatedContacts() {
		return contactChanges.getUpdated();
	}

	public Set<Integer> getRemovedContacts() {
		return contactChanges.getRemoved();
	}

	public void setContactChanges(ContactChanges contactChanges) {
		this.contactChanges = contactChanges;
	}
	
	public void setUpdatedAddressBooks(List<Folder> updated) {
		this.booksChanges.setUpdated(updated);
	}
	
	public List<Folder> getUpdatedAddressBooks() {
		return this.booksChanges.getUpdated();
	}
	
	public void setRemovedAddressBooks(Set<Integer> removed) {
		this.booksChanges.setRemoved(removed);
	}
	
	public Set<Integer> getRemovedAddressBooks() {
		return this.booksChanges.getRemoved();
	}

	public void setBooksChanges(FolderChanges booksChanges) {
		this.booksChanges = booksChanges;
	}

	public FolderChanges getBooksChanges() {
		return booksChanges;
	}
	
	public ContactChanges getContactChanges() {
		return contactChanges;
	}
}
