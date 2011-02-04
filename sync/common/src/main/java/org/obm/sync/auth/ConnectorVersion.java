package org.obm.sync.auth;

public class ConnectorVersion implements Comparable<ConnectorVersion> {

	private Integer major;
	private Integer minor;
	private Integer release;
	private Integer subRelease;

	public ConnectorVersion(Integer major, Integer minor, Integer release,
			Integer subRelease) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.subRelease = subRelease;
	}

	public Integer getMajor() {
		return major;
	}

	public Integer getMinor() {
		return minor;
	}

	public Integer getRelease() {
		return release;
	}

	public Integer getSubRelease() {
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
}
