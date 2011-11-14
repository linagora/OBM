package org.obm.push.state;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StateMachine {

	private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);
	
	private final CollectionDao collectionDao;
	private final ItemTrackingDao itemTrackingDao;

	@Inject
	private StateMachine(CollectionDao collectionDao, ItemTrackingDao itemTrackingDao) {
		this.collectionDao = collectionDao;
		this.itemTrackingDao = itemTrackingDao;
	}

	public SyncState getSyncState(String syncKey) throws DaoException {
		return collectionDao.findStateForKey(syncKey);
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId, Date lastSync, 
		Collection<ItemChange> changes, Collection<ItemChange> deletedItems) throws DaoException, InvalidServerId {
		
		String newSk = UUID.randomUUID().toString();
		SyncState newState = new SyncState(newSk, lastSync);
		int syncStateId = collectionDao.updateState(bs.getDevice(), collectionId, newState);
		newState.setId(syncStateId);
		
		if (changes != null && !changes.isEmpty()) {
			itemTrackingDao.markAsSynced(newState, listNewItems(changes));
		}
		if (deletedItems != null && !deletedItems.isEmpty()) {
			itemTrackingDao.markAsDeleted(newState, itemChangesAsServerIdSet(deletedItems));
		}
		
		log(bs, newState);
		return newSk;
	}

	private Set<ServerId> itemChangesAsServerIdSet(Iterable<ItemChange> changes) throws InvalidServerId {
		Set<ServerId> ids = Sets.newHashSet();
		for (ItemChange change: changes) {
			ids.add(new ServerId(change.getServerId()));
		}
		return ids;
	}

	private void log(BackendSession bs, SyncState newState) {
		String collectionPath = "obm:\\\\" + bs.getUser().getLoginAtDomain();
		logger.info("Allocate new synckey {} for collectionPath {} with {} last sync", 
				new Object[]{newState.getKey(), collectionPath, newState.getLastSync()});
	}

	private Set<ServerId> listNewItems(Collection<ItemChange> changes) throws InvalidServerId {
		HashSet<ServerId> serverIds = Sets.newHashSet();
		for (ItemChange change: changes) {
			if (change.isNew()) {
				ServerId serverId = new ServerId(change.getServerId());
				if (serverId.isItem()) {
					serverIds.add(serverId);
				}
			}
		}
		return serverIds;
	}
	
}
