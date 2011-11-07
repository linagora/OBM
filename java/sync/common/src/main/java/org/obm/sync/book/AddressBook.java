package org.obm.sync.book;

import com.google.common.base.Objects;

public class AddressBook {

	private String name;
	private Integer uid;
	private boolean readOnly;

	public AddressBook() {
		this(null, null, true);
	}
	
	public AddressBook(String name, Integer uid, boolean readOnly) {
		this.name = name;
		this.uid = uid;
		this.readOnly = readOnly;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getUid() {
		return uid;
	}
	
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(uid);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof AddressBook) {
			AddressBook that = (AddressBook) object;
			return Objects.equal(this.uid, that.uid);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.add("uid", uid)
			.add("readOnly", readOnly)
			.toString();
	}
	
}
