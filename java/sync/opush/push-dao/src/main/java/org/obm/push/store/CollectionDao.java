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
package org.obm.push.store;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;

public interface CollectionDao {

	int addCollectionMapping(Device device, String collection) throws DaoException;

	Integer getCollectionMapping(Device device, String collection) throws DaoException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException, DaoException;

	void resetCollection(Device device, Integer collectionId) throws DaoException;
	
	/**
	 * Create a new SyncState entry in database and returns its unique id
	 * @return SyncState database unique id
	 */
	ItemSyncState updateState(Device device, Integer collectionId, SyncKey syncKey, Date syncDate) throws DaoException;

	FolderSyncState allocateNewFolderSyncState(Device device, SyncKey newSyncKey) throws DaoException;
	
	ItemSyncState findItemStateForKey(SyncKey syncKey) throws DaoException ;
	
	FolderSyncState findFolderStateForKey(SyncKey syncKey) throws DaoException ;

	ChangedCollections getCalendarChangedCollections(Date lastSync) throws DaoException;

	ChangedCollections getContactChangedCollections(Date lastSync) throws DaoException;

	ItemSyncState lastKnownState(Device device, Integer collectionId) throws DaoException;

	List<String> getUserCollections(FolderSyncState folderSyncState) throws DaoException;
}
