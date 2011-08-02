package org.obm.push;

import java.io.Serializable;

import org.obm.push.bean.IApplicationData;
import org.obm.push.store.FolderType;

public class ItemChange  implements Serializable {
	
	private String serverId;
	private String parentId;
	
	private String displayName;
	private FolderType itemType;
	private boolean isNew;
	
	private IApplicationData data;

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public FolderType getItemType() {
		return itemType;
	}

	public void setItemType(FolderType itemType) {
		this.itemType = itemType;
	}

	public IApplicationData getData() {
		return data;
	}

	public void setData(IApplicationData data) {
		this.data = data;
	}

	public Boolean isNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}
}
