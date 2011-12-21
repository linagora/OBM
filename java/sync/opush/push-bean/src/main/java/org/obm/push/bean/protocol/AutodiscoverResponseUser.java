package org.obm.push.bean.protocol;

import com.google.common.base.Objects;

public class AutodiscoverResponseUser {

	private final String emailAddress;
	private final String displayName;
	
	public AutodiscoverResponseUser(String emailAddress, String displayName) {
		this.emailAddress = emailAddress;
		this.displayName = displayName;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(emailAddress, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponseUser) {
			AutodiscoverResponseUser that = (AutodiscoverResponseUser) object;
			return Objects.equal(this.emailAddress, that.emailAddress)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("emailAddress", emailAddress)
			.add("displayName", displayName)
			.toString();
	}
	
}