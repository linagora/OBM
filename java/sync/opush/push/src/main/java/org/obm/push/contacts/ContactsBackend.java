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
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.items.ContactChangesResponse;
import org.obm.sync.items.FolderChanges;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContactsBackend extends ObmSyncBackend {

	private final ContactConfiguration contactConfiguration;

	@Inject
	private ContactsBackend(ContactConfiguration contactConfiguration, CollectionDao collectionDao, BookClient bookClient, 
			CalendarClient calendarClient, TodoClient todoClient) {
		
		super(collectionDao, bookClient, calendarClient, todoClient);
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
		BookClient bc = getBookClient();
		AccessToken token = login(bc, bs);
		try {
			return bc.listAddressBooksChanged(token, lastSync);
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			bc.logout(token);
		}
	}
	
	private ItemChange createItemChange(BackendSession bs, Folder folder) throws DaoException {
		ItemChange itemChange = new ItemChange();
		String col = "obm:\\\\" + bs.getUser().getLoginAtDomain() + "\\" + folder.getName();
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
			serverId = collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = createCollectionMapping(bs.getDevice(), col);
			itemChange.setIsNew(true);
		}

		itemChange.setServerId(serverId);
		itemChange.setParentId("0");
		itemChange.setDisplayName(folder.getName());
		itemChange.setItemType(getItemType(folder));
		return itemChange;
	}

	private FolderType getItemType(Folder folder) {
		if (folder.getName().equalsIgnoreCase(contactConfiguration.getDefaultAddressBookName())) {
			return FolderType.DEFAULT_CONTACTS_FOLDER;
		} else {
			return FolderType.USER_CREATED_CONTACTS_FOLDER;
		}
	}
	
	public DataDelta getContentChanges(BackendSession bs, SyncState state, Integer collectionId) throws UnknownObmSyncServerException {
		BookClient bc = getBookClient();
		AccessToken token = login(bc, bs);
		
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		Date lastSync = null;
		
		try {
			ContactChangesResponse changes = bc.getSync(token, BookType.contacts, state.getLastSync());
			for (Contact c: changes.getUpdated()) {
				ItemChange change = getContactChange(collectionId, c);
				addUpd.add(change);
			}

			for (Integer del: changes.getRemoved()) {
				ItemChange change = getItemChange(collectionId, "" + del);
				deletions.add(change);
			}
			lastSync = changes.getLastSync();
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			bc.logout(token);
		}
		return new DataDelta(addUpd, deletions, lastSync);
	}

	private ItemChange getContactChange( Integer collectionId,
			Contact c) {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, ""
				+ c.getUid()));
		MSContact cal = new ContactConverter().convert(c);
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, Integer collectionId, String serverId, MSContact data) throws UnknownObmSyncServerException {
		logger.info("create contact ({} | {}) in collectionId {}", 
				new Object[]{data.getFirstName(), data.getLastName(), collectionId});

		BookClient bc = getBookClient();
		AccessToken token = login(bc, bs);

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
			bc.logout(token);
		}

		return getServerIdFor(collectionId, itemId);
	}

	public void delete(BackendSession bs, String serverId) throws UnknownObmSyncServerException, ServerItemNotFoundException {
		logger.info("delete serverId {}", serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				BookClient bc = getBookClient();
				AccessToken token = login(bc, bs);
				try {
					bc.removeContact(token, BookType.contacts, serverId.substring(idx + 1) );
				} catch (ServerFault e) {
					throw new UnknownObmSyncServerException(e);
				} catch (ContactNotFoundException e) {
					throw new ServerItemNotFoundException(serverId);
				} finally {
					bc.logout(token);
				}
			}
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchServerIds) {
		BookClient bc = getBookClient();
		AccessToken token = login(bc, bs);

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
		bc.logout(token);
		return ret;
	}
	
}