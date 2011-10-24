package org.obm.push.bean;

import java.util.List;

public class HierarchyItemsChanges {

	private final List<ItemChange> itemsChanged;
	private final List<ItemChange> itemsDeleted;

	public HierarchyItemsChanges(List<ItemChange> itemsChanged, 
			List<ItemChange> itemsDeleted) {
		this.itemsChanged = itemsChanged;
		this.itemsDeleted = itemsDeleted;
	}
	
	public List<ItemChange> getItemsAddedOrUpdated() {
		return itemsChanged;
	}
	
	public List<ItemChange> getItemsDeleted() {
		return itemsDeleted;
	}

}
