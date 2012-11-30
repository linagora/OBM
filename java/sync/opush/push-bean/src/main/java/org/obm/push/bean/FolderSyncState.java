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
package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;

import org.obm.push.utils.DateUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class FolderSyncState extends SyncState implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder extends SyncStateBuilder<FolderSyncState> {
		
		private Builder() {}
		
		@Override
		public FolderSyncState build() {
			Preconditions.checkArgument(key != null, "key can't be null or empty");
			lastSync = Objects.firstNonNull(lastSync, DateUtils.getEpochPlusOneSecondCalendar().getTime());
			return new FolderSyncState(lastSync, lastSyncFiltred, key, id);
		}
	}
	
	public FolderSyncState(SyncKey syncKey) {
		this(syncKey, null);
	}

	public FolderSyncState(SyncKey key, Date lastSync) {
		super(key, lastSync);
	}

	public FolderSyncState(SyncState syncState) {
		super(syncState.getKey(), syncState.getLastSync());
		setId(syncState.getId());
		setLastSyncFiltred(syncState.isLastSyncFiltred());
	}
	
	public boolean isInitialFolderSync() {
		return isSyncKeyOfInitialFolderSync(getKey());
	}
	
	public static boolean isSyncKeyOfInitialFolderSync(SyncKey syncKey) {
		return SyncKey.INITIAL_FOLDER_SYNC_KEY.equals(syncKey);
	}
	
	private FolderSyncState(Date lastSync, boolean lastSyncFiltred, SyncKey key, int id) {
		super(lastSync, lastSyncFiltred, key, id);
	}
}
