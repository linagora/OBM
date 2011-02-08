package org.obm.sync.auth;

public class OBMConnectorVersionException extends Exception {

	private AccessToken token;
	private ConnectorVersion requiredVersion;
	
	public OBMConnectorVersionException(AccessToken token, ConnectorVersion requiredVersion) {
		this.token = token;
		this.requiredVersion = requiredVersion;
	}
	
	public AccessToken getToken() {
		return token;
	}

	public ConnectorVersion getConnectorVersion() {
		return requiredVersion;
	}
	
}
