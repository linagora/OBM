/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.contact;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.ContactChangesResponse;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.items.FolderChangesResponse;
import org.obm.sync.services.IAddressBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.StoreException;
import fr.aliacom.obm.services.constant.ConstantService;
import fr.aliacom.obm.utils.LogUtils;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * OBM {@link IAddressBook} web service implementation
 */
@Singleton
public class AddressBookBindingImpl implements IAddressBook {

	private static final Logger logger = LoggerFactory
			.getLogger(AddressBookBindingImpl.class);
	public static final String GLOBAL_ADDRESS_BOOK_SYNC = "globalAddressBookSync";
	public static final boolean GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE = true;

	private final ContactDao contactDao;
	private final UserDao userDao;
	private final ObmHelper obmHelper;
	private final ContactMerger contactMerger;
	private final ConstantService configuration;

	@Inject
	/*package*/ AddressBookBindingImpl(ContactDao contactDao, UserDao userDao, ContactMerger contactMerger, ObmHelper obmHelper, ConstantService configuration) {
		this.contactDao = contactDao;
		this.userDao = userDao;
		this.contactMerger = contactMerger;
		this.obmHelper = obmHelper;
		this.configuration = configuration;
	}

	@Override
	@Transactional
	public boolean isReadOnly(AccessToken token, BookType book) {
		if (book == BookType.contacts) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public BookType[] listBooks(AccessToken token) {
		return new BookType[] { BookType.contacts, BookType.users };
	}

	@Override
	@Transactional
	public List<AddressBook> listAllBooks(AccessToken token) throws ServerFault {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return contactDao.findAddressBooks(con, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error finding addressbooks ");
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}
	
	@Override
	@Transactional
	public ContactChangesResponse getSync(AccessToken token, BookType book, Date d)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getSync()");
			return getSync(book, d, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}
	}
	
	private ContactChangesResponse getSync(BookType book, Date timestamp, AccessToken token) throws ServerFault {
		ContactChangesResponse response = new ContactChangesResponse();
		Connection connection = null;
		try {
			if (book == BookType.users) {
				response.setChanges(getUsersChanges(token, timestamp));
			} else {
				response.setChanges(getContactsChanges(token, timestamp));
			}
			connection = obmHelper.getConnection();
			response.setLastSync(obmHelper.selectNow(connection));
		} catch (Throwable t) {
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw new ServerFault(t);
		} finally {
			obmHelper.cleanup(connection, null, null);
		}
		return response;
	}

	private ContactChanges getUsersChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		if (configuration.getBooleanValue(GLOBAL_ADDRESS_BOOK_SYNC, GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE)) {
			changes.setUpdated(userDao.findUpdatedUsers(timestamp, token).getContacts());
			changes.setRemoved(userDao.findRemovalCandidates(timestamp, token));			
		}
		return changes;
	}
	
	@Override
	@Transactional
	public AddressBookChangesResponse getAddressBookSync(AccessToken token, Date timestamp)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getAddressBookSync()");
			return getSync(token, timestamp);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error synchronizing contacts ", e);
		}
	}
	
	private AddressBookChangesResponse getSync(AccessToken token, Date timestamp) throws ServerFault {
		AddressBookChangesResponse response = new AddressBookChangesResponse();
		Connection connection = null;
		try {
			connection = obmHelper.getConnection();
			response.setContactChanges(getContactsChanges(token, timestamp));
			response.setBooksChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(connection));
		} catch (Throwable t) {
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw new ServerFault(t);
		} finally {
			obmHelper.cleanup(connection, null, null);
		}
		
		return response;
	}
	
	private ContactChanges getContactsChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		
		ContactUpdates contactUpdates = contactDao.findUpdatedContacts(timestamp, token);

		ContactUpdates userUpdates = new ContactUpdates();
		if (configuration.getBooleanValue(GLOBAL_ADDRESS_BOOK_SYNC,
				GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE)) {
			userUpdates = userDao.findUpdatedUsers(timestamp, token);
		} else {
			userUpdates = new ContactUpdates();
		}
		Set<Integer> removalCandidates = contactDao.findRemovalCandidates(timestamp, token);
		
		List<Contact> updated = getUpdatedContacts(contactUpdates, userUpdates);
		changes.setUpdated(updated);
		
		Set<Integer> removed = getRemovedContacts(contactUpdates, userUpdates, removalCandidates);
		changes.setRemoved(removed);
		
		return changes;
	}
	
	private Set<Integer> getRemovedContacts(ContactUpdates contactUpdates,
			ContactUpdates userUpdates, Set<Integer> removalCandidates) {
		SetView<Integer> archived = Sets.union( contactUpdates.getArchived(), userUpdates.getArchived());
		return Sets.union(archived, removalCandidates);
	}

	private List<Contact> getUpdatedContacts(ContactUpdates contactUpdates, ContactUpdates userUpdates) {
		List<Contact> updates = new ArrayList<Contact>(contactUpdates.getContacts().size()+userUpdates.getContacts().size());
		updates.addAll(contactUpdates.getContacts());
		updates.addAll(userUpdates.getContacts());
		return updates;
	}

	@Override
	@Transactional
	public Contact createContact(AccessToken token, BookType book, Contact contact)
		throws ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			Contact c = contactDao.createContact(token, contact);
			
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public Contact createContactWithoutDuplicate(AccessToken token, BookType book, Contact contact)
		throws ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			contact.setUid(null);

			List<String> duplicates = contactDao.findContactTwinKeys(token, contact);
			if (duplicates != null && !duplicates.isEmpty()) {
				logger.info(LogUtils.prefix(token) + "AddressBook : "
						+ duplicates.size()
						+ " duplicate(s) found for contact ["
						+ contact.getFirstname() + "," + contact.getLastname()
						+ "]");
				Integer contactId = Integer.parseInt(duplicates.get(0));
				contactDao.markUpdated(contactId);
				logger.info(LogUtils.prefix(token) + "Contact["+contactId+"] marked as updated");
				return contactDao.findContact(token, contactId);
			}

			Contact c = contactDao.createContact(token,	contact);
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private void checkContactsAddressBook(AccessToken token, BookType book) throws StoreException {
		if (isReadOnly(token, book)) {
			throw new StoreException("users not writable");
		}
		if (book != BookType.contacts) {
			throw new StoreException("booktype not supported");
		}
	}

	@Override
	@Transactional
	public Contact modifyContact(AccessToken token, BookType book, Contact contact)
		throws ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			Contact c = modifyContact(token, contact);

			logger.info(LogUtils.prefix(token) + "contact[" + c.getFirstname()
					+ " " + c.getLastname() + "] modified");
			return c;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Contact modifyContact(AccessToken token, Contact c) throws SQLException, FindException, EventNotFoundException, ServerFault {
		Contact modifiedContact;
		try {
			Contact previous = contactDao.findContact(token, c.getUid());
			if (!contactDao.hasRightsOn(token, c.getUid())) {
				logger.warn("contact " + c.getLastname() + " " + c.getFirstname()
						+ "(" + c.getUid() + ") not modified. not allowed for "
						+ token.getEmail());
				modifiedContact = previous;
			} else {
				contactMerger.merge(previous, c);
				modifiedContact = contactDao.modifyContact(token, c);
			}
		} catch (ContactNotFoundException e) {
			logger.warn("previous version not found for c.uid: " + c.getUid() + " c.last: " + c.getLastname());
			modifiedContact = c;
		}
		return modifiedContact;
	}
	
	@Override
	@Transactional
	public Contact removeContact(AccessToken token, BookType book, String uid) throws ServerFault, ContactNotFoundException { 
		try {
			Integer integerUid = Integer.valueOf(uid);
			
			checkContactsAddressBook(token, book);

			Contact c = contactDao.removeContact(token, integerUid);
			logger.info(LogUtils.prefix(token) + "contact[" + uid + "] removed (archived)");
			return c;
		} catch (NumberFormatException e) {
			throw new ContactNotFoundException("contact uid [" + uid +"] is not an integer");
		} catch (StoreException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		} catch (SQLException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Contact getContactFromId(AccessToken token, BookType book, String id)
			throws ServerFault {
		if (book != BookType.contacts) {
			return null;
		}

		try {
			int contactId = Integer.parseInt(id);
			return contactDao.findContact(token, contactId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public KeyList getContactTwinKeys(AccessToken token, BookType book, Contact contact)
		throws ServerFault {

		try {
			checkContactsAddressBook(token, book);
			
			List<String> keys = contactDao.findContactTwinKeys(token, contact);
			return new KeyList(keys);

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Contact> searchContact(AccessToken token, String query, int limit) 
		throws ServerFault {

		try {
			return contactDao.searchContact(token, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public FolderChangesResponse getFolderSync(AccessToken token, Date d)
			throws ServerFault {

		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getFolderSync(" + d + ")");
			FolderChangesResponse sync = getFolderSync(d, token);
			return sync;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
	}

	private FolderChangesResponse getFolderSync(Date timestamp, AccessToken token) throws Throwable {
		Connection connection = obmHelper.getConnection();
		try {
			FolderChangesResponse response = new FolderChangesResponse();
			response.setFolderChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(connection));
			return response;
		} catch (Throwable t) {
			throw  new ServerFault(t);
		} finally {
			obmHelper.cleanup(connection, null, null);
		}
	}

	private FolderChanges getFolderChanges(AccessToken token, Date timestamp) {
		FolderChanges changes = new FolderChanges();
		
		List<Folder> updated = contactDao.findUpdatedFolders(timestamp, token);
		if (configuration.getBooleanValue(GLOBAL_ADDRESS_BOOK_SYNC,
				GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE)) {
			updated.addAll(userDao.findUpdatedFolders(timestamp));
		}
		changes.setUpdated(updated);
		
		changes.setRemoved(contactDao.findRemovedFolders(timestamp, token));
		return changes;
	}

	@Override
	@Transactional
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook book, String query, int limit) throws ServerFault {

		try {
			return contactDao.searchContact(token, book, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Contact createContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws ServerFault {
		try {
			Contact c = contactDao.createContactInAddressBook(token, contact, addressBookId);
			
			logger.info(LogUtils.prefix(token) + "AddressBook : contact["
					+ c.getFirstname() + " " + c.getLastname() + "] created");
			return c;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public Contact modifyContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws ServerFault {
		try  {
			return modifyContact(token, contact);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
	}
	
	@Override
	@Transactional
	public Contact removeContactInBook(AccessToken token, int addressBookId, String uid) 
			throws ServerFault, ContactNotFoundException {
		return removeContact(token, BookType.contacts, uid);
	}
	
	@Override
	@Transactional
	public Contact getContactInBook(AccessToken token, int addressBookId,
			String id) throws ServerFault {
		return getContactFromId(token, BookType.contacts, id);
	}

	@Override
	@Transactional
	public boolean unsubscribeBook(AccessToken token, Integer addressBookId) throws ServerFault {
		try {
			return contactDao.unsubscribeBook(token, addressBookId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
}
