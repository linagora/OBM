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

package org.obm.push.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapUtils {

	private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final DirContext ctx;
	private final String baseDn;

	public LdapUtils(DirContext ctx, String baseDn) {
		this.ctx = ctx;
		this.baseDn = baseDn;
	}

	/**
	 * Search ldap attributes using the given filter/query
	 * 
	 * @param filter
	 *            ldap filter where %q is replaced by query
	 * @param query
	 *            replaces %q in filter
	 * @param attributes
	 *            the searched attributes (only first value is returned)
	 * @return
	 * @throws NamingException
	 */
	public List<Map<String, List<String>>> getAttributes(String filter, String query, String[] attributes) throws NamingException {
		return getAttributes(filter, query, DEFAULT_LIMIT, attributes);
	}

	/**
	 * Search ldap attributes using the given filter/query
	 * 
	 * @param filter
	 *            ldap filter where %q is replaced by query
	 * @param query
	 *            replaces %q in filter
	 * @param limit
	 *            max entry count to read from the server 
	 * @param attributes
	 *            the searched attributes (only first value is returned)
	 * @return
	 * @throws NamingException
	 */
	public List<Map<String, List<String>>> getAttributes(String filter, String query,
			int limit, String[] attributes) throws NamingException {
		
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String attrList[] = attributes;
		constraints.setReturningAttributes(attrList);
		NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter.replace("%q", query).replace("**", "*"), constraints);
		List<Map<String, List<String>>> matched = new LinkedList<Map<String, List<String>>>();
		NamingEnumeration<? extends Attribute> ae = null;
		try {
			for (int resultIndex = 0; resultIndex < limit && results.hasMore(); resultIndex++) {
				SearchResult si = results.next();
				Attributes attrs = si.getAttributes();
				if (attrs == null) {
					continue;
				}
				ae = attrs.getAll();
				Map<String, List<String>> ret = new HashMap<String, List<String>>();
				while (ae.hasMoreElements()) {
					Attribute attr = ae.next();
					String id = attr.getID();
					List<String> vals = null;
					if (!ret.containsKey(id)) {
						vals = new LinkedList<String>();
						ret.put(id, vals);
					} else {
						vals = ret.get(id);
					}
					for (int i = 0; i < attr.size(); i++) {
						vals.add((String) attr.get(i));
					}
				}
				matched.add(ret);
			}
		} catch (SizeLimitExceededException e) {
			logger.info("No more entry can be read: {}", e.getMessage());
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (NamingException e) {
					logger.error("Cannot close results NamingEnumeration", e);
				}
			}
			if (ae != null) {
				try {
					ae.close();
				} catch (NamingException e) {
					logger.error("Cannot close attributes NamingEnumeration", e);
				}
			}
		}
		return matched;
	}
}
