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
package org.obm.push.protocol.bean;

import java.util.List;

import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;

import com.google.common.base.Objects;

public class FolderSyncResponse {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private FolderSyncStatus status;
		private HierarchyCollectionChanges hierarchyItemsChanges;
		private SyncKey newSyncKey;

		private Builder() {}
		
		public Builder hierarchyItemsChanges(HierarchyCollectionChanges hierarchyItemsChanges) {
			this.hierarchyItemsChanges = hierarchyItemsChanges;
			return this;
		}
		
		public Builder newSyncKey(SyncKey newSyncKey) {
			this.newSyncKey = newSyncKey;
			return this;
		}
		
		public Builder status(FolderSyncStatus status) {
			this.status = status;
			return this;
		}
		
		public FolderSyncResponse build() {
			return new FolderSyncResponse(status, hierarchyItemsChanges, newSyncKey);
		}
	}
	
	private final FolderSyncStatus status;
	private final HierarchyCollectionChanges hierarchyItemsChanges;
	private final SyncKey newSyncKey;
	
	private FolderSyncResponse(FolderSyncStatus status,
			HierarchyCollectionChanges hierarchyItemsChanges, SyncKey newSyncKey) {
		this.status = status;
		this.hierarchyItemsChanges = hierarchyItemsChanges;
		this.newSyncKey = newSyncKey;
	}

	public HierarchyCollectionChanges getHierarchyItemsChanges() {
		return hierarchyItemsChanges;
	}
	
	public SyncKey getNewSyncKey() {
		return newSyncKey;
	}
	
	public int getCount() {
		int count = 0;
		if (getCollectionsAddedAndUpdated() != null) {
			count += getCollectionsAddedAndUpdated().size();
		}
		if (getCollectionsDeleted() != null) {
			count += getCollectionsDeleted().size();
		}
		return count;
	}

	public List<CollectionChange> getCollectionsAddedAndUpdated() {
		return hierarchyItemsChanges.getCollectionChanges();
	}
	
	public List<CollectionDeletion> getCollectionsDeleted() {
		return hierarchyItemsChanges.getCollectionDeletions();
	}
	
	public FolderSyncStatus getStatus() {
		return status;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(status, hierarchyItemsChanges, newSyncKey);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof FolderSyncResponse) {
			FolderSyncResponse that = (FolderSyncResponse) object;
			return Objects.equal(this.status, that.status)
				&& Objects.equal(this.hierarchyItemsChanges, that.hierarchyItemsChanges)
				&& Objects.equal(this.newSyncKey, that.newSyncKey);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("status", status)
			.add("hierarchyItemsChanges", hierarchyItemsChanges)
			.add("newSyncKey", newSyncKey)
			.toString();
	}
}