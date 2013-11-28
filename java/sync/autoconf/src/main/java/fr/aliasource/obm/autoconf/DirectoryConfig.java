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
package fr.aliasource.obm.autoconf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.aliasource.obm.utils.ConstantService;

public class DirectoryConfig {
	private static final Logger logger = LoggerFactory.getLogger(DirectoryConfig.class);

	private final Integer ldapPort;
	private final String ldapSearchBase;
	private final String[] ldapAtts;
	private final String ldapFilter;
	private final String searchWithDomain ;
	private final String ldapHost;
	private String configXml;

	public DirectoryConfig(String login, String domain ,ConstantService cs) {
		searchWithDomain = cs.getStringValue("searchWithDomain");
		ldapHost = cs.getStringValue("ldapHost");
		ldapPort = cs.getIntValue("ldapPort");
		ldapSearchBase = cs.getStringValue("ldapSearchBase");
		ldapAtts = cs.getStringValue("ldapAtts").split(",");
		if ( "true".equals(searchWithDomain)) {
			logger.info("DirectoryConfig : search with domain'");
			login = login + "@" + domain ;
		}
		ldapFilter = "(" + cs.getStringValue("ldapFilter") + "=" + login + ")";
		configXml = "/usr/share/obm-autoconf/config.xml";
	}

	public int getLdapPort() {
		return ldapPort;
	}

	public String getLdapSearchBase() {
		return ldapSearchBase;
	}

	public String[] getLdapAtts() {
		return ldapAtts;
	}

	public String getLdapFilter() {
		return ldapFilter;
	}

	public String getLdapHost() {
		return ldapHost;
	}

	public String getConfigXml() {
		return configXml;
	}

	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}

}
