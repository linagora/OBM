package org.obm.sync.auth;

public class OBMConnectorVersionException extends Exception {

	private static final long serialVersionUID = -4167563415379678235L;

	private AccessToken token;
	private Integer requiredMajor;
	private Integer requiredMinor;
	private Integer requiredRelease;
	private Integer requiredSubRelease;
	
	public OBMConnectorVersionException(AccessToken token, Integer requiredMajor, Integer requiredMinor, Integer requiredRelease, Integer requiredSubRelease) {
		this.token= token; 
	}
	
	public AccessToken getToken() {
		return token;
	}

	public Integer getRequiredMajor() {
		return requiredMajor;
	}

	public Integer getRequiredMinor() {
		return requiredMinor;
	}

	public Integer getRequiredRelease() {
		return requiredRelease;
	}

	public Integer getRequiredSubRelease() {
		return requiredSubRelease;
	}
	
}
