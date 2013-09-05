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

import static org.easymock.EasyMock.createNiceMock;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.InvalidConfigurationException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class) @Slow
public class EhCacheSettingsTest extends StoreManagerConfigurationTest {

	private ConfigurationService configurationService;
	private EhCacheConfiguration config;
	private Logger logger;

	
	@Before
	public void init() throws IOException {
		logger = createNiceMock(Logger.class);
		configurationService = super.mockConfigurationService();
		config = new TestingEhCacheConfiguration();
	}

	@Ignore("ehCache always respond false to isElementInMemory")
	@Test
	public void overflowToDisk() throws Exception {
		TestingEhCacheConfiguration configOneMBMax = new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(1)
			.withPercentageAllowedToCache(null);
		ObjectStoreManager cacheManager = new ObjectStoreManager(configurationService, configOneMBMax, logger);
		
		byte[] arrayOf300KB = new byte[300 * 1024];
		Arrays.fill(arrayOf300KB, (byte) 65);
		
		TransactionManagerServices.getTransactionManager().begin();
		Cache cache = cacheManager.getStore("mailSnapshotStore");
		cache.put(new Element("key1", arrayOf300KB));
		cache.put(new Element("key2", arrayOf300KB));
		cache.put(new Element("key3", arrayOf300KB));
		cache.put(new Element("key4", arrayOf300KB));
		
		assertThat(cache.isElementInMemory("key4")).isTrue();
		assertThat(cache.isElementInMemory("key3")).isTrue();
		assertThat(cache.isElementInMemory("key2")).isTrue();
		assertThat(cache.isElementInMemory("key1")).isFalse();
		assertThat(cache.isElementOnDisk("key1")).isTrue();
		TransactionManagerServices.getTransactionManager().commit();
		TransactionManagerServices.getTransactionManager().shutdown();
		cacheManager.shutdown();
	}

	@Test
	public void persistentCachesAreRestoredAfterRestart() throws Exception {
		Element el1 = new Element("key1", "value1");
		Element el2 = new Element("key2", "value2");
		Iterable<String> persistentStoreNames = ImmutableList.of(
				ObjectStoreManager.SYNCED_COLLECTION_STORE,
				ObjectStoreManager.MONITORED_COLLECTION_STORE,
				ObjectStoreManager.SYNCED_COLLECTION_STORE,
				ObjectStoreManager.UNSYNCHRONIZED_ITEM_STORE,
				ObjectStoreManager.MAIL_SNAPSHOT_STORE,
				ObjectStoreManager.MAIL_WINDOWING_INDEX_STORE,
				ObjectStoreManager.MAIL_WINDOWING_CHUNKS_STORE,
				ObjectStoreManager.SYNC_KEYS_STORE);

		ObjectStoreManager beforeCacheManager = new ObjectStoreManager(configurationService, config, logger);
		TransactionManagerServices.getTransactionManager().begin();
		for (String persistentStoreName : persistentStoreNames) {
			Cache cache = beforeCacheManager.getStore(persistentStoreName);
			cache.put(el1);
			cache.put(el2);
		}
		TransactionManagerServices.getTransactionManager().commit();
		TransactionManagerServices.getTransactionManager().shutdown();
		beforeCacheManager.shutdown();

		TransactionManagerServices.getTransactionManager().begin();
		ObjectStoreManager newCacheManager = new ObjectStoreManager(configurationService, config, logger);
		for (String persistentStoreName : persistentStoreNames) {
			Cache loadedCache = newCacheManager.getStore(persistentStoreName);
			assertThat(loadedCache.get(el1.getObjectKey())).isEqualTo(el1);
			assertThat(loadedCache.get(el2.getObjectKey())).isEqualTo(el2);
		}
		
		TransactionManagerServices.getTransactionManager().commit();
		TransactionManagerServices.getTransactionManager().shutdown();
		newCacheManager.shutdown();
	}

	@SuppressWarnings("unused")
	@Test(expected=InvalidConfigurationException.class)
	public void testWhenSumIsOverOneHundredPercent() {
		TestingEhCacheConfiguration configWhenMoreThanOneHundred = new TestingEhCacheConfiguration()
			.withPercentageAllowedToCache(60);
		new ObjectStoreManager(configurationService, configWhenMoreThanOneHundred, logger);
	}
}
