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
package fr.aliasource.obm.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour des constantes basé sur le fichier
 * <code>/etc/obm/obm_conf.ini</code>.
 * 
 * 
 */
public class ConstantService {
	private static final Logger logger = LoggerFactory.getLogger(ConstantService.class);

	private static ConstantService cs = new ConstantService();

	public static ConstantService getInstance() {
		return cs;
	}

	private Properties props;

	private ConstantService() {
		props = new Properties();
		try {
			props.load(new FileInputStream("/etc/obm/obm_conf.ini"));
		} catch (IOException e) {
			logger.error("Problem while trying to read obm_conf.ini", e);
		}
	}

	public Set<Object> getKeySet() {
		return props.keySet();
	}

	public String getStringValue(String prop) {
		String val = props.getProperty(prop);
		if (val != null && val.startsWith("\"") && val.endsWith("\"")) {
			val = val.replace("\"", "");
		}
		return val;
	}

	public Boolean getBooleanValue(String prop) {
		String val = getStringValue(prop);
		if (val == null) {
			logger.warn("No value for property "+prop);
			return null;
		}
		return Boolean.valueOf(val);
	}

	public Integer getIntValue(String prop) {
		String val = getStringValue(prop);
		if (val == null) {
			logger.warn("No value for property "+prop);
			return null;
		}
		return Integer.parseInt(val);
	}

}
