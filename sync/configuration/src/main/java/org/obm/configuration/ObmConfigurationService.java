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

import javax.naming.ConfigurationException;

import org.obm.configuration.store.StoreNotFoundException;

public class ObmConfigurationService extends AbstractConfigurationService{

	private final static String LOCATOR_PORT = "8082";
	private final static String LOCATOR_APP_NAME = "obm-locator";

	private final static String OBM_SYNC_PORT = "8080";
	private final static String OBM_SYNC_APP_NAME = "obm-sync/services";
	
	public ObmConfigurationService() {
		super("/etc/obm/obm_conf.ini");
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

}
