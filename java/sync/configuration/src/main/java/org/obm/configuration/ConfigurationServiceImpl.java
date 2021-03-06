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
package org.obm.configuration;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.TimeUnitMapper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

public class ConfigurationServiceImpl implements ConfigurationService {

	private static final Charset DEFAULT_ENCODING = Charsets.UTF_8;

	private static final String TRANSACTION_TIMEOUT_UNIT_KEY = "transaction-timeout-unit";
	private static final String TRANSACTION_TIMEOUT_KEY = "transaction-timeout";
	private static final int TRANSACTION_TIMEOUT_DEFAULT = 1;

	private static final String TRANSACTION_TOLERANCE_TIMEOUT_KEY = "transaction-tolerance-timeout-seconds";
	private static final int TRANSACTION_TOLERANCE_TIMEOUT_DEFAULT = 0;

	private static final String TRUST_TOKEN_TIMEOUT_KEY = "trust-token-timeout";
	private static final int TRUST_TOKEN_TIMEOUT_DEFAULT = 60;

	private static final String SOLR_CHECKING_INTERVAL_KEY = "solr-checking-interval";
	private static final int SOLR_CHECKING_INTERVAL_DEFAULT = 10;

	private final static String EXTERNAL_URL_KEY = "external-url";
	private final static String EXTERNAL_URL_PROTOCOL_KEY = "external-protocol";
	
	private final static String EXTERNAL_URL_PREFIX_KEY = "obm-prefix";
	private final static String EXTERNAL_URL_PREFIX_DEFAULT = "";
	
	private final static String OBM_SYNC_PORT = "8080";
	private final static String OBM_SYNC_APP_NAME = "obm-sync";
	private final static String SERVICES_APP_NAME = "services";
	
	private final static String ANONYMIZE_PRIVATE_EVENTS = "anonymize-private-events";
	private final static boolean ANONYMIZE_PRIVATE_EVENTS_DEFAULT = true;

	private final static String CONFIDENTIAL_EVENTS_ENABLED = "enable-confidential-events";
	private final static boolean CONFIDENTIAL_EVENTS_ENABLED_DEFAULT = true;

	private final static String CYRUS_PARTITION = "cyrusPartition";
	private final static boolean CYRUS_PARTITION_DEFAULT = false;

	private final static String PASSWORD_HASH = "password-hash";
	private final static Hash PASSWORD_HASH_DEFAULT = Hash.NONE;

	private final static String USER_MAILBOX_DEFAULT_FOLDERS = "userMailboxDefaultFolders";

	public static class Factory {
		
		protected IniFile.Factory iniFileFactory;

		public Factory() {
			iniFileFactory = new IniFile.Factory();
		}
		
		public ConfigurationServiceImpl create(String globalConfigurationFile, String applicationName) {
			return new ConfigurationServiceImpl(iniFileFactory.build(globalConfigurationFile), applicationName);
		}
	}
	
	private final String applicationName;
	private final TimeUnitMapper timeUnitMapper;
	protected final IniFile iniFile;

	@VisibleForTesting 
	protected ConfigurationServiceImpl(IniFile globalConfigurationIniFile, String applicationName) {
		this.iniFile = globalConfigurationIniFile;
		this.applicationName = applicationName;
		this.timeUnitMapper = new TimeUnitMapper();
	}

	private int getTransactionTimeout() {
		return iniFile.getIntValue(TRANSACTION_TIMEOUT_KEY, TRANSACTION_TIMEOUT_DEFAULT);
	}

	private TimeUnit getTransactionTimeoutUnit() {
		String key = iniFile.getStringValue(TRANSACTION_TIMEOUT_UNIT_KEY);
		return timeUnitMapper.getTimeUnitOrDefault(key, TimeUnit.MINUTES);
	}

	@Override
	public int transactionTimeoutInSeconds() {
		TimeUnit transactionTimeoutUnit = getTransactionTimeoutUnit();
		int transactionTimeout = getTransactionTimeout();
		long transactionTimeoutInSeconds = transactionTimeoutUnit.toSeconds(transactionTimeout);
		return Ints.checkedCast(transactionTimeoutInSeconds);
	}

    @Override
    public int getTransactionToleranceTimeoutInSeconds() {
            return iniFile.getIntValue(TRANSACTION_TOLERANCE_TIMEOUT_KEY, TRANSACTION_TOLERANCE_TIMEOUT_DEFAULT);
    }

	@Override
	public Charset getDefaultEncoding() {
		return DEFAULT_ENCODING;
	}

	@Override
	public int trustTokenTimeoutInSeconds() {
		return iniFile.getIntValue(TRUST_TOKEN_TIMEOUT_KEY, TRUST_TOKEN_TIMEOUT_DEFAULT);
	}

	@Override
	public int solrCheckingInterval() {
		return iniFile.getIntValue(SOLR_CHECKING_INTERVAL_KEY, SOLR_CHECKING_INTERVAL_DEFAULT);
	}

	@Override
	public String getDataDirectory() {
		return "/var/lib/" + applicationName;
	}

	@Override
	public String getLdapServer() {
		return iniFile.getStringValue("auth-ldap-server");
	}

	@Override
	public String getLdapBaseDn() {
		return iniFile.getStringValue("auth-ldap-basedn").replace("\"", "");
	}

	@Override
	public String getLdapFilter() {
		return iniFile.getStringValue("auth-ldap-filter").replace("\"", "");
	}

	@Override
	public String getLdapBindDn() {
		String bindDn = iniFile.getStringValue("auth-ldap-binddn");
		if (bindDn != null) {
			return bindDn.replace("\"", "");
		}
		return null;
	}

	@Override
	public String getLdapBindPassword() {
		String bindPassword = iniFile.getStringValue("auth-ldap-bindpw");
		if (bindPassword != null) {
			return bindPassword.replace("\"", "");
		}
		return null;
	}
	
	@Override
	public String getGlobalDomain() {
		return GLOBAL_DOMAIN;
	}

	@Override
	public String getObmUIBaseUrl() {
		String protocol = getObmUIUrlProtocol();
		String hostname = getObmUIUrlHost();
		String path = getObmUIUrlPrefix();
		return protocol + "://" + hostname + path;
	}
	
	@Override
	public String getObmUIUrlProtocol() {
		return iniFile.getStringValue(EXTERNAL_URL_PROTOCOL_KEY);
	}

	@Override
	public String getObmUIUrlHost() {
		return iniFile.getStringValue(EXTERNAL_URL_KEY);
	}
	
	@Override
	public String getObmUIUrlPrefix() {
		return iniFile.getStringValue(EXTERNAL_URL_PREFIX_KEY, EXTERNAL_URL_PREFIX_DEFAULT);
	}

	@Override
	public String getObmSyncServicesUrl(String obmSyncHost) {
		return getObmSyncBaseUrl(obmSyncHost) + "/" + SERVICES_APP_NAME;
	}

	@Override
	public boolean isPrivateEventAnonymizationEnabled() {
		return iniFile.getBooleanValue(ANONYMIZE_PRIVATE_EVENTS, ANONYMIZE_PRIVATE_EVENTS_DEFAULT);
	}

	@Override
	public String getObmSyncBaseUrl(String obmSyncHost) {
		return "http://" + obmSyncHost + ":" + OBM_SYNC_PORT + "/" + OBM_SYNC_APP_NAME;
	}

	@Override
	public boolean isLdapModuleEnabled() {
		return this.iniFile.getBooleanValue("obm-ldap");
	}

	@Override
	public boolean isConfidentialEventsEnabled() {
		return iniFile.getBooleanValue(CONFIDENTIAL_EVENTS_ENABLED, CONFIDENTIAL_EVENTS_ENABLED_DEFAULT);
	}

	@Override
	public boolean isCyrusPartitionEnabled() {
		return iniFile.getBooleanValue(CYRUS_PARTITION, CYRUS_PARTITION_DEFAULT);
	}

	@Override
	public Hash getPasswordHash() {
		String strValue = iniFile.getStringValue(PASSWORD_HASH);

		return strValue == null ? PASSWORD_HASH_DEFAULT : Hash.valueOf(strValue.toUpperCase());
	}

	@Override
	public Collection<String> getUserMailboxDefaultFolders() {
		String value = iniFile.getStringValue(USER_MAILBOX_DEFAULT_FOLDERS);

		return Splitter
				.on(',')
				.omitEmptyStrings()
				.trimResults()
				.splitToList(IniFile.removeEnclosingQuotes(Strings.nullToEmpty(value)));
	}

}
