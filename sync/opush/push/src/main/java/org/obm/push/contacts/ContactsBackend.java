package org.obm.push.contacts;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.FolderTypeNotFoundException;
import org.obm.push.exception.activesync.ServerItemNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.items.ContactChangesResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContactsBackend extends ObmSyncBackend {

	@Inject
	private ContactsBackend(CollectionDao collectionDao, BookClient bookClient, CalendarClient calendarClient, TodoClient todoClient) {
		super(collectionDao, bookClient, calendarClient, todoClient);
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) throws DaoException {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\contacts";
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
			serverId = collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = createCollectionMapping(bs.getDevice(), col);
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " contacts");
		ic.setItemType(FolderType.DEFAULT_CONTACTS_FOLDER);
		ret.add(ic);
		return ret;
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

		String id = null;
		try {
			if (serverId != null) {
				int idx = serverId.lastIndexOf(":");
				id = serverId.substring(idx + 1);
				Contact convertedContact = new ContactConverter().contact(data);
				convertedContact.setUid(Integer.parseInt(id));
				bc.modifyContact(token, BookType.contacts, convertedContact);
			} else {
				Contact createdContact = bc.createContactWithoutDuplicate(token, BookType.contacts,
						new ContactConverter().contact(data));
				id = createdContact.getUid().toString();
			}
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			bc.logout(token);
		}

		return getServerIdFor(collectionId, id);
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

	/**
	 * obm:\\adrien@test.tlse.lng\contacts
	 * 
	 */
	public FolderType getFolderType(String collectionPath)  throws FolderTypeNotFoundException {
		if (collectionPath != null) {
			if (collectionPath.contains("contacts")) {
				return FolderType.DEFAULT_CONTACTS_FOLDER;
			}	
		}
		throw new FolderTypeNotFoundException("The collection's path [ " + collectionPath + " ] is invalid");
	}

}
