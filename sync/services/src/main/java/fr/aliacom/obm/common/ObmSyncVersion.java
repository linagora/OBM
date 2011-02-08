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

import org.obm.sync.auth.VersionInfo;

// versionning MAJOR.MINOR.RELEASE
// DO NOT FORGET TO EDIT THIS FILE BEFORE TAG !
public final class ObmSyncVersion {

	public static final VersionInfo current() {
		VersionInfo version = new VersionInfo();
		version.setMajor(ObmSyncVersion.MAJOR);
		version.setMinor(ObmSyncVersion.MINOR);
		version.setRelease(ObmSyncVersion.RELEASE);
		return version;
	}
	
	public static final String MAJOR = "2";
	public static final String MINOR = "3";
	public static final String RELEASE = "16";

}