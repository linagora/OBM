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
package org.obm.configuration;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.resourcebundle.Control;
import org.obm.configuration.store.StoreNotFoundException;

import com.google.common.collect.ImmutableMap;

public class ObmConfigurationService extends AbstractConfigurationService {

	private final static String LOCATOR_PORT = "8082";
	private final static String LOCATOR_APP_NAME = "obm-locator";

	private final static String OBM_SYNC_PORT = "8080";
	private final static String OBM_SYNC_APP_NAME = "obm-sync/services";
	
	private final ImmutableMap<String, TimeUnit> timeUnits;
	
	public ObmConfigurationService() {
		super("/etc/obm/obm_conf.ini");
		timeUnits = ImmutableMap.of("milliseconds", TimeUnit.MILLISECONDS,
								"seconds", TimeUnit.SECONDS,
								"minutes", TimeUnit.MINUTES,
								"hours", TimeUnit.HOURS);
	}

	public String getLocatorUrl() throws ConfigurationException {
		String locatorHost = getStringValue("host");
		if (locatorHost == null) {
			throw new ConfigurationException(
					"Missing host key in configuration");
		}
		return "http://" + locatorHost + ":" + LOCATOR_PORT + "/" + LOCATOR_APP_NAME + "/";
	}

	public String getObmUIBaseUrl() {
		String protocol = getStringValue("external-protocol");
		String hostname = getStringValue("external-url");
		String path = getStringValue("obm-prefix");
		return protocol + "://" + hostname + path;
	}

	public InputStream getStoreConfiguration() throws StoreNotFoundException {
		throw new StoreNotFoundException("Store not found for " + getClass() + " configuration.");
	}
	
	public String getObmSyncUrl(String obmSyncHost) {
		return "http://" + obmSyncHost + ":" + OBM_SYNC_PORT + "/" + OBM_SYNC_APP_NAME;
	}
	
	public int getLocatorCacheTimeout() {
		return getIntValue("locator-cache-timeout", 30);
	}
	
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
	
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("Messages", locale, new Control());
	}

}
