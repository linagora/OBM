package org.obm.sync.services;

import java.util.Date;
import java.util.List;

import javax.naming.NoPermissionException;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.exception.ContactAlreadyExistException;
import org.obm.sync.exception.ContactNotFoundException;
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
	Contact createContact(AccessToken token, Integer addressBook, Contact contact) 
			throws ServerFault, ContactAlreadyExistException, NoPermissionException;

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
	Contact removeContact(AccessToken token, Integer addressBookId, Integer contactId) throws ServerFault, ContactNotFoundException, NoPermissionException;

	/**
	 * Search contacts using a solr query
	 */
	List<Contact> searchContact(AccessToken token, String query,
			int limit) throws ServerFault;

	// Methods for Funambol sync

	/**
	 * Retrieve a contact by its uid
	 */
	Contact getContactFromId(AccessToken token, Integer addressBookId, Integer contactId) throws ServerFault, ContactNotFoundException;

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
