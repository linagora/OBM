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
package org.obm.push.java.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class OpushImapFolderConnection {

	private static final Logger logger = LoggerFactory.getLogger(OpushImapFolderConnection.class);
	
	private OpushImapFolder opushImapFolder;
	
	public OpushImapFolderConnection() {
	}

	@VisibleForTesting OpushImapFolderConnection(OpushImapFolder opushImapFolder) {
		this.opushImapFolder = opushImapFolder;
	}
	
	public OpushImapFolder getOpushImapFolder() {
		return opushImapFolder;
	}
	
	public void closeImapFolderWhenChanged(String folderName) throws MessagingException {
		if (folderName == null) {
			closeAndUnsetImapFolder();
			return;
		}
		
		if (opushImapFolder != null && !opushImapFolder.getFullName().equals(folderName)) {
			closeAndUnsetImapFolder();
		}
	}
	
	@VisibleForTesting void closeAndUnsetImapFolder() throws MessagingException {
		if (opushImapFolder != null) {
			logger.debug("ImapFolder {} closed", opushImapFolder.getFullName());
			opushImapFolder.close();
			opushImapFolder = null;
		}
	}

	public void setImapFolderToFolderNameAndOpenIt(String folderName, ImapStore store) throws MessagingException {
		if (opushImapFolder == null && folderName != null) {
			logger.debug("ImapFolder {} opened", folderName);
			opushImapFolder = store.openFolder(folderName, Folder.READ_WRITE);
		}
		
		openImapFolderIfNeeded(folderName, store);
	}

	@VisibleForTesting void openImapFolderIfNeeded(String folderName, ImapStore store) throws MessagingException {
		if (opushImapFolder != null && !opushImapFolder.isOpen() && folderName != null) {
			logger.debug("ImapFolder {} opened", folderName);
			opushImapFolder = store.openFolder(folderName, Folder.READ_WRITE);
		}
	}
}
