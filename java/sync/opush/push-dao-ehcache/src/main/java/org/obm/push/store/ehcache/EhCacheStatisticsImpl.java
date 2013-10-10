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

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.statistics.extended.ExtendedStatistics.Statistic;
import net.sf.ehcache.store.StoreOperationOutcomes.GetOutcome;

import org.terracotta.statistics.archive.Timestamped;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EhCacheStatisticsImpl implements EhCacheStatistics {

	@VisibleForTesting final ObjectStoreManager manager;
	@VisibleForTesting final EhCacheConfiguration config;

	@Inject
	@VisibleForTesting EhCacheStatisticsImpl(EhCacheConfiguration config, ObjectStoreManager manager) {
		this.config = config;
		this.manager = manager;
	}

	@Override
	public long memorySizeInBytes(String storeName) {
		return getCacheOrException(storeName).getStatistics().getLocalHeapSizeInBytes();
	}
	
	@Override
	public long shortTimeDiskGets(String storeName) throws StatisticsNotAvailableException {
		return computeDiskGetsOfCache(storeName, config.statsShortSamplingTimeInSeconds());
	}

	@Override
	public long mediumTimeDiskGets(String storeName) throws StatisticsNotAvailableException {
		return computeDiskGetsOfCache(storeName, config.statsMediumSamplingTimeInSeconds());
	}

	@Override
	public long longTimeDiskGets(String storeName) throws StatisticsNotAvailableException {
		return computeDiskGetsOfCache(storeName, config.statsLongSamplingTimeInSeconds());
	}

	private long computeDiskGetsOfCache(String storeName, int samplingTimeInSeconds)
			throws StatisticsNotAvailableException {
		
		History diskStatsHistory = getCacheDiskStatsHistory(storeName);
		if (diskStatsHistory.hasEnoughSamples(samplingTimeInSeconds)) {
			return computeSamplesDiff(diskStatsHistory, samplingTimeInSeconds);
		}
		throw new StatisticsNotAvailableException("Retry in " + samplingTimeInSeconds + " second(s)");
	}

	@VisibleForTesting long computeSamplesDiff(History diskStatsHistory, int samplingTimeInSeconds) {
		long lastSampleCount = diskStatsHistory.getLastSampleCount();
		long referenceSampleCount = diskStatsHistory.getReferenceSampleCount(samplingTimeInSeconds);
		return lastSampleCount - referenceSampleCount;
	}

	private History getCacheDiskStatsHistory(String cacheName) {
		Cache cache = getCacheOrException(cacheName);
		Statistic<Long> diskStats = cache.getStatistics().getExtended().diskGet().component(GetOutcome.HIT).count();
		return new History(diskStats.history());
	}

	private Cache getCacheOrException(String storeName) {
		Cache cache = manager.getStore(storeName);
		if (cache == null) {
			throw new IllegalArgumentException("No store found for: " + storeName);
		}
		return cache;
	}
	
	public static class History {

		private final List<Timestamped<Long>> history;

		public History(List<Timestamped<Long>> history) {
			this.history = history;
		}

		public long getLastSampleCount() {
			return history.get(getLastIndex()).getSample();
		}

		public long getReferenceSampleCount(int samplingTimeInSeconds) {
			int lastIndex = getLastIndex();
			int requiredSampleCount = requiredSampleCount(samplingTimeInSeconds);
			if (lastIndex == 0 || requiredSampleCount == history.size()) {
				return 0;
			}
			return history.get(lastIndex - requiredSampleCount).getSample();
		}

		public boolean hasEnoughSamples(int samplingTimeInSeconds) {
			return history != null && history.size() >= requiredSampleCount(samplingTimeInSeconds);
		}

		private int getLastIndex() {
			return history.size() - 1;
		}

		private int requiredSampleCount(int samplingTimeInSeconds) {
			return samplingTimeInSeconds * EhCacheConfiguration.STATS_SAMPLING_IN_SECONDS;
		}
		
	}
}
