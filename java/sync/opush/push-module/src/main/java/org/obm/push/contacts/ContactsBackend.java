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
package org.obm.push.contacts;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.configuration.ContactConfiguration;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ServerItemNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.services.ICalendar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ContactsBackend extends ObmSyncBackend {

	private final ContactConfiguration contactConfiguration;
	
	@Inject
	private ContactsBackend(CollectionDao collectionDao, IAddressBook bookClient, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			@Named(CalendarType.TODO) ICalendar todoClient,
			LoginService login, ContactConfiguration contactConfiguration) {
		
		super(collectionDao, bookClient, calendarClient, todoClient, login);
		this.contactConfiguration = contactConfiguration;
	}

	public HierarchyItemsChanges getHierarchyChanges(BackendSession bs, Date lastSync) throws DaoException, UnknownObmSyncServerException {
		List<ItemChange> itemsChanged = new LinkedList<ItemChange>();
		List<ItemChange> itemsDeleted = new LinkedList<ItemChange>();
			
		FolderChanges folderChanges = listAddressBooksChanged(bs, lastSync);
		
		for (Folder folder: folderChanges.getUpdated()) {
			itemsChanged.add( createItemChange(bs, folder) );
		}
		
		for (Integer collectionId: folderChanges.getRemoved()) {
			String toString = collectionIdToString(collectionId);
			itemsDeleted.add( new ItemChange(toString) );
		}
		
		return new HierarchyItemsChanges(itemsChanged, itemsDeleted, folderChanges.getLastSync());
	}
	
	private FolderChanges listAddressBooksChanged(BackendSession bs, Date lastSync) throws UnknownObmSyncServerException {
		IAddressBook bc = getBookClient();
		AccessToken token = login(bs);
		try {
			return bc.listAddressBooksChanged(token, lastSync);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private ItemChange createItemChange(BackendSession bs, Folder folder) throws DaoException {
		boolean isNew = false;
		String collectionPath = getCollectionPath(bs, folder.getName());
		String serverId = getServerIdFromCollectionPath(bs, collectionPath);
		if (serverId == null) {
			serverId = createCollectionMapping(bs.getDevice(), collectionPath);
			isNew = true;
		}
		String parentId = getParentId(bs, folder);
		FolderType itemType = getItemType(folder);
		return new ItemChange(serverId, parentId, folder.getName(), itemType, isNew);
	}

	private String getCollectionPath(BackendSession bs, String folderName)  {
		if (isDefaultFolder(folderName)) {
			return "obm:\\\\" + bs.getUser().getLoginAtDomain() + "\\\\" + folderName;
		} else {
			return "obm:\\\\" + bs.getUser().getLoginAtDomain() + "\\\\" + 
						contactConfiguration.getDefaultAddressBookName() + "\\\\" + folderName;
		}
	}
	
	private String getServerIdFromCollectionPath(BackendSession bs, String collectionPath) throws DaoException {
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), collectionPath);
			return collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			return null;
		}	
	}
	
	private String getParentId(BackendSession bs, Folder folder) throws DaoException {
		if (isDefaultFolder(folder.getName())) {
			return contactConfiguration.getDefaultParentId(); 
		} else {
			String collectionPath = getCollectionPath(bs, contactConfiguration.getDefaultAddressBookName());
			return getServerIdFromCollectionPath(bs, collectionPath);
		}
	}
	
	private FolderType getItemType(Folder folder) {
		if (isDefaultFolder(folder.getName())) {
			return FolderType.DEFAULT_CONTACTS_FOLDER;
		} else {
			return FolderType.DEFAULT_CONTACTS_FOLDER;
		}
	}
	
	private boolean isDefaultFolder(String folderName) {
		if (folderName.equalsIgnoreCase(contactConfiguration.getDefaultAddressBookName())) {
			return true;
		}
		return false;
	}
	
	public DataDelta getContentChanges(BackendSession bs, SyncState state, Integer defaultCollectionId) throws UnknownObmSyncServerException, DaoException {
		IAddressBook bc = getBookClient();
		AccessToken token = login(bs);

		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		try {
			ContactChanges changes = bc.listContactsChanged(token, state.getLastSync());
			List<AddressBook> addressBooks = bc.listAllBooks(token);
			
			for (Contact contact: changes.getUpdated()) {
				ItemChange change = getContactChange(defaultCollectionId, contact, bs, addressBooks);
				addUpd.add(change);
			}

			for (Integer remove: changes.getRemoved()) {
				ItemChange change = getItemChange(defaultCollectionId, String.valueOf(remove));
				deletions.add(change);
			}
			
			return new DataDelta(addUpd, deletions, changes.getLastSync());
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private ItemChange getContactChange(Integer defaultCollectionId, Contact c, BackendSession bs, List<AddressBook> addressBooks) throws DaoException {
		ItemChange ic = new ItemChange();
		Integer collectionId = findAddressBookCollectionId(c.getFolderId(), bs, addressBooks);
		if (collectionId != null) {
			ic.setServerId( getServerIdFor(collectionId, String.valueOf(c.getUid())) );
		} else {
			ic.setServerId( getServerIdFor(defaultCollectionId, String.valueOf(c.getUid())) );
		}
		ic.setData( new ContactConverter().convert(c) );
		return ic;
	}

	private Integer findAddressBookCollectionId(Integer folderId, BackendSession bs, List<AddressBook> addressBooks) throws DaoException {
		AddressBook addressBook = findAddressBookFromId(folderId, addressBooks);
		if (addressBook != null) {
			String colllectionPath = getCollectionPath(bs, addressBook.getName());
			try {
				return getCollectionIdFor(bs.getDevice(), colllectionPath);
			} catch (CollectionNotFoundException e) {
				logger.warn("Address book not found, so, adding contact to defaut address book");
			}	
		}
		return null;
	}
	
	private AddressBook findAddressBookFromId(Integer folderId, List<AddressBook> addressBooks) {
		for (AddressBook addressBook: addressBooks) {
			if (addressBook.getUid().equals(folderId)) {
				return addressBook;
			}
		}
		return null;
	}
	
	public String createOrUpdate(BackendSession bs, Integer collectionId, String serverId, MSContact data) throws UnknownObmSyncServerException {
		logger.info("create contact ({} | {}) in collectionId {}", 
				new Object[]{data.getFirstName(), data.getLastName(), collectionId});

		IAddressBook bc = getBookClient();
		AccessToken token = login(bs);

		String itemId = null;
		try {
			if (serverId != null) {
				int idx = serverId.lastIndexOf(":");
				itemId = serverId.substring(idx + 1);
				Contact convertedContact = new ContactConverter().contact(data);
				convertedContact.setUid(Integer.parseInt(itemId));
				bc.modifyContact(token, BookType.contacts, convertedContact);
			} else {
				Contact createdContact = bc.createContactWithoutDuplicate(token, BookType.contacts,
						new ContactConverter().contact(data));
				itemId = createdContact.getUid().toString();
			}
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}

		return getServerIdFor(collectionId, itemId);
	}

	public void delete(BackendSession bs, String serverId) throws UnknownObmSyncServerException, ServerItemNotFoundException {
		logger.info("delete serverId {}", serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				IAddressBook bc = getBookClient();
				AccessToken token = login(bs);
				try {
					bc.removeContact(token, BookType.contacts, serverId.substring(idx + 1) );
				} catch (ServerFault e) {
					throw new UnknownObmSyncServerException(e);
				} catch (ContactNotFoundException e) {
					throw new ServerItemNotFoundException(serverId);
				} finally {
					logout(token);
				}
			}
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchServerIds) {
		IAddressBook bc = getBookClient();
		AccessToken token = login(bs);

		List<ItemChange> ret = new LinkedList<ItemChange>();
		for (String serverId: fetchServerIds) {
			Integer id = getItemIdFor(serverId);
			if (id != null) {
				try {
					Contact c = bc.getContactFromId(token, BookType.contacts, id.toString());
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					MSContact cal = new ContactConverter().convert(c);
					ic.setData(cal);
					ret.add(ic);
				} catch (ServerFault e) {
					logger.error("Contact from id {} not found", id.toString());
				}
			}
		}
		logout(token);
		return ret;
	}
	
}