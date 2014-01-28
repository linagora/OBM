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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.utils.IniFile;
import org.obm.configuration.utils.IniFile.Factory;
import org.obm.configuration.utils.TimeUnitMapper;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.obm.push.utils.jvm.JvmUtils;


public class EhCacheConfigurationFileImplTest {

	private IMocksControl control;
	private IniFile iniFile;
	private Factory factory;
	private TimeUnitMapper timeUnitMapper;

	@Before
	public void setup() {
		control = createControl();
		iniFile = control.createMock(IniFile.class);
		factory = control.createMock(IniFile.Factory.class);
		timeUnitMapper = control.createMock(TimeUnitMapper.class);
		expect(factory.build("/etc/opush/ehcache_conf.ini")).andReturn(iniFile);
	}

	private EhCacheConfigurationFileImpl testee() {
		return new EhCacheConfigurationFileImpl(factory, timeUnitMapper);
	}

	@Test
	public void testMaxMemoryInMBWhenZero() {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(0);
		
		control.replay();
		assertThat(testee().maxMemoryInMB()).isEqualTo(JvmUtils.maxRuntimeJvmMemoryInMB() / 2);
		control.verify();
	}

	@Test
	public void testMaxMemoryInMBWhenNegative() {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(-1);
		
		control.replay();
		assertThat(testee().maxMemoryInMB()).isEqualTo(JvmUtils.maxRuntimeJvmMemoryInMB() / 2);
		control.verify();
	}

	@Test(expected=IllegalStateException.class)
	public void testMaxMemoryInMBWhenHigherThanXmx() throws Exception {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(Integer.MAX_VALUE);
		
		control.replay();
		try {
			testee().maxMemoryInMB();
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test
	public void testMaxMemoryInMB() {
		expect(iniFile.getIntValue("maxMemoryInMB", -1)).andReturn(150);
		
		control.replay();
		assertThat(testee().maxMemoryInMB()).isEqualTo(150);
		control.verify();
	}

	@Test
	public void testPercentageWhenUndefined() {
		expect(iniFile.getIntegerValue("mailSnapshotStore", null)).andReturn(null);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("mailSnapshotStore")).isEqualTo(Percentage.of(30));
		control.verify();
	}

	@Test
	public void testPercentageWhenOne() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(1);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.of(1));
		control.verify();
	}

	@Test
	public void testPercentageWhenOneHundred() {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(100);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("cacheName")).isEqualTo(Percentage.of(100));
		control.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPercentageWhenMoreThanOneHundred() throws Exception {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(101);
		
		control.replay();
		try {
			testee().percentageAllowedToCache("cacheName");
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPercentageWhenNegative() throws Exception {
		expect(iniFile.getIntegerValue("cacheName", null)).andReturn(-1);
		
		control.replay();
		try {
			testee().percentageAllowedToCache("cacheName");
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test
	public void testPercentageWhenZeroIsDefaultValue() {
		expect(iniFile.getIntegerValue("mailSnapshotStore", null)).andReturn(0);
		
		control.replay();
		assertThat(testee().percentageAllowedToCache("mailSnapshotStore")).isEqualTo(Percentage.of(30));
		control.verify();
	}
	
	@Test
	public void testTimeToLiveInSeconds() {
		expect(iniFile.getIntValue("timeToLive", EhCacheConfigurationFileImpl.DEFAULT_TIME_TO_LIVE)).andReturn(100);
		expect(iniFile.getStringValue(EhCacheConfigurationFileImpl.TIME_TO_LIVE_UNIT)).andReturn("minutes");
		expect(timeUnitMapper.getTimeUnitOrDefault("minutes", TimeUnit.SECONDS)).andReturn(TimeUnit.MINUTES);
		control.replay();
		
		assertThat(testee().timeToLiveInSeconds()).isEqualTo(100 * 60);
		control.verify();
	}
	
	@Test
	public void testTimeToLiveInSecondsWithDefaultValue() {
		expect(iniFile.getIntValue("timeToLive", EhCacheConfigurationFileImpl.DEFAULT_TIME_TO_LIVE)).andReturn(EhCacheConfigurationFileImpl.DEFAULT_TIME_TO_LIVE);
		expect(iniFile.getStringValue(EhCacheConfigurationFileImpl.TIME_TO_LIVE_UNIT)).andReturn("seconds");
		expect(timeUnitMapper.getTimeUnitOrDefault("seconds", TimeUnit.SECONDS)).andReturn(TimeUnit.SECONDS);
		control.replay();
		
		assertThat(testee().timeToLiveInSeconds()).isEqualTo(EhCacheConfigurationFileImpl.DEFAULT_TIME_TO_LIVE);
		control.verify();
	}
	
	@Test
	public void testTimeToLiveInSecondsWithDefaultTimeUnit() {
		expect(iniFile.getIntValue("timeToLive", EhCacheConfigurationFileImpl.DEFAULT_TIME_TO_LIVE)).andReturn(100);
		expect(iniFile.getStringValue(EhCacheConfigurationFileImpl.TIME_TO_LIVE_UNIT)).andReturn(null);
		expect(timeUnitMapper.getTimeUnitOrDefault(null, TimeUnit.SECONDS)).andReturn(TimeUnit.SECONDS);
		control.replay();
		
		assertThat(testee().timeToLiveInSeconds()).isEqualTo(100);
		control.verify();
	}

	@Test
	public void testStatsSampleToRecordCount() {
		expect(iniFile.getIntValue("statsSampleToRecordCount", 180)).andReturn(5);

		control.replay();
		assertThat(testee().statsSampleToRecordCount()).isEqualTo(5);
		control.verify();
	}

	@Test
	public void testStatsShortSamplingTimeInSeconds() {
		expect(iniFile.getIntValue("statsShortSamplingTimeInSeconds", 1)).andReturn(10);

		control.replay();
		assertThat(testee().statsShortSamplingTimeInSeconds()).isEqualTo(10);
		control.verify();
	}

	@Test
	public void testStatsMediumSamplingTimeInSeconds() {
		expect(iniFile.getIntValue("statsMediumSamplingTimeInSeconds", 10)).andReturn(100);

		control.replay();
		assertThat(testee().statsMediumSamplingTimeInSeconds()).isEqualTo(100);
		control.verify();
	}

	@Test
	public void testStatsLongSamplingTimeInSeconds() {
		expect(iniFile.getIntValue("statsLongSamplingTimeInSeconds", 60)).andReturn(100);

		control.replay();
		assertThat(testee().statsLongSamplingTimeInSeconds()).isEqualTo(100);
		control.verify();
	}
	
	@Test
	public void testStatsSamplingTimeStopInMinutes() {
		expect(iniFile.getIntValue("statsSamplingTimeStopInMinutes", 10)).andReturn(5);
		
		control.replay();
		assertThat(testee().statsSamplingTimeStopInMinutes()).isEqualTo(5);
		control.verify();
	}
}
