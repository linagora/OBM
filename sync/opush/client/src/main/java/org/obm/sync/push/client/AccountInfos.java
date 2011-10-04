package org.obm.sync.push.client;

import org.apache.commons.codec.binary.Base64;

public class AccountInfos {
	private String login;
	private String password;
	private String userId;
	private String devId;
	private String devType;
	private String url;
	private String userAgent;

	public AccountInfos(String login, String password, String devId,
			String devType, String url, String userAgent) {
		this.login = login;
		int idx = login.indexOf('@');
		if (idx > 0) {
			String d = login.substring(idx + 1);
			this.userId = d + "\\" + login.substring(0, idx);
		}

		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.url = url;
		this.userAgent = userAgent;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getUserId() {
		return userId;
	}

	public String getDevId() {
		return devId;
	}

	public String getDevType() {
		return devType;
	}

	public String getUrl() {
		return url;
	}

	public String getUserAgent() {
		return userAgent;
	}
	
	public String authValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");
		String unCodedString = userId + ":" + password;
		String encoded = new String(Base64.encodeBase64(unCodedString.getBytes()));
		sb.append(encoded);
		String ret = sb.toString();
		return ret;
	}

}
