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
package org.obm.push.technicallog.jaxb.store.ehcache;

import java.util.Arrays;
import java.util.List;

import org.obm.sync.LifecycleListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObjectStoreManager implements LifecycleListener {

	public static final String REQUEST_STORE = "request";
	public static final String STORE_NAME = ObjectStoreManager.class.getName();
	
	private final static int UNLIMITED_CACHE_MEMORY = 0;
	private final static int MAX_ELEMENT_IN_MEMORY = 5000;
	private final CacheManager singletonManager;

	@Inject 
	@VisibleForTesting ObjectStoreManager() {
		Configuration configuration = ehCacheConfiguration();
		configuration.addCache(unlimitedMemoryConfiguration().name(REQUEST_STORE));
		this.singletonManager = new CacheManager(configuration);
	}

	@Override
	public void shutdown() {
		this.singletonManager.shutdown();
	}
	
	private Configuration ehCacheConfiguration() {
		return new Configuration()
			.name(STORE_NAME)
			.updateCheck(false);
	}
	
	private CacheConfiguration unlimitedMemoryConfiguration() {
		return new CacheConfiguration()
			.maxEntriesLocalHeap(UNLIMITED_CACHE_MEMORY)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false);
	}
	
	public Cache getStore(String storeName) {
		Cache store = singletonManager.getCache(storeName);
		if (store == null) {
			store = createStore(storeName);
			singletonManager.addCache(store);
		}
		return store;
	}

	private Cache createStore(String storeName) {
		return new Cache(createStoreConfiguration(storeName));
	}

	private CacheConfiguration createStoreConfiguration(String storeName) {
		return new CacheConfiguration(storeName, MAX_ELEMENT_IN_MEMORY);
	}

	public void removeStore(String storeName) {
		singletonManager.removeCache(storeName);
	}

	public List<String> listStores() {
		return Arrays.asList(singletonManager.getCacheNames());
	}

}
