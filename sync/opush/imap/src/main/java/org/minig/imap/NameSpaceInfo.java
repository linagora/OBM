package org.minig.imap;

import java.util.List;

import com.google.common.base.Objects;

public class NameSpaceInfo {
	
	private List<String> personal;
	private List<String> otherUsers;
	private List<String> mailShares;
	
	public List<String> getPersonal() {
		return personal;
	}
	public void setPersonal(List<String> personal) {
		this.personal = personal;
	}
	public List<String> getOtherUsers() {
		return otherUsers;
	}
	public void setOtherUsers(List<String> otherUsers) {
		this.otherUsers = otherUsers;
	}
	public List<String> getMailShares() {
		return mailShares;
	}
	public void setMailShares(List<String> mailShares) {
		this.mailShares = mailShares;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
			add("personal", personal).
			add("otherUsers", otherUsers).
			add("mailShares", mailShares).toString();
	}

}
