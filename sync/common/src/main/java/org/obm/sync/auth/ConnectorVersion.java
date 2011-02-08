package org.obm.sync.auth;

public class ConnectorVersion implements Comparable<ConnectorVersion> {

	private int major;
	private int minor;
	private int release;
	private int subRelease;

	public ConnectorVersion(int major, int minor, int release, int subRelease) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.subRelease = subRelease;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getRelease() {
		return release;
	}

	public int getSubRelease() {
		return subRelease;
	}

	@Override
	public int compareTo(ConnectorVersion o) {
		if (major != o.major) {
			return major > o.major ? 10 : -10;
		} else if (minor != o.minor) {
			return minor > o.minor ? 10 : -10;
		} else if (release != o.release) {
			return release > o.release ? 10 : -10;
		} else if (subRelease != o.subRelease) {
			return subRelease > o.subRelease ? 10 : -10;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return String.format("%d.%d.%d.%d", major, minor, release, subRelease); 
	}
}
