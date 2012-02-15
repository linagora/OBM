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
package org.obm.push.store.ehcache;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import org.obm.configuration.ObmConfigurationService;
import org.obm.configuration.store.StoreNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObjectStoreManager {

	private final static int MAX_ELEMENT_IN_MEMORY = 5000;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final CacheManager singletonManager;

	@Inject ObjectStoreManager(ObmConfigurationService configurationService) throws StoreNotFoundException {
		InputStream storeConfiguration = configurationService.getStoreConfiguration();
		this.singletonManager = new CacheManager(storeConfiguration);
		int transactionTimeoutInSeconds = transactionTimeoutInSeconds(configurationService); 
		this.singletonManager.getTransactionController().setDefaultTransactionTimeout(transactionTimeoutInSeconds);
		logger.info("initializing ehcache with transaction timeout = {} seconds", transactionTimeoutInSeconds);
	}

	private static int transactionTimeoutInSeconds(ObmConfigurationService configurationService) {
		TimeUnit transactionTimeoutUnit = configurationService.getTransactionTimeoutUnit();
		int transactionTimeout = configurationService.getTransactionTimeout();
		long transactionTimeoutInSeconds = transactionTimeoutUnit.toSeconds(transactionTimeout);
		return Ints.checkedCast(transactionTimeoutInSeconds);
	}
	
	public Cache createNewStore(String storeName) {
		Cache store = getStore(storeName);
		if (store == null) {
			store = createStore(storeName);
			this.singletonManager.addCache(store);
		}
		return store;
	}

	private Cache createStore(String storeName) {
		return new Cache(createStoreConfiguration(storeName));
	}

	private CacheConfiguration createStoreConfiguration(String storeName) {
		return new CacheConfiguration(storeName, MAX_ELEMENT_IN_MEMORY);
	}

	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}

	public void removeStore(String storeName) {
		this.singletonManager.removeCache(storeName);
	}

	public List<String> listStores() {
		return Arrays.asList(this.singletonManager.getCacheNames());
	}

}
