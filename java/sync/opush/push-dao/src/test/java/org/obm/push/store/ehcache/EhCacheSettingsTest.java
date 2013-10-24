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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.statistics.extended.ExtendedStatistics.Operation;
import net.sf.ehcache.statistics.extended.ExtendedStatistics.Statistic;
import net.sf.ehcache.store.StoreOperationOutcomes.GetOutcome;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.obm.transaction.TransactionManagerRule;
import org.slf4j.Logger;
import org.terracotta.statistics.archive.Timestamped;

import bitronix.tm.BitronixTransactionManager;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class) @Slow
public class EhCacheSettingsTest extends StoreManagerConfigurationTest {

	private ConfigurationService configurationService;
	private EhCacheConfiguration config;
	private Logger logger;
	private BitronixTransactionManager tm;

	
	@Before
	public void init() throws IOException {
		logger = createNiceMock(Logger.class);
		configurationService = super.mockConfigurationService();
		config = new TestingEhCacheConfiguration();

		tm = TransactionManagerRule.setupTransactionManager(temporaryFolder);
	}

	@After
	public void teardown() {
		tm.shutdown();
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
		
		tm.begin();
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
		tm.commit();
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

		tm.begin();
		for (String persistentStoreName : persistentStoreNames) {
			Cache cache = beforeCacheManager.getStore(persistentStoreName);
			cache.put(el1);
			cache.put(el2);
		}
		tm.commit();
		tm.shutdown();
		beforeCacheManager.shutdown();

		tm = TransactionManagerRule.setupTransactionManager(temporaryFolder);
		tm.begin();
		ObjectStoreManager newCacheManager = new ObjectStoreManager(configurationService, config, logger);
		for (String persistentStoreName : persistentStoreNames) {
			Cache loadedCache = newCacheManager.getStore(persistentStoreName);
			assertThat(loadedCache.get(el1.getObjectKey())).isEqualTo(el1);
			assertThat(loadedCache.get(el2.getObjectKey())).isEqualTo(el2);
		}
		
		tm.commit();
		newCacheManager.shutdown();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWhenSumIsUnderOneHundredPercent() {
		TestingEhCacheConfiguration configWhenLessThanOneHundred = new TestingEhCacheConfiguration()
			.withPercentageAllowedToCache(60);
		Map<String, Percentage> stores = configWhenLessThanOneHundred.getStores();
		stores.remove(ObjectStoreManager.MAIL_SNAPSHOT_STORE);
		stores.put(ObjectStoreManager.MAIL_SNAPSHOT_STORE, Percentage.of(1));
		
		ObjectStoreManager objectStoreManager = null;
		try {
			objectStoreManager = new ObjectStoreManager(configurationService, configWhenLessThanOneHundred, logger);
		} finally {
			if (objectStoreManager != null) {
				objectStoreManager.shutdown();
			}
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWhenSumIsOverOneHundredPercent() {
		TestingEhCacheConfiguration configWhenMoreThanOneHundred = new TestingEhCacheConfiguration()
			.withPercentageAllowedToCache(60);
		Map<String, Percentage> stores = configWhenMoreThanOneHundred.getStores();
		stores.remove(ObjectStoreManager.MAIL_SNAPSHOT_STORE);
		stores.put(ObjectStoreManager.MAIL_SNAPSHOT_STORE, Percentage.of(101));
		
		ObjectStoreManager objectStoreManager = null;
		try {
			objectStoreManager = new ObjectStoreManager(configurationService, configWhenMoreThanOneHundred, logger);
		} finally {
			if (objectStoreManager != null) {
				objectStoreManager.shutdown();
			}
		}
	}
	
	@Test
	public void testWhenSumEqualsOneHundredPercentDefaultConfiguration() {
		TestingEhCacheConfiguration configWhenEqualsOneHundred = new TestingEhCacheConfiguration()
			.withPercentageAllowedToCache(60);
		
		ObjectStoreManager objectStoreManager = null;
		try {
			objectStoreManager = new ObjectStoreManager(configurationService, configWhenEqualsOneHundred, logger);
		} finally {
			if (objectStoreManager != null) {
				objectStoreManager.shutdown();
			}
		}
	}
	
	@Test
	public void testWhenSumEqualsOneHundredPercent() {
		TestingEhCacheConfiguration configWhenEqualsOneHundred = new TestingEhCacheConfiguration()
			.withPercentageAllowedToCache(60);
		Map<String, Percentage> stores = configWhenEqualsOneHundred.getStores();
		Percentage removed = stores.remove(ObjectStoreManager.MAIL_SNAPSHOT_STORE);
		stores.put(ObjectStoreManager.MAIL_SNAPSHOT_STORE, removed);
		
		ObjectStoreManager objectStoreManager = null;
		try {
			objectStoreManager = new ObjectStoreManager(configurationService, configWhenEqualsOneHundred, logger);
		} finally {
			if (objectStoreManager != null) {
				objectStoreManager.shutdown();
			}
		}
	}
	
	@Test
	public void testTimeToLiveReached() throws Exception {
		TestingEhCacheConfiguration configOneMBMax = new TestingEhCacheConfiguration()
			.withTimeToLive(1);
		
		ObjectStoreManager cacheManager = new ObjectStoreManager(configurationService, configOneMBMax, logger);
	
		try {
			Element el1 = new Element("key1", "value1");
			tm.begin();
			Cache cache = cacheManager.getStore("mailSnapshotStore");
			cache.put(el1);
		
			Thread.sleep(2000);
			assertThat(cache.get(el1.getObjectKey())).isNull();
		} finally {
			tm.commit();
			cacheManager.shutdown();
		}
	}
	
	@Test
	public void testTimeToLiveStillAlive() throws Exception {
		TestingEhCacheConfiguration configOneMBMax = new TestingEhCacheConfiguration()
			.withTimeToLive(1);

		ObjectStoreManager cacheManager = new ObjectStoreManager(configurationService, configOneMBMax, logger);
	
		try {
			Element element = new Element("key1", "value1");
			tm.begin();
			Cache cache = cacheManager.getStore("mailSnapshotStore");
			cache.put(element);
		
			Thread.sleep(20);
			assertThat(cache.get(element.getObjectKey())).isNotNull();
		} finally {
			tm.commit();
			cacheManager.shutdown();
		}
	}
	
	@Test
	public void statsDiskHitRatioWhenNoOperation() {
		ObjectStoreManager cacheManager = new ObjectStoreManager(configurationService, config, logger);
		Cache store = cacheManager.createNewStore("storeName");
		
		assertThat(store.getStatistics().localDiskHitCount()).isEqualTo(0l);
		assertThat(store.getStatistics().cacheHitCount()).isEqualTo(0l);
		cacheManager.shutdown();
	}
	
	@Test
	public void statsDiskHitRatioWhenMoreWriteThanInMemoryLimit() {
		int maxElementsInMemory = 3;
		int untilWriteCount = 10;
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);

		for (int writeCount = 0; writeCount < untilWriteCount; writeCount ++) {
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}

		assertThat(store.getStatistics().localDiskHitCount()).isEqualTo(0l);
		assertThat(store.getStatistics().cacheHitCount()).isEqualTo(0l);
		store.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHitRatioWhenWriteLimitAcceptedInMemory() {
		int maxElementsInMemory = 3;
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < maxElementsInMemory; writeCount ++) {
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}

		assertThat(store.getStatistics().localDiskHitCount()).isEqualTo(0l);
		assertThat(store.getStatistics().cacheHitCount()).isEqualTo(0l);
		store.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHitRatioWhenReadInMemory() {
		int maxElementsInMemory = 3;
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		store.put(new Element("a", "b"));
		for (int readCount = 0; readCount < maxElementsInMemory; readCount ++) {
			store.get("a");
		}

		assertThat(store.getStatistics().localDiskHitCount()).isEqualTo(0l);
		assertThat(store.getStatistics().cacheHitCount()).isEqualTo(maxElementsInMemory);
		store.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHitRatioWhenReadInMemoryAndOnDisk() {
		int maxElementsInMemory = 3;
		int untilCount = 10;
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		for (int readCount = 0; readCount < untilCount; readCount ++) { 
			store.get("a" + readCount);
		}
		
		Operation<GetOutcome> diskGets = store.getStatistics().getExtended().diskGet();
		diskGets.setWindow(10, TimeUnit.SECONDS);
		assertThat(diskGets.component(GetOutcome.HIT).count().value()).isEqualTo(untilCount - maxElementsInMemory);
		assertThat(store.getStatistics().localDiskHitCount()).isEqualTo(untilCount - maxElementsInMemory);
		assertThat(store.getStatistics().cacheHitCount()).isEqualTo(untilCount);
		store.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHitRatioWhenReadAfterRestart() {
		int maxElementsInMemory = 3;
		int untilCount = 5;
		
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) {
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int readCount = 0; readCount < untilCount; readCount ++) { 
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
		}
		
		assertThat(afterRestartStore.getStatistics().localDiskHitCount()).isEqualTo(untilCount);
		assertThat(afterRestartStore.getStatistics().cacheHitCount()).isEqualTo(untilCount);
		afterRestartStore.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHistoryWhenNoGetInDisk() throws InterruptedException {
		int maxElementsInMemory = 3;
		int samplingTime = 100;
		
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < maxElementsInMemory; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		
		Operation<GetOutcome> diskGet = store.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(10, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < maxElementsInMemory; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(store.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
		}
		Thread.sleep(samplingTime + 10);
		
		try {
			assertThat(diskGetStats.history()).hasSize(1);
			assertThat(diskGetStats.history().get(0).getSample()).isEqualTo(0);
		} finally {
			store.getCacheManager().shutdown();
		}
	}
	
	@Test
	public void statsDiskHistoryWhenGetsFitInOneSample() throws InterruptedException {
		int maxElementsInMemory = 3;
		int untilCount = 5;
		int samplingTime = 100;
		
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(10, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(10);
		}
		Thread.sleep(samplingTime / 2);
		
		List<Timestamped<Long>> history = diskGetStats.history();
		assertThat(history).hasSize(1);
		assertThat(history.get(0).getSample()).isEqualTo(untilCount);
		afterRestartStore.getCacheManager().shutdown();
	}
	
	@Test
	public void statsDiskHistoryWhenGetsFitInThreeSample() throws InterruptedException {
		int maxElementsInMemory = 3;
		int untilCount = 20;
		int samplingTime = 100;
		
		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(10, samplingTime, TimeUnit.MILLISECONDS);
		diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(11);
		}
		Thread.sleep(samplingTime);
		
		try {
			List<Timestamped<Long>> history = diskGetStats.history();
			assertThat(history).hasSize(3);
			assertThat(history.get(0).getSample()).isLessThan(10);
			assertThat(history.get(1).getSample()).isLessThan(untilCount);
			assertThat(history.get(2).getSample()).isEqualTo(untilCount);
		} finally {
			afterRestartStore.getCacheManager().shutdown();
		}
	}
	
	@Test
	public void statsDiskHistoryWhenOneDiskHitBySampleTime() throws InterruptedException {
		int maxElementsInMemory = 3;
		int untilCount = 5;
		int samplingTime = 100;

		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(10, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(samplingTime);
		}
		
		try {
			assertThat(diskGetStats.history().get(0).getSample()).isEqualTo(1);
			assertThat(diskGetStats.history().get(1).getSample()).isEqualTo(2);
			assertThat(diskGetStats.history().get(2).getSample()).isEqualTo(3);
			assertThat(diskGetStats.history().get(3).getSample()).isEqualTo(4);
			assertThat(diskGetStats.history().get(4).getSample()).isEqualTo(5);
		} finally {
			afterRestartStore.getCacheManager().shutdown();
		}
	}
	
	@Test
	public void statsDiskHistoryWhenHistorySizeIsReached() throws InterruptedException {
		int maxElementsInMemory = 3;
		int untilCount = 5;
		int samplingTime = 100;
		int historySize = 3;

		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(historySize, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(samplingTime);
		}
		
		try {
			assertThat(diskGetStats.history().get(0).getSample()).isEqualTo(3);
			assertThat(diskGetStats.history().get(1).getSample()).isEqualTo(4);
			assertThat(diskGetStats.history().get(2).getSample()).isEqualTo(5);
		} finally {
			afterRestartStore.getCacheManager().shutdown();
		}
	}
	
	@Test
	public void statsDiskHistoryWhenNoDiskReadInLastSample() throws InterruptedException {
		int maxElementsInMemory = 5;
		int samplingTime = 100;
		int historySize = 3;

		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < maxElementsInMemory; writeCount ++) { 
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(historySize, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < maxElementsInMemory; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(samplingTime);
		}
		afterRestartStore.get("a0");
		Thread.sleep(samplingTime);
		assertThat(diskGetStats.history().size()).isEqualTo(historySize);
		
		try {
			assertThat(diskGetStats.history().get(0).getSample()).isEqualTo(maxElementsInMemory - 1);
			assertThat(diskGetStats.history().get(1).getSample()).isEqualTo(maxElementsInMemory);
			assertThat(diskGetStats.history().get(2).getSample()).isEqualTo(maxElementsInMemory);
		} finally {
			afterRestartStore.getCacheManager().shutdown();
		}
	}
	
	@Ignore
	@Test
	public void statsDiskHistoryWhenTimeToDisableIsReached() throws InterruptedException {
		int maxElementsInMemory = 3;
		int untilCount = 5;
		int samplingTime = 100;
		int historySize = 3;

		Cache store = storeAcceptingXElementsInMemory(maxElementsInMemory);
		for (int writeCount = 0; writeCount < untilCount; writeCount ++) {
			store.put(new Element("a" + writeCount, "b" + writeCount));
		}
		store.getCacheManager().shutdown();
		
		Cache afterRestartStore = storeAcceptingXElementsInMemory(maxElementsInMemory);
		Operation<GetOutcome> diskGet = afterRestartStore.getStatistics().getExtended().diskGet();
		afterRestartStore.getStatistics().setStatisticsTimeToDisable(samplingTime, TimeUnit.MILLISECONDS);
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGet.setHistory(historySize, samplingTime, TimeUnit.MILLISECONDS);
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			diskGetStats.history(); // TOUCH TO CONTINUE RECORDING
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
			Thread.sleep(samplingTime);
		}
		afterRestartStore.get("a0");
		Thread.sleep(samplingTime);
		assertThat(diskGetStats.history().size()).isEqualTo(historySize);
		
		try {
			assertThat(diskGetStats.history().get(0).getSample()).isEqualTo(untilCount - 1);
			assertThat(diskGetStats.history().get(1).getSample()).isEqualTo(untilCount);
			assertThat(diskGetStats.history().get(2).getSample()).isEqualTo(untilCount);
		} finally {
			afterRestartStore.getCacheManager().shutdown();
		}
	}

	@SuppressWarnings("deprecation")
	private Cache storeAcceptingXElementsInMemory(int maxElementsInMemory) {
		return new CacheManager(new Configuration()
			.name("manager")
			.diskStore(new DiskStoreConfiguration().path(configurationService.getDataDirectory()))
			.updateCheck(false)
			.cache(new CacheConfiguration()
				.name("storeName")
				.maxEntriesLocalHeap(maxElementsInMemory)
				.overflowToDisk(true)
				.diskPersistent(true)
				.eternal(true)))
			.getCache("storeName");
	}
}
