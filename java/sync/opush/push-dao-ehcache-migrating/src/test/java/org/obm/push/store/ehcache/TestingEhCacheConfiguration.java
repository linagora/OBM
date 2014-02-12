package org.obm.push.store.ehcache;
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


import java.util.Map;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.push.utils.ShareAmount;
import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class TestingEhCacheConfiguration implements EhCacheConfiguration {

	private int maxMemoryInMB;
	private Integer percentageAllowedToCache;
	private long timeToLive;
	private int statsShortSamplingTimeInSeconds;
	private int statsMediumSamplingTimeInSeconds;
	private int statsLongSamplingTimeInSeconds;
	private Map<String, Percentage> stores = Maps.newHashMap();
	private TransactionalMode transactionalMode;

	public TestingEhCacheConfiguration() {
		percentageAllowedToCache = null;
		maxMemoryInMB = JvmUtils.maxRuntimeJvmMemoryInMB() / 2;
		timeToLive = 60;
		statsShortSamplingTimeInSeconds = 1;
		stores = Maps.transformValues(
				ShareAmount.forEntries(EhCacheStores.STORES).amount(100),
				new Function<Integer, Percentage>() {
					@Override
					public Percentage apply(Integer input) {
						return Percentage.of(input);
					}
				});
		transactionalMode = TransactionalMode.XA;
	}
	
	public TestingEhCacheConfiguration withPercentageAllowedToCache(Integer percentageAllowedToCache) {
		this.percentageAllowedToCache = percentageAllowedToCache;
		return this;
	}
	
	public TestingEhCacheConfiguration withMaxMemoryInMB(int maxMemoryInMB) {
		this.maxMemoryInMB = maxMemoryInMB;
		return this;
	}

	public TestingEhCacheConfiguration withTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
		return this;
	}
	
	public TestingEhCacheConfiguration withStatsShortSamplingTimeInSeconds(int statsShortSamplingTimeInSeconds) {
		this.statsShortSamplingTimeInSeconds = statsShortSamplingTimeInSeconds;
		return this;
	}

	public TestingEhCacheConfiguration withStatsMediumSamplingTimeInSeconds(int statsMediumSamplingTimeInSeconds) {
		this.statsMediumSamplingTimeInSeconds = statsMediumSamplingTimeInSeconds;
		return this;
	}
	
	public TestingEhCacheConfiguration withStatsLongSamplingTimeInSeconds(int statsLongSamplingTimeInSeconds) {
		this.statsLongSamplingTimeInSeconds = statsLongSamplingTimeInSeconds;
		return this;
	}

	public TestingEhCacheConfiguration withTransactionMode(TransactionalMode mode) {
		this.transactionalMode = mode;
		return this;
	}

	public Map<String, Percentage> getStores() {
		return stores;
	}
	
	@Override
	public int maxMemoryInMB() {
		return maxMemoryInMB;
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		Percentage defaultValue = stores.get(cacheName);
		if (defaultValue != null) {
			return defaultValue;
		}
		
		if (percentageAllowedToCache == null) {
			return Percentage.UNDEFINED;
		}
		return Percentage.of(percentageAllowedToCache);
	}
	
	@Override
	public Map<String, Percentage> percentageAllowedToCaches() {
		return stores;
	}

	@Override
	public long timeToLiveInSeconds() {
		return timeToLive;
	}

	@Override
	public TransactionalMode transactionalMode() {
		return transactionalMode;
	}
	
	@Override
	public int statsSampleToRecordCount() {
		return 10;
	}

	@Override
	public int statsShortSamplingTimeInSeconds() {
		return statsShortSamplingTimeInSeconds;
	}
	
	@Override
	public int statsMediumSamplingTimeInSeconds() {
		return statsMediumSamplingTimeInSeconds;
	}
	
	@Override
	public int statsLongSamplingTimeInSeconds() {
		return statsLongSamplingTimeInSeconds;
	}

	@Override
	public int statsSamplingTimeStopInMinutes() {
		return 10;
	}

}
