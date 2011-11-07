package org.obm.push.store;

import java.util.Date;

import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.PIMDataTypeNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;

public interface CollectionDao {

	Integer addCollectionMapping(Device device, String collection) throws DaoException;

	int getCollectionMapping(Device device, String collectionId)
			throws CollectionNotFoundException, DaoException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException, DaoException;

	void resetCollection(Device device, Integer collectionId) throws DaoException;
	
	/**
	 * Create a new SyncState entry in database and returns its unique id
	 * @return SyncState database unique id
	 */
	int updateState(Device device, Integer collectionId, SyncState state) throws DaoException;

	SyncState findStateForKey(String syncKey) throws DaoException, CollectionNotFoundException, PIMDataTypeNotFoundException;
	
	ChangedCollections getCalendarChangedCollections(Date lastSync) throws DaoException;

	ChangedCollections getContactChangedCollections(Date lastSync) throws DaoException;

	Date findLastSyncDateFromKey(String syncKey) throws DaoException, CollectionNotFoundException;
	
}
