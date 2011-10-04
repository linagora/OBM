package org.obm.sync.book;

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
	
	@Override
	public String toString() {
		return "AddressBook '" + name + "' (" + uid + ")";
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
}
