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
package org.obm.opush;

import javax.mail.Session;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.java.mail.ImapMailBoxUtils;
import org.obm.push.java.mail.ImapStoreImpl;
import org.obm.push.mail.exception.ImapLoginException;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.MessageInputStreamProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPStore;

public class CountingImapStore extends ImapStoreImpl {

	private ImapConnectionCounter counter;

	@Singleton
	public static class Factory implements ImapStore.Factory {

		private ImapConnectionCounter counter;
		private final EmailConfiguration emailConfiguration;
		private final ImapMailBoxUtils imapMailBoxUtils;

		@Inject
		private Factory(ImapConnectionCounter counter, EmailConfiguration emailConfiguration, ImapMailBoxUtils imapMailBoxUtils) {
			this.counter = counter;
			this.emailConfiguration = emailConfiguration;
			this.imapMailBoxUtils = imapMailBoxUtils;
		}
		
		@Override
		public ImapStore create(Session session, IMAPStore store, MessageInputStreamProvider messageInputStreamProvider,
				String userId, String password, String host) {
			return new CountingImapStore(session, store, messageInputStreamProvider, imapMailBoxUtils, userId, password, host, emailConfiguration.imapPort(), counter);
		}
		
	}
	
	private CountingImapStore(Session session, IMAPStore store, 
			MessageInputStreamProvider messageInputStreamProvider, ImapMailBoxUtils imapMailBoxUtils,
			String userId, String password, String host, int port, ImapConnectionCounter counter) {
		super(session, store, messageInputStreamProvider, imapMailBoxUtils, userId, password, host, port);
		this.counter = counter;
	}
	
	@Override
	public void login() throws ImapLoginException {
		super.login();
		counter.loginCounter.incrementAndGet();
	}
	
	@Override
	public void close() {
		super.close();
		counter.closeCounter.incrementAndGet();
	}
	
}
