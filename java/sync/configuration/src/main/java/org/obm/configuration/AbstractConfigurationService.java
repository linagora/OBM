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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public abstract class AbstractConfigurationService {

	protected static final String GLOBAL_CONFIGURATION_FILE = "/etc/obm/obm_conf.ini";

	protected Properties props;

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected AbstractConfigurationService() {}

	protected AbstractConfigurationService(String filename) {
		props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(filename);
			props.load(in);
		} catch (IOException e) {
			logger.error(filename + " not found", e);
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
	
	protected String getStringValue(String prop) {
		return props.getProperty(prop);
	}
	
	protected String getStringValue(String prop, String defaultValue) {
		return Objects.firstNonNull(getStringValue(prop), defaultValue);
	}

	protected boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	protected boolean getBooleanValue(String prop, boolean defaultValue) {
		String valueString = getStringValue(prop);
		boolean value = valueString != null ? Boolean.valueOf(valueString).booleanValue()
				: defaultValue;
		return value;
	}

	protected int getIntValue(String prop, int defaultValue) {
		try {
			return Integer.parseInt(getStringValue(prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
}
