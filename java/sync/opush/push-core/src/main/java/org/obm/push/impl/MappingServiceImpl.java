/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.CollectionPath;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.impl.MappingService;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSnapshotDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class MappingServiceImpl implements MappingService {

	private final CollectionDao collectionDao;
	private final FolderSyncStateBackendMappingDao folderSyncStateBackendMappingDao;
	private final Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	private final FolderSnapshotDao folderSnapshotDao;

	@Inject
	@VisibleForTesting MappingServiceImpl(CollectionDao collectionDao,
			FolderSyncStateBackendMappingDao folderSyncStateBackendMappingDao,
			FolderSnapshotDao folderSnapshotDao,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider) {
		this.collectionDao = collectionDao;
		this.folderSyncStateBackendMappingDao = folderSyncStateBackendMappingDao;
		this.folderSnapshotDao = folderSnapshotDao;
		this.collectionPathBuilderProvider = collectionPathBuilderProvider;
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
	public int createCollectionMapping(Device device, String col) throws DaoException {
		return collectionDao.addCollectionMapping(device, col);
	}

	@Override
	public void createBackendMapping(PIMDataType pimDataType, FolderSyncState outgoingSyncState) throws DaoException {
		folderSyncStateBackendMappingDao.createMapping(pimDataType, outgoingSyncState);
	}

	@Override
	public Date getLastBackendMapping(PIMDataType dataType, FolderSyncState lastKnownState) throws DaoException {
		return folderSyncStateBackendMappingDao.getLastSyncDate(dataType, lastKnownState);
	}
	
	@Override
	public String getCollectionPathFor(Integer collectionId) throws CollectionNotFoundException, DaoException {
		return collectionDao.getCollectionPath(collectionId);
	}
	
	@Override
	public List<ItemDeletion> buildItemsToDeleteFromUids(Integer collectionId, Collection<Long> uids) {
		List<ItemDeletion> deletions = Lists.newLinkedList();
		for (Long uid: uids) {
			deletions.add(ItemDeletion.builder()
					.serverId(ServerId.buildServerIdString(collectionId, uid))
					.build());
		}
		return deletions;
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

	@Override
	public List<CollectionPath> listCollections(final UserDataRequest udr, FolderSyncState folderSyncState)
			throws DaoException {
		
		List<String> userCollections = collectionDao.getUserCollections(folderSyncState);
		return Lists.transform(userCollections, new Function<String, CollectionPath>(){

			@Override
			public CollectionPath apply(String fullyQualifiedCollectionPath) {
				try {
					return collectionPathBuilderProvider.get()
						.userDataRequest(udr)
						.fullyQualifiedCollectionPath(fullyQualifiedCollectionPath)
						.build();
				} catch (IndexOutOfBoundsException e) {
					// Guava Lists.transform translates IndexOutOfBoundsException into NoSuchElementException 
					throw new CollectionPathException("Unbuildable collection path : " + fullyQualifiedCollectionPath, e);
				}
			}
		});
	}

	@Override
	public void snapshotCollections(FolderSyncState outgoingSyncState, Set<Integer> collectionIds)
			throws DaoException {

		folderSnapshotDao.createFolderSnapshot(outgoingSyncState.getId(), collectionIds);
	}

}