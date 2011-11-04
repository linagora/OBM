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
package fr.aliacom.obm.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.obm.sync.server.auth.IAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.contact.UserDao;
import fr.aliacom.obm.common.domain.ObmDomain;

/**
 * Authenticate on LDAP
 */
@Singleton
public class LDAPAuthService implements IAuthentificationService {

	private static Logger logger =  LoggerFactory.getLogger(LDAPAuthService.class);
	private LDAPAuthConfig authConfig;
	private LDAPDirectory directory;
	private final UserDao userDao;

	@Inject
	private LDAPAuthService(LDAPAuthConfig ldapAuthConfig, UserDao userDao) {
		this.authConfig = ldapAuthConfig;
		this.userDao = userDao;
		this.directory = authConfig.getDirectory();
	}

	public String getObmDomain(String login) {
		String ret = directory.getObmDomain();
		if (ret == null) {
			ret = userDao.getUserDomain(login);
		}
		return ret;
	}

	public boolean doAuth(String userLogin, ObmDomain obmDomain,
			String clearTextPassword) {
		return doBindAuth(userLogin, obmDomain.getName(), clearTextPassword);
	}

	private boolean doBindAuth(String login, String domain,
			String clearTextPassword) {

		LDAPUtils utils = new LDAPUtils(directory.getUri(), directory
				.getRootDN(), directory.getRootPW(), directory.getBaseDN());
		boolean ret = false;
		DirContext lookup = null;
		DirContext bind = null;
		String dn = null;
		try {
			lookup = utils.getConnection();
			SearchResult sr = utils.findResultByFilter(directory
					.getUserPattern().replace("%u", login)
					.replace("%d", domain), lookup);
			if (sr != null) {
				dn = sr.getName() + "," + directory.getBaseDN();
				logger.info("dn lookup: " + login + "@" + domain + " => " + dn);
			}

		} catch (Exception e) {
			logger.error(e.getMessage() + " (dn: " + dn + ")", e);
		} finally {
			if (lookup != null) {
				try {
					lookup.close();
				} catch (NamingException e) {
					logger.debug("error closing lookup DirContext", e);
				}
			}
		}
		if (dn != null) {
			try {
				Hashtable<String, String> bindenv = new Hashtable<String, String>();
				bindenv.put(Context.INITIAL_CONTEXT_FACTORY,
						"com.sun.jndi.ldap.LdapCtxFactory");
				bindenv.put(Context.PROVIDER_URL, directory.getUri());
				bindenv.put(Context.SECURITY_AUTHENTICATION, "simple");
				bindenv.put(Context.SECURITY_PRINCIPAL, dn);
				bindenv.put(Context.SECURITY_CREDENTIALS, clearTextPassword);
				bind = new InitialDirContext(bindenv);
				ret = true;
			} catch (Exception e) {
				logger.error(e.getMessage() + " with dn: " + dn + ")", e);
			} finally {
				if (bind != null) {
					try {
						bind.close();
					} catch (NamingException e) {
						logger.debug("error closing bind DirContext", e);
					}
				}
			}
		}
		return ret;
	}

	public String getType() {
		return "LDAP";
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

}
