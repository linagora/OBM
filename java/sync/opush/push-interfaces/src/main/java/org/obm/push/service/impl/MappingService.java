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
package org.obm.push.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.CollectionPath;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;

public interface MappingService {

	String collectionIdToString(Integer collectionId);

	int createCollectionMapping(Device device, String col)
			throws DaoException;
	
	Date getLastBackendMapping(PIMDataType dataType, FolderSyncState folderSyncState)
			throws DaoException;
	
	void createBackendMapping(PIMDataType pimDataType, FolderSyncState outgoingSyncState)
			throws DaoException;
	
	String getCollectionPathFor(Integer collectionId)
			throws CollectionNotFoundException, DaoException;

	Integer getCollectionIdFor(Device device, String collection) throws CollectionNotFoundException, DaoException;
	
	List<ItemDeletion> buildItemsToDeleteFromUids(Integer collectionId,
			Collection<Long> uids);

	String getServerIdFor(Integer collectionId, String clientId);
	
	Integer getItemIdFromServerId(String serverId);

	Integer getCollectionIdFromServerId(String serverId);
	
	List<CollectionPath> listCollections(UserDataRequest udr, FolderSyncState folderSyncState) throws DaoException;

	void snapshotCollections(FolderSyncState outgoingSyncState, Set<Integer> collectionIds)
			throws DaoException;
}