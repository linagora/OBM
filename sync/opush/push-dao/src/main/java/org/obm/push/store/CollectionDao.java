package org.obm.push.store;

import java.sql.SQLException;
import java.util.Date;

import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.CollectionNotFoundException;

public interface CollectionDao {

	Integer addCollectionMapping(Device device, String collection) throws SQLException;

	/**
	 * Fetches the id associated with a given collection id string.
	 */
	int getCollectionMapping(Device device, String collectionId)
			throws CollectionNotFoundException, SQLException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException;

	void resetCollection(Device device, Integer collectionId) throws SQLException;
	
	void updateState(Device device, Integer collectionId, SyncState state) throws SQLException;

	SyncState findStateForKey(String syncKey);
	
	ChangedCollections getCalendarChangedCollections(Date lastSync) throws SQLException;

	ChangedCollections getContactChangedCollections(Date lastSync) throws SQLException;
	
}
