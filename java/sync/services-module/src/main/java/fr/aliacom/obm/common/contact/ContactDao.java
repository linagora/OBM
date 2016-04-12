/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.contact;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.annotations.database.DatabaseEntity;
import org.obm.locator.LocatorClientException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.book.DeletedContact;
import org.obm.sync.book.Folder;
import org.obm.sync.exception.ContactNotFoundException;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;

public interface ContactDao {

	ContactUpdates findUpdatedContacts(Date timestamp, AccessToken at) throws SQLException;

	Contact createContact(AccessToken at, Connection con, Contact c) throws SQLException, ServerFault;

	Contact createContactInAddressBook(AccessToken at, Contact c, int addressbookId)
			throws SQLException, ServerFault;

	Contact createCollectedContact(String name, String email, ObmDomain domain, Integer ownerId)
			throws ServerFault, SQLException;

	Contact modifyContact(AccessToken token, @DatabaseEntity Contact c)
			throws SQLException, FindException, EventNotFoundException, ServerFault;

	Contact updateContact(AccessToken token, @DatabaseEntity Contact contact) 
			throws SQLException, FindException, EventNotFoundException, ServerFault;

	boolean hasRightsOnAddressBook(AccessToken token, int addressBookId);

	/**
	 * @return the contact with the given id if it is not archived
	 * @throws ContactNotFoundException 
	 * @throws SQLException 
	 */
	Contact findContact(AccessToken token, int contactId) throws ContactNotFoundException, SQLException;

	Contact findAttendeeContactFromEmailForUser(String email, Integer userId) throws SQLException;

	Contact removeContact(AccessToken at, Contact c) throws ServerFault, SQLException;

	Set<DeletedContact> findRemovalCandidates(Date d, AccessToken at) throws SQLException;

	List<AddressBook> findAddressBooks(AccessToken at) throws SQLException;

	List<Contact> searchContactsInAddressBooksList(AccessToken at, Collection<AddressBook> addrBooks, String query,
			int limit, Integer offset)
			throws MalformedURLException, LocatorClientException, SQLException;

	/**
	 * Search contacts. Query will match against lastname, firstname & email
	 * prefixes.
	 */
	List<Contact> searchContact(AccessToken at, AddressBook book, String query, int limit, Integer offset);

	Set<Folder> findUpdatedFolders(Date timestamp, AccessToken at) throws SQLException;

	Set<Folder> findRemovedFolders(Date date, AccessToken at) throws SQLException;

	int markUpdated(int databaseId) throws SQLException;

	boolean unsubscribeBook(AccessToken at, Integer addressBookId) throws SQLException;

	ContactUpdates findUpdatedContacts(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException;

	Set<DeletedContact> findRemovalCandidates(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException;

	Collection<AddressBook> listSynchronizedAddressBooks(AccessToken token) throws SQLException;

	int countContactsInGroup(int gid) throws SQLException;

}