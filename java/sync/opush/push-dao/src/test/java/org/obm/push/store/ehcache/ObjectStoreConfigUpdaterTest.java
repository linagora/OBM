/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
import static org.obm.push.store.ehcache.EhCacheStores.MAIL_SNAPSHOT_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.MAIL_WINDOWING_CHUNKS_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.MAIL_WINDOWING_INDEX_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.MONITORED_COLLECTION_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.SYNCED_COLLECTION_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.SYNC_KEYS_STORE;
import static org.obm.push.store.ehcache.EhCacheStores.UNSYNCHRONIZED_ITEM_STORE;

import java.io.IOException;
import java.util.Map;

import net.sf.ehcache.config.MemoryUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.obm.push.utils.jvm.JvmUtils;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.ImmutableMap;

@RunWith(SlowFilterRunner.class) @Slow
public class ObjectStoreConfigUpdaterTest extends StoreManagerConfigurationTest {

	private int initialMaxMemory = 200;
	private ObjectStoreConfigUpdater testee;

	@Before
	public void init() throws IOException {
		Logger logger = createNiceMock(Logger.class);
		ConfigurationService configurationService = super.mockConfigurationService();
		EhCacheConfiguration config = new TestingEhCacheConfiguration().withMaxMemoryInMB(initialMaxMemory);
		testee = new ObjectStoreConfigUpdater(new ObjectStoreManager(configurationService, config, logger));
	}

	@After
	public void shutdown() {
		testee.storeManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenNegative() {
		testee.updateMaxMemoryInMB(-1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenZero() {
		testee.updateMaxMemoryInMB(0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenSameThanJVM() {
		testee.updateMaxMemoryInMB(JvmUtils.maxRuntimeJvmMemoryInMB());
	}
	
	@Test
	public void updateMaxMemoryWhenIncreaseValue() {
		Map<String, Percentage> percentageBeforeUpdate = testee.configReader.getRunningStoresPercentages();
		
		testee.updateMaxMemoryInMB(initialMaxMemory + 50);

		assertThat(testee.configReader.getRunningStoresPercentages()).isEqualTo(percentageBeforeUpdate);
		assertThat(testee.storeManager.singletonManager.getConfiguration().getMaxBytesLocalHeap())
			.isGreaterThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory + 50))
			.isLessThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory + 51));
	}
	
	@Test
	public void updateMaxMemoryWhenIncreaseValueTwice() {
		Map<String, Percentage> percentageBeforeUpdate = testee.configReader.getRunningStoresPercentages();
		
		testee.updateMaxMemoryInMB(initialMaxMemory + 50);
		testee.updateMaxMemoryInMB(initialMaxMemory + 100);

		assertThat(testee.configReader.getRunningStoresPercentages()).isEqualTo(percentageBeforeUpdate);
		assertThat(testee.storeManager.singletonManager.getConfiguration().getMaxBytesLocalHeap())
			.isGreaterThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory + 100))
			.isLessThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory + 101));
	}
	
	@Test
	public void updateMaxMemoryWhenDecreaseValue() {
		Map<String, Percentage> percentageBeforeUpdate = testee.configReader.getRunningStoresPercentages();
		
		testee.updateMaxMemoryInMB(initialMaxMemory - 50);

		assertThat(testee.configReader.getRunningStoresPercentages()).isEqualTo(percentageBeforeUpdate);
		assertThat(testee.storeManager.singletonManager.getConfiguration().getMaxBytesLocalHeap())
			.isGreaterThanOrEqualTo(150000000)
			.isLessThanOrEqualTo(160000000);
	}
	
	@Test
	public void updateMaxMemoryWhenDecreaseValueTwice() {
		Map<String, Percentage> percentageBeforeUpdate = testee.configReader.getRunningStoresPercentages();
		
		testee.updateMaxMemoryInMB(initialMaxMemory - 50);
		testee.updateMaxMemoryInMB(initialMaxMemory - 100);

		assertThat(testee.configReader.getRunningStoresPercentages()).isEqualTo(percentageBeforeUpdate);
		assertThat(testee.storeManager.singletonManager.getConfiguration().getMaxBytesLocalHeap())
			.isGreaterThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory - 101))
			.isLessThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory - 100));
	}
	
	@Test
	public void updateMaxMemoryWhenDecreaseIncreaseDecreaseValue() {
		Map<String, Percentage> percentageBeforeUpdate = testee.configReader.getRunningStoresPercentages();
		
		testee.updateMaxMemoryInMB(initialMaxMemory - 50);
		testee.updateMaxMemoryInMB(initialMaxMemory + 100);
		testee.updateMaxMemoryInMB(initialMaxMemory - 150);

		assertThat(testee.configReader.getRunningStoresPercentages()).isEqualTo(percentageBeforeUpdate);
		assertThat(testee.storeManager.singletonManager.getConfiguration().getMaxBytesLocalHeap())
			.isGreaterThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory - 151))
			.isLessThanOrEqualTo(MemoryUnit.MEGABYTES.toBytes(initialMaxMemory - 150));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenNotAllStoresGiven() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(95))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(1))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(1))
				.build());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenOneUnknownStore() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(94))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(1))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(1))
				.put("nonExistingStore", Percentage.of(1))
				.build());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenAllPlusOneStore() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(93))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(1))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(1))
				.put(SYNC_KEYS_STORE, Percentage.of(1))
				.put("anotherStore", Percentage.of(1))
				.build());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenLessThanHundred() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(93))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(1))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(1))
				.put(SYNC_KEYS_STORE, Percentage.of(1))
				.build());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateMaxMemoryWhenMoreThanHundred() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(95))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(1))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(1))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(1))
				.put(SYNC_KEYS_STORE, Percentage.of(1))
				.build());
	}
	
	@Test
	public void updateMaxMemory() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(MONITORED_COLLECTION_STORE, Percentage.of(30))
				.put(SYNCED_COLLECTION_STORE, Percentage.of(30))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(20))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(10))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(5))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(4))
				.put(SYNC_KEYS_STORE, Percentage.of(1))
				.build());

		assertThat(getCacheHeapPercentage(MONITORED_COLLECTION_STORE)).isEqualTo(30);
		assertThat(getCacheHeapPercentage(SYNCED_COLLECTION_STORE)).isEqualTo(30);
		assertThat(getCacheHeapPercentage(UNSYNCHRONIZED_ITEM_STORE)).isEqualTo(20);
		assertThat(getCacheHeapPercentage(MAIL_SNAPSHOT_STORE)).isEqualTo(10);
		assertThat(getCacheHeapPercentage(MAIL_WINDOWING_INDEX_STORE)).isEqualTo(5);
		assertThat(getCacheHeapPercentage(MAIL_WINDOWING_CHUNKS_STORE)).isEqualTo(4);
		assertThat(getCacheHeapPercentage(SYNC_KEYS_STORE)).isEqualTo(1);
	}
	
	@Test
	public void updateMaxMemoryDifferentOrder() {
		testee.updateStoresMaxMemory(ImmutableMap.<String, Percentage>builder()
				.put(SYNCED_COLLECTION_STORE, Percentage.of(30))
				.put(SYNC_KEYS_STORE, Percentage.of(1))
				.put(MAIL_SNAPSHOT_STORE, Percentage.of(10))
				.put(MAIL_WINDOWING_INDEX_STORE, Percentage.of(5))
				.put(UNSYNCHRONIZED_ITEM_STORE, Percentage.of(20))
				.put(MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(4))
				.put(MONITORED_COLLECTION_STORE, Percentage.of(30))
				.build());
		
		assertThat(getCacheHeapPercentage(MONITORED_COLLECTION_STORE)).isEqualTo(30);
		assertThat(getCacheHeapPercentage(SYNCED_COLLECTION_STORE)).isEqualTo(30);
		assertThat(getCacheHeapPercentage(UNSYNCHRONIZED_ITEM_STORE)).isEqualTo(20);
		assertThat(getCacheHeapPercentage(MAIL_SNAPSHOT_STORE)).isEqualTo(10);
		assertThat(getCacheHeapPercentage(MAIL_WINDOWING_INDEX_STORE)).isEqualTo(5);
		assertThat(getCacheHeapPercentage(MAIL_WINDOWING_CHUNKS_STORE)).isEqualTo(4);
		assertThat(getCacheHeapPercentage(SYNC_KEYS_STORE)).isEqualTo(1);
	}

	private Integer getCacheHeapPercentage(String storeName) {
		return testee.storeManager.requiredStoreConfiguration(storeName).getMaxBytesLocalHeapPercentage();
	}
}
