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

package org.obm.configuration.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class IniFile {

	public static class Factory {

		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public Factory() {
			super();
		}
		
		public IniFile build(String path) {
			return this.build(path, null);
		}
		
		public IniFile build(String path, String category) {
			return new IniFile(loadFileContent(path), category);
		}
		
		private Map<String, String> loadFileContent(String path) {
			Map<String, String> settings = Maps.newHashMap();
			File f = new File(path);
			if (f.exists()) {
				loadIniFile(f, settings);
			} else {
				logger.warn(path+ " does not exist.");
			}
			return ImmutableMap.copyOf(settings);
		}
		
		private void loadIniFile(File f, Map<String, String> settings) {
			FileInputStream in = null;
			try {
				Properties p = new Properties();
				in = new FileInputStream(f);
				p.load(in);
				for (Object key : p.keySet()) {
					settings.put((String) key, p.getProperty((String) key));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	private final Map<String, String> settings;
	private final String category;
	
	@VisibleForTesting IniFile(Map<String, String> settings, String category) {
		this.settings = settings;
		this.category = category;
	}

	protected String getSetting(String settingName) {
		return settings.get(settingName);
	}
	
	public Map<String, String> getData() {
		return ImmutableMap.copyOf(settings);
	}

	public String getCategory() {
		return category;
	}

	public String getStringValue(String prop) {
		return getSetting(prop);
	}
	
	public String getStringValue(String prop, String defaultValue) {
		return Objects.firstNonNull(getStringValue(prop), defaultValue);
	}

	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	public boolean getBooleanValue(String prop, boolean defaultValue) {
		String valueString = getStringValue(prop);
		boolean value = valueString != null ? Boolean.valueOf(valueString).booleanValue()
				: defaultValue;
		return value;
	}

	public Boolean getNullableBooleanValue(String prop, Boolean defaultValue) {
		String valueString = getStringValue(prop);
		if (valueString != null)  {
			return Boolean.parseBoolean(valueString);
		} else {
			return defaultValue;
		}
	}
	
	public int getIntValue(String prop, int defaultValue) {
		try {
			return Integer.parseInt(getStringValue(prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

	public Integer getIntegerValue(String prop, Integer defaultValue) {
		try {
			return Integer.parseInt(getStringValue(prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
}
