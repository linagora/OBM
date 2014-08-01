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
package fr.aliacom.obm.ldap;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
import org.obm.sync.server.auth.IAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Authenticate on LDAP
 */
@Singleton
public class LDAPAuthService implements IAuthentificationService {

	private static Logger logger = LoggerFactory.getLogger(LDAPAuthService.class);
	private LDAPDirectory directory;
	private LDAPUtilsFactory ldapUtilsFactory;

	@Inject
	private LDAPAuthService(LDAPAuthConfig ldapAuthConfig, LDAPUtilsFactory ldapUtilsFactory) {
		this(ldapAuthConfig.getDirectory(), ldapUtilsFactory);
	}

	@VisibleForTesting
	LDAPAuthService(LDAPDirectory directory, LDAPUtilsFactory ldapUtilsFactory) {
		this.ldapUtilsFactory = ldapUtilsFactory;
		this.directory = directory;
	}

	@Override
	public boolean doAuth(Credentials credentials) throws AuthFault {
		Preconditions.checkArgument(!credentials.isPasswordHashed(), "The LDAP authentication service does not handle already hashed passwords");

		doBindAuth(credentials.getLogin().getLogin(), credentials.getLogin().getDomain(), credentials.getPassword());
		return true;
	}

	private void doBindAuth(String login, String domain, String clearTextPassword) throws AuthFault {
		LDAPUtils utils = ldapUtilsFactory.create(directory.getUri(), directory.getRootDN(), directory.getRootPW(), directory.getBaseDN());
		DirContext lookup = null;
		DirContext bind = null;

		try {
			lookup = utils.getConnection();

			SearchResult sr = utils.findResultByFilter(directory.getUserPattern().replace("%u", login).replace("%d", domain), lookup);

			if (sr == null) {
				throw new AuthenticationException("User with login '" + login + "' not found in LDAP directory.");
			}

			String dn = sr.getName() + "," + directory.getBaseDN();

			logger.info("dn lookup: " + login + "@" + domain + " => " + dn);
			bind = ldapUtilsFactory.create(directory.getUri(), dn, clearTextPassword, directory.getBaseDN()).getConnection();
		} catch (AuthenticationException e) {
			throw new AuthFault(e);
		} catch (Exception e) {
			Throwables.propagate(e);
		} finally {
			closeQuietly(lookup);
			closeQuietly(bind);
		}
	}

	@Override
	public String getType() {
		return "LDAP";
	}

	private void closeQuietly(DirContext context) {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
				logger.debug("Error closing DirContext.", e);
			}
		}
	}

}
