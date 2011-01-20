package org.obm.sync.services;

import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.items.ContactChanges;

public interface IAddressBook {

	/**
	 * check if given book is read only for logged user
	 */
	public boolean isReadOnly(AccessToken token, BookType book)
			throws AuthFault, ServerFault;

	
	/**
	 * list accessible book types for logged user
	 */
	public BookType[] listBooks(AccessToken token);
	
	/**
	 * List all accessible books for logged user
	 */
	public List<AddressBook> listAllBooks(AccessToken token) throws AuthFault, ServerFault;

	/**
	 * get a list of updated and removed contacts sync given date for a given BookType
	 */
	public ContactChanges getSync(AccessToken token, BookType book, Date date)
			throws AuthFault, ServerFault;

	/**
	 * Create the given contact into given book if book is writable
	 */
	public Contact createContact(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault;

	/**
	 * try to create the given contact : if it finds a similar contact,
	 * no contact will be created, but the first similar contact will be return
	 * 
	 * Please use it only if you know what you are doing.
	 */
	public Contact createContactWithoutDuplicate(AccessToken token,
			BookType book, Contact contact) throws AuthFault, ServerFault;

	/**
	 * modify existing contact with data provided if possible.
	 */
	public Contact modifyContact(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault;

	/**
	 * remove the contact with specified uid 
	 */
	public Contact removeContact(AccessToken token, BookType book, String uid)
			throws AuthFault, ServerFault;

	/**
	 * Search contacts using a solr query
	 */
	public List<Contact> searchContact(AccessToken token, String query,
			int limit) throws AuthFault, ServerFault;

	// Methods for Funambol sync

	/**
	 * Retrieve a contact by its uid
	 */
	public Contact getContactFromId(AccessToken token, BookType book, String id)
			throws AuthFault, ServerFault;

	
	/**
	 * Search contact similar to the given one.
	 */
	public KeyList getContactTwinKeys(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault;

	/**
	 * Get the list of folders that changed or were deleted since given date.
	 */
	public FolderChanges getFolderSync(AccessToken token, Date lastSync)
			throws AuthFault, ServerFault;

	/**
	 * Search contacts in a group, based on a solr query
	 */
	List<Contact> searchContactInGroup(AccessToken token, AddressBook group,
			String query, int limit) throws AuthFault, ServerFault;
	
}
