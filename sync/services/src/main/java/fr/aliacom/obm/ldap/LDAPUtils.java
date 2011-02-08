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

import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LDAPUtils {

	private Log logger = LogFactory.getLog(LDAPUtils.class);

	private String baseDn;

	private Hashtable<String, String> env;

	public LDAPUtils(String uri, String rootDn, String rootPw, String baseDn) {
		this.baseDn = baseDn;

		env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial",
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", uri);
		env.put(DirContext.SECURITY_AUTHENTICATION, "simple");
		env.put(DirContext.SECURITY_PRINCIPAL, rootDn);
		env.put(DirContext.SECURITY_CREDENTIALS, rootPw);

		if (logger.isDebugEnabled()) {
			logger.debug("binddn: " + rootDn + " bindpw: " + rootPw);
		}
	}

	/**
	 * Finds an ldap entry where matchField=matchValue, crypts the clear text
	 * userPassword, then check equality
	 */
	public boolean checkPassword(String matchField, String matchValue,
			String userPassword) {
		try {
			Attributes userEntry = getUserEntry(matchField, matchValue);
			if (userEntry == null) {
				logger.warn("Entry with '" + matchField + "=" + matchValue
						+ "' not found in directory");
				return false;
			}
			return isMatchingPassword(userPassword, (byte[]) userEntry.get("userPassword").get());
		} catch (NamingException e) {
			logger.warn("Error checking password", e);
			return false;
		}
	}

	private boolean isMatchingPassword(String userPassword,
			byte[] hashedPassword) {
		boolean ret = false;
		try {
			ret = new PasswordHandler().verify(new String(hashedPassword),
					userPassword);
		} catch (NoSuchAlgorithmException nsae) {
			logger.fatal("Cannot match encrypted password", nsae);
		}
		return ret;
	}

	public DirContext getConnection() throws NamingException {
		return new InitialDirContext(env);
	}

	public Attributes getUserEntry(String matchField, String matchValue) {
		DirContext ctx = null;
		Attributes ldapEntry = null;
		try {
			ctx = getConnection();
			ldapEntry = findOneAttributeSetBy(matchField, matchValue, ctx);
		} catch (NamingException ne) {
			logger.error(ne, ne);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					logger.error(e, e);
				}
			}
		}
		return ldapEntry;
	}

	private Attributes findOneAttributeSetBy(String field, String value,
			DirContext ctx) {
		return findResultByFilter(field + "=" + value, ctx).getAttributes();
	}

	public SearchResult findResultByFilter(String filter, DirContext ctx) {
		SearchResult result = null;
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			NamingEnumeration<SearchResult> results = ctx.search(baseDn,
					filter, controls);
			if (results.hasMoreElements()) {
				result = results.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug("Entry with " + filter
							+ " found in directory.");
				}
			}
		} catch (NamingException ne) {
			logger.fatal("no entry found in directory", ne);
		}
		return result;
	}
}
