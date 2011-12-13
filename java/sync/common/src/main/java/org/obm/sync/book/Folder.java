package org.obm.sync.book;

public class Folder {

	private Integer uid;
	private String name;
	private String ownerDisplayName;
	
	public Folder() {
		super();
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}
	
	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

}
