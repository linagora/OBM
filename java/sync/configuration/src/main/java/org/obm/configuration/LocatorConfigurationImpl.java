/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.configuration;

import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.TimeUnitMapper;

import com.google.common.annotations.VisibleForTesting;


public class LocatorConfigurationImpl implements LocatorConfiguration {

	@VisibleForTesting static final String LOCATOR_CACHE_TIMEUNIT_KEY = "locator-cache-timeunit";
	@VisibleForTesting static final String LOCATOR_CACHE_TIMEOUT_KEY = "locator-cache-timeout";
	@VisibleForTesting static final int LOCATOR_CACHE_TIMEOUT_DEFAULT = 30;
	@VisibleForTesting static final String LOCATOR_CLIENT_TIMEOUT_KEY = "locator-client-timeout-seconds";
	@VisibleForTesting static final int LOCATOR_CLIENT_TIMEOUT_DEFAULT = 5;

	@VisibleForTesting final static String ASCMD = "Microsoft-Server-ActiveSync";

	@VisibleForTesting final static String EXTERNAL_URL_KEY = "external-url";

	@VisibleForTesting final static String LOCATOR_HOST_KEY = "host";
	@VisibleForTesting final static int LOCATOR_PORT = 8084;
	private final static String LOCATOR_APP_NAME = "obm-locator";

	public static class Factory {
		
		protected IniFile.Factory iniFileFactory;

		public Factory() {
			iniFileFactory = new IniFile.Factory();
		}
		
		public LocatorConfigurationImpl create(String globalConfigurationFile) {
			return new LocatorConfigurationImpl(iniFileFactory.build(globalConfigurationFile));
		}
	}
	
	protected final TimeUnitMapper timeUnitMapper;
	protected final IniFile iniFile;

	@VisibleForTesting
	protected LocatorConfigurationImpl(IniFile globalConfigurationIniFile) {
		this.iniFile = globalConfigurationIniFile;
		this.timeUnitMapper = new TimeUnitMapper();
	}

	@Override
	public String getLocatorUrl() throws ConfigurationException {
		String locatorHost = iniFile.getStringValue(LOCATOR_HOST_KEY);
		if (locatorHost == null) {
			throw new ConfigurationException(
					"Missing host key in configuration");
		}
		return "http://" + locatorHost + ":" + LOCATOR_PORT + "/" + LOCATOR_APP_NAME + "/";
	}
	
	@Override
	public int getLocatorPort() {
		return LOCATOR_PORT;
	}

	@Override
	public int getLocatorClientTimeoutInSeconds() {
		return iniFile.getIntValue(LOCATOR_CLIENT_TIMEOUT_KEY, LOCATOR_CLIENT_TIMEOUT_DEFAULT);
	}
	
	@Override
	public int getLocatorCacheTimeout() {
		return iniFile.getIntValue(LOCATOR_CACHE_TIMEOUT_KEY, LOCATOR_CACHE_TIMEOUT_DEFAULT);
	}

	@Override
	public TimeUnit getLocatorCacheTimeUnit() {
		String key = iniFile.getStringValue(LOCATOR_CACHE_TIMEUNIT_KEY);
		return timeUnitMapper.getTimeUnitOrDefault(key, TimeUnit.MINUTES);
	}
}
