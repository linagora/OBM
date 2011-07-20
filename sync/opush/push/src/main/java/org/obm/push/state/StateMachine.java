package org.obm.push.state;

import java.sql.SQLException;
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

	private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

	private final ISyncStorage store;

	@Inject
	private StateMachine(ISyncStorage store) {
		this.store = store;
	}
	
	public SyncState getFolderSyncState(String loginAtDomain, String deviceId, String collectionUrl, String syncKey) throws SQLException {
		try {
			int collectionId = store.getCollectionMapping(loginAtDomain, deviceId, collectionUrl);
			return getSyncState(collectionId, syncKey);
		
		} catch (CollectionNotFoundException e) {
			return new SyncState(collectionUrl, syncKey);
		}
	}

	public SyncState getSyncState(Integer collectionId, String syncKey) throws CollectionNotFoundException {
		SyncState syncState = store.findStateForKey(syncKey);
		if (syncState != null) {
			return syncState;
		}
		return new SyncState(store.getCollectionPath(collectionId), syncKey);
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId, Date lastSync) throws CollectionNotFoundException, SQLException {
		String newSk = UUID.randomUUID().toString();
		final SyncState newState = new SyncState(store.getCollectionPath(collectionId), newSk, lastSync);
		
		logger.info("allocateNewSyncKey [ " + collectionId + ", " + newState.getLastSync().toString() + " ]");
		
		store.updateState(bs.getLoginAtDomain(), bs.getDevId(), collectionId, newState);
		return newSk;
	}


}
