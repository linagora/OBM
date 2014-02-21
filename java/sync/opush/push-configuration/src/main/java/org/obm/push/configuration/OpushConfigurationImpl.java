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

package org.obm.push.configuration;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.obm.configuration.resourcebundle.Control;
import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.TimeUnitMapper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;

public class OpushConfigurationImpl implements OpushConfiguration {

	private static final Charset DEFAULT_ENCODING = Charsets.UTF_8;

	private static final String TRANSACTION_TIMEOUT_UNIT_KEY = "transaction-timeout-unit";
	private static final String TRANSACTION_TIMEOUT_KEY = "transaction-timeout";
	private static final int TRANSACTION_TIMEOUT_DEFAULT = 1;
	
	@VisibleForTesting static final String GLOBAL_DOMAIN = "global.virt";
	@VisibleForTesting final static String ASCMD = "Microsoft-Server-ActiveSync";
	@VisibleForTesting final static String EXTERNAL_URL_KEY = "external-url";
	@VisibleForTesting final static String OBM_SYNC_PORT = "8080";
	@VisibleForTesting final static String OBM_SYNC_APP_NAME = "obm-sync/services";
	
	public static class Factory {
		
		protected IniFile.Factory iniFileFactory;

		public Factory() {
			iniFileFactory = new IniFile.Factory();
		}
		
		public OpushConfigurationImpl create(String opushConfigurationFile, String applicationName) {
			return new OpushConfigurationImpl(iniFileFactory.build(opushConfigurationFile), applicationName);
		}
	}
	
	private final String applicationName;
	private final TimeUnitMapper timeUnitMapper;
	private final IniFile iniFile;

	@VisibleForTesting 
	protected OpushConfigurationImpl(IniFile opushConfigurationFile, String applicationName) {
		this.iniFile = opushConfigurationFile;
		this.applicationName = applicationName;
		this.timeUnitMapper = new TimeUnitMapper();
	}
	
	@Override
	public Charset getDefaultEncoding() {
		return DEFAULT_ENCODING;
	}

	@Override
	public String getDataDirectory() {
		return "/var/lib/" + applicationName;
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
	public boolean usePersistentEhcacheStore() {
		return true;
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("Messages", locale, new Control());
	}
	
	@Override
	public String getGlobalDomain() {
		return GLOBAL_DOMAIN;
	}

	@Override
	public String getActiveSyncServletUrl() {
		return "https://" + getExternalUrl() + "/" + ASCMD;
	}

	private String getExternalUrl() {
		return iniFile.getStringValue(EXTERNAL_URL_KEY);
	}

	@Override
	public String getObmSyncUrl(String obmSyncHost) {
		return "http://" + obmSyncHost + ":" + OBM_SYNC_PORT + "/" + OBM_SYNC_APP_NAME;
	}
}
