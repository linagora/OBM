package org.obm.push.store;

import java.util.Set;

import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;

public interface ItemTrackingDao {

	void markAsSynced(SyncState syncState, Set<ServerId> serverIds) throws DaoException;
	
	void markAsDeleted(SyncState syncState, Set<ServerId> serverIds) throws DaoException;
	
	Set<ServerId> getSyncedServerIds(SyncState syncState, Set<ServerId> serverIds) throws DaoException;
	
	boolean isServerIdSynced(SyncState syncState, ServerId serverId) throws DaoException;
	
}
