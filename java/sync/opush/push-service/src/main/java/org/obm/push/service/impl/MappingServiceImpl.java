package org.obm.push.service.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MappingServiceImpl implements MappingService {

	private final CollectionDao collectionDao;

	@Inject
	private MappingServiceImpl(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}

	@Override
	public Integer getItemIdFromServerId(String serverId) {
		if (serverId != null) {
			String[] idx = serverId.split(":");
			if (idx.length == 2) {
				return Integer.parseInt(idx[1]);
			}
		}
		return null;
	}

	@Override
	public Integer getCollectionIdFromServerId(String serverId) {
		if (serverId != null) {
			String[] idx = serverId.split(":");
			if (idx.length == 2) {
				return Integer.parseInt(idx[0]);
			}
		}
		return null;
	}
	

	
	@Override
	public String collectionIdToString(Integer collectionId) {
		return String.valueOf(collectionId);
	}

	@Override
	public String createCollectionMapping(Device device, String col) throws DaoException {
		return collectionDao.addCollectionMapping(device, col).toString();
	}
	
	@Override
	public String getCollectionPathFor(Integer collectionId) throws CollectionNotFoundException, DaoException {
		return collectionDao.getCollectionPath(collectionId);
	}
	
	@Override
	public List<ItemChange> buildItemsToDeleteFromUids(Integer collectionId, Collection<Long> uids) {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Long uid: uids) {
			deletions.add( getItemChange(collectionId, uid.toString()) );
		}
		return deletions;
	}


	@Override
	public ItemChange getItemChange(Integer collectionId, String clientId) {
		return new ItemChange( getServerIdFor(collectionId, clientId) );
	}

	@Override
	public String getServerIdFor(Integer collectionId, String clientId) {
		if (collectionId == null || Strings.isNullOrEmpty(clientId)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(10);
		sb.append(collectionId);
		sb.append(':');
		sb.append(clientId);
		return sb.toString();
	}
	
	@Override
	public Integer getCollectionIdFor(Device device, String collection) throws CollectionNotFoundException, DaoException {
		Integer collectionId = collectionDao.getCollectionMapping(device, collection);
		if (collectionId == null) {
			throw new CollectionNotFoundException("Collection {" + collection + "} not found.");
		}
		return collectionId;
	}

	
}
