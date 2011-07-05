package org.obm.push.state;

import java.util.Date;
import java.util.UUID;

import org.obm.push.backend.BackendSession;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StateMachine {

	private static final Logger logger = LoggerFactory
			.getLogger(StateMachine.class);

	private final ISyncStorage store;

	@Inject
	private StateMachine(ISyncStorage store) {
		this.store = store;
	}
	
	public SyncState getFolderSyncState(String deviceId, String collectionUrl, String syncKey) {
		try {
			return getSyncState(store.getCollectionMapping(deviceId, collectionUrl),syncKey);
		} catch (CollectionNotFoundException e) {
			SyncState ret = new SyncState(collectionUrl);
			ret.setKey(syncKey);
			if (!"0".equals(syncKey)) {
				ret.setLastSync(null);
			}
			return ret;
		}
	}

	public SyncState getSyncState(Integer collectionId, String syncKey) throws CollectionNotFoundException {
		SyncState ret = null;

		ret = store.findStateForKey(syncKey);
		if (ret == null) {
			ret = new SyncState(store.getCollectionPath(collectionId));
			ret.setKey(syncKey);
			if (!"0".equals(syncKey)) {
				ret.setLastSync(null);
			}
		}
		return ret;
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId) throws CollectionNotFoundException {
		final SyncState newState = new SyncState(store.getCollectionPath(collectionId));
		final Date date = bs.getUpdatedSyncDate(collectionId);
		if (date != null) {
			logger.info("allocateNewSyncKey [ " + collectionId + ", " + date.toString() + " ]");
			newState.setLastSync(date);
		}
		String newSk = UUID.randomUUID().toString();
		newState.setKey(newSk);
		
		store.updateState(bs.getDevId(), collectionId, newState);
		return newSk;
	}

}
