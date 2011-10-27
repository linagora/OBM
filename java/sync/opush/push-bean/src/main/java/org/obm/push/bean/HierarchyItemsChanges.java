package org.obm.push.bean;

import java.util.Date;
import java.util.List;

public class HierarchyItemsChanges {

	private final List<ItemChange> itemsChanged;
	private final List<ItemChange> itemsDeleted;
	private final Date lastSync;

	public HierarchyItemsChanges(List<ItemChange> itemsChanged, 
			List<ItemChange> itemsDeleted, Date lastSync) {
		this.itemsChanged = itemsChanged;
		this.itemsDeleted = itemsDeleted;
		this.lastSync = lastSync;
	}
	
	public List<ItemChange> getItemsAddedOrUpdated() {
		return itemsChanged;
	}
	
	public List<ItemChange> getItemsDeleted() {
		return itemsDeleted;
	}

	public Date getLastSync() {
		return lastSync;
	}

}
