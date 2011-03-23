package org.obm.sync.auth;

public class Version implements Comparable<Version> {
	
	private final int major;
	private final int minor;
	private Integer release;
	private Integer subRelease;
	private String suffix;

	public interface Factory<T extends Version> {
		public T create(int major, int minor, Integer release, Integer subRelease, String suffix);
	}
	
	public static class FactoryImpl implements Factory<Version> {
		public Version create(int major, int minor, Integer release, Integer subRelease, String suffix) {
			return new Version(major, minor, release, subRelease, suffix);
		}
	}

	public Version(int major, int minor, Integer release, Integer subRelease) {
		this(major, minor, release, subRelease, null);
	}
	
	public Version(int major, int minor, Integer release, Integer subRelease, String suffix) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.subRelease = subRelease;
		this.suffix = suffix;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public Integer getRelease() {
		return release;
	}

	public Integer getSubRelease() {
		return subRelease;
	}

	public String getSuffix() {
		return suffix;
	}
	
	@Override
	public int compareTo(Version o) {
		if (getClass() != o.getClass()) {
			return getClass().hashCode() - o.getClass().hashCode();
		}
		if (major != o.major) {
			return major > o.major ? 10 : -10;
		} else if (minor != o.minor) {
			return minor > o.minor ? 10 : -10;
		} else if (compareInteger(release,o.release) != 0) {
			return compareInteger(release,o.release);
		} else if (compareInteger(subRelease, o.subRelease) != 0) {
			return compareInteger(subRelease, o.subRelease);
		}
		return 0;
	}
	
	private int compareInteger(Integer lhs, Integer rhs) {
		if (lhs == null) {
			if (rhs == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (rhs == null) {
				return 0;
			} else {
				return lhs - rhs;
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("%d.%d.%d.%d", major, minor, release, subRelease); 
	}

	public void setSubRelease(Integer subRelease) {
		this.subRelease = subRelease;
	}
	
	public void setRelease(Integer release) {
		this.release = release;
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
}
