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
package org.obm.push.mail;

import java.io.InputStream;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.obm.push.exception.ImapCommandException;
import org.obm.push.exception.ImapLoginException;
import org.obm.push.exception.ImapLogoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImapStore {

	private static final Logger logger = LoggerFactory.getLogger(ImapStore.class);
	
	private final Session session;
	private final Store store;
	
	private final String password;
	private final String userId;
	private final String host;
	private final int port;

	
	public ImapStore(Session session, Store store, String userId, String password, String host, int port) {
		this.session = session;
		this.store = store;
		this.userId = userId;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	public Message createMessage(InputStream messageContent) throws MessagingException {
		return new MimeMessage(session, messageContent);
	}

	public String getUserId() {
		return userId;
	}

	public void login() throws ImapLoginException {
		logger.debug("attempt imap login to {}:{} for user : {}",
				new Object[]{host, port, userId});
		
		try {
			store.connect(host, port, userId, password);
		} catch (MessagingException e) {
			String msg = "attempt imap login failed"; 
			throw new ImapLoginException(msg, e);
		}
	}

	public void logout() throws ImapLogoutException {
		try {
			store.close();
		} catch (MessagingException e) {
			String msg = "Logout failed for user: {} " + userId;
			throw new ImapLogoutException(msg, e);
		}
	}

	public Folder getFolder(String folderName) throws ImapCommandException {
		try {
			return store.getDefaultFolder().getFolder(folderName);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command getFolder failed. user={}, folder={}", userId, folderName);
			throw new ImapCommandException(msg, e);
		}
	}
}
