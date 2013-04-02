/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.services;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.naming.NoPermissionException;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.exception.InvalidContactException;
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
	 * 
	 * @param clientId
	 *            a SHA1, when this method is called many times with the same value for clientId,
	 *            only one event must be created.
	 *            Other calls returns the event created on first call. (idempotence)
	 *            This param is not mandatory, it must have a size of 40 characters when not null
	 *            
	 */
	Contact createContact(AccessToken token, Integer addressBook, Contact contact, String clientId) 
			throws ServerFault, NoPermissionException, InvalidContactException;

	/**
	 * modify existing contact with data provided if possible.
	 */
	Contact modifyContact(AccessToken token, Integer addressBookId, Contact contact) 
			throws ServerFault, NoPermissionException, ContactNotFoundException, InvalidContactException;

	/**
	 * remove the contact with specified uid 
	 */
	Contact removeContact(AccessToken token, Integer addressBookId, Integer contactId) throws ServerFault, ContactNotFoundException, NoPermissionException;

	/**
	 * Search contacts using a solr query
	 */
	List<Contact> searchContact(AccessToken token, String query,
			int limit, Integer offset) throws ServerFault;

	// Methods for Funambol sync

	/**
	 * Retrieve a contact by its uid
	 */
	Contact getContactFromId(AccessToken token, Integer addressBookId, Integer contactId) throws ServerFault, ContactNotFoundException;

	/**
	 * Search contact similar to the given one.
	 */
	KeyList getContactTwinKeys(AccessToken token, Contact contact);

	/**
	 * Search contacts in a group, based on a solr query
	 */
	List<Contact> searchContactInGroup(AccessToken token, AddressBook group,
			String query, int limit, Integer offset) throws ServerFault;


	boolean unsubscribeBook(AccessToken token, Integer bookId) throws ServerFault;

	int countContactsInGroup(AccessToken token, int gid) throws ServerFault, SQLException;

	/**
	 * Get the list of folders that changed or were deleted since given date.
	 * @throws ServerFault 
	 */
	FolderChanges listAddressBooksChanged(AccessToken token, Date timestamp) throws ServerFault;

	ContactChanges listContactsChanged(AccessToken token, Date lastSync, Integer addressBookId) throws ServerFault;

	List<Contact> searchContactsInSynchronizedAddressBooks(AccessToken token, String query, int limit, Integer offset) throws ServerFault;
	
}
