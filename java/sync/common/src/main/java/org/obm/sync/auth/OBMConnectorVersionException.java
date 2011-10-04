package org.obm.sync.auth;

public class OBMConnectorVersionException extends Exception {

	private AccessToken token;
	private Version requiredVersion;
	
	public OBMConnectorVersionException(AccessToken token, Version requiredVersion) {
		this.token = token;
		this.requiredVersion = requiredVersion;
	}
	
	public AccessToken getToken() {
		return token;
	}

	public Version getConnectorVersion() {
		return requiredVersion;
	}
	
}
