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
package org.obm.push.impl;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * 	Pattern :
 * 
 *  obm:\\login@domain\email\Sent
 *  obm:\\login@domain\calendar\login@domain
 *  obm:\\login@domain\contacts
 *  obm:\\login@domain\contacts\collected_contacts
 * 
 */
@Singleton
public class CollectionPathHelper implements ICollectionPathHelper {

	private static final char BACKSLASH = '\\';
	private static final String PROTOCOL = "obm:" + BACKSLASH + BACKSLASH;

	private final EmailConfiguration emailConfiguration;
	
	@Inject
	@VisibleForTesting CollectionPathHelper(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}
	
	@Override
	public PIMDataType recognizePIMDataType(String collectionPath) 
			throws CollectionPathException {
		
		Preconditions.checkNotNull(Strings.emptyToNull(collectionPath));
		
		String userPath = getUserPath(collectionPath).toString();
		if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.EMAIL)) {
			return PIMDataType.EMAIL;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.CALENDAR)) {
			return PIMDataType.CALENDAR;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.CONTACTS)) {
			return PIMDataType.CONTACTS;
		} else if (pathStartWithTypedUserPath(collectionPath, userPath, PIMDataType.TASKS)) {
			return PIMDataType.TASKS;
		}
		return PIMDataType.UNKNOWN;
	}

	private boolean pathStartWithTypedUserPath(String pathToVerify, String userPath, PIMDataType dataType) {
		return pathToVerify.startsWith(userPathWithDataType(userPath, dataType));
	}

	private String userPathWithDataType(String userPath, PIMDataType pimDataType) {
		return userPath.concat(pimDataType.asCollectionPathValue());
	}
	
	@Override
	public String buildCollectionPath(UserDataRequest udr, PIMDataType collectionType, String...imapFolders) {
		Preconditions.checkNotNull(udr);
		Preconditions.checkNotNull(collectionType);
		Preconditions.checkArgument(collectionType != PIMDataType.UNKNOWN);
		Preconditions.checkNotNull(imapFolders);
		
		StringBuilder userPath = getUserPathByCollection(udr, collectionType);
		for (String folder: imapFolders) {
			userPath.append(BACKSLASH);
			userPath.append(folder);
		}
		return userPath.toString();
	}

	@Override
	public String extractFolder(UserDataRequest udr, String collectionPath, PIMDataType collectionType)
			throws CollectionPathException {
		
		Preconditions.checkNotNull(udr);
		Preconditions.checkNotNull(collectionType);
		Preconditions.checkNotNull(Strings.emptyToNull(collectionPath));
		Preconditions.checkArgument(collectionType != PIMDataType.UNKNOWN);
		
		String userPath = getUserPathByCollection(udr, collectionType).toString();
		
		if (collectionPath.startsWith(userPath)) {
			return extractFolder(udr, collectionPath, userPath);
		} else {
			String msg = String.format( 
					"The collection path given doesn't start with the user path. collection:{%s} user:{%s} ",
					collectionPath, userPath);
			throw new CollectionPathException(msg);
		}
	}

	private String extractFolder(UserDataRequest udr, String collectionPath, String userPath) {
		int backslashLength = 1;
		int imapFolderStartIndex = userPath.length() + backslashLength;
		if (imapFolderStartIndex > collectionPath.length()) {
			imapFolderStartIndex = getUserPath(udr).length();
		}
		String folders = collectionPath.substring(imapFolderStartIndex);
		return handleSpecificFolder(folders);
	}

	private String handleSpecificFolder(String folder) {
		if (folder.equals(EmailConfiguration.IMAP_DRAFTS_NAME)) {
			return emailConfiguration.imapMailboxDraft();
		} else if (folder.equals(EmailConfiguration.IMAP_SENT_NAME)) {
			return emailConfiguration.imapMailboxSent();
		} else if (folder.equals(EmailConfiguration.IMAP_TRASH_NAME)) {
			return emailConfiguration.imapMailboxTrash();
		}
		return folder;
	}

	private StringBuilder getUserPathByCollection(UserDataRequest udr, PIMDataType collectionType) {
		StringBuilder userPath = getUserPath(udr);
		userPath.append(collectionType.asCollectionPathValue());
		return userPath;
	}
	
	private StringBuilder getUserPath(UserDataRequest udr) {
		return buildUserPath(udr.getUser().getLoginAtDomain());
	}
	
	private StringBuilder getUserPath(String collectionPath) {
		String collectionPathWihtoutProtocol = substringProtocol(collectionPath);
		String userAtLogin = substringUserAtLogin(collectionPathWihtoutProtocol);
		return buildUserPath(userAtLogin);
	}

	private String substringUserAtLogin(String collectionPathWihtoutProtocol) {
		String userAtLogin = collectionPathWihtoutProtocol;
		if (collectionPathWihtoutProtocol.contains(String.valueOf(BACKSLASH))) {
			int firstBackslashIndex = collectionPathWihtoutProtocol.indexOf(BACKSLASH);
			userAtLogin = collectionPathWihtoutProtocol.substring(0, firstBackslashIndex);
		}
		return checkUserAtLoginValidity(userAtLogin);
	}

	private String checkUserAtLoginValidity(String userAtLogin) {
		Iterable<String> loginParts = Splitter.on('@').omitEmptyStrings().split(userAtLogin);
		if (Iterables.size(loginParts) == 2) {
			return userAtLogin;
		}
		throw new CollectionPathException("The collection path doesn't contain valid userAtLogin : " + userAtLogin); 
	}

	private String substringProtocol(String collectionPath) {
		if (collectionPath.startsWith(PROTOCOL)) {
			return collectionPath.substring(PROTOCOL.length());
		}
		throw new CollectionPathException("The collection path doesn't start with the protocol : " +collectionPath);
	}
	
	private StringBuilder buildUserPath(String user) {
		StringBuilder userPath = new StringBuilder();
		userPath.append(PROTOCOL);
		userPath.append(user);
		userPath.append(BACKSLASH);
		return userPath;
	}
}
