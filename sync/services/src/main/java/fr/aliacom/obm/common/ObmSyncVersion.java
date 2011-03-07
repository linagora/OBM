/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common;

import java.util.StringTokenizer;

import org.obm.sync.auth.MavenVersion;

// versionning MAJOR.MINOR.RELEASE
// DO NOT FORGET TO EDIT THIS FILE BEFORE TAG !
public final class ObmSyncVersion {

	public static final MavenVersion current() {
		Package p = ObmSyncVersion.class.getPackage();
		return parseImplementationVersion(p.getImplementationVersion());
	}

	// 2.3.22-SNAPSHOT
	private static MavenVersion parseImplementationVersion(
			String implementationVersion) {
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