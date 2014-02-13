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
package org.obm.push.mail.imap;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.mail.imap.idle.IdleClient;
import org.obm.push.minig.imap.StoreClient;
import org.obm.push.service.OpushLocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class LinagoraImapClientProvider {

	public static final String IMAP_STORE = "IMAP_STORE";
	
	private static final Logger logger = LoggerFactory.getLogger(LinagoraImapClientProvider.class);

	
	private final MinigStoreClient.Factory minigStoreClientFactory;
	private final OpushLocatorService locatorService;
	private final boolean loginWithDomain;
	private final boolean activateTLS;
	private final IdleClient.Factory idleClientFactory;

	@Inject
	@VisibleForTesting LinagoraImapClientProvider(MinigStoreClient.Factory minigStoreClientFactory,
			EmailConfiguration emailConfiguration, OpushLocatorService locatorService,
			IdleClient.Factory idleClientFactory) {
		
		this.minigStoreClientFactory = minigStoreClientFactory;
		this.locatorService = locatorService;
		this.loginWithDomain = emailConfiguration.loginWithDomain();
		this.activateTLS = emailConfiguration.activateTls();
		this.idleClientFactory = idleClientFactory;
	}
	
	private String locateImap(UserDataRequest udr) throws OpushLocatorException {
		String imapLocation = locatorService.
				getServiceLocation("mail/imap_frontend", udr.getUser().getLoginAtDomain());
		logger.info("Using {} as imap host.", imapLocation);
		return imapLocation;
	}

	public StoreClient getImapClient(UserDataRequest udr) throws OpushLocatorException, IMAPException {
		StoreClient storeClient = retrieveWorkingStoreClient(udr);
		if (storeClient != null) {
			return storeClient;
		}
		
		final String imapHost = locateImap(udr);
		final String login = getLogin(udr);

		MinigStoreClient newMinigStoreClient = minigStoreClientFactory.create(imapHost, login, udr.getPassword());
		newMinigStoreClient.login(activateTLS);
		udr.putResource(IMAP_STORE, newMinigStoreClient);
		
		logger.debug("Creating storeClient with login {} : loginWithDomain = {}", login, loginWithDomain);
		
		return newMinigStoreClient.getStoreClient(); 
	}

	private StoreClient retrieveWorkingStoreClient(UserDataRequest udr) {
		MinigStoreClient minigStoreClient = (MinigStoreClient) udr.getResource(IMAP_STORE);
		if (minigStoreClient != null) {
			try {
				StoreClient storeClient = minigStoreClient.getStoreClient();
				if (storeClient.isConnected()) {
					return storeClient;
				} else {
					minigStoreClient.close();
				}
			} catch (RuntimeException e) {
				minigStoreClient.close();
			}
		}
		return null;
	}

	private String getLogin(UserDataRequest udr) {
		User user = udr.getUser();
		if (loginWithDomain) {
			return user.getLoginAtDomain();
		} else {
			return user.getLogin();
		}
	}


	public IdleClient getImapIdleClient(UserDataRequest udr) throws OpushLocatorException {
		String login = getLogin(udr);
		logger.debug("Creating idleClient with login: {}, (useDomain {})", login, loginWithDomain);
		return idleClientFactory.create(locateImap(udr), 143, login, udr.getPassword());
	}

}
