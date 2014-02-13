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
package org.obm.opush;

import org.apache.mina.transport.socket.SocketConnector;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.StoreClientImpl;
import org.obm.push.minig.imap.impl.ClientSupport;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

public class CountingStoreClient extends StoreClientImpl {

	private final ImapConnectionCounter counter;

	@Singleton
	public static class Factory extends StoreClientImpl.Factory {

		private final ImapConnectionCounter counter;

		@Inject
		private Factory(ImapConnectionCounter counter, EmailConfiguration emailConfiguration, Provider<SocketConnector> socketConnectorProvider) {
			super(emailConfiguration, socketConnectorProvider);
			this.counter = counter;
		}
		
		@Override
		public CountingStoreClient create(String hostname, String login, String password) {
			return new CountingStoreClient(counter, hostname, emailConfiguration.imapPort(),
					login, password, emailConfiguration.mailboxNameCheckPolicy(), super.createClientSupport());
		}
		
	}
	
	private CountingStoreClient(ImapConnectionCounter counter, String hostname, int port,
			String login, String password, EmailConfiguration.MailboxNameCheckPolicy mailboxNameCheckPolicy, ClientSupport clientSupport) {
		super(hostname, port, login, password, mailboxNameCheckPolicy, clientSupport);
		this.counter = counter;
	}
	
	@Override
	protected boolean selectMailboxImpl(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		boolean selected = super.selectMailboxImpl(mailbox);
		counter.selectCounter.incrementAndGet();
		return selected;
	}
	
	@Override
	public ListResult listAll() throws ImapTimeoutException {
		ListResult listAll = super.listAll();
		counter.listMailboxesCounter.incrementAndGet();
		return listAll;
	}
}
