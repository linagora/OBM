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
package org.obm.opush.env;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.Configuration;
import org.obm.StaticLocatorConfiguration;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.SyncPermsConfigurationService;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.configuration.RemoteConsoleConfiguration;
import org.obm.push.store.ehcache.EhCacheConfiguration;
import org.obm.push.store.ehcache.EhCacheStores;
import org.obm.push.utils.ShareAmount;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class OpushStaticConfiguration extends StaticLocatorConfiguration implements OpushConfiguration {

	private final Configuration configuration;

	public OpushStaticConfiguration(OpushConfigurationFixture configuration) {
		super(configuration.locator);
		this.configuration = configuration;
	}

	public static class Email implements EmailConfiguration {

		private final OpushConfigurationFixture.Mail configuration;

		public Email(OpushConfigurationFixture.Mail configuration) {
			this.configuration = configuration;
		}

		@Override
		public boolean loginWithDomain() {
			return configuration.loginWithDomain;
		}

		@Override
		public int imapTimeoutInMilliseconds() {
			return configuration.timeoutInMilliseconds;
		}

		@Override
		public ExpungePolicy expungePolicy() {
			return ExpungePolicy.ALWAYS;
		}
		
		@Override
		public int imapPort() {
			return configuration.imapPort;
		}

		@Override
		public String imapMailboxTrash() {
			return EmailConfiguration.IMAP_TRASH_NAME;
		}

		@Override
		public String imapMailboxSent() {
			return EmailConfiguration.IMAP_SENT_NAME;
		}

		@Override
		public String imapMailboxDraft() {
			return EmailConfiguration.IMAP_DRAFTS_NAME;
		}

		@Override
		public int getMessageMaxSize() {
			return configuration.maxMessageSize;
		}

		@Override
		public int getImapFetchBlockSize() {
			return configuration.fetchBlockSize;
		}

		@Override
		public boolean activateTls() {
			return configuration.activateTls;
		}

		@Override
		public MailboxNameCheckPolicy mailboxNameCheckPolicy() {
			return MailboxNameCheckPolicy.ALWAYS;
		}

	}
	
	public static class RemoteConsole implements RemoteConsoleConfiguration {

		private final OpushConfigurationFixture.RemoteConsole configuration;

		public RemoteConsole(OpushConfigurationFixture.RemoteConsole configuration) {
			this.configuration = configuration;
		}

		@Override
		public boolean enable() {
			return configuration.enable;
		}

		@Override
		public int port() {
			return configuration.port;
		}

		@Override
		public String authoritativeDomain() {
			return DEFAULT_AUTHORITATIVE_DOMAIN;
		}
	}
	
	public static class SyncPerms implements SyncPermsConfigurationService {

		private final OpushConfigurationFixture.SyncPerms configuration;

		public SyncPerms(OpushConfigurationFixture.SyncPerms configuration) {
			this.configuration = configuration;
		}

		@Override
		public String getBlackListUser() {
			return configuration.blacklist;
		}

		@Override
		public Boolean allowUnknownPdaToSync() {
			return configuration.allowUnknownDevice;
		}
	}

	public static class EhCache implements EhCacheConfiguration {

		private final OpushConfigurationFixture.EhCache configuration;
		private final Map<String, Percentage> percentageByStoreMap;

		public EhCache(OpushConfigurationFixture.EhCache configuration) {
			this.configuration = configuration;
			this.percentageByStoreMap = Maps.transformValues(
					ShareAmount.forEntries(EhCacheStores.STORES).amount(100),
					new Function<Integer, Percentage>() {
						@Override
						public Percentage apply(Integer input) {
							return Percentage.of(input);
						}
					});
		}

		@Override
		public int maxMemoryInMB() {
			return configuration.maxMemoryInMB;
		}

		@Override
		public Percentage percentageAllowedToCache(String cacheName) {
			return Objects.firstNonNull(percentageByStoreMap.get(cacheName), Percentage.UNDEFINED);
		}
		
		@Override
		public Map<String, Percentage> percentageAllowedToCaches() {
			return percentageByStoreMap;
		}

		@Override
		public long timeToLiveInSeconds() {
			return configuration.timeToLiveInSeconds;
		}

		@Override
		public TransactionalMode transactionalMode() {
			return TransactionalMode.XA;
		}
		
		@Override
		public int statsSampleToRecordCount() {
			return configuration.statsSampleToRecordCount;
		}

		@Override
		public int statsShortSamplingTimeInSeconds() {
			return configuration.statsShortSamplingTimeInSeconds;
		}
		
		@Override
		public int statsMediumSamplingTimeInSeconds() {
			return configuration.statsMediumSamplingTimeInSeconds;
		}
		
		@Override
		public int statsLongSamplingTimeInSeconds() {
			return configuration.statsLongSamplingTimeInSeconds;
		}

		@Override
		public int statsSamplingTimeStopInMinutes() {
			return configuration.statsSamplingTimeStopInMinutes;
		}
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
	public String getDataDirectory() {
		try {
			return configuration.dataDir.getCanonicalPath();
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		throw new IllegalStateException();
	}
	
	@Override
	public boolean usePersistentEhcacheStore() {
		return configuration.transaction.usePersistentCache;
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return configuration.bundle;
	}

	@Override
	public String getGlobalDomain() {
		return "global.test";
	}

	@Override
	public String getActiveSyncServletUrl() {
		return configuration.activeSyncServletUrl;
	}

	@Override
	public String getObmSyncUrl(String obmSyncHost) {
		return obmSyncHost + configuration.obmSyncServices;
	}
}