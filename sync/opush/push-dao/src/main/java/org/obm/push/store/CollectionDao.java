package org.obm.push.store;

import java.sql.SQLException;

import org.obm.push.bean.SyncState;
import org.obm.push.exception.CollectionNotFoundException;

public interface CollectionDao {

	Integer addCollectionMapping(String loginAtDomain, String deviceId, String collection) throws SQLException;

	/**
	 * Fetches the id associated with a given collection id string.
	 */
	int getCollectionMapping(String loginAtDomain, String deviceId, String collectionId)
			throws CollectionNotFoundException, SQLException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException;

	void resetCollection(String loginAtDomain, String devId, Integer collectionId) throws SQLException;
	
	void updateState(String loginAtDomain, String devId, Integer collectionId, SyncState state) throws SQLException;

	SyncState findStateForKey(String syncKey);
	
}
