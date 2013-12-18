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
import java.util.List;
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
import org.obm.transaction.TransactionManagerRule;
import org.slf4j.Logger;
import org.terracotta.statistics.archive.Timestamped;

import bitronix.tm.BitronixTransactionManager;

@Ignore("OBMFULL-5663")
@Slow
@RunWith(SlowFilterRunner.class)
public class EhCacheSettingsTimeDependentTest extends StoreManagerConfigurationTest {

	private ConfigurationService configurationService;
	private Logger logger;
	private BitronixTransactionManager tm;

	
	@Before
	public void init() throws IOException {
		logger = createNiceMock(Logger.class);
		configurationService = super.mockConfigurationService();

		tm = TransactionManagerRule.setupTransactionManager(temporaryFolder);
	}

	@After
	public void teardown() {
		tm.shutdown();
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
		diskGet.setHistory(10, samplingTime, TimeUnit.MILLISECONDS);
		Statistic<Long> diskGetStats = diskGet.component(GetOutcome.HIT).count();
		diskGetStats.history(); // TOUCH TO START RECORDING
		for (int readCount = 0; readCount < untilCount; readCount ++) {
			assertThat(afterRestartStore.get("a" + readCount).getObjectValue()).isEqualTo("b" + readCount);
		}
		Thread.sleep(samplingTime);
		
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
