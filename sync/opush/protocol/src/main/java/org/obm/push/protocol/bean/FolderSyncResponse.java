package org.obm.push.protocol.bean;

import java.util.Collection;

import org.obm.push.bean.ItemChange;

public class FolderSyncResponse {
	
	private final Collection<ItemChange> itemChanges;
	private final String newSyncKey;
	
	public FolderSyncResponse(Collection<ItemChange> itemChanges, String newSyncKey) {
		this.itemChanges = itemChanges;
		this.newSyncKey = newSyncKey;
	}
	
	public int getCount() {
		return itemChanges.size();
	}

	public Collection<ItemChange> getItemChanges() {
		return itemChanges;
	}
	
	public String getNewSyncKey() {
		return newSyncKey;
	}
	
}