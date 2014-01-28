/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.statistics.extended.ExtendedStatistics;
import net.sf.ehcache.statistics.extended.ExtendedStatistics.Result;
import net.sf.ehcache.store.StoreOperationOutcomes.GetOutcome;
import net.sf.ehcache.store.StoreOperationOutcomes.PutOutcome;
import net.sf.ehcache.store.StoreOperationOutcomes.RemoveOutcome;

import org.terracotta.statistics.archive.Timestamped;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EhCacheStatisticsImpl implements EhCacheStatistics {

	@VisibleForTesting final StoreManager manager;
	@VisibleForTesting final EhCacheConfiguration config;

	@Inject
	@VisibleForTesting EhCacheStatisticsImpl(EhCacheConfiguration config, StoreManager manager) {
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
		BigDecimal diskGetCount = BigDecimal.valueOf(lastSampleCount - referenceSampleCount);
		long diskGetAverage = diskGetCount.divide(BigDecimal.valueOf(samplingTimeInSeconds), RoundingMode.HALF_UP).longValue();
		return fixDuringCommitSample(diskGetAverage);
	}

	private long fixDuringCommitSample(long diskGetAverage) {
		return Math.max(diskGetAverage, 0);
	}

	private History getCacheDiskStatsHistory(String cacheName) {
		ExtendedStatistics cacheStats = getCacheOrException(cacheName).getStatistics().getExtended();
		Result diskGetStats = cacheStats.diskGet().component(GetOutcome.HIT);
		Result diskPutStats = cacheStats.diskPut().compound(ImmutableSet.of(PutOutcome.ADDED, PutOutcome.UPDATED));
		Result diskRemoveStats = cacheStats.diskRemove().component(RemoveOutcome.SUCCESS);
		
		return History.builder()
				.gets(diskGetStats.count().history())
				.puts(diskPutStats.count().history())
				.removes(diskRemoveStats.count().history())
				.build();
	}

	private Cache getCacheOrException(String storeName) {
		Cache cache = manager.getStore(storeName);
		if (cache == null) {
			throw new IllegalArgumentException("No store found for: " + storeName);
		}
		return cache;
	}
	
	public static class History {
		
		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {
			
			private final ImmutableList.Builder<Timestamped<Long>> gets;
			private final ImmutableList.Builder<Timestamped<Long>> puts;
			private final ImmutableList.Builder<Timestamped<Long>> removes;
			
			public Builder() {
				gets = ImmutableList.builder();
				puts = ImmutableList.builder();
				removes = ImmutableList.builder();
			}
			
			public Builder gets(List<Timestamped<Long>> getHistory) {
				this.gets.addAll(getHistory);
				return this;
			}
			public Builder gets(Timestamped<Long>...getHistory) {
				this.gets.addAll(Iterators.forArray(getHistory));
				return this;
			}
			
			public Builder puts(List<Timestamped<Long>> putHistory) {
				this.puts.addAll(putHistory);
				return this;
			}

			public Builder puts(Timestamped<Long>...putHistory) {
				this.puts.addAll(Iterators.forArray(putHistory));
				return this;
			}
			
			public Builder removes(List<Timestamped<Long>> removeHistory) {
				this.removes.addAll(removeHistory);
				return this;
			}
			
			public Builder removes(Timestamped<Long>...removeHistory) {
				this.removes.addAll(Iterators.forArray(removeHistory));
				return this;
			}
			
			public History build() {
				ImmutableList<Timestamped<Long>> getHistory = gets.build();
				ImmutableList<Timestamped<Long>> putHistory = puts.build();
				ImmutableList<Timestamped<Long>> removeHistory = removes.build();
				Preconditions.checkState(
						getHistory.size() == putHistory.size() &&
						getHistory.size() == removeHistory.size(), "Histories have different size");
				return new History(getHistory, putHistory, removeHistory);
			}
		}
		
		private final ImmutableList<Timestamped<Long>> getHistory;
		private final ImmutableList<Timestamped<Long>> putHistory;
		private final ImmutableList<Timestamped<Long>> removeHistory;

		private History(
				ImmutableList<Timestamped<Long>> getHistory, 
				ImmutableList<Timestamped<Long>> putHistory, 
				ImmutableList<Timestamped<Long>> removeHistory) {
			this.getHistory = getHistory;
			this.putHistory = putHistory;
			this.removeHistory = removeHistory;
		}

		public long getLastSampleCount() {
			Long getSample = getHistory.get(lastIndexOf(getHistory)).getSample();
			Long putSample = putHistory.get(lastIndexOf(putHistory)).getSample();
			Long removeSample = removeHistory.get(lastIndexOf(removeHistory)).getSample();
			
			return getSample - putSample - removeSample;
		}

		public long getReferenceSampleCount(int samplingTimeInSeconds) {
			int getHistoryLastIndex = lastIndexOf(getHistory);
			
			int requiredSampleCount = requiredSampleCount(samplingTimeInSeconds);
			if (getHistoryLastIndex == 0 || requiredSampleCount == getHistory.size()) {
				return 0;
			}
			
			int referenceIndex = getHistoryLastIndex - requiredSampleCount;
			Long getSample = getHistory.get(referenceIndex).getSample();
			Long putSample = putHistory.get(referenceIndex).getSample();
			Long removeSample = removeHistory.get(referenceIndex).getSample();
			
			return getSample - putSample - removeSample;
		}

		public boolean hasEnoughSamples(int samplingTimeInSeconds) {
			return getHistory != null && getHistory.size() >= requiredSampleCount(samplingTimeInSeconds);
		}

		private int lastIndexOf(List<Timestamped<Long>> of) {
			return of.size() - 1;
		}

		private int requiredSampleCount(int samplingTimeInSeconds) {
			return samplingTimeInSeconds * EhCacheConfiguration.STATS_SAMPLING_IN_SECONDS;
		}
		
	}
}
