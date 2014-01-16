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
package org.obm.push.search.ldap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.obm.configuration.module.LoggerModule;
import org.obm.configuration.utils.IniFile;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
class Configuration {
	
	private final Logger logger;
	
	private static final String LDAP_CONF_FILE = "/etc/opush/ldap_conf.ini";
	private static final String SEARCH_LDAP_URL = "search.ldap.url";
	private static final String SEARCH_LDAP_BASE = "search.ldap.basedn";
	private static final String SEARCH_LDAP_FILTER = "search.ldap.filter";
	private static final String PROTOCOL_LDAP = "ldap";
	private static final String PROTOCOL_LDAPS = "ldaps";
	private static final String PROTOCOL_DEFAULT = PROTOCOL_LDAP;

	private final String url;
	private final String baseDn;
	private final String filter;
	private final Properties settings;
	private final boolean isValidConfiguration;

	@Inject
	@VisibleForTesting Configuration(IniFile.Factory iniFileFactory,
			@Named(LoggerModule.CONFIGURATION) Logger configurationLogger) {
		IniFile ini = iniFileFactory.build(LDAP_CONF_FILE);

		logger = configurationLogger;
		url = validUrlOrNull(ini.getData().get(SEARCH_LDAP_URL));
		baseDn = Strings.emptyToNull(ini.getData().get(SEARCH_LDAP_BASE));
		filter = Strings.emptyToNull(ini.getData().get(SEARCH_LDAP_FILTER));
		settings = buildSettings();
		isValidConfiguration = checkConfigurationValidity();
		
		logConfiguration();
	}

	private String validUrlOrNull(String url) {
		if (!Strings.isNullOrEmpty(url)) {
			try {
				return sanitizeURL(url);
			} catch (URISyntaxException e) {
				logger.error("A url is found but is invalid", e);
			}
		}
		return null;
	}

	private String sanitizeURL(String url) throws URISyntaxException {
		URI parsedUrl = new URI(url);
		String scheme = parsedUrl.getScheme();
		if (Strings.isNullOrEmpty(scheme)) {
			return String.format("%s://%s", PROTOCOL_DEFAULT, parsedUrl.toString());
		}
		else if (scheme.equals(PROTOCOL_LDAP) || scheme.equals(PROTOCOL_LDAPS)) {
			return parsedUrl.toString();
		}
		throw new URISyntaxException(url, "url format must respect pattern : ldap(s)://my_server");
	}

	private void logConfiguration() {
		logger.info("LDAP configuration done, url={} basedn={} filter={} (valid conf={})",
				url, baseDn, filter, isValidConfiguration());
		if (!isValidConfiguration()) {
			logger.error("{} configuration seems not valid, ldap connection will not be activated", LDAP_CONF_FILE);
		}
	}

	private boolean checkConfigurationValidity() {
		return !Strings.isNullOrEmpty(url)
			&& !Strings.isNullOrEmpty(baseDn)
			&& !Strings.isNullOrEmpty(filter);
	}

	private Properties buildSettings() {
		Properties settings = new Properties();
		settings.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		if (!Strings.isNullOrEmpty(url)) {
			settings.put("java.naming.provider.url", url);
		}
		return settings;
	}

	public DirContext buildContextConnection() throws NamingException {
		if (isValidConfiguration()) {
			try {
				return new InitialDirContext(settings);
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
		}
		throw new IllegalStateException("Can't build connection because settings are not valid");
	}
	
	public String getUrl() {
		return url;
	}

	public String getBaseDn() {
		return baseDn;
	}

	public String getFilter() {
		return filter;
	}

	public void cleanup(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
			}
		}
	}

	public boolean isValidConfiguration() {
		return isValidConfiguration;
	}

}
