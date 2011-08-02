package org.obm.push.state;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.obm.push.backend.BackendSession;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StateMachine {

	private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

	private final CollectionDao collectionDao;

	@Inject
	private StateMachine(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}
	
	public SyncState getFolderSyncState(String loginAtDomain, String deviceId, String collectionUrl, String syncKey) throws SQLException {
		try {
			int collectionId = collectionDao.getCollectionMapping(loginAtDomain, deviceId, collectionUrl);
			return getSyncState(collectionId, syncKey);
		
		} catch (CollectionNotFoundException e) {
			return new SyncState(collectionUrl, syncKey);
		}
	}

	public SyncState getSyncState(Integer collectionId, String syncKey) throws CollectionNotFoundException {
		SyncState syncState = collectionDao.findStateForKey(syncKey);
		if (syncState != null) {
			return syncState;
		}
		return new SyncState(collectionDao.getCollectionPath(collectionId), syncKey);
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId, Date lastSync) throws CollectionNotFoundException, SQLException {
		final String newSk = UUID.randomUUID().toString();
		final SyncState newState = new SyncState(collectionDao.getCollectionPath(collectionId), newSk, lastSync);
		logger.info("allocateNewSyncKey [ collectionId = {} | lastSync.toString = {} ]",
				new Object[]{ collectionId, newState.getLastSync().toString() });
		collectionDao.updateState(bs.getLoginAtDomain(), bs.getDevId(), collectionId, newState);
		return newSk;
	}

}
