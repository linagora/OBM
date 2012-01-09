package org.obm.push.service.impl;

import java.util.Collection;
import java.util.List;

import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;

public interface MappingService {

	String collectionIdToString(Integer collectionId);

	String createCollectionMapping(Device device, String col)
			throws DaoException;

	String getCollectionPathFor(Integer collectionId)
			throws CollectionNotFoundException, DaoException;

	Integer getCollectionIdFor(Device device, String collection) throws CollectionNotFoundException, DaoException;
	
	List<ItemChange> buildItemsToDeleteFromUids(Integer collectionId,
			Collection<Long> uids);

	ItemChange getItemChange(Integer collectionId, String clientId);

	String getServerIdFor(Integer collectionId, String clientId);
	
	Integer getItemIdFromServerId(String serverId);

	Integer getCollectionIdFromServerId(String serverId);
}