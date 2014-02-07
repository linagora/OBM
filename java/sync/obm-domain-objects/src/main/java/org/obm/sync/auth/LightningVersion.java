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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LightningVersion extends Version {

	public static class FactoryImpl implements Version.Factory<LightningVersion> {
		public LightningVersion create(int major, int minor, Integer release, Integer subRelease, String suffix) {
			return new LightningVersion(major, minor, release, subRelease, suffix);
		}
	}
	
	private final Integer linagoraVersion;
	private final Pattern obmVersionPattern;
	private final Pattern linagoraVersionPattern;

	public LightningVersion(int major, int minor, Integer release, Integer subRelease, String suffix) {
		super(major, minor, release, subRelease, suffix);
		obmVersionPattern = Pattern.compile(".*?(\\d+)obm");
		linagoraVersionPattern = Pattern.compile(".*-LINAGORA-(\\d{2})");		
		linagoraVersion = parseSuffix();
	}

	private Integer parseSuffix() {
		Integer version = parseLinagoraSuffix(getSuffix());
		if (version == null) {
			version = parseObmSuffix(getSuffix());
			if (version == null) {
				version = parseObmSuffix(lastVersionPartAndSuffix());
				if (version != null) {
					mergeLastVersionAndSuffix();
				}
			}
		}
		return version;
	}

	private void mergeLastVersionAndSuffix() {
		if (getSubRelease() != null) {
			setSuffix(getSubRelease() + getSuffix());
			setSubRelease(null);
			return;
		}
		if (getRelease() != null) {
			setSuffix(getRelease() + getSuffix());
			setRelease(null);
			return;
		}
	}

	private String lastVersionPartAndSuffix() {
		return firstNonNull(getSubRelease(), getRelease()) + getSuffix();
	}
	
	private String firstNonNull(Integer... integers) {
		for (Integer i: integers) {
			if (i != null) {
				return String.valueOf(i);
			}
		}
		return "";
	}
	
	private Integer parseLinagoraSuffix(String suffix) {
		return extractVersionWithPattern(suffix, linagoraVersionPattern);
	}

	private Integer parseObmSuffix(String suffix) {
		return extractVersionWithPattern(suffix, obmVersionPattern);
	}

	private Integer extractVersionWithPattern(String suffix, Pattern pattern) {
		Matcher matcher = pattern.matcher(suffix);
		return getIntegerFromMatcher(matcher);
	}

	private Integer getIntegerFromMatcher(Matcher matcher) {
		if (matcher.matches()) {
			return Integer.valueOf(matcher.group(1));
		}
		return null;
	}

	
	public Integer getLinagoraVersion() {
		return linagoraVersion;
	}
	
}
