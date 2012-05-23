package org.obm.push.utils;

import java.util.Iterator;

import com.google.common.base.Splitter;
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
