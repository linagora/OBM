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
package org.obm.push.mail.imap;

import javax.mail.Session;

import org.obm.sync.tag.Closable;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPStore;

public class ManagedLifecycleImapStore extends ImapStoreImpl implements Closable {

	@Singleton
	public static class Factory {
		
		private final MessageInputStreamProvider messageInputStreamProvider;
		private final Provider<ImapStoreManager> imapStoreManagerProvider;
		private final ImapMailBoxUtils imapMailBoxUtils;

		@Inject
		@VisibleForTesting Factory(Provider<ImapStoreManager> imapStoreManagerProvider,
				MessageInputStreamProvider messageInputStreamProvider,
				ImapMailBoxUtils imapMailBoxUtils) {
			this.imapStoreManagerProvider = imapStoreManagerProvider;
			this.messageInputStreamProvider = messageInputStreamProvider;
			this.imapMailBoxUtils = imapMailBoxUtils;
		}
		
		public ImapStore create(Session session, IMAPStore store, String userId, String password, String host, int port) {
			ImapStoreManager imapStoreManager = imapStoreManagerProvider.get();
			MessageInputStreamProvider imapStoreMessageInputStreamProvider =
					new ImapStoreMessageInputStreamProvider(messageInputStreamProvider, imapStoreManager);
			
			ManagedLifecycleImapStore imapStore = new ManagedLifecycleImapStore(imapStoreManager,
					imapStoreMessageInputStreamProvider, imapMailBoxUtils,
					session, store, userId, password, host, port);
			imapStoreManager.setImapStore(imapStore);
			return imapStore;
		}
	}

	private final ImapStoreManager imapStoreManager;	
	
	private ManagedLifecycleImapStore(ImapStoreManager imapStoreManager, 
			MessageInputStreamProvider messageInputStreamProvider, ImapMailBoxUtils imapMailBoxUtils,
			Session session, IMAPStore store, String userId, String password, String host, int port) {
		
		super(session, store, messageInputStreamProvider, imapMailBoxUtils, userId, password, host, port);
		this.imapStoreManager = imapStoreManager;
	}

	@Override
	public void logout() {
		imapStoreManager.closeWhenDone();
	}
	
	@Override
	public void close() {
		super.logout();
	}
	
}
