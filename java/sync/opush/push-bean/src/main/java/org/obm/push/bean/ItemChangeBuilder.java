package org.obm.push.bean;

import com.google.common.base.Preconditions;

public class ItemChangeBuilder implements Builder<ItemChange> {
	
	private ItemChange itemChange;
	private boolean newIsDefined;
	
	public ItemChangeBuilder() {
		itemChange = new ItemChange();
	}
	
	public ItemChangeBuilder withServerId(String serverId) {
		Preconditions.checkState(itemChange.getServerId() == null, "serverId already defined");
		itemChange.setServerId(serverId);
		return this;
	}
	
	public ItemChangeBuilder withParentId(String parentId) {
		Preconditions.checkState(itemChange.getParentId() == null, "parentId already defined");
		itemChange.setParentId(parentId);
		return this;
	}
	
	public ItemChangeBuilder withDisplayName(String displayName) {
		Preconditions.checkState(itemChange.getDisplayName() == null, "displayName already defined");
		itemChange.setDisplayName(displayName);
		return this;
	}
	
	public ItemChangeBuilder withItemType(FolderType itemType) {
		Preconditions.checkState(itemChange.getItemType() == null, "itemType already defined");
		itemChange.setItemType(itemType);
		return this;
	}
	
	public ItemChangeBuilder withNewFlag(boolean isNew) {
		Preconditions.checkState(newIsDefined, "new flag already defined");
		itemChange.setIsNew(isNew);
		this.newIsDefined = true;
		return this;
	}
	
	public ItemChangeBuilder withApplicationData(IApplicationData data) {
		Preconditions.checkState(itemChange.getData() == null, "data already defined");
		itemChange.setData(data);
		return this;
	}

	public ItemChange build() {
		return itemChange;
	}
}