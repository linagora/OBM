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
import org.obm.push.exception.activesync.CollectionNotFoundException;
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

	public SyncState getSyncState(Integer collectionId, String syncKey) throws CollectionNotFoundException, DaoException {
		SyncState syncState = collectionDao.findStateForKey(syncKey);
		if (syncState != null) {
			return syncState;
		}
		return new SyncState(collectionDao.getCollectionPath(collectionId), syncKey);
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId, Date lastSync, Collection<ItemChange> changes) 
			throws CollectionNotFoundException, DaoException, InvalidServerId {
		
		final String newSk = UUID.randomUUID().toString();
		final SyncState newState = new SyncState(collectionDao.getCollectionPath(collectionId), newSk, lastSync);
		collectionDao.updateState(bs.getDevice(), collectionId, newState);
		logger.info("Allocate new synckey {} for collectionPath {} with {} last sync", 
				new Object[]{newState.getKey(), newState.getDataType().asXmlValue(), newState.getLastSync()});
		itemTrackingDao.markAsSynced(newState, listNewItems(changes));
		return newSk;
	}

	private Set<ServerId> listNewItems(Collection<ItemChange> changes) throws InvalidServerId {
		HashSet<ServerId> serverIds = Sets.newHashSet();
		for (ItemChange change: changes) {
			if (change.isNew()) {
				serverIds.add(new ServerId(change.getServerId()));
			}
		}
		return serverIds;
	}
}
