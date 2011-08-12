package org.obm.push.contacts;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ObmConfigurationService;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ActiveSyncException;
import org.obm.push.exception.activesync.FolderTypeNotFoundException;
import org.obm.push.exception.activesync.ObjectNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.items.ContactChangesResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContactsBackend extends ObmSyncBackend {

	@Inject
	private ContactsBackend(ObmConfigurationService configurationService, CollectionDao collectionDao)
			throws ConfigurationException {
		
		super(configurationService, collectionDao);
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) throws DaoException {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\contacts";
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
			serverId = getServerIdFor(collectionId);
		} catch (ActiveSyncException e) {
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

	public DataDelta getContentChanges(BackendSession bs, SyncState state,
			Integer collectionId) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		Date lastSync = null;
		logger.info("getContentChanges(" + state.getLastSync() + ")");
		BookClient bc = getBookClient(bs);
		AccessToken token = login(bc, bs);

		try {
			ContactChangesResponse changes = bc.getSync(token, BookType.contacts, state
					.getLastSync());

			for (Contact c : changes.getUpdated()) {
				ItemChange change = getContactChange(collectionId, c);
				addUpd.add(change);
			}

			for (Integer del : changes.getRemoved()) {
				ItemChange change = createItemChangeToRemove(collectionId, "" + del);
				deletions.add(change);
			}
			lastSync = changes.getLastSync();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, MSContact data) {
		logger.info("create in " + collectionId + " (contact: "
				+ data.getFirstName() + " " + data.getLastName() + ")");
		BookClient bc = getBookClient(bs);
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
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			bc.logout(token);
		}

		return getServerIdFor(collectionId, id);
	}

	public void delete(BackendSession bs, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				String id = serverId.substring(idx + 1);
				BookClient bc = getBookClient(bs);
				AccessToken token = login(bc, bs);
				try {
					bc.removeContact(token, BookType.contacts, id);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					bc.logout(token);
				}
			}
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs,
			List<String> fetchServerIds) throws ObjectNotFoundException {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		BookClient bc = getBookClient(bs);
		AccessToken token = login(bc, bs);

		try {
			for (String serverId : fetchServerIds) {
				Integer id = getItemIdFor(serverId);
				if (id != null) {
					
					Contact c = bc.getContactFromId(token, BookType.contacts,
							id.toString());
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					MSContact cal = new ContactConverter().convert(c);
					ic.setData(cal);
					ret.add(ic);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ObjectNotFoundException();
		} finally {
			bc.logout(token);
		}
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
