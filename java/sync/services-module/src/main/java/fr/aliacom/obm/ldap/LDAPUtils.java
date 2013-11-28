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

import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPUtils {

	private final Logger logger =  LoggerFactory.getLogger(LDAPUtils.class);

	private final String baseDn;

	private final Hashtable<String, String> env;

	public LDAPUtils(String uri, String rootDn, String rootPw, String baseDn) {
		this.baseDn = baseDn;

		env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial",
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", uri);
		env.put(DirContext.SECURITY_AUTHENTICATION, "simple");
		if(rootDn != null && rootPw != null){
			env.put(DirContext.SECURITY_PRINCIPAL, rootDn);
			env.put(DirContext.SECURITY_CREDENTIALS, rootPw);
		}

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
			logger.error("Cannot match encrypted password", nsae);
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
			logger.error(ne.getMessage(), ne);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					logger.error(e.getMessage(), e);
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
		NamingEnumeration<SearchResult> results = null;
		try {
			results = ctx.search(baseDn, filter, controls);
			if (results.hasMoreElements()) {
				result = results.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug("Entry with " + filter
							+ " found in directory.");
				}
			}
		} catch (NamingException ne) {
			logger.error("no entry found in directory", ne);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (NamingException e) {
					logger.error("Cannot close NamingEnumeration", e);
				}
			}
		}
		return result;
	}
}
