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
package org.obm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.Hash;
import org.obm.configuration.TransactionConfiguration;

import com.google.common.base.Throwables;

public class StaticConfigurationService extends StaticLocatorConfiguration implements ConfigurationService {

	public static class Transaction implements TransactionConfiguration {
		
		private final org.obm.Configuration.Transaction configuration;
		
		public Transaction(org.obm.Configuration.Transaction configuration) {
			this.configuration = configuration;
		}
		
		@Override
		public boolean enableJournal() {
			return configuration.enableJournal;
		}
		
		@Override
		public File getJournalPart1Path() {
			return configuration.journal1;
		}
		
		@Override
		public File getJournalPart2Path() {
			return configuration.journal2;
		}
		
		@Override
		public int getTimeOutInSecond() {
			return configuration.timeoutInSeconds;
		}
	}
	
	private final Configuration configuration;

	public StaticConfigurationService(Configuration configuration) {
		super(configuration.locator);
		this.configuration = configuration;
	}

	@Override
	public Charset getDefaultEncoding() {
		return configuration.defautEncoding;
	}

	@Override
	public int transactionTimeoutInSeconds() {
		return configuration.transaction.timeoutInSeconds;
	}

	@Override
	public int trustTokenTimeoutInSeconds() {
		return configuration.trustTokenTimeoutInSeconds;
	}

	@Override
	public int solrCheckingInterval() {
		return configuration.solrCheckingInterval;
	}

	@Override
	public String getDataDirectory() {
		try {
			return configuration.dataDir.getCanonicalPath();
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		throw new IllegalStateException();
	}

	@Override
	public String getLdapServer() {
		return null;
	}

	@Override
	public String getLdapBaseDn() {
		return null;
	}

	@Override
	public String getLdapFilter() {
		return null;
	}

	@Override
	public String getLdapBindDn() {
		return null;
	}

	@Override
	public String getLdapBindPassword() {
		return null;
	}

	@Override
	public String getGlobalDomain() {
		return "global.test";
	}

    @Override
    public int getTransactionToleranceTimeoutInSeconds() {
            return configuration.transaction.toleranceInSeconds;
    }

	@Override
	public String getObmUIBaseUrl() {
		return configuration.obmUiBaseUrl;
	}

	@Override
	public String getObmSyncServicesUrl(String obmSyncHost) {
		return obmSyncHost + configuration.obmSyncServices;
	}

	@Override
	public String getObmSyncBaseUrl(String obmSyncHost) {
		return obmSyncHost;
	}

	@Override
	public boolean isLdapModuleEnabled() {
		return true;
	}

	@Override
	public String getObmUIUrlProtocol() {
		return "http";
	}

	@Override
	public String getObmUIUrlHost() {
		return "localhost";
	}

	@Override
	public String getObmUIUrlPrefix() {
		return "";
	}
	
	@Override
	public boolean isPrivateEventAnonymizationEnabled() {
		return configuration.anonymizePrivateEvent;
	}

	@Override
	public boolean isConfidentialEventsEnabled() {
		return true;
	}

	@Override
	public boolean isCyrusPartitionEnabled() {
		return false;
	}

	@Override
	public Hash getPasswordHash() {
		return configuration.passwordHash;
	}

}