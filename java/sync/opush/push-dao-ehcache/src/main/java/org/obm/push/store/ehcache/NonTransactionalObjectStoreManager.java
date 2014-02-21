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
package org.obm.push.store.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.obm.configuration.module.LoggerModule;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.sync.LifecycleListener;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class NonTransactionalObjectStoreManager implements LifecycleListener {

	public static final String STORE_NAME = NonTransactionalObjectStoreManager.class.getName();
	public static final String PENDING_CONTINUATIONS = "pendingContinuation";
	
	private final static int UNLIMITED_CACHE_MEMORY = 0;
	
	@VisibleForTesting final CacheManager singletonManager;
	private final Logger configurationLogger;
	
	@Inject NonTransactionalObjectStoreManager(
			OpushConfiguration opushConfiguration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		this.configurationLogger = configurationLogger;
		int transactionTimeoutInSeconds = opushConfiguration.transactionTimeoutInSeconds();
		this.singletonManager = new CacheManager(ehCacheConfiguration(transactionTimeoutInSeconds));
	}

	@Override
	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration(int transactionTimeoutInSeconds) {
		return new Configuration()
			.name(STORE_NAME)
			.updateCheck(false)
			.cache(pendingContinuationConfiguration(PENDING_CONTINUATIONS))
			.defaultTransactionTimeoutInSeconds(transactionTimeoutInSeconds);
	}
	
	private CacheConfiguration pendingContinuationConfiguration(String name) {
		configurationLogger.info("Configuring store {}", name);
		return new CacheConfiguration()
			.name(name)
			.maxEntriesLocalHeap(UNLIMITED_CACHE_MEMORY)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false);
	}

	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}
}
