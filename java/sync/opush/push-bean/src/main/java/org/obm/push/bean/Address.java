package org.obm.push.bean;

import com.google.common.base.Objects;

public class Address {

	private final String mailAddress;

	public Address(String mailAddress) {
		this.mailAddress = mailAddress;
	}

	public String getMailAddress() {
		return mailAddress;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(mailAddress);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof Address) {
			Address that = (Address) object;
			return Objects.equal(this.mailAddress, that.mailAddress);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mailAddress", mailAddress)
			.toString();
	}
	
	

}
