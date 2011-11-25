package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class MSAddress implements Serializable {
	
	private final String mail;
	private final String displayName;

	public MSAddress(String displayName, String mail) {
		super();
		this.displayName = displayName;
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(mail, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAddress) {
			MSAddress that = (MSAddress) object;
			return Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mail", mail)
			.add("displayName", displayName)
			.toString();
	}

}