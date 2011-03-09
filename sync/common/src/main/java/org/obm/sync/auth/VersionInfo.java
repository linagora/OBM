package org.obm.sync.auth;

public class VersionInfo {
	
	private String major;
	private String minor;
	private String release;

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	@Override
	public String toString() {
		return getMajor() + "." + getMinor() + "." + getRelease();
	}

}
