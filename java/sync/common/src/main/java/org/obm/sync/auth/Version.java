/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.auth;

public class Version implements Comparable<Version> {
	
	private final int major;
	private final int minor;
	private Integer release;
	private Integer subRelease;
	private String suffix;

	public interface Factory<T extends Version> {
		T create(int major, int minor, Integer release, Integer subRelease, String suffix);
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
		return firstNonNull(suffix, "");
	}
	
	private String firstNonNull(String... values) {
		for (String s: values) {
			if (s != null) {
				return s;
			}
		}
		return "";
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
