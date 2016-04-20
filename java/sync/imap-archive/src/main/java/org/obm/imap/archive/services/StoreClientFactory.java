/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.services;

import org.obm.domain.dao.SharedMailboxDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.imap.archive.exception.NoBackendDefineForSharedMailboxException;
import org.obm.imap.archive.exception.SharedMailboxNotFoundException;
import org.obm.locator.store.LocatorService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.push.minig.imap.StoreClient.Factory;
import org.obm.sync.host.ObmHost;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.resource.SharedMailbox;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class StoreClientFactory {

	private final LocatorService locatorService;
	private final UserSystemDao userSystemDao;
	private final UserDao userDao;
	private final SharedMailboxDao sharedMailboxDao;
	private final Factory storeClientFactory;

	@Inject
	@VisibleForTesting StoreClientFactory(LocatorService locatorService, 
			UserSystemDao userSystemDao, 
			UserDao userDao,
			SharedMailboxDao sharedMailboxDao,
			StoreClient.Factory storeClientFactory) {
		
		this.locatorService = locatorService;
		this.userSystemDao = userSystemDao;
		this.userDao = userDao;
		this.sharedMailboxDao = sharedMailboxDao;
		this.storeClientFactory = storeClientFactory;
	}
	
	public StoreClient create(String domainName) throws SystemUserNotFoundException, DaoException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(domainName));
		 
		ObmSystemUser cyrusUser = userSystemDao.getByLogin(ObmSystemUser.CYRUS);
		String cyrusAddress = locatorService.getServiceLocation("mail/imap_frontend", domainName);
		return storeClientFactory.create(cyrusAddress, cyrusUser.getLogin(), cyrusUser.getPassword().getStringValue().toCharArray());
	}
	
	public StoreClient createOnUserBackend(String user, ObmDomain domain) throws SystemUserNotFoundException, DaoException, UserNotFoundException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(user));
		Preconditions.checkArgument(domain != null);
		 
		ObmSystemUser cyrusUser = userSystemDao.getByLogin(ObmSystemUser.CYRUS);
		return storeClientFactory.create(cyrusBackendFor(user, domain), cyrusUser.getLogin(), cyrusUser.getPassword().getStringValue().toCharArray());
	}
	
	private String cyrusBackendFor(String user, ObmDomain domain) throws UserNotFoundException {
		ObmUser userObm = userDao.findUserByLogin(user, domain);
		if (userObm == null) {
			throw new UserNotFoundException(String.format("User %s on domain %s not found in OBM database", user, domain.getName()));
		}
		
		return userObm.getUserEmails().getServer().getIp();
	}
	
	public StoreClient createOnSharedMailboxBackend(String sharedMailboxName, ObmDomain domain) 
			throws SystemUserNotFoundException, DaoException, SharedMailboxNotFoundException, NoBackendDefineForSharedMailboxException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(sharedMailboxName));
		Preconditions.checkArgument(domain != null);
		 
		ObmSystemUser cyrusUser = userSystemDao.getByLogin(ObmSystemUser.CYRUS);
		return storeClientFactory.create(cyrusBackendForSharedMailbox(sharedMailboxName, domain), cyrusUser.getLogin(), cyrusUser.getPassword().getStringValue().toCharArray());
	}
	
	private String cyrusBackendForSharedMailbox(String name, ObmDomain domain) throws SharedMailboxNotFoundException, NoBackendDefineForSharedMailboxException {
		Optional<SharedMailbox> sharedMailbox = sharedMailboxDao.findSharedMailboxByName(name, domain);
		if (!sharedMailbox.isPresent()) {
			throw new SharedMailboxNotFoundException(name);
		}
		
		Optional<ObmHost> server = sharedMailbox.get().getServer();
		if (!server.isPresent()) {
			throw new NoBackendDefineForSharedMailboxException(name);
		}
		return server.get().getIp();
	}
}
