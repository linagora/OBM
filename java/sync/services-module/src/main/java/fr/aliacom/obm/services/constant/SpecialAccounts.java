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
	private SpecialAccounts(ObmSyncConfigurationService configuration) {
		try {
			rootAccounts = new HashMap<String, String>();
			anyUserAccounts = new ArrayList<String>();
			appliAccounts = new HashMap<String, String>();

			String roots = configuration.getRootAccounts();
			String applis = configuration.getAppliAccounts();
			String anyUsers = configuration.getAnyUserAccounts();

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
