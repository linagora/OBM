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

import static org.easymock.EasyMock.createMock;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.store.ehcache.EhCacheStatisticsImpl.History;
import org.terracotta.statistics.archive.Timestamped;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class EhCacheStatisticsImplMockTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager cacheManager;
	private TestingEhCacheConfiguration config;
	private EhCacheStatisticsImpl testee;
	
	@Before
	public void init() {
		cacheManager = createMock(ObjectStoreManager.class);
		config = new TestingEhCacheConfiguration();
		testee = new EhCacheStatisticsImpl(config, cacheManager);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testComputeSamplesDiffWhenNotEnoughSampleOfOne() {
		History diskStatsHistory = new History(ImmutableList.<Timestamped<Long>>of());
		testee.computeSamplesDiff(diskStatsHistory, 1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testComputeSamplesDiffWhenNotEnoughSampleOfFive() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(1), timestamp(2), timestamp(5), timestamp(9)));
		testee.computeSamplesDiff(diskStatsHistory, 5);
	}

	@Test
	public void testComputeSamplesDiffWhenJustEnoughSampleOfOne() {
		History diskStatsHistory = new History(ImmutableList.of(timestamp(10)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(10);
	}
	
	@Test
	public void testComputeSamplesDiffWhenJustEnoughSampleOf5() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(1), timestamp(2), timestamp(5), timestamp(9), timestamp(13)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 5)).isEqualTo(13);
	}
	
	@Test
	public void testComputeSamplesDiffWhenMoreSamplesOfOne() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(1), timestamp(2), timestamp(5), timestamp(9), timestamp(13)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(4);
	}
	
	@Test
	public void testComputeSamplesDiffWhenMoreSamplesOfFour() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(1), timestamp(2), timestamp(5), timestamp(9), timestamp(13)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 4)).isEqualTo(12);
	}
	
	@Test
	public void testComputeSamplesDiffWhenNoAccessOfOne() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(10), timestamp(10)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(0);
	}
	
	@Test
	public void testComputeSamplesDiffWhenNoAccessOfThree() {
		History diskStatsHistory = new History(ImmutableList.of(
				timestamp(5), timestamp(10), timestamp(10), timestamp(10), timestamp(10)));
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 3)).isEqualTo(0);
	}

	private Timestamped<Long> timestamp(final long sample) {
		return new Timestamped<Long>() {

			@Override
			public Long getSample() {
				return sample;
			}

			@Override
			public long getTimestamp() {
				return sample;
			}
		};
	}
}
