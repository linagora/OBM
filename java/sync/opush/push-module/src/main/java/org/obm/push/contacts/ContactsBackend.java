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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.naming.NoPermissionException;

import org.obm.configuration.ContactConfiguration;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ServerItemNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.service.impl.MappingService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.exception.ContactAlreadyExistException;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.services.IAddressBook;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContactsBackend extends ObmSyncBackend implements PIMBackend {

	private final ContactConfiguration contactConfiguration;
	private final IAddressBook bookClient;
	
	@Inject
	private ContactsBackend(MappingService mappingService, IAddressBook bookClient, 
			LoginService login, ContactConfiguration contactConfiguration) {
		
		super(mappingService, login);
		this.bookClient = bookClient;
		this.contactConfiguration = contactConfiguration;
	}

	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.CONTACTS;
	}
	
	public HierarchyItemsChanges getHierarchyChanges(BackendSession bs, Date lastSync) throws DaoException, UnknownObmSyncServerException {
		List<ItemChange> itemsChanged = new LinkedList<ItemChange>();
		List<ItemChange> itemsDeleted = new LinkedList<ItemChange>();
			
		FolderChanges folderChanges = listAddressBooksChanged(bs, lastSync);
		
		Iterator<Folder> folderChangesSorted = 
				sortedFolderChangesByDefaultAddressBook(folderChanges, contactConfiguration.getDefaultAddressBookName());
		while (folderChangesSorted.hasNext()) {
			itemsChanged.add( createItemChange(bs, folderChangesSorted.next()) );
		}
		
		for (Folder folder: folderChanges.getRemoved()) {
			ItemChange item = createItemDelete(bs, folder);
			if (item != null) {
				itemsDeleted.add( item );
			}
		}
		
		return new HierarchyItemsChanges(itemsChanged, itemsDeleted, folderChanges.getLastSync());
	}

	private Iterator<Folder> sortedFolderChangesByDefaultAddressBook(FolderChanges folderChanges, String defaultAddressBookName) {
		TreeSet<Folder> treeSet = new TreeSet<Folder>( new ComparatorUsingFolderName(defaultAddressBookName) );
		treeSet.addAll(folderChanges.getUpdated());
		return treeSet.iterator();
	}

	private FolderChanges listAddressBooksChanged(BackendSession bs, Date lastSync) throws UnknownObmSyncServerException {
		AccessToken token = login(bs);
		try {
			return bookClient.listAddressBooksChanged(token, lastSync);
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
			serverId = mappingService.createCollectionMapping(bs.getDevice(), collectionPath);
			isNew = true;
		}
		String parentId = getParentId(bs, folder);
		FolderType itemType = getItemType(folder);
		return new ItemChange(serverId, parentId, folder.getName(), itemType, isNew);
	}

	private ItemChange createItemDelete(BackendSession bs, Folder folder) throws DaoException {
		String collectionPath = getCollectionPath(bs, folder.getName());
		String serverId = getServerIdFromCollectionPath(bs, collectionPath);
		if (serverId != null) {
			return new ItemChange(serverId);
		}
		return null;
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
			Integer collectionId = mappingService.getCollectionIdFor(bs.getDevice(), collectionPath);
			return mappingService.collectionIdToString(collectionId);
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
			return FolderType.USER_CREATED_CONTACTS_FOLDER;
		}
	}
	
	private boolean isDefaultFolder(String folderName) {
		if (folderName.equalsIgnoreCase(contactConfiguration.getDefaultAddressBookName())) {
			return true;
		}
		return false;
	}
	
	@Override
	public int getItemEstimateSize(BackendSession bs, FilterType filterType,
			Integer collectionId, SyncState state)
			throws CollectionNotFoundException, ProcessingEmailException,
			DaoException, UnknownObmSyncServerException {
		DataDelta dataDelta = getChanged(bs, state, filterType, collectionId);
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state,
			FilterType filterType, Integer collectionId) throws DaoException,
			CollectionNotFoundException, UnknownObmSyncServerException,
			ProcessingEmailException {
		return getChanged(bs, state, collectionId);
	}
	
	public DataDelta getChanged(BackendSession bs, SyncState state, Integer collectionId) 
			throws UnknownObmSyncServerException, DaoException, CollectionNotFoundException {
		
		Integer addressBookId = findAddressBookIdFromCollectionId(bs,collectionId);
		ContactChanges contactChanges = listContactsChanged(bs,state.getLastSync(), addressBookId);

		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		for (Contact contact : contactChanges.getUpdated()) {
			addUpd.add(convertContactToItemChange(collectionId, contact));
		}

		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Integer remove : contactChanges.getRemoved()) {
			ItemChange change = mappingService.getItemChange(collectionId, String.valueOf(remove));
			deletions.add(change);
		}

		return new DataDelta(addUpd, deletions, contactChanges.getLastSync());
	}

	private Integer findAddressBookIdFromCollectionId(BackendSession bs, Integer collectionId) 
			throws UnknownObmSyncServerException, DaoException, CollectionNotFoundException {
		
		List<AddressBook> addressBooks = listAddressBooks(bs);
		for (AddressBook addressBook: addressBooks) {
			String colllectionPath = getCollectionPath(bs, addressBook.getName());
			try {
				Integer addressBookCollectionId = mappingService.getCollectionIdFor(bs.getDevice(), colllectionPath);
				if (addressBookCollectionId.intValue() == collectionId.intValue()) {
					return addressBook.getUid();
				}
			} catch (CollectionNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		throw new CollectionNotFoundException(collectionId);
	}
	
	private List<AddressBook> listAddressBooks(BackendSession bs) throws UnknownObmSyncServerException {
		AccessToken token = login(bs);
		try {
			return bookClient.listAllBooks(token);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private ContactChanges listContactsChanged(BackendSession bs, Date lastSync, Integer addressBookId) throws UnknownObmSyncServerException {
		AccessToken token = login(bs);
		try {
			return bookClient.listContactsChanged(token, lastSync, addressBookId);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}
	
	private ItemChange convertContactToItemChange(Integer collectionId, Contact contact) {
		ItemChange ic = new ItemChange();
		ic.setServerId( mappingService.getServerIdFor(collectionId, String.valueOf(contact.getUid())) );
		ic.setData( new ContactConverter().convert(contact) );
		return ic;
	}

	@Override
	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException,
			DaoException, UnknownObmSyncServerException,
			ServerItemNotFoundException {

		MSContact contact = (MSContact) data;
		Integer contactId = mappingService.getItemIdFromServerId(serverId);
		Integer addressBookId = findAddressBookIdFromCollectionId(bs, collectionId);
		try {

			if (serverId != null) {
				Contact convertedContact = new ContactConverter().contact(contact);
				convertedContact.setUid(contactId);
				modifyContact(bs, addressBookId, convertedContact);
			} else {
				Contact createdContact = createContact(bs, addressBookId, new ContactConverter().contact(contact));
				contactId = createdContact.getUid();
			}

		} catch (ContactNotFoundException e) {
			throw new ServerItemNotFoundException(e);
		} catch (NoPermissionException e) {
			logger.warn(e.getMessage());
			return null;
		} catch (ContactAlreadyExistException e) {
			logger.warn(e.getMessage());
			return null;
		}
		
		return mappingService.getServerIdFor(collectionId, String.valueOf(contactId));
	}

	private Contact modifyContact(BackendSession bs, Integer addressBookId, Contact contact) 
			throws UnknownObmSyncServerException, NoPermissionException, ContactNotFoundException {
		
		AccessToken token = login(bs);
		try {
			return bookClient.modifyContact(token, addressBookId, contact);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}
	
	private Contact createContact(BackendSession bs, Integer addressBookId, Contact contact) 
			throws UnknownObmSyncServerException, NoPermissionException, ContactAlreadyExistException {
		
		AccessToken token = login(bs);
		try {
			return bookClient.createContact(token, addressBookId, contact);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	@Override
	public void delete(BackendSession bs, Integer collectionId, String serverId, Boolean moveToTrash)
			throws CollectionNotFoundException, DaoException,
			UnknownObmSyncServerException, ServerItemNotFoundException {
		
		Integer contactId = mappingService.getItemIdFromServerId(serverId);
		Integer addressBookId = findAddressBookIdFromCollectionId(bs, collectionId);
		try {
			removeContact(bs, addressBookId, contactId);
		} catch (NoPermissionException e) {
			logger.warn(e.getMessage());
		} catch (ContactNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}

	private Contact removeContact(BackendSession bs, Integer addressBookId, Integer contactId) 
			throws UnknownObmSyncServerException, NoPermissionException, ContactNotFoundException {
		
		AccessToken token = login(bs);
		try {
			return bookClient.removeContact(token, addressBookId, contactId);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> fetchIds)
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		
		List<ItemChange> ret = new LinkedList<ItemChange>();
		for (String serverId: fetchIds) {
			try {

				Integer contactId = mappingService.getItemIdFromServerId(serverId);
				Integer collectionId = mappingService.getCollectionIdFromServerId(serverId);
				Integer addressBookId = findAddressBookIdFromCollectionId(bs, collectionId);
				
				Contact contact = getContactFromId(bs, addressBookId, contactId);
				ret.add( convertContactToItemChange(collectionId, contact) );
				
			} catch (ContactNotFoundException e) {
				logger.error(e.getMessage());
			}
		}
		return ret;
	}

	private Contact getContactFromId(BackendSession bs, Integer addressBookId, Integer contactId) 
			throws UnknownObmSyncServerException, ContactNotFoundException {
		
		AccessToken token = login(bs);
		try {
			return bookClient.getContactFromId(token, addressBookId, contactId);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	public void createDefaultContactFolder(BackendSession bs) throws DaoException {
		String collectionPath = getCollectionPath(bs, contactConfiguration.getDefaultAddressBookName());
		String serverId = getServerIdFromCollectionPath(bs, collectionPath);
		if (serverId == null) {
			mappingService.createCollectionMapping(bs.getDevice(), collectionPath);
		}
	}


	@Override
	public String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) throws CollectionNotFoundException,
			ProcessingEmailException {
		return null;
	}

	@Override
	public void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException {
		throw new NotAllowedException(
				"emptyFolderContent is only supported for emails, collection was "
						+ collectionPath);
	}
	
	
}
