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
package org.obm.sync.login;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
import org.obm.sync.server.auth.AuthentificationServiceFactory;
import org.obm.sync.server.auth.impl.DatabaseAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.DomainNotFoundException;
import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.domain.DomainDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

@Singleton
public class LoginBindingImpl extends AbstractLoginBackend implements LoginBackend {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AuthentificationServiceFactory authentificationServiceFactory;
	private final DatabaseAuthentificationService databaseAuthentificationService;
	private final ObmSyncConfigurationService configurationService;
	private final DomainDao domainDao;
	private final UserDao userDao;

	@Inject
	protected LoginBindingImpl(SessionManagement sessionManagement,
			AuthentificationServiceFactory authentificationServiceFactory,
			DatabaseAuthentificationService databaseAuthentificationService,
			ObmSyncConfigurationService configurationService,
			DomainDao domainDao,
			UserDao userDao) {
		
		super(sessionManagement);
		this.authentificationServiceFactory = authentificationServiceFactory;
		this.databaseAuthentificationService = databaseAuthentificationService;
		this.configurationService = configurationService;
		this.domainDao = domainDao;
		this.userDao = userDao;
	}

	@Override
	@Transactional(readOnly=true)
	public AccessToken logUserIn(String user, String password, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain, boolean isPasswordHashed) throws ObmSyncVersionNotFoundException, DomainNotFoundException, AuthFault {

		return sessionManagement.login(user, password, origin, clientIP, remoteIP, lemonLogin, lemonDomain, isPasswordHashed);
	}

	@Transactional(readOnly=true)
	public boolean authenticateGlobalAdmin(String user, String password, String origin, boolean isPasswordHashed) throws AuthFault {
		logger.info("trying global admin authentication with login '{}' from '{}'", user, origin);
		Credentials credentials = Credentials.builder()
			.login(user)
			.domain(configurationService.getGlobalDomain())
			.hashedPassword(isPasswordHashed)
			.password(password)
			.build();
		
		return databaseAuthentificationService.doAuth(credentials);
	}

	@Transactional(readOnly=true)
	public boolean authenticateAdmin(String user, String password, String origin, String domainName, boolean isPasswordHashed) throws AuthFault {
		logger.info("trying {} admin authentication with login '{}' from '{}'", new String[] { domainName, user, origin });
		ObmDomain domain = domainDao.findDomainByName(domainName);
		Credentials credentials = Credentials.builder()
			.login(user)
			.domain(domain.getName())
			.hashedPassword(isPasswordHashed)
			.password(password)
			.build();
		
		return authenticateAdmin(domain, credentials);
	}

	private boolean authenticateAdmin(ObmDomain domain, Credentials credentials) throws AuthFault {
		ObmUser user = userDao.findUserByLogin(credentials.getLogin().getLogin(), domain);
		if (user == null || !user.isAdmin()) {
			return false;
		}
		
		if (!domain.isGlobal()) {
			return authentificationServiceFactory.get().doAuth(credentials);
		}
		return databaseAuthentificationService.doAuth(credentials);
	}
}
