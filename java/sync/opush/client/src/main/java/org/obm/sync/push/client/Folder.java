package org.obm.sync.push.client;

public class Folder {
	
	private String serverId;
	private String parentId;
	private String name;
	private FolderType type;
	
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String exchangeId) {
		this.serverId = exchangeId;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public FolderType getType() {
		return type;
	}
	public void setType(FolderType type) {
		this.type = type;
	}
	

}
