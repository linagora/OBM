package org.obm.sync.book;

public class Email implements IMergeable {

	public Email(String email) {
		super();
		this.email = email;
	}

	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public void merge(IMergeable previous) {
		//do nothing on merge
	}

}
