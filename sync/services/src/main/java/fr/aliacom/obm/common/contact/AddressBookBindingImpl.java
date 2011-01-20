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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.services.IAddressBook;

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
	public ContactChanges getSync(AccessToken token, BookType book, Date d)
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getSync()");
			return getSync(book, d, token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}

	}

	private ContactChanges getSync(BookType book, Date timestamp, AccessToken token) {
		ContactUpdates updated = new ContactUpdates();
		Set<Integer> todelete = new HashSet<Integer>();

		if (book == BookType.users) {
			updated = userDao.findUpdatedUsers(timestamp, token);
			todelete = userDao.findRemovalCandidates(timestamp, token);
		} else {
			ContactUpdates cu = contactDao.findUpdatedContacts(timestamp, token);
			updated.addAll(cu);
			updated.setLastSync(cu.getLastSync());
			todelete.addAll(contactDao.findRemovalCandidates(timestamp, token));
			todelete.addAll(cu.getArchived());
		}
		ContactChanges sync = new ContactChanges();
		sync.setLastSync(updated.getLastSync());
		sync.setUpdated(updated);
		sync.setRemoved(todelete);

		return sync;
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
	public FolderChanges getFolderSync(AccessToken token, Date d)
			throws AuthFault, ServerFault {

		try {
			logger.info(LogUtils.prefix(token) + "AddressBook : getFolderSync(" + d + ")");
			FolderChanges sync = getFolderSync(d, token);

			return sync;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error find contacts ");
		}
	}

	private FolderChanges getFolderSync(Date timestamp, AccessToken token) {
		FolderUpdates updated = new FolderUpdates();
		Set<Integer> todelete = new HashSet<Integer>();

		FolderUpdates fu = contactDao.findUpdatedFolders(timestamp, token);
		updated.addAll(fu);
		updated.setLastSync(fu.getLastSync());
		todelete.addAll(contactDao.findRemovedFolders(timestamp, token));

		FolderChanges sync = new FolderChanges();
		sync.setLastSync(updated.getLastSync());
		sync.setUpdated(updated);
		sync.setRemoved(todelete);

		return sync;
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
	
}
