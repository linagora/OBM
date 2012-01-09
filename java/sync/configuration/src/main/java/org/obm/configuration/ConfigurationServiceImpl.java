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
package org.obm.configuration;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.resourcebundle.Control;
import org.obm.configuration.store.StoreNotFoundException;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

public class ConfigurationServiceImpl extends AbstractConfigurationService implements ConfigurationService {

	private final Charset DEFAULT_ENCODING = Charsets.UTF_8;

	private final static String LOCATOR_PORT = "8082";
	private final static String LOCATOR_APP_NAME = "obm-locator";

	private final static String OBM_SYNC_PORT = "8080";
	private final static String OBM_SYNC_APP_NAME = "obm-sync/services";
	
	private final ImmutableMap<String, TimeUnit> timeUnits;
	
	public ConfigurationServiceImpl() {
		super("/etc/obm/obm_conf.ini");
		timeUnits = ImmutableMap.of("milliseconds", TimeUnit.MILLISECONDS,
								"seconds", TimeUnit.SECONDS,
								"minutes", TimeUnit.MINUTES,
								"hours", TimeUnit.HOURS);
	}

	@Override
	public String getLocatorUrl() throws ConfigurationException {
		String locatorHost = getStringValue("host");
		if (locatorHost == null) {
			throw new ConfigurationException(
					"Missing host key in configuration");
		}
		return "http://" + locatorHost + ":" + LOCATOR_PORT + "/" + LOCATOR_APP_NAME + "/";
	}

	@Override
	public String getObmUIBaseUrl() {
		String protocol = getStringValue("external-protocol");
		String hostname = getStringValue("external-url");
		String path = getStringValue("obm-prefix");
		return protocol + "://" + hostname + path;
	}

	@Override
	public InputStream getStoreConfiguration() throws StoreNotFoundException {
		throw new StoreNotFoundException("Store not found for " + getClass() + " configuration.");
	}
	
	@Override
	public String getObmSyncUrl(String obmSyncHost) {
		return "http://" + obmSyncHost + ":" + OBM_SYNC_PORT + "/" + OBM_SYNC_APP_NAME;
	}
	
	@Override
	public int getLocatorCacheTimeout() {
		return getIntValue("locator-cache-timeout", 30);
	}
	
	@Override
	public TimeUnit getLocatorCacheTimeUnit() {
		String key = getStringValue("locator-cache-timeunit");
		return getTimeUnitOrDefault(key, TimeUnit.MINUTES);
	}
	
	private TimeUnit getTimeUnitOrDefault(String key, TimeUnit defaultUnit) {
		if (key != null) {
			TimeUnit unit = timeUnits.get(key.toLowerCase());
			if (unit != null) {
				return unit;
			}
		}
		return defaultUnit;
	}
	
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("Messages", locale, new Control());
	}

	@Override
	public Charset getDefaultEncoding() {
		return DEFAULT_ENCODING;
	}

	
}
