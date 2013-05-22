/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.handler;

import java.util.Date;
import java.util.List;

import org.obm.push.backend.BackendWindowingService;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BackendWindowingServiceImpl implements BackendWindowingService {

	private final ResponseWindowingService responseWindowingService;
	
	@Inject
	@VisibleForTesting BackendWindowingServiceImpl(ResponseWindowingService responseWindowingService) {
		this.responseWindowingService = responseWindowingService;
	}
	
	@Override
	public DataDelta windowedChanges(UserDataRequest udr, ItemSyncState itemSyncState, AnalysedSyncCollection collection,
			SyncClientCommands clientCommands, SyncKey newSyncKey, BackendChangesProvider backendChangesProvider) {
		Preconditions.checkNotNull(udr, "UserDataRequest is required");
		Preconditions.checkNotNull(itemSyncState, "itemSyncState is required");
		Preconditions.checkNotNull(collection, "collection is required");
		Preconditions.checkNotNull(clientCommands, "clientCommands is required");
		Preconditions.checkNotNull(newSyncKey, "newSyncKey is required");
		Preconditions.checkNotNull(backendChangesProvider, "backendChangesProvider is required");
		
		if (collectionHasPendingResponse(collection)) {
			return continueWindowing(itemSyncState, collection, clientCommands, newSyncKey);
		} else {
			return getBackendChanges(backendChangesProvider, collection, clientCommands, newSyncKey);
		}
	}

	private DataDelta getBackendChanges(BackendChangesProvider backendChangesProvider,
		AnalysedSyncCollection collection, SyncClientCommands clientCommands, SyncKey newSyncKey) {

		return windowing(collection, clientCommands, newSyncKey, backendChangesProvider.getAllChanges());
	}

	private DataDelta continueWindowing(ItemSyncState itemSyncState, AnalysedSyncCollection collection, SyncClientCommands clientCommands,
			SyncKey newSyncKey) {
		
		Date lastSync = itemSyncState.getSyncDate();
		return windowing(collection, clientCommands, newSyncKey, DataDelta.newEmptyDelta(lastSync, newSyncKey));
	}

	private boolean collectionHasPendingResponse(AnalysedSyncCollection collection) {
		return responseWindowingService.hasPendingResponse(collection.getSyncKey());
	}


	private DataDelta windowing(AnalysedSyncCollection collection,
			SyncClientCommands clientCommands, SyncKey newSyncKey, DataDelta delta) {
		
		List<ItemChange> changes = responseWindowingService.windowChanges(collection, newSyncKey, delta, clientCommands);
		List<ItemDeletion> deletions = responseWindowingService.windowDeletions(collection, newSyncKey, delta, clientCommands);
		boolean moreAvailable = responseWindowingService.hasPendingResponse(newSyncKey);
		
		return DataDelta.builder()
				.changes(changes)
				.deletions(deletions)
				.syncDate(delta.getSyncDate())
				.syncKey(delta.getSyncKey())
				.moreAvailable(moreAvailable)
				.build();
	}
	
}
