package org.obm.push.protocol.bean;

import java.util.Collection;

import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;

public class FolderSyncResponse {
	
	private final HierarchyItemsChanges hierarchyItemsChanges;
	private final String newSyncKey;
	
	public FolderSyncResponse(HierarchyItemsChanges hierarchyItemsChanges, String newSyncKey) {
		this.hierarchyItemsChanges = hierarchyItemsChanges;
		this.newSyncKey = newSyncKey;
	}
	
	public int getCount() {
		int count = 0;
		if (getItemsAddedAndUpdated() != null) {
			count += getItemsAddedAndUpdated().size();
		}
		if (getItemsDeleted() != null) {
			count += getItemsDeleted().size();
		}
		return count;
	}

	public Collection<ItemChange> getItemsAddedAndUpdated() {
		return hierarchyItemsChanges.getItemsAddedOrUpdated();
	}
	
	public Collection<ItemChange> getItemsDeleted() {
		return hierarchyItemsChanges.getItemsDeleted();
	}
	
	public String getNewSyncKey() {
		return newSyncKey;
	}
	
}