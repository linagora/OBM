package org.obm.sync.book;

public class AddressBook {

	private String name;
	private Integer uid;

	public AddressBook() {
		this(null, null);
	}
	
	public AddressBook(String name, Integer uid) {
		this.name = name;
		this.uid = uid;
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
}
