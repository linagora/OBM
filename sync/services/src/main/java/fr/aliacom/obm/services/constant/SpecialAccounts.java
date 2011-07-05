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
package fr.aliacom.obm.services.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SpecialAccounts {

	private static final Logger logger = LoggerFactory
			.getLogger(SpecialAccounts.class);

	private Map<String, String> rootAccounts;
	private ArrayList<String> anyUserAccounts;
	private Map<String, String> appliAccounts;

	@Inject
	private SpecialAccounts(ConstantService configuration) {
		try {
			rootAccounts = new HashMap<String, String>();
			anyUserAccounts = new ArrayList<String>();
			appliAccounts = new HashMap<String, String>();

			String roots = configuration.getStringValue("rootAccounts");
			String applis = configuration.getStringValue("appliAccounts");
			String anyUsers = configuration.getStringValue("anyUserAccounts");

			if (roots != null && !roots.equals("")) {
				for (String account : roots.split(",")) {
					account = account.replace("|", "#");
					rootAccounts.put(account.split("#")[0],
							account.split("#")[1]);
				}
			}
			if (applis != null && !applis.equals("")) {
				for (String account : applis.split(",")) {
					account = account.replace("|", "#");
					appliAccounts.put(account.split("#")[0],
							account.split("#")[1]);
				}
			}
			if (anyUsers != null && !anyUsers.equals("")) {
				for (String account : anyUsers.split(",")) {
					anyUserAccounts.add(account);
				}
			}

		} catch (Exception e) {
			logger.error("invalid parameters in conf file", e);
		}
	}

	public boolean isRootAccount(String login, String IP) {
		String accountIP = rootAccounts.get(login);
		return accountIP != null && accountIP.equals(IP);
	}

	public boolean isApplicAccount(String login, String IP) {
		String accountIP = appliAccounts.get(login);
		return accountIP != null && accountIP.equals(IP);
	}

	public boolean isAnyUserAccount(String IP) {
		return anyUserAccounts.contains(IP);
	}

}
