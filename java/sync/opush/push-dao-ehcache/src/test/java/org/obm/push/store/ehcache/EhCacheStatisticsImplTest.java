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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.TransactionProvider;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

import com.google.common.base.Stopwatch;

@RunWith(SlowFilterRunner.class) @Slow
public class EhCacheStatisticsImplTest {

	@Rule public TemporaryFolder tempFolder =  new TemporaryFolder();

	private static final String STATS_ENABLED_CACHE = ObjectStoreManager.MAIL_SNAPSHOT_STORE;
	
	private Logger logger;
	private ConfigurationService configurationService;
	private TransactionProvider transactionProvider;
	private BitronixTransactionManager transactionManager;
	private ObjectStoreManager cacheManager;


	@Before
	public void init() throws Exception {
		logger = LoggerFactory.getLogger(getClass());
		transactionManager = TransactionManagerServices.getTransactionManager();
		
		IMocksControl control = createControl();
		transactionProvider = control.createMock(TransactionProvider.class);
		expect(transactionProvider.get()).andReturn(transactionManager).anyTimes();
		control.replay();
		
		configurationService = new EhCacheConfigurationService().mock(tempFolder);
	}

	@After
	public void shutdown() {
		transactionManager.shutdown();
	}
	
	@Test(expected=StatisticsNotAvailableException.class)
	public void testShortTimeDiskGetsFirstTriggersNA() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
		
		try {
			transactionManager.commit();
			testee.shortTimeDiskGets(STATS_ENABLED_CACHE);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	@Test(expected=StatisticsNotAvailableException.class)
	public void testShortTimeDiskGetsWhenSampleNotFinishedTriggersNA() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(10));
		
		try {
			transactionManager.commit();
			testee.shortTimeDiskGets(STATS_ENABLED_CACHE);
		} catch(StatisticsNotAvailableException e) {
			// expected
		}
		try {
			testee.shortTimeDiskGets(STATS_ENABLED_CACHE);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenNoAccess() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));

		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetOnly() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetOnNonExistingKey() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		try {
			startStatisticsSampling(testee);
			putXElements(2, testee);
			testee.manager.getStore(STATS_ENABLED_CACHE).get("nonExistingKey");
			transactionManager.commit();
			waitForStatisticsSamples(testee.config.statsShortSamplingTimeInSeconds());
			assertThat(testee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	/*
	 * This test is wrong, only remove should returns 0 disk access.
	 * But EHCache tells that there were 4 GETs and 2 REMOVEs
	 */
	@Test
	public void testShortTimeDiskGetsWhenNoGetButRemove() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
		
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			removeAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenRemoveOnNonExistingKey() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		try {
			startStatisticsSampling(testee);
			putXElements(2, testee);
			testee.manager.getStore(STATS_ENABLED_CACHE).remove("nonExistingKey");
			transactionManager.commit();
			waitForStatisticsSamples(testee.config.statsShortSamplingTimeInSeconds());
			assertThat(testee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetThenRemove() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			removeAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenRemoveThenGet() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			removeAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			transactionManager.begin();
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetThenRemoveInAnotherTransaction() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			transactionManager.begin();
			removeAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}

	@Test
	public void testShortTimeDiskGetsWhenGetAfterUpdate() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			putXElements(2, restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetAfterUpdateInAnotherTransaction() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			
			transactionManager.begin();
			putXElements(2, restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenPutOnly() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		try {
			startStatisticsSampling(testee);
			putXElements(2, testee);
			transactionManager.commit();
			waitForStatisticsSamples(testee.config.statsShortSamplingTimeInSeconds());
			assertThat(testee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenPutThenGet() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		try {
			startStatisticsSampling(testee);
			putXElements(5, testee);
			readAllElementsInCache(testee);
			transactionManager.commit();
			waitForStatisticsSamples(testee.config.statsShortSamplingTimeInSeconds());
			assertThat(testee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			testee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenGetOnDiskAndInHeap() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));

		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			putXElements(3, 5, restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsWhenAccessesAreOutOfScope() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			readAllElementsInCache(restartedTestee);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}

	@Test
	public void testLongTimeDiskGetsWhenAccessDuringManySample() throws Exception {
		int totalReadTimeInSeconds = 3;
		int waitBetweenReadsInMs = 500;

		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1)
			.withStatsLongSamplingTimeInSeconds(totalReadTimeInSeconds));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(5, testee);
		
		try {
			startStatisticsSampling(restartedTestee);
			Thread.sleep(100); // First read must be in first sample
			readAllElementsInCacheWithWait(restartedTestee, waitBetweenReadsInMs);
			transactionManager.commit();
			Thread.sleep(waitBetweenReadsInMs); // Wait the last sample to be finished
			assertThat(restartedTestee.longTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(2);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testLongTimeDiskGetsWhenAccessDuringManySampleRoundDown() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1)
			.withStatsLongSamplingTimeInSeconds(3));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(5, testee);
		
		try {
			Cache store = restartedTestee.manager.getStore(STATS_ENABLED_CACHE);

			startStatisticsSampling(restartedTestee);
			Thread.sleep(100); // First read must be in first sample
			store.get(store.getKeys().get(0));
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.longTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testLongTimeDiskGetsWhenAccessDuringManySampleRoundUp() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1)
			.withStatsLongSamplingTimeInSeconds(3));
			
		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(5, testee);
		
		try {
			Cache store = restartedTestee.manager.getStore(STATS_ENABLED_CACHE);

			startStatisticsSampling(restartedTestee);
			Thread.sleep(100); // First read must be in first sample
			store.get(store.getKeys().get(0));
			store.get(store.getKeys().get(1));
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.longTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(1);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	/*
	 * Using EHCache statistics, a PUT always performs a disk
	 * GET (even if there is enough free memory available).
	 * 
	 * The GET is took in stats as soon as the PUT is performed.
	 * The PUT is took in stats only at the commit time.
	 * 
	 * If a sample finish during a commit, all GETs are took in stats
	 * but not all PUTs so the sample is wrong. 
	 * Compute samples between a right one and a wrong one can give a 
	 * negative diff value that we don't want to show because it's not the reality. 
	 */
	@Test
	public void testShortTimeDiskGetsWhenSampleFinishDuringCommit() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1));

		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			Cache store = restartedTestee.manager.getStore(STATS_ENABLED_CACHE);

			startStatisticsSampling(restartedTestee);
			Stopwatch watch = Stopwatch.createStarted();
			Thread.sleep(100); // First read must be in first sample
			
			int putIndex = 0;
			while (watch.elapsed(TimeUnit.MILLISECONDS) < 900) {
				store.put(new Element("a" + putIndex, "b" + putIndex));
				putIndex++;
			}
			Thread.sleep(100);
			transactionManager.commit();
			waitForStatisticsSamples(restartedTestee.config.statsShortSamplingTimeInSeconds());
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}
	
	@Test
	public void testShortTimeDiskGetsDoesNotCountHitForTTL() throws Exception {
		EhCacheStatisticsImpl testee = testeeWithConfig(new TestingEhCacheConfiguration()
			.withMaxMemoryInMB(10)
			.withStatsShortSamplingTimeInSeconds(1)
			.withTimeToLive(1));

		EhCacheStatisticsImpl restartedTestee = putXElementsThenRestartWithSameConfig(2, testee);
		
		try {
			Cache store = restartedTestee.manager.getStore(STATS_ENABLED_CACHE);
			startStatisticsSampling(restartedTestee);

			Element element = new Element("a", "b");
			store.put(element);
			Thread.sleep(TimeUnit.SECONDS.toMillis(2));
			assertThat(store.get(element.getObjectKey())).isNull();

			transactionManager.commit();
			assertThat(restartedTestee.shortTimeDiskGets(STATS_ENABLED_CACHE)).isEqualTo(0);
		} finally {
			restartedTestee.manager.shutdown();
		}
	}

	private void readAllElementsInCacheWithWait(EhCacheStatisticsImpl testee, int waitInMs) throws Exception {
		Cache cache = testee.manager.getStore(STATS_ENABLED_CACHE);
		for (Object key : cache.getKeys()) {
			cache.get(key);
			Thread.sleep(waitInMs);
		}
	}
	
	private void readAllElementsInCache(EhCacheStatisticsImpl testee) {
		Cache cache = testee.manager.getStore(STATS_ENABLED_CACHE);
		for (Object key : cache.getKeys()) {
			cache.get(key);
		}
	}
	
	private void removeAllElementsInCache(EhCacheStatisticsImpl testee) {
		testee.manager.getStore(STATS_ENABLED_CACHE).removeAll();
	}

	private EhCacheStatisticsImpl putXElementsThenRestartWithSameConfig(int elementsCount, EhCacheStatisticsImpl testee)
			throws Exception {
		
		EhCacheConfiguration previousConfigReference = testee.config;
		putXElements(elementsCount, testee);
		transactionManager.commit();
		testee.manager.shutdown();

		return testeeWithConfig(previousConfigReference);
	}

	private void putXElements(int elementsCount, EhCacheStatisticsImpl testee) {
		putXElements(0, elementsCount, testee);
	}

	private void putXElements(int startIndex, int elementsCount, EhCacheStatisticsImpl testee) {
		int putUntil = elementsCount + startIndex;
		for (int putCount = startIndex; putCount < putUntil; putCount++) {
			testee.manager.getStore(STATS_ENABLED_CACHE).put(new Element("a" + putCount, "b" + putCount));
		}
	}

	private void startStatisticsSampling(EhCacheStatisticsImpl testee) {
		try {
			testee.shortTimeDiskGets(STATS_ENABLED_CACHE);
		} catch(StatisticsNotAvailableException e) {
			// expected
		}
	}

	private void waitForStatisticsSamples(int samplingTimeInSeconds) throws Exception {
		Thread.sleep(TimeUnit.SECONDS.toMillis(samplingTimeInSeconds) + 20);
	}

	private EhCacheStatisticsImpl testeeWithConfig(EhCacheConfiguration config) throws Exception {
		transactionManager.begin();
		cacheManager = new ObjectStoreManager(configurationService, config, logger, transactionProvider);
		return new EhCacheStatisticsImpl(config, cacheManager);
	}
}
