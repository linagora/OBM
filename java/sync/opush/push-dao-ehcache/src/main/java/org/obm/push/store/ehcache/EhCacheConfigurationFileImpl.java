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

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.TimeUnitMapper;
import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

public class EhCacheConfigurationFileImpl implements EhCacheConfiguration {

	public final static String TIME_TO_LIVE_UNIT = "timeToLiveUnit";
	public final static String TIME_TO_LIVE = "timeToLive";
	public final static int DEFAULT_TIME_TO_LIVE = Ints.checkedCast(TimeUnit.DAYS.toSeconds(30));
	public final static TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.XA;
	public final static int DEFAULT_STATS_SAMPLE_COUNT = 10;
	public final static int DEFAULT_STATS_SHORT_SAMPLE_TIME = 1;
	public final static int DEFAULT_STATS_MEDIUM_SAMPLE_TIME = 10;
	public final static int DEFAULT_STATS_LONG_SAMPLE_TIME = 60;
	public final static int DEFAULT_STATS_SAMPLE_TIME_STOP = 10;

	private static final String configFilePath = "/etc/opush/ehcache_conf.ini";
	private final IniFile iniFile;
	private final TimeUnitMapper timeUnitMapper;
	
	@Inject
	@VisibleForTesting
	protected EhCacheConfigurationFileImpl(IniFile.Factory factory, TimeUnitMapper timeUnitMapper) {
		iniFile = factory.build(configFilePath);
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
		throw new IllegalStateException("illegal maxMemoryInMB value");
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		Integer value = iniFile.getIntegerValue(cacheName, null);
		if (value == null || value == 0) {
			return Percentage.UNDEFINED;
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
}
