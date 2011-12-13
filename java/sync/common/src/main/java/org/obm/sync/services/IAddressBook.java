package org.obm.sync.services;

import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;

public interface IAddressBook {

	/**
	 * check if given book is read only for logged user
	 * @throws ServerFault 
	 */
	boolean isReadOnly(AccessToken token, BookType book) throws ServerFault ;

	
	/**
	 * list accessible book types for logged user
	 * @throws ServerFault 
	 */
	BookType[] listBooks(AccessToken token) throws ServerFault;
	
	/**
	 * List all accessible books for logged user
	 */
	List<AddressBook> listAllBooks(AccessToken token) throws ServerFault;

	/**
	 * list of updated and removed contacts sync given date
	 */
	ContactChanges listContactsChanged(AccessToken token, Date lastSync) throws ServerFault;

	
	/**
	 * get a list of updated and removed addressbooks since given date, including addressbooks content
	 */
	AddressBookChangesResponse getAddressBookSync(AccessToken token, Date date)
			throws ServerFault;

	
	/**
	 * Create the given contact into given book if book is writable
	 */
	Contact createContact(AccessToken token, BookType book, Contact contact) 
		throws ServerFault;

	/**
	 * Create the given contact into given book if book is writable
	 */
	Contact createContactInBook(AccessToken token, int addressBookId, Contact contact) 
		throws ServerFault;

	
	/**
	 * try to create the given contact : if it finds a similar contact,
	 * no contact will be created, but the first similar contact will be return
	 * 
	 * Please use it only if you know what you are doing.
	 */
	Contact createContactWithoutDuplicate(AccessToken token,
			BookType book, Contact contact) throws ServerFault;

	/**
	 * modify existing contact with data provided if possible.
	 */
	Contact modifyContact(AccessToken token, BookType book,
			Contact contact) throws ServerFault;

	/**
	 * modify existing contact with data provided if possible.
	 */
	Contact modifyContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws ServerFault;

	/**
	 * remove the contact with specified uid 
	 */
	Contact removeContact(AccessToken token, BookType book, String uid)
			throws ServerFault, ContactNotFoundException;

	/**
	 * remove the contact with specified uid 
	 */
	Contact removeContactInBook(AccessToken token, int addressBookId, String uid)
			throws ServerFault, ContactNotFoundException;
	
	/**
	 * Search contacts using a solr query
	 */
	List<Contact> searchContact(AccessToken token, String query,
			int limit) throws ServerFault;

	// Methods for Funambol sync

	/**
	 * Retrieve a contact by its uid
	 */
	Contact getContactFromId(AccessToken token, BookType book, String id)
			throws ServerFault;

	/**
	 * Retrieve a contact by its uid
	 */
	Contact getContactInBook(AccessToken token, int addressBookId, String id)
			throws ServerFault;

	/**
	 * Search contact similar to the given one.
	 */
	KeyList getContactTwinKeys(AccessToken token, BookType book,
			Contact contact) throws ServerFault;

	/**
	 * Search contacts in a group, based on a solr query
	 */
	List<Contact> searchContactInGroup(AccessToken token, AddressBook group,
			String query, int limit) throws ServerFault;


	boolean unsubscribeBook(AccessToken token, Integer bookId) throws ServerFault;

	/**
	 * Get the list of folders that changed or were deleted since given date.
	 * @throws ServerFault 
	 */
	FolderChanges listAddressBooksChanged(AccessToken token, Date timestamp) throws ServerFault;

	ContactChanges listContactsChanged(AccessToken token, Date lastSync, Integer addressBookId) throws ServerFault;
	
}
