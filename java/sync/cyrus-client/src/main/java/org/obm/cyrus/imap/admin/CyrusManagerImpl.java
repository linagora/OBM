/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.cyrus.imap.admin;

import org.obm.push.mail.IMAPException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.StoreClient;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class CyrusManagerImpl implements CyrusManager {

	private final static String TRASH = "Trash";
	private final static String DRAFTS = "Drafts";
	private final static String SPAM = "SPAM";
	private final static String TEMPLATES = "Templates";
	private final static String SENT = "Sent";

	public static class Factory implements CyrusManager.Factory {

		private Connection.Factory connectionFactory;
		private StoreClient.Factory storeClientFactory;

		@Inject
		public Factory(Connection.Factory connectionFactory, StoreClient.Factory storeClientFactory) {
			this.connectionFactory = connectionFactory;
			this.storeClientFactory = storeClientFactory;
		}

		@Override
		public CyrusManagerImpl create(String hostname, String login, String password) throws IMAPException {
			StoreClient storeClient = storeClientFactory.create(hostname, login, password);
			storeClient.login(false);
			return new CyrusManagerImpl(connectionFactory.create(storeClient));
		}
		
	}

	private Connection conn;

	private CyrusManagerImpl(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void create(ObmUser obmUser) {
		final String domain = obmUser.getDomain().getName();
		String user = obmUser.getLogin();
		Partition partition = Partition.fromObmDomain(domain);
		conn.createUserMailboxes(
				partition,
				ImapPath.builder().user(user).domain(domain).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TRASH).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(DRAFTS).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SPAM).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TEMPLATES).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SENT).build()
				);
	}
	
	@Override
	public void delete(ObmUser obmUser) {
		final String domain = obmUser.getDomain().getName();
		String user = obmUser.getLogin();
		conn.delete(ImapPath.builder().user(user).domain(domain).build());
	}
	
	@Override
	public void setAcl(ObmUser obmUser, String identifier, Acl acl) {
		final String domain = obmUser.getDomain().getName();
		String user = obmUser.getLogin();
		conn.setAcl(
				ImapPath.builder().user(user).domain(domain).build(),
				identifier,
				acl);
	}

	@Override
	public void applyQuota(ObmUser obmUser) {
		conn.setQuota(ImapPath
				.builder()
				.user(obmUser.getLogin())
				.domain(obmUser.getDomain().getName())
				.build(), Quota.valueOf(obmUser.getMailQuota()));
	}

	@Override
	public void shutdown() {
		conn.shutdown();
	}

}
