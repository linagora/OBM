package org.obm.sync.book;

public class Phone implements IMergeable {

	private String number;

	public Phone(String number) {
		super();
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Override
	public void merge(IMergeable previous) {
		//do nothing on merge
	}

}
