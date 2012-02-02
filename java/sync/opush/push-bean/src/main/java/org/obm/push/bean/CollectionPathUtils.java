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
package org.obm.push.bean;

import org.obm.push.exception.CollectionPathException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class CollectionPathUtils {

	// Pattern: obm:\\user@domain\email\Sent
	
	private static final char BACKSLASH = '\\';
	private static final String PROTOCOL = "obm:" + BACKSLASH + BACKSLASH;

	public static PIMDataType recognizePIMDataType(BackendSession bs, String collectionPath) 
			throws CollectionPathException {
		Preconditions.checkNotNull(Strings.emptyToNull(collectionPath));
		
		String userPath = getUserPath(bs).toString();
		if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.EMAIL)) {
			return PIMDataType.EMAIL;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.CALENDAR)) {
			return PIMDataType.CALENDAR;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.CONTACTS)) {
			return PIMDataType.CONTACTS;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.TASKS)) {
			return PIMDataType.TASKS;
		}
		String msg = String.format( 
				"Cannot reconize a PIMDataType from the collection path given . collection:{%s}",
				collectionPath);
		throw new CollectionPathException(msg);
	}


	private static boolean pathStartWithTypedUserPath(String pathToVerify,
			String userPath, PIMDataType dataType) {
		return pathToVerify.startsWith(userPathWithDataType(userPath, dataType));
	}


	private static String userPathWithDataType(String userPath, PIMDataType pimDataType) {
		return userPath.concat(pimDataType.asCollectionPathValue());
	}
	
	public static String buildCollectionPath(BackendSession bs, 
			PIMDataType collectionType, String imapFolder) {
		Preconditions.checkNotNull(bs);
		Preconditions.checkNotNull(collectionType);
		Preconditions.checkNotNull(Strings.emptyToNull(imapFolder));
		
		StringBuilder userPath = getUserPathByCollection(bs, collectionType);
		userPath.append(imapFolder);
		return userPath.toString();
	}
	
	public static String buildDefaultCollectionPath(BackendSession bs, PIMDataType collectionType) {
		Preconditions.checkNotNull(bs);
		Preconditions.checkNotNull(collectionType);
		
		StringBuilder defaultPath = getUserPathByCollection(bs, collectionType);
		trimEndingBackslash(defaultPath);
		return defaultPath.toString();
	}

	public static String extractImapFolder(BackendSession bs,
			String collectionPath, PIMDataType collectionType) throws CollectionPathException {
		Preconditions.checkNotNull(bs);
		Preconditions.checkNotNull(collectionType);
		Preconditions.checkNotNull(Strings.emptyToNull(collectionPath));
		
		String userPath = getUserPathByCollection(bs, collectionType).toString();
		
		if (collectionPath.startsWith(userPath)) {
			return extractFirstImapFolder(collectionPath, userPath);
		} else {
			String msg = String.format( 
					"The collection path given doesn't start with the user path. collection:{%s} user:{%s} ",
					collectionPath, userPath);
			throw new CollectionPathException(msg);
		}
	}

	private static String extractFirstImapFolder(String collectionPath, String userPath) {
		int imapFolderStartIndex = userPath.length();
		int imapFolderEndIndex = collectionPath.indexOf(BACKSLASH, imapFolderStartIndex);
		if (imapFolderEndIndex == -1) {
			return collectionPath.substring(imapFolderStartIndex);
		} else {
			return collectionPath.substring(imapFolderStartIndex, imapFolderEndIndex);
		}
	}

	private static StringBuilder getUserPathByCollection(BackendSession bs, PIMDataType collectionType) {
		StringBuilder userPath = getUserPath(bs);
		userPath.append(collectionType.asCollectionPathValue());
		userPath.append(BACKSLASH);
		return userPath;
	}
	
	private static StringBuilder getUserPath(BackendSession bs) {
		StringBuilder userPath = new StringBuilder();
		userPath.append(PROTOCOL);
		userPath.append(bs.getUser().getLoginAtDomain());
		userPath.append(BACKSLASH);
		return userPath;
	}

	private static void trimEndingBackslash(StringBuilder defaultPath) {
		int lastCharIndex = defaultPath.length() -1;
		if (defaultPath.charAt(lastCharIndex) == BACKSLASH) {
			defaultPath.deleteCharAt(lastCharIndex);
		}
	}
}
