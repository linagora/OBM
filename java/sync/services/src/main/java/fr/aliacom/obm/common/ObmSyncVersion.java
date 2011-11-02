/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common;

import java.util.StringTokenizer;

import org.obm.sync.auth.MavenVersion;

// versionning MAJOR.MINOR.RELEASE
// DO NOT FORGET TO EDIT THIS FILE BEFORE TAG !
public final class ObmSyncVersion {

	public static final MavenVersion current() throws ObmSyncVersionNotFoundException {
		Package p = ObmSyncVersion.class.getPackage();
		String version = p.getImplementationVersion();
		if (version == null) {
			throw new ObmSyncVersionNotFoundException();
		}
		return parseImplementationVersion(version);
	}
	
	// 2.3.22-SNAPSHOT
	private static MavenVersion parseImplementationVersion(String implementationVersion) {
		MavenVersion version = new MavenVersion();
		StringTokenizer token = new StringTokenizer(implementationVersion, ".");
		if (token.hasMoreTokens()) {
			String major = getNextString(token);
			String minor = getNextString(token);
			String release = "0";
			// 22-SNAPSHOT
			String tokenSubRelease = getNextString(token);
			token = new StringTokenizer(tokenSubRelease, "-");
			if (token.hasMoreTokens()) {
				release = getNextString(token);
			} else {
				release = tokenSubRelease;
			}

			version.setMajor(major);
			version.setMinor(minor);
			version.setRelease(release);
		}
		return version;
	}

	private static String getNextString(StringTokenizer token) {
		if (token.hasMoreTokens()) {
			return token.nextToken();
		}
		return "0";
	}
	
}