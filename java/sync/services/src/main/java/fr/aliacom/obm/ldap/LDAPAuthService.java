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
package fr.aliacom.obm.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.obm.sync.auth.Credentials;
import org.obm.sync.server.auth.IAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.contact.UserDao;

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

	@Override
	public String getObmDomain(String login) {
		String ret = directory.getObmDomain();
		if (ret == null) {
			ret = userDao.getUserDomain(login);
		}
		return ret;
	}

	@Override
	public boolean doAuth(Credentials credentials) {
		Preconditions.checkArgument(!credentials.isPasswordHashed(), "The LDAP authentication service does not handle already hashed passwords");
		return doBindAuth(credentials.getLogin().getLogin(), credentials.getLogin().getDomain(), credentials.getPassword());
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

	@Override
	public String getType() {
		return "LDAP";
	}

}
