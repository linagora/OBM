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
package org.obm.push.backend;

import java.util.List;

import javax.naming.NoPermissionException;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.exception.FilterTypeChangedException;

public interface PIMBackend {

	String createOrUpdate(UserDataRequest udr, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, 
			DaoException, UnexpectedObmSyncServerException, ItemNotFoundException,
			ConversionException, HierarchyChangedException, NoPermissionException;
	
	String move(UserDataRequest udr, String srcFolder, String dstFolder, String messageId)
		throws CollectionNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException;
	
	void delete(UserDataRequest udr, Integer collectionId, String serverId, Boolean moveToTrash) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, ItemNotFoundException,
			ProcessingEmailException, UnsupportedBackendFunctionException;
	
	void emptyFolderContent(UserDataRequest udr, String collectionPath, boolean deleteSubFolder)
			throws NotAllowedException, CollectionNotFoundException, ProcessingEmailException;
	
	PIMDataType getPIMDataType();
	
	List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds, SyncCollectionOptions syncCollectionOptions) 
			throws ProcessingEmailException, CollectionNotFoundException, 
			DaoException, UnexpectedObmSyncServerException, ConversionException;

	List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds,
			SyncCollectionOptions syncCollectionOptions, ItemSyncState previousItemSyncState) 
		throws ProcessingEmailException, CollectionNotFoundException, 
			DaoException, UnexpectedObmSyncServerException, ConversionException;

	DataDelta getChanged(UserDataRequest udr, ItemSyncState itemSyncState, AnalysedSyncCollection syncCollection, SyncClientCommands clientCommands, SyncKey newSyncKey)
		throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException, FilterTypeChangedException, HierarchyChangedException;
	
	int getItemEstimateSize(UserDataRequest udr, ItemSyncState syncState, Integer collectionId, 
		SyncCollectionOptions collectionOptions) throws CollectionNotFoundException, 
		ProcessingEmailException, DaoException, UnexpectedObmSyncServerException, ConversionException,
		FilterTypeChangedException, HierarchyChangedException;

	HierarchyCollectionChanges getHierarchyChanges(UserDataRequest userDataRequest, FolderSyncState lastKnownState, FolderSyncState outgoingSyncState)
			throws DaoException, InvalidSyncKeyException;
}
