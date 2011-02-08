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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Configuration service
 */
@Singleton
public class ConstantService {

	private static final Log logger = LogFactory.getLog(ConstantService.class);
	private static final String DEFAULT_TEMPLATE_FOLDER = "/usr/share/obm-sync/resources";
	private static final String OVERRIDE_TEMPLATE_FOLDER = "/etc/obm-sync/resources/template/";
	private static final String OBM_SYNC_MAILER = "x-obm-sync";

	private Properties props;

	@Inject
	private ConstantService() {
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
		return "https://" + locatorHost + ":8084/";
	}

	public String getObmUIBaseUrl() {
		String protocol = getStringValue("external-protocol");
		String hostname = getStringValue("external-url");
		String path = getStringValue("obm-prefix");
		return protocol + "://" + hostname + path;
	}

	public String getDefaultTemplateFolder() {
		return DEFAULT_TEMPLATE_FOLDER;
	}

	public String getOverrideTemplateFolder() {
		return OVERRIDE_TEMPLATE_FOLDER;
	}

	public String getObmSyncMailer(AccessToken at) {
		return OBM_SYNC_MAILER + "@" + at.getDomain();
	}
}
