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

import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.exception.ImapLoginException;
import org.obm.push.mail.exception.NoImapClientAvailableException;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.ImapStore.Factory;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.OpushImapFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.sun.mail.imap.IMAPStore;

public class ImapClientProviderImpl {

	private static final Set<String> AVAILABLE_PROTOCOLS = ImmutableSet.of("imap", "imaps");
	private static final String IMAP_STORE_RESOURCE = "imap-store";

	private static final Logger logger = LoggerFactory.getLogger(ImapClientProviderImpl.class);

	
	private final Factory imapStoreFactory;
	private final LocatorService locatorService;
	private final MessageInputStreamProvider messageInputStreamProvider;
	private final boolean loginWithDomain;
	@VisibleForTesting final Session defaultSession;

	@Inject
	@VisibleForTesting ImapClientProviderImpl(ImapStore.Factory imapStoreFactory,
			EmailConfiguration emailConfiguration, LocatorService locatorService, 
			MessageInputStreamProvider messageInputStreamProvider) {
		
		this.imapStoreFactory = imapStoreFactory;
		this.locatorService = locatorService;
		this.messageInputStreamProvider = messageInputStreamProvider;
		this.loginWithDomain = emailConfiguration.loginWithDomain();
		
		Properties imapProperties = buildProperties(emailConfiguration);
		this.defaultSession = Session.getInstance(imapProperties);
	}

	private Properties buildProperties(EmailConfiguration emailConfiguration) {
		boolean activateTls = emailConfiguration.activateTls();
		int imapTimeout = emailConfiguration.imapTimeoutInMilliseconds();
		int imapFetchBlockSize = emailConfiguration.getImapFetchBlockSize();
		logger.debug("Java Mail settings : STARTTLS=" + activateTls);
		logger.debug("Java Mail settings : TIMEOUT=" + imapTimeout);
		logger.debug("Java Mail settings : BLOCKSIZE=" + imapFetchBlockSize);

		Properties properties = new Properties();
		properties.put("mail.debug", "true");
		properties.put("mail.imap.starttls.enable", activateTls);

		for (String availableProtocol : AVAILABLE_PROTOCOLS) {
			properties.put("mail." + availableProtocol +".timeout", imapTimeout);
			properties.put("mail." + availableProtocol +".fetchsize", imapFetchBlockSize);
		}
		
		return properties;
	}

	
	private String locateImap(UserDataRequest udr) throws LocatorClientException {
		String imapLocation = locatorService.
				getServiceLocation("mail/imap_frontend", udr.getUser().getLoginAtDomain());
		logger.info("Using {} as imap host.", imapLocation);
		return imapLocation;
	}

	public ImapStore getImapClient(UserDataRequest udr, OpushImapFolder opushImapFolder) throws LocatorClientException, IMAPException {
		try {
			ImapStore imapStore = retrieveWorkingImapStore(udr, opushImapFolder);
			if (imapStore != null) {
				return imapStore;
			} else {
				ImapStore newStore = buildImapStore(udr);
				newStore.login();
				udr.putResource(IMAP_STORE_RESOURCE, newStore);
				return newStore;
			}
		} catch (NoImapClientAvailableException e) {
			throw new IMAPException(e);
		} catch (ImapLoginException e) {
			throw new IMAPException(e);
		} catch (MessagingException e) {
			throw new IMAPException(e);
		} catch (RuntimeException e) {
			throw e;
		}
	}

	@VisibleForTesting ImapStore retrieveWorkingImapStore(UserDataRequest udr, OpushImapFolder opushImapFolder) throws MessagingException {
		ImapStore imapStore = (ImapStore) udr.getResource(IMAP_STORE_RESOURCE);
		if (imapStore != null && opushImapFolder != null) {
			try {
				if (imapStore.isConnected(opushImapFolder)) {
					return imapStore;
				} else {
					closeFolderAndStore(opushImapFolder, imapStore);
				}
			} catch (RuntimeException e) {
				closeFolderAndStore(opushImapFolder, imapStore);
			}
			return null;
		}
		if (imapStore != null) {
			closeStore(imapStore);
		}
		if (opushImapFolder != null) {
			closeFolder(opushImapFolder);
		}
		return null;
	}

	private void closeFolderAndStore(OpushImapFolder opushImapFolder, ImapStore imapStore) throws MessagingException {
		closeFolder(opushImapFolder);
		closeStore(imapStore);
	}
	
	private void closeStore(ImapStore imapStore) {
		imapStore.close();
	}
	
	private void closeFolder(OpushImapFolder opushImapFolder) throws MessagingException {
		opushImapFolder.close();
	}

	private ImapStore buildImapStore(UserDataRequest udr) throws NoImapClientAvailableException {
		final String imapHost = locateImap(udr);
		final String login = getLogin(udr);
		
		try {
			logger.debug("Creating storeClient with login {} : loginWithDomain = {}", 
					new Object[]{login, loginWithDomain});

			IMAPStore store = (IMAPStore) defaultSession.getStore(EmailConfiguration.IMAP_PROTOCOL);
			return imapStoreFactory.create(defaultSession, store, messageInputStreamProvider, login, udr.getPassword(), imapHost);
		} catch (NoSuchProviderException e) {
			throw new NoImapClientAvailableException(
					"No client available for protocol : " + EmailConfiguration.IMAP_PROTOCOL, e);
		}
	}

	private String getLogin(UserDataRequest udr) {
		User user = udr.getUser();
		if (loginWithDomain) {
			return user.getLoginAtDomain();
		} else {
			return user.getLogin();
		}
	}

}
