package org.obm.sync.setting;

public class ForwardingSettings {

	private boolean enabled;
	private boolean localCopy;
	private boolean allowed;

	private String email;

	public ForwardingSettings() {
		allowed = true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isLocalCopy() {
		return localCopy;
	}

	public void setLocalCopy(boolean localCopy) {
		this.localCopy = localCopy;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

}
