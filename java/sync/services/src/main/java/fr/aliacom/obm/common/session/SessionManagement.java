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
package fr.aliacom.obm.common.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
import org.obm.sync.auth.Login;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.server.auth.AuthentificationServiceFactory;
import org.obm.sync.server.auth.IAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.DomainNotFoundException;
import fr.aliacom.obm.common.ObmSyncVersion;
import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.services.constant.SpecialAccounts;

/**
 * Helpers for working with userobm & domain tables Authentification method is
 * also here
 */
@Singleton
public class SessionManagement {

	private final ConcurrentMap<String, AccessToken> sessions;

	private static final Logger logger = LoggerFactory
			.getLogger(SessionManagement.class);

	private final AtomicInteger conversationUidGenerator;
	private final AuthentificationServiceFactory authentificationServiceFactory;
	private final UserDao userManagementDAO;
	private final DomainService domainService;
	private final ObmSyncConfigurationService configuration;
	private final SpecialAccounts specialAccounts;

	@Inject
	@VisibleForTesting SessionManagement(AuthentificationServiceFactory authentificationServiceFactory,
			DomainService domainService, UserDao userDao, ObmSyncConfigurationService configurationService,
			SpecialAccounts specialAccounts) {

		this.userManagementDAO = userDao;
		this.configuration = configurationService;
		this.specialAccounts = specialAccounts;
		this.conversationUidGenerator = new AtomicInteger();
		this.sessions = configureSessionCache();
		this.domainService = domainService;
		this.authentificationServiceFactory = authentificationServiceFactory;
	}

	private ConcurrentMap<String, AccessToken> configureSessionCache() {
		Cache<String, AccessToken> cache =  CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES)
				.concurrencyLevel(16)
				.removalListener(new RemovalListener<String, AccessToken>() {

					@Override
					public void onRemoval(RemovalNotification<String, AccessToken> notification) {
						logSessionRemoval();
					}
				})
				.build(new CacheLoader<String, AccessToken>() {
					@Override
					public AccessToken load(String accessToken) throws Exception {
						return null;
					}
				});
		return cache.asMap();
	}

	private String newSessionId() {
		return UUID.randomUUID().toString();
	}

	private boolean doAuthSpecialAccount(String userLogin, ObmDomain obmDomain,	String clientIP) {
		String fullLogin = userLogin + "@" + obmDomain.getName();
		if (specialAccounts.isRootAccount(fullLogin, clientIP)) {
			logger.info("login as Root Account : " + fullLogin + " from  " + clientIP);
			return true;
		} else if (specialAccounts.isAnyUserAccount(clientIP)) {
			logger.info("login as AnyUser Account : " + fullLogin + " from  " + clientIP);
			return true;
		} else if (specialAccounts.isApplicAccount(fullLogin,	clientIP)) {
			logger.info("login as Appli Account : " + fullLogin + " from  "	+ clientIP);
			return true;
		}
		return false;
	}

	private boolean doAuthLemonLdap(String remoteIP) {
		String ip = remoteIP.trim();
		logger.debug("lemon login with ip " + remoteIP);
		Iterable<String> lemonIPs = configuration.getLemonLdapIps();
		for (String lemonIp: lemonIPs) {
			if (lemonIp.equals(ip)) {
				logger.info("login from lemonLDAP ok from ip " + remoteIP);
				return true;
			}
		}

		logger.warn("LemonLDAP login refused for ip '" + ip
				+ "' with authorized ips '" + lemonIPs + "'");
		return false;
	}

	/**
	 * logs user without authenticating (simply accepts as a valid login) this should
	 * either not be exposed to the outside world or have its result encrypted lest
	 * one wants everyone to be able to login as anyone!
	 */
	public AccessToken trustedLogin(String specifiedLogin, String origin,
			String clientIP, String remoteIP, String lemonLogin, String lemonDomain) throws DomainNotFoundException {
		IAuthentificationService authService = authentificationServiceFactory.get();

		Login login = prepareLogin(specifiedLogin, lemonLogin, lemonDomain);
		logLoginAttempt(origin, clientIP, remoteIP, lemonLogin, lemonDomain, login);

		ObmDomain obmDomain = domainFromLogin(login);

		return login(origin, login.getLogin(), obmDomain, authService.getType());
	}

	/**
	 * @return null if the credential are not valid
	 * @throws ObmSyncVersionNotFoundException
	 */
	public AccessToken login(String specifiedLogin, String password, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain, boolean isPasswordHashed) throws ObmSyncVersionNotFoundException, DomainNotFoundException, AuthFault {

		IAuthentificationService authService = authentificationServiceFactory.get();

		Login login = prepareLogin(specifiedLogin, lemonLogin, lemonDomain);
		logLoginAttempt(origin, clientIP, remoteIP, lemonLogin, lemonDomain, login);

		ObmDomain obmDomain = domainFromLogin(login);

		if ((lemonLogin != null && lemonDomain != null
				&& doAuthLemonLdap(remoteIP))
			|| doAuthSpecialAccount(login.getLogin(), obmDomain, clientIP)
			|| authService.doAuth(buildCredentials(login, password, isPasswordHashed))) {

			return login(origin, login.getLogin(), obmDomain, authService.getType());
		}
		logLoginFailure(login.getLogin(), authService, obmDomain.getName());
		return null;
	}

	private ObmDomain domainFromLogin(Login login) throws DomainNotFoundException {
		ObmDomain domain = null;

		if (login.hasDomain()) {
			domain = domainService.findDomainByName(login.getDomain());
		}

		if (domain == null) {
			throw new DomainNotFoundException("Cannot figure out domain from login '" + login.getFullLogin() + "'.");
		}

		return domain;
	}

	private Credentials buildCredentials(Login login, String password, boolean isPasswordHashed) {
		return Credentials.builder()
				.login(login)
				.hashedPassword(isPasswordHashed)
				.password(password)
				.build();
	}

	@VisibleForTesting Login prepareLogin(String specifiedLogin, String lemonLogin,
			String lemonDomain) throws DomainNotFoundException {
		Login login = Login.builder().login(chooseLogin(specifiedLogin, lemonLogin, lemonDomain)).build();
		return login.hasDomain()
				? login
				: login.withDomain(userManagementDAO.getUniqueObmDomain(login.getLogin()));
	}

	private AccessToken login(String origin, String userLogin, ObmDomain obmDomain, String authServiceType) {
		AccessToken token = buildAccessToken(origin, userLogin, obmDomain);
		registerTokenInSession(token);
		logLoginSuccess(userLogin, obmDomain.getName(), authServiceType, token);
		return token;
	}

	private void logLoginFailure(String userLogin,
			IAuthentificationService authService, String obmDomainName) {
		logger.info("access refused to login: '" + userLogin
				+ "' domain: '" + obmDomainName + "' auth type: "
				+ authService.getType());
	}

	private void logLoginSuccess(String login,
			String domainName, String authServiceType, AccessToken token) {
		logger.info(login + "@" + domainName + " logged in from " + token.getOrigin()
				+ ". auth type: " + authServiceType + " (mail: " + token.getUserEmail()
				+ ") on obm-sync " + token.getVersion());
	}

	private void logLoginAttempt(String origin, String clientIP, String remoteIP,
			String lemonLogin, String lemonDomain, Login login) {
		logger.debug("Login trial for login: " + login.getFullLogin()
				+ " from client ip: " + clientIP + ", remoteIP: "
				+ remoteIP + " origin: " + origin + " lemonLogin: "
				+ lemonLogin + " lemonDomain: " + lemonDomain);
	}

	private String chooseLogin(String specifiedLogin, String lemonLogin, String lemonDomain) {
		if (lemonLogin != null && lemonDomain != null) {
			return lemonLogin + "@" + lemonDomain;
		}
		return specifiedLogin;
	}

	private void registerTokenInSession(AccessToken token) {
		sessions.put(token.getSessionId(), token);
	}

	private AccessToken buildAccessToken(String origin,	String userLogin, ObmDomain obmDomain) throws ObmSyncVersionNotFoundException {
		ObmUser databaseUser = userManagementDAO.findUserByLogin(userLogin, obmDomain);
		if (databaseUser == null) {
			logger.info("access refused to login: " + userLogin
					+ " domain:" + obmDomain.getName()
					+ "=> user not found in database");
			return null;
		}
		AccessToken token = new AccessToken(databaseUser.getUid(), origin);
		token.setDomain(obmDomain);
		token.setUserDisplayName(databaseUser.getDisplayName());
		token.setUserLogin(userLogin);
		token.setUserEmail(databaseUser.getEmail());

		token.setSessionId(newSessionId());
		token.setConversationUid(conversationUidGenerator.incrementAndGet());
		token.setVersion(getObmSyncVersion());
		//FIXME: probably broken
		token.setRootAccount(false);
		token.setServiceProperties(userManagementDAO.loadUserProperties(token));
		return token;
	}

	@VisibleForTesting
	MavenVersion getObmSyncVersion() {
		return ObmSyncVersion.current();
	}

	/**
	 * Checks the validity of the given session id
	 */
	public void checkToken(AccessToken at) throws AuthFault {
		logger.debug("checkToken sessionId:" + at.getSessionId());

		AccessToken u = sessions.get(at.getSessionId());

		if (u == null) {
			throw new AuthFault("Invalid access token");
		}
		at.setDomain(u.getDomain());
		at.setUserLogin(u.getUserLogin());
		at.setObmId(u.getObmId());
		at.setOrigin(u.getOrigin());
		at.setUserEmail(u.getUserEmail());
		at.setIsoCodeToNameCache(u.getIsoCodeToNameCache());
		at.setServiceProperties(u.getServiceProperties());
		at.setVersion(u.getVersion());
		at.setRootAccount(u.isRootAccount());
		at.setConversationUid(u.getConversationUid());

		/* restart expiration timer */
		sessions.put(u.getSessionId(), u);
	}

	public void logout(String sessionId) {
		AccessToken token = sessions.remove(sessionId);
		if (token != null) {
			logSessionRemoval();
		} else {
			logger.info("logout for " + sessionId + "...");
		}
	}

	private void logSessionRemoval() {
		logger.info("logout.");
	}
}