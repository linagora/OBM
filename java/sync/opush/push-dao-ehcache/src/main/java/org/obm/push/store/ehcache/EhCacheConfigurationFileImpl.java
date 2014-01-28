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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.TimeUnitMapper;
import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

public class EhCacheConfigurationFileImpl implements EhCacheConfiguration {

	public final static int DEFAULT_MAX_MEMORY_IN_MB = JvmUtils.maxRuntimeJvmMemoryInMB() / 2;
	public final static ImmutableMap<String, Integer> DEFAULT_PERCENTAGE_ALLOW_TO_CACHES = ImmutableMap.<String, Integer> builder()
			.put(EhCacheStores.MAIL_SNAPSHOT_STORE, 30)
			.put(EhCacheStores.MONITORED_COLLECTION_STORE, 5)
			.put(EhCacheStores.SYNCED_COLLECTION_STORE, 5)
			.put(EhCacheStores.UNSYNCHRONIZED_ITEM_STORE, 25)
			.put(EhCacheStores.MAIL_WINDOWING_INDEX_STORE, 5)
			.put(EhCacheStores.MAIL_WINDOWING_CHUNKS_STORE, 25)
			.put(EhCacheStores.SYNC_KEYS_STORE, 5)
			.build();
	public final static String TIME_TO_LIVE_UNIT = "timeToLiveUnit";
	public final static String TIME_TO_LIVE = "timeToLive";
	public final static int DEFAULT_TIME_TO_LIVE = Ints.checkedCast(TimeUnit.DAYS.toSeconds(30));
	public final static TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.XA;
	public final static int DEFAULT_STATS_SAMPLE_COUNT = 180;
	public final static int DEFAULT_STATS_SHORT_SAMPLE_TIME = 1;
	public final static int DEFAULT_STATS_MEDIUM_SAMPLE_TIME = 10;
	public final static int DEFAULT_STATS_LONG_SAMPLE_TIME = 60;
	public final static int DEFAULT_STATS_SAMPLE_TIME_STOP = 10;

	public static final String CONFIG_FILE_PATH = "/etc/opush/ehcache_conf.ini";
	private final IniFile iniFile;
	private final TimeUnitMapper timeUnitMapper;
	
	@Inject
	@VisibleForTesting
	protected EhCacheConfigurationFileImpl(IniFile.Factory factory, TimeUnitMapper timeUnitMapper) {
		iniFile = factory.build(CONFIG_FILE_PATH);
		this.timeUnitMapper = timeUnitMapper;
	}
	
	@Override
	public int maxMemoryInMB() {
		int value = iniFile.getIntValue("maxMemoryInMB", -1);
		if (value > JvmUtils.maxRuntimeJvmMemoryInMB()) {
			throw new IllegalStateException("maxMemoryInMB is higher than JVM Xmx value");
		}
		if (value > 0) {
			return value;
		}
		return DEFAULT_MAX_MEMORY_IN_MB;
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		Integer value = iniFile.getIntegerValue(cacheName, null);
		if (value == null || value == 0) {
			return Percentage.of(DEFAULT_PERCENTAGE_ALLOW_TO_CACHES.get(cacheName));
		}
		return Percentage.of(value);
	}

	@Override
	public long timeToLiveInSeconds() {
		TimeUnit timeToLiveUnit = getTimeToLiveUnit();
		long timeToLiveInSeconds = timeToLiveUnit.toSeconds(getTimeToLive());
		return Ints.checkedCast(timeToLiveInSeconds);
	}

	private TimeUnit getTimeToLiveUnit() {
		return timeUnitMapper.getTimeUnitOrDefault(iniFile.getStringValue(TIME_TO_LIVE_UNIT), TimeUnit.SECONDS);
	}

	private int getTimeToLive() {
		return iniFile.getIntValue(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE);
	}

	@Override
	public TransactionalMode transactionalMode() {
		return TRANSACTIONAL_MODE;
	}
	
	@Override
	public int statsSampleToRecordCount() {
		return iniFile.getIntValue("statsSampleToRecordCount", DEFAULT_STATS_SAMPLE_COUNT);
	}

	@Override
	public int statsSamplingTimeStopInMinutes() {
		return iniFile.getIntValue("statsSamplingTimeStopInMinutes", DEFAULT_STATS_SAMPLE_TIME_STOP);
	}

	@Override
	public int statsShortSamplingTimeInSeconds() {
		return iniFile.getIntValue("statsShortSamplingTimeInSeconds", DEFAULT_STATS_SHORT_SAMPLE_TIME);
	}

	@Override
	public int statsMediumSamplingTimeInSeconds() {
		return iniFile.getIntValue("statsMediumSamplingTimeInSeconds", DEFAULT_STATS_MEDIUM_SAMPLE_TIME);
	}

	@Override
	public int statsLongSamplingTimeInSeconds() {
		return iniFile.getIntValue("statsLongSamplingTimeInSeconds", DEFAULT_STATS_LONG_SAMPLE_TIME);
	}

	@Override
	public Map<String, Percentage> percentageAllowedToCaches() {
		return Maps.toMap(EhCacheStores.STORES, 
			new Function<String, Percentage>() {
		
				@Override
				public Percentage apply(String name) {
					return loadStorePercentage(name);
				}
		});
	}

	private Percentage loadStorePercentage(String name) {
		Percentage percentageAllowedToCache = percentageAllowedToCache(name);
		if (percentageAllowedToCache.isDefined()) {
			return percentageAllowedToCache;
		}
		throw new IllegalArgumentException("Store " + name + " has no percentage defined in configuration");
	}
}
