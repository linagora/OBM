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

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.resourcebundle.Control;
import org.obm.push.utils.IniFile;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ConfigurationServiceImpl extends AbstractConfigurationService
        implements ConfigurationService {

    private final Charset DEFAULT_ENCODING = Charsets.UTF_8;

    private static final String LOCATOR_CACHE_TIMEUNIT_KEY = "locator-cache-timeunit";
    private static final String LOCATOR_CACHE_TIMEOUT_KEY = "locator-cache-timeout";
    private static final int LOCATOR_CACHE_TIMEOUT_DEFAULT = 30;

    private static final String TRANSACTION_TIMEOUT_UNIT_KEY = "transaction-timeout-unit";
    private static final String TRANSACTION_TIMEOUT_KEY = "transaction-timeout";
    private static final int TRANSACTION_TIMEOUT_DEFAULT = 1;

	private static final String TRUST_TOKEN_TIMEOUT_KEY = "trust-token-timeout";
	private static final int TRUST_TOKEN_TIMEOUT_DEFAULT = 60;
	
	private static final String SOLR_CHECKING_INTERVAL_KEY = "solr-checking-interval";
	private static final int SOLR_CHECKING_INTERVAL_DEFAULT = 10;

    private final static String ASCMD = "Microsoft-Server-ActiveSync";

    private final static String EXTERNAL_URL_KEY = "external-url";

    private final static String LOCATOR_HOST_KEY = "host";
    private final static String LOCATOR_PORT = "8084";
    private final static String LOCATOR_APP_NAME = "obm-locator";

    private final static String OBM_SYNC_PORT = "8080";
    private final static String OBM_SYNC_APP_NAME = "obm-sync/services";

    private final ImmutableMap<String, TimeUnit> timeUnits;

	private final String applicationName;

    @Inject
	public ConfigurationServiceImpl(IniFile.Factory iniFileFactory, @Named("application-name")String applicationName) {
		super(iniFileFactory.build(GLOBAL_CONFIGURATION_FILE));
		this.applicationName = applicationName;
		this.timeUnits = ImmutableMap.of("milliseconds", TimeUnit.MILLISECONDS,
								"seconds", TimeUnit.SECONDS,
								"minutes", TimeUnit.MINUTES,
								"hours", TimeUnit.HOURS);
	}

	@Override
	public String getLocatorUrl() throws ConfigurationException {
		String locatorHost = getStringValue(LOCATOR_HOST_KEY);
		if (locatorHost == null) {
			throw new ConfigurationException(
					"Missing host key in configuration");
		}
		return "http://" + locatorHost + ":" + LOCATOR_PORT + "/" + LOCATOR_APP_NAME + "/";
	}

	@Override
	public String getObmUIBaseUrl() {
		String protocol = getStringValue("external-protocol");
		String hostname = getExternalUrl();
		String path = getStringValue("obm-prefix");
		return protocol + "://" + hostname + path;
	}

	@Override
	public String getObmSyncUrl(String obmSyncHost) {
		return "http://" + obmSyncHost + ":" + OBM_SYNC_PORT + "/" + OBM_SYNC_APP_NAME;
	}
	
	@Override
	public int getLocatorCacheTimeout() {
		return getIntValue(LOCATOR_CACHE_TIMEOUT_KEY, LOCATOR_CACHE_TIMEOUT_DEFAULT);
	}
	
	@Override
	public TimeUnit getLocatorCacheTimeUnit() {
		String key = getStringValue(LOCATOR_CACHE_TIMEUNIT_KEY);
		return getTimeUnitOrDefault(key, TimeUnit.MINUTES);
	}
	
	private int getTransactionTimeout() {
		return getIntValue(TRANSACTION_TIMEOUT_KEY, TRANSACTION_TIMEOUT_DEFAULT);
	}
	
	private TimeUnit getTransactionTimeoutUnit() {
		String key = getStringValue(TRANSACTION_TIMEOUT_UNIT_KEY);
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
	public int transactionTimeoutInSeconds() {
		TimeUnit transactionTimeoutUnit = getTransactionTimeoutUnit();
		int transactionTimeout = getTransactionTimeout();
		long transactionTimeoutInSeconds = transactionTimeoutUnit.toSeconds(transactionTimeout);
		return Ints.checkedCast(transactionTimeoutInSeconds);
	}
	
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("Messages", locale, new Control());
	}

	@Override
	public Charset getDefaultEncoding() {
		return DEFAULT_ENCODING;
	}

	@Override
	public String getActiveSyncServletUrl() {
		return "https://" + getExternalUrl() + "/" + ASCMD;
	}

	private String getExternalUrl() {
		return getStringValue(EXTERNAL_URL_KEY);
	}


	@Override
	public boolean usePersistentCache() {
		return true;
	}

	@Override
	public int trustTokenTimeoutInSeconds() {
		return getIntValue(TRUST_TOKEN_TIMEOUT_KEY, TRUST_TOKEN_TIMEOUT_DEFAULT);
	}

	@Override
	public int solrCheckingInterval() {
		return getIntValue(SOLR_CHECKING_INTERVAL_KEY, SOLR_CHECKING_INTERVAL_DEFAULT);
	}
	
	@Override
	public String getDataDirectory() {
		return "/var/lib/" + applicationName;
	}
}
