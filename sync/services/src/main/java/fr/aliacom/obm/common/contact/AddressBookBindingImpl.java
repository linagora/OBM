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

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
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

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.StoreException;
import fr.aliacom.obm.utils.LogUtils;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * OBM {@link IAddressBook} web service implementation
 */
@Singleton
public class AddressBookBindingImpl implements IAddressBook {

	private static final Log logger = LogFactory.getLog(AddressBookBindingImpl.class);

	private final ContactDao contactDao;
	private final UserDao userDao;
	private final ObmHelper obmHelper;
	private final ContactMerger contactMerger;

	@Inject
	private AddressBookBindingImpl(ContactDao contactDao, UserDao userDao, ContactMerger contactMerger, ObmHelper obmHelper) {
		this.contactDao = contactDao;
		this.userDao = userDao;
		this.contactMerger = contactMerger;
		this.obmHelper = obmHelper;
	}

	@Override
	public boolean isReadOnly(AccessToken token, BookType book)	throws AuthFault, ServerFault {
		if (book == BookType.contacts) {
			return false;
		}
		return true;
	}

	@Override
	public BookType[] listBooks(AccessToken token) {
		return new BookType[] { BookType.contacts, BookType.users };
	}

	@Override
	public List<AddressBook> listAllBooks(AccessToken token) throws AuthFault, ServerFault {
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
	public ContactChangesResponse getSync(AccessToken token, BookType book, Date d)
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getSync()");
			return getSync(book, d, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}
	}
	
	private ContactChangesResponse getSync(BookType book, Date timestamp, AccessToken token) throws Throwable {
		UserTransaction ut = obmHelper.getUserTransaction();
		ContactChangesResponse response = new ContactChangesResponse();

		try {
			ut.begin();
			if (book == BookType.users) {
				response.setChanges(getUsersChanges(token, timestamp));
			} else {
				response.setChanges(getContactsChanges(token, timestamp));
			}
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
			ut.commit();
		} catch (Throwable t) {
			ut.rollback();
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw t;
		}
		
		return response;
	}

	private ContactChanges getUsersChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		changes.setUpdated(userDao.findUpdatedUsers(timestamp, token).getContacts());
		changes.setRemoved(userDao.findRemovalCandidates(timestamp, token));
		return changes;
	}
	
	@Override
	public AddressBookChangesResponse getAddressBookSync(AccessToken token, Date timestamp)
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getAddressBookSync()");
			return getSync(token, timestamp);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error synchronizing contacts ");
		}
	}
	
	private AddressBookChangesResponse getSync(AccessToken token, Date timestamp) throws Throwable {
		UserTransaction ut = obmHelper.getUserTransaction();
		AddressBookChangesResponse response = new AddressBookChangesResponse();
		try {
			ut.begin();
			response.setContactChanges(getContactsChanges(token, timestamp));
			response.setBooksChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
			ut.commit();
		} catch (Throwable t) {
			ut.rollback();
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw t;
		}
		
		return response;
	}
	
	private ContactChanges getContactsChanges(AccessToken token, Date timestamp) {
		ContactChanges changes = new ContactChanges();
		
		ContactUpdates contactUpdates = contactDao.findUpdatedContacts(timestamp, token);
		ContactUpdates userUpdates = userDao.findUpdatedUsers(timestamp, token);
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
	public Contact createContact(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

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
	public Contact createContactWithoutDuplicate(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

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

	private void checkContactsAddressBook(AccessToken token, BookType book)
			throws AuthFault, ServerFault, StoreException {
		
		if (isReadOnly(token, book)) {
			throw new StoreException("users not writable");
		}
		
		if (book != BookType.contacts) {
			throw new StoreException("booktype not supported");
		}
	}

	@Override
	public Contact modifyContact(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

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

	private Contact modifyContact(AccessToken token, Contact c)
		throws SQLException {

		Contact previous = contactDao.findContact(token, c.getUid());
		if (previous != null) {
			contactMerger.merge(previous, c);
		} else {
			logger.warn("previous version not found for c.uid: "
					+ c.getUid() + " c.last: " + c.getLastname());
		}
		return contactDao.modifyContact(token, c);
	}
	
	@Override
	public Contact removeContact(AccessToken token, BookType book, String uid)
			throws AuthFault, ServerFault {

		Integer integerUid = null;
		try {
			integerUid = Integer.valueOf(uid);
		} catch (NumberFormatException e) {
			logger.error(LogUtils.prefix(token) + "contact uid is not an integer", e);
			return null;
		}

		try {
			checkContactsAddressBook(token, book);

			Contact c = contactDao.removeContact(token, integerUid);
			logger.info(LogUtils.prefix(token) + "contact[" + uid
					+ "] removed (archived)");
			return c;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public Contact getContactFromId(AccessToken token, BookType book, String id)
			throws AuthFault, ServerFault {
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
	public KeyList getContactTwinKeys(AccessToken token, BookType book, Contact contact)
		throws AuthFault, ServerFault {

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
	public List<Contact> searchContact(AccessToken token, String query, int limit) 
		throws AuthFault, ServerFault {

		try {
			return contactDao.searchContact(token, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public FolderChangesResponse getFolderSync(AccessToken token, Date d)
			throws AuthFault, ServerFault {

		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getFolderSync(" + d + ")");
			FolderChangesResponse sync = getFolderSync(d, token);
			return sync;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}
	}

	private FolderChangesResponse getFolderSync(Date timestamp, AccessToken token) throws Throwable {
		UserTransaction ut = obmHelper.getUserTransaction();
		
		try {
			ut.begin();
			FolderChangesResponse response = new FolderChangesResponse();
			response.setFolderChanges(getFolderChanges(token, timestamp));
			response.setLastSync(obmHelper.selectNow(obmHelper.getConnection()));
			ut.commit();
			return response;
		} catch (Throwable t) {
			ut.rollback();
			logger.error(LogUtils.prefix(token) + t.getMessage(), t);
			throw t;
		}
	}

	private FolderChanges getFolderChanges(AccessToken token, Date timestamp) {
		FolderChanges changes = new FolderChanges();
		
		List<Folder> updated = contactDao.findUpdatedFolders(timestamp, token);
		updated.addAll(userDao.findUpdatedFolders(timestamp));
		changes.setUpdated(updated);
		
		changes.setRemoved(contactDao.findRemovedFolders(timestamp, token));
		return changes;
	}
	

	@Override
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook book, String query, int limit) throws AuthFault, ServerFault {

		try {
			return contactDao.searchContact(token, book, query, limit);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public Contact createContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws AuthFault, ServerFault {
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
	public Contact modifyContactInBook(AccessToken token, int addressBookId,
			Contact contact) throws AuthFault, ServerFault {
		try  {
			return modifyContact(token, contact);
		} catch (SQLException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	public Contact removeContactInBook(AccessToken token, int addressBookId,
			String uid) throws AuthFault, ServerFault {
		return removeContact(token, BookType.contacts, uid);
	}
	
	@Override
	public Contact getContactInBook(AccessToken token, int addressBookId,
			String id) throws AuthFault, ServerFault {
		return getContactFromId(token, BookType.contacts, id);
	}

	@Override
	public boolean unsubscribeBook(AccessToken token, Integer addressBookId) throws AuthFault, ServerFault {
		try {
			return contactDao.unsubscribeBook(token, addressBookId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
}