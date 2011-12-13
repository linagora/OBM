package org.obm.push.protocol.bean;

import java.util.Collection;

import org.obm.push.bean.ItemChange;

public class FolderSyncResponse {
	
	private final Collection<ItemChange> itemsAddedAndUpdated;
	private final Collection<ItemChange> itemsDeleted;
	
	private final String newSyncKey;
	
	public FolderSyncResponse(Collection<ItemChange> itemsAddedAndUpdates, Collection<ItemChange> itemsDeleted, 
			String newSyncKey) {
		this.itemsAddedAndUpdated = itemsAddedAndUpdates;
		this.itemsDeleted = itemsDeleted;
		this.newSyncKey = newSyncKey;
	}
	
	public int getCount() {
		int count = 0;
		if (itemsAddedAndUpdated != null) {
			count += itemsAddedAndUpdated.size();
		}
		if (itemsDeleted != null) {
			count += itemsDeleted.size();
		}
		return count;
	}

	public Collection<ItemChange> getItemsAddedAndUpdated() {
		return itemsAddedAndUpdated;
	}
	
	public Collection<ItemChange> getItemsDeleted() {
		return itemsDeleted;
	}
	
	public String getNewSyncKey() {
		return newSyncKey;
	}
	
}