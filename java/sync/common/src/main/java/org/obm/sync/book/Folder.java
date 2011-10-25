package org.obm.sync.book;

import com.google.common.base.Objects;

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

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("name", name)
			.add("ownerDisplayName", ownerDisplayName)
			.toString();
	}

}
