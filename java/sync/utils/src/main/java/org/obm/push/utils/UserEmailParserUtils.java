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

import java.util.Iterator;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;

@Singleton
public class UserEmailParserUtils {

	private static final int LOGIN = 0;
	private static final int DOMAIN = 1;

	public String getLogin(String userId) {
		String[] loginAndDomain = getLoginAndDomain(userId);
		return loginAndDomain[LOGIN];
	}
	
	public String getDomain(String userId) {
		String[] loginAndDomain = getLoginAndDomain(userId);
		return loginAndDomain[DOMAIN];
	}
	
	public boolean isAddress(String email) {
		try {
			String[] loginAndDomain = getLoginAndDomain(email);
			return !Strings.isNullOrEmpty(loginAndDomain[LOGIN])
				&& !Strings.isNullOrEmpty(loginAndDomain[DOMAIN]);
		} catch (Exception e) {
			return false;
		}
	}
	
	private String[] getLoginAndDomain(String userId) {
		Iterable<String> parts = splitOnSlashes(userId);
		String[] loginAndDomain = buildUserFromLoginParts(parts);
		if (loginAndDomain == null) {
			parts = splitOnAtSign(userId);
			loginAndDomain = buildUserFromLoginParts(parts);
		}
		if (loginAndDomain == null) {
			throw new IllegalArgumentException();
		}
		return loginAndDomain;
	}
	
	private Iterable<String> splitOnSlashes(String userId) {
		Iterable<String> parts = Splitter.on("\\").omitEmptyStrings().split(userId);
		return parts;
	}

	private Iterable<String> splitOnAtSign(String userId) {
		Iterable<String> parts = Splitter.on("@").split(trimBeginSlash(userId));
		return ImmutableList.copyOf(parts).reverse();
	}

	private CharSequence trimBeginSlash(String userId) {
		if (userId.startsWith("\\")) {
			return userId.substring(1);
		} else {
			return userId;
		}
	}

	private String[] buildUserFromLoginParts(Iterable<String> parts) {
		int nbParts = Iterables.size(parts);
		if (nbParts > 2) {
			throw new IllegalArgumentException();
		} else if (nbParts == 2) {
			Iterator<String> iterator = parts.iterator();
			String domain = iterator.next();
			String login = iterator.next();
			checkField("domain", domain);
			checkField("login", login);
			return new String[]{login, domain};
		}
		return null;
	}
	
	private void checkField(String key, String field) {
		if (field.contains("@") || field.contains("\\")) {
			throw new IllegalArgumentException(key + " is invalid : " + field);
		}
	}
	
}
