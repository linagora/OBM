/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.server.auth.AuthentificationServiceFactory;
import org.obm.sync.server.auth.IAuthentificationService;

import com.google.common.base.Splitter;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.ObmSyncVersion;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.services.constant.ConstantService;
import fr.aliacom.obm.services.constant.SpecialAccounts;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.LogUtils;

/**
 * Helpers for working with userobm & domain tables Authentification method is
 * also here
 */
@Singleton
public class SessionManagement {

	private ConcurrentMap<String, AccessToken> sessions;

	private static final Log logger = LogFactory.getLog(SessionManagement.class);
	
	private final AtomicInteger conversationUidGenerator;
	private final AuthentificationServiceFactory authentificationServiceFactory;
	private final UserDao userManagementDAO;
	private final DomainService domainService;
	private final ConstantService constantService;
	private final SpecialAccounts specialAccounts;
	private final Helper helper;
	
	@Inject
	private SessionManagement(AuthentificationServiceFactory authentificationServiceFactory, 
			DomainService domainService, UserDao userDao, ConstantService constantService, 
			SpecialAccounts specialAccounts, Helper helper) {
		
		this.userManagementDAO = userDao;
		this.constantService = constantService;
		this.specialAccounts = specialAccounts;
		this.helper = helper;
		this.conversationUidGenerator = new AtomicInteger();
		this.sessions = configureSessionCache();
		this.domainService = domainService;
		this.authentificationServiceFactory = authentificationServiceFactory;
	}

	private ConcurrentMap<String, AccessToken> configureSessionCache() {
		return new MapMaker().
			expiration(20, TimeUnit.MINUTES).
			concurrencyLevel(16).
			evictionListener(new MapEvictionListener<String, AccessToken>() {
				@Override
				public void onEviction(String sessionId, AccessToken token) {
					logSessionRemoval(token);
				}
			}).	makeMap();
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
		String lemonIPs = constantService.getStringValue("lemonLdapIps");
		for (String lemonIp: Splitter.on(',').trimResults().split(lemonIPs)) {
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
	 * @return null if the credential are not valid
	 */
	public AccessToken login(String specifiedLogin, String password, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain) {
		String login = chooseLogin(specifiedLogin, lemonLogin, lemonDomain);
		logger.debug("Login trial for login: " + login
				+ " from client ip: " + clientIP + ", remoteIP: "
				+ remoteIP + " origin: " + origin + " lemonLogin: "
				+ lemonLogin + " lemonDomain: " + lemonDomain);

		String[] splited = login.split("@", 2);
		String userLogin = splited[0];
		String domainName = null;

		if (splited.length == 2) {
			domainName = splited[1];
		}

		IAuthentificationService authService = authentificationServiceFactory.get();

		if (domainName == null) {
			domainName = authService.getObmDomain(userLogin);
		}
		ObmDomain obmDomain = domainService.findDomainByName(domainName);
		if (obmDomain == null) {
			logger.warn("cannot figure out domain for the domain_name "	+ domainName);
			return null;
		}

		boolean valid = false;
		if (lemonLogin != null && lemonDomain != null) {
			valid = doAuthLemonLdap(remoteIP);
		} else {
			valid = doAuthSpecialAccount(userLogin, obmDomain, clientIP);
			if (!valid) {
				valid = authService.doAuth(userLogin, obmDomain, password);
			}
		}
		if (valid) {
			AccessToken token = buildAccessToken(origin, userLogin, obmDomain);
			registerTokenInSession(token);
			logger.info(LogUtils.prefix(token) + login + " logged in from " + token.getOrigin()
					+ ". auth type: " + authService.getType() + " (mail: " + token.getEmail()
					+ ") on obm-sync " + token.getVersion());
			return token;
		}
		logger.info("access refused to login: '" + userLogin
				+ "' domain: '" + obmDomain.getName() + "' auth type: "
				+ authService.getType());
		return null;
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

	private AccessToken buildAccessToken(String origin,	String userLogin, ObmDomain obmDomain) {
		ObmUser databaseUser = userManagementDAO.findUserByLogin(userLogin, obmDomain);
		if (databaseUser == null) {
			logger.info("access refused to login: " + userLogin
					+ " domain:" + obmDomain.getName()
					+ "=> user not found in database");
			return null;
		}
		AccessToken token = new AccessToken(databaseUser.getUid(), obmDomain.getId(), origin);
		token.setUser(userLogin);
		token.setDomain(obmDomain.getName());
		token.setSessionId(newSessionId());
		token.setEmail(helper.constructEmailFromList(databaseUser.getEmail(), obmDomain.getName()));
		token.setConversationUid(conversationUidGenerator.incrementAndGet());
		token.setVersion(ObmSyncVersion.current());
		//FIXME: probably broken
		token.setRootAccount(false);
		token.setServiceProperties(userManagementDAO.loadUserProperties(token));
		return token;
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
		at.setUser(u.getUser());
		at.setDomainId(u.getDomainId());
		at.setObmId(u.getObmId());
		at.setOrigin(u.getOrigin());
		at.setEmail(u.getEmail());
		at.setIsoCodeToNameCache(u.getIsoCodeToNameCache());
		at.setServiceProperties(u.getServiceProperties());
		at.setCalendarRights(u.getCalendarRights());
		at.setVersion(u.getVersion());
		at.setRootAccount(u.isRootAccount());
		at.setConversationUid(u.getConversationUid());
		
		/* restart expiration timer */
		sessions.put(u.getSessionId(), u);
	}

	public void logout(String sessionId) {
		AccessToken token = sessions.remove(sessionId);
		if (token != null) {
			logSessionRemoval(token);
		} else {
			logger.info("logout for " + sessionId + "...");
		}
	}

	private void logSessionRemoval(AccessToken token) {
		logger.info(LogUtils.prefix(token) + "logout.");
	}
	
}