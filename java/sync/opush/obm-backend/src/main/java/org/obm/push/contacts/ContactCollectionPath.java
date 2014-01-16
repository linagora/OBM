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
package org.obm.push.contacts;

import org.obm.push.backend.CollectionPath;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Folder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class ContactCollectionPath {

	private static final String BACKEND_NAME_SEPARATOR = ":";
	private static final int BACKEND_NAME_SEPARATOR_LENGHT = BACKEND_NAME_SEPARATOR.length();
	
	public static String folderName(CollectionPath collectionPath) {
		return parseFolderName(collectionPath.backendName());
	}
	
	@VisibleForTesting static String parseFolderName(String backendName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(backendName));
		try {
			int uidEndingPosition = backendName.indexOf(BACKEND_NAME_SEPARATOR);
			int nameStartingPosition = uidEndingPosition + BACKEND_NAME_SEPARATOR_LENGHT;
			if (uidEndingPosition > 0 && backendName.length() > nameStartingPosition) {
				return backendName.substring(nameStartingPosition);
			}
		} catch (IndexOutOfBoundsException e) {
			// wrong format
		}
		throw new IllegalArgumentException("backendName format is not as expected:{folderUid:folderName}," +
				" found:{" + backendName + "}");
	}

	public static String backendName(Folder folder) {
		return backendNameFromParts(folder.getUid(), folder.getName());
	}

	public static String backendName(AddressBook addressBook) {
		return backendNameFromParts(addressBook.getUid().getId(), addressBook.getName());
	}

	public static String backendNameFromParts(int uid, String name) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		return Joiner.on(BACKEND_NAME_SEPARATOR).join(String.valueOf(uid), name);
	}
}
