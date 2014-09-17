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
import java.util.Map;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

public class IniFile {

	public static class Factory {

		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public Factory() {
			super();
		}
		
		public IniFile build(String path) {
			return new IniFile(loadIniFile(new File(path)));
		}
		
		private Ini loadIniFile(File f) {
			try {
				Ini ini =  new Ini();

				ini.getConfig().setGlobalSection(true); // So that our properties files can be parsed as ini files
				ini.getConfig().setMultiOption(false);

				ini.load(f);

				return ini;
			} catch (Exception e) {
				logger.error("Unable to parse ini file '{}'", f, e);
				return new Ini();
			}
		}
	}

	private final Map<String, ? extends Map<String, String>> settings;

	private IniFile(Ini ini) {
		this(ImmutableMap.copyOf(ini));
	}

	@VisibleForTesting
	public IniFile(Map<String, ? extends Map<String, String>> settings) {
		this.settings = settings;
	}

	public String getStringValue(String prop) {
		for (Map<String, String> section : settings.values()) {
			String value = section.get(prop);

			if (value != null) {
				return value;
			}
		}

		return null;
	}

	public String getIniStringValue(String section, String prop) {
		Map<String, String> values = settings.get(section);

		return values != null ? values.get(prop) : null;
	}

	public String getStringValue(String prop, String defaultValue) {
		return Objects.firstNonNull(getStringValue(prop), defaultValue);
	}

	public String getIniStringValue(String section, String prop, String defaultValue) {
		return Objects.firstNonNull(getIniStringValue(section, prop), defaultValue);
	}

	public boolean getBooleanValue(String prop) {
		return Boolean.valueOf(getStringValue(prop)).booleanValue();
	}

	public boolean getIniBooleanValue(String section, String prop) {
		return Boolean.valueOf(getIniStringValue(section, prop)).booleanValue();
	}

	public boolean getBooleanValue(String prop, boolean defaultValue) {
		String valueString = getStringValue(prop);
		boolean value = valueString != null ? Boolean.valueOf(valueString).booleanValue()
				: defaultValue;
		return value;
	}

	public boolean getIniBooleanValue(String section, String prop, boolean defaultValue) {
		String value = getIniStringValue(section, prop);

		return value != null ? Boolean.valueOf(value).booleanValue() : defaultValue;
	}

	public Boolean getNullableBooleanValue(String prop, Boolean defaultValue) {
		String valueString = getStringValue(prop);
		if (valueString != null)  {
			return Boolean.parseBoolean(valueString);
		} else {
			return defaultValue;
		}
	}

	public Boolean getNullableIniBooleanValue(String section, String prop, Boolean defaultValue) {
		String value = getIniStringValue(section, prop);

		return value != null ? Boolean.valueOf(value) : defaultValue;
	}

	public int getIntValue(String prop, int defaultValue) {
		try {
			return Integer.parseInt(getStringValue(prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

	public int getIniIntValue(String section, String prop, int defaultValue) {
		try {
			return Integer.parseInt(getIniStringValue(section, prop));
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

	public Integer getIniIntegerValue(String section, String prop, Integer defaultValue) {
		try {
			return Integer.parseInt(getIniStringValue(section, prop));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

}
