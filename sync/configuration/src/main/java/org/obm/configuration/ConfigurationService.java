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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.obm.configuration.store.StoreNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration service
 */
public class ConfigurationService {

	private final static String LOCATOR_PORT = "8082";
	private final static String LOCATOR_APP_NAME = "obm-locator";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Properties props;

	public ConfigurationService() {
		props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream("/etc/obm/obm_conf.ini");
			props.load(in);
		} catch (IOException e) {
			logger.error("/etc/obm/obm_conf.ini not found", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("error closing ini file inputstream", e);
				}
			}
		}
	}

	public String getStringValue(String prop) {
		return props.getProperty(prop);
	}

	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	public int getIntValue(String prop) {
		return Integer.parseInt(getStringValue(prop));
	}

	public String getLocatorUrl() throws ConfigurationException {
		String locatorHost = getStringValue("host");
		if (locatorHost == null) {
			throw new ConfigurationException(
					"Missing host key in configuration");
		}
		return "http://" + locatorHost + ":" + LOCATOR_PORT + "/"
				+ LOCATOR_APP_NAME + "/";
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

}
