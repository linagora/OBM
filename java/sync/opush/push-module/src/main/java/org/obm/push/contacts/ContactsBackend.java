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
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
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
	private final CollectionPathHelper collectionPathHelper;
	
	@Inject
	private ContactsBackend(MappingService mappingService, IAddressBook bookClient, 
			LoginService login, ContactConfiguration contactConfiguration,
			CollectionPathHelper collectionPathHelper) {
		
		super(mappingService, login);
		this.bookClient = bookClient;
		this.contactConfiguration = contactConfiguration;
		this.collectionPathHelper = collectionPathHelper;
	}

	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.CONTACTS;
	}
	
	public HierarchyItemsChanges getHierarchyChanges(UserDataRequest udr, Date lastSync) throws DaoException, UnexpectedObmSyncServerException {
		List<ItemChange> itemsChanged = new LinkedList<ItemChange>();
		List<ItemChange> itemsDeleted = new LinkedList<ItemChange>();
			
		FolderChanges folderChanges = listAddressBooksChanged(udr, lastSync);
		
		Iterator<Folder> folderChangesSorted = 
				sortedFolderChangesByDefaultAddressBook(folderChanges, contactConfiguration.getDefaultAddressBookName());
		while (folderChangesSorted.hasNext()) {
			itemsChanged.add( createItemChange(udr, folderChangesSorted.next()) );
		}
		
		for (Folder folder: folderChanges.getRemoved()) {
			ItemChange item = createItemDelete(udr, folder);
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

	private FolderChanges listAddressBooksChanged(UserDataRequest udr, Date lastSync) throws UnexpectedObmSyncServerException {
		AccessToken token = login(udr);
		try {
			return bookClient.listAddressBooksChanged(token, lastSync);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private ItemChange createItemChange(UserDataRequest udr, Folder folder) throws DaoException {
		boolean isNew = false;
		String collectionPath = getCollectionPath(udr, folder.getName());
		String serverId = getServerIdFromCollectionPath(udr, collectionPath);
		if (serverId == null) {
			serverId = mappingService.createCollectionMapping(udr.getDevice(), collectionPath);
			isNew = true;
		}
		String parentId = getParentId(udr, folder);
		FolderType itemType = getItemType(folder);
		return new ItemChange(serverId, parentId, folder.getName(), itemType, isNew);
	}

	private ItemChange createItemDelete(UserDataRequest udr, Folder folder) throws DaoException {
		String collectionPath = getCollectionPath(udr, folder.getName());
		String serverId = getServerIdFromCollectionPath(udr, collectionPath);
		if (serverId != null) {
			return new ItemChange(serverId);
		}
		return null;
	}
	
	private String getCollectionPath(UserDataRequest udr, String folderName)  {
		
		if (isDefaultFolder(folderName)) {
			return collectionPathHelper.buildCollectionPath(udr, PIMDataType.CONTACTS, folderName);
		} else {
			return collectionPathHelper.buildCollectionPath(udr, PIMDataType.CONTACTS, 
					contactConfiguration.getDefaultAddressBookName(), folderName);
		}
	}
	
	private String getServerIdFromCollectionPath(UserDataRequest udr, String collectionPath) throws DaoException {
		try {
			Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath);
			return mappingService.collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			return null;
		}	
	}
	
	private String getParentId(UserDataRequest udr, Folder folder) throws DaoException {
		String defaultParentId = contactConfiguration.getDefaultParentId();
		if (!isDefaultFolder(folder.getName())) {
			String collectionPath = getCollectionPath(udr, contactConfiguration.getDefaultAddressBookName());
			String parentId = getServerIdFromCollectionPath(udr, collectionPath);
			if (parentId != null) {
				return parentId;
			}
		}
		return defaultParentId;
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
	public int getItemEstimateSize(UserDataRequest udr, Integer collectionId, SyncState state, 
			SyncCollectionOptions syncCollectionOptions) throws CollectionNotFoundException, 
			DaoException, UnexpectedObmSyncServerException {
		
		DataDelta dataDelta = getChanged(udr, state, collectionId, syncCollectionOptions);
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(UserDataRequest udr, SyncState state, Integer collectionId, 
			SyncCollectionOptions syncCollectionOptions) throws UnexpectedObmSyncServerException, 
			DaoException, CollectionNotFoundException {
		
		Integer addressBookId = findAddressBookIdFromCollectionId(udr, collectionId);
		ContactChanges contactChanges = listContactsChanged(udr, state.getLastSync(), addressBookId);

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

	private Integer findAddressBookIdFromCollectionId(UserDataRequest udr, Integer collectionId) 
			throws UnexpectedObmSyncServerException, DaoException, CollectionNotFoundException {
		
		List<AddressBook> addressBooks = listAddressBooks(udr);
		for (AddressBook addressBook: addressBooks) {
			String colllectionPath = getCollectionPath(udr, addressBook.getName());
			try {
				Integer addressBookCollectionId = mappingService.getCollectionIdFor(udr.getDevice(), colllectionPath);
				if (addressBookCollectionId.intValue() == collectionId.intValue()) {
					return addressBook.getUid();
				}
			} catch (CollectionNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		throw new CollectionNotFoundException(collectionId);
	}
	
	private List<AddressBook> listAddressBooks(UserDataRequest udr) throws UnexpectedObmSyncServerException {
		AccessToken token = login(udr);
		try {
			return bookClient.listAllBooks(token);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private ContactChanges listContactsChanged(UserDataRequest udr, Date lastSync, Integer addressBookId) throws UnexpectedObmSyncServerException {
		AccessToken token = login(udr);
		try {
			return bookClient.listContactsChanged(token, lastSync, addressBookId);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
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
	public String createOrUpdate(UserDataRequest udr, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException,
			DaoException, UnexpectedObmSyncServerException,
			ItemNotFoundException {

		MSContact contact = (MSContact) data;
		Integer contactId = mappingService.getItemIdFromServerId(serverId);
		Integer addressBookId = findAddressBookIdFromCollectionId(udr, collectionId);
		try {

			if (serverId != null) {
				Contact convertedContact = new ContactConverter().contact(contact);
				convertedContact.setUid(contactId);
				modifyContact(udr, addressBookId, convertedContact);
			} else {
				Contact createdContact = createContact(udr, addressBookId, new ContactConverter().contact(contact));
				contactId = createdContact.getUid();
			}

		} catch (ContactNotFoundException e) {
			throw new ItemNotFoundException(e);
		} catch (NoPermissionException e) {
			logger.warn(e.getMessage());
			return null;
		} catch (ContactAlreadyExistException e) {
			logger.warn(e.getMessage());
			return null;
		}
		
		return mappingService.getServerIdFor(collectionId, String.valueOf(contactId));
	}

	private Contact modifyContact(UserDataRequest udr, Integer addressBookId, Contact contact) 
			throws UnexpectedObmSyncServerException, NoPermissionException, ContactNotFoundException {
		
		AccessToken token = login(udr);
		try {
			return bookClient.modifyContact(token, addressBookId, contact);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}
	
	private Contact createContact(UserDataRequest udr, Integer addressBookId, Contact contact) 
			throws UnexpectedObmSyncServerException, NoPermissionException, ContactAlreadyExistException {
		
		AccessToken token = login(udr);
		try {
			return bookClient.createContact(token, addressBookId, contact);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	@Override
	public void delete(UserDataRequest udr, Integer collectionId, String serverId, Boolean moveToTrash)
			throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ItemNotFoundException {
		
		Integer contactId = mappingService.getItemIdFromServerId(serverId);
		Integer addressBookId = findAddressBookIdFromCollectionId(udr, collectionId);
		try {
			removeContact(udr, addressBookId, contactId);
		} catch (NoPermissionException e) {
			logger.warn(e.getMessage());
		} catch (ContactNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}

	private Contact removeContact(UserDataRequest udr, Integer addressBookId, Integer contactId) 
			throws UnexpectedObmSyncServerException, NoPermissionException, ContactNotFoundException {
		
		AccessToken token = login(udr);
		try {
			return bookClient.removeContact(token, addressBookId, contactId);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, List<String> itemIds, SyncCollectionOptions options)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException {
		
		List<ItemChange> ret = new LinkedList<ItemChange>();
		for (String serverId: itemIds) {
			try {

				Integer contactId = mappingService.getItemIdFromServerId(serverId);
				Integer collectionId = mappingService.getCollectionIdFromServerId(serverId);
				Integer addressBookId = findAddressBookIdFromCollectionId(udr, collectionId);
				
				Contact contact = getContactFromId(udr, addressBookId, contactId);
				ret.add( convertContactToItemChange(collectionId, contact) );
				
			} catch (ContactNotFoundException e) {
				logger.error(e.getMessage());
			}
		}
		return ret;
	}

	private Contact getContactFromId(UserDataRequest udr, Integer addressBookId, Integer contactId) 
			throws UnexpectedObmSyncServerException, ContactNotFoundException {
		
		AccessToken token = login(udr);
		try {
			return bookClient.getContactFromId(token, addressBookId, contactId);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	public void createDefaultContactFolder(UserDataRequest udr) throws DaoException {
		String collectionPath = getCollectionPath(udr, contactConfiguration.getDefaultAddressBookName());
		String serverId = getServerIdFromCollectionPath(udr, collectionPath);
		if (serverId == null) {
			mappingService.createCollectionMapping(udr.getDevice(), collectionPath);
		}
	}


	@Override
	public String move(UserDataRequest udr, String srcFolder, String dstFolder,
			String messageId) throws CollectionNotFoundException,
			ProcessingEmailException {
		return null;
	}

	@Override
	public void emptyFolderContent(UserDataRequest udr, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException {
		throw new NotAllowedException(
				"emptyFolderContent is only supported for emails, collection was "
						+ collectionPath);
	}
	
	
}
