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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.store.ehcache.EhCacheStatisticsImpl.History;
import org.obm.transaction.TransactionManagerRule;
import org.terracotta.statistics.archive.Timestamped;

@RunWith(SlowFilterRunner.class)
public class EhCacheStatisticsImplMockTest {


	@Rule 
	public TransactionManagerRule transactionManagerRule = new TransactionManagerRule();
	
	private ObjectStoreManager cacheManager;
	private TestingEhCacheConfiguration config;
	private EhCacheStatisticsImpl testee;
	
	@Before
	public void init() {
		cacheManager = createMock(ObjectStoreManager.class);
		config = new TestingEhCacheConfiguration();
		testee = new EhCacheStatisticsImpl(config, cacheManager);
	}

	@SuppressWarnings("unchecked")
	@Test(expected=IllegalStateException.class)
	public void testComputeSamplesDiffWhenGetHasDifferentSizeThanPut() {
		History.builder()
			.gets(timestamp(5))
			.puts()
			.removes(timestamp(5))
			.build();
	}

	@SuppressWarnings("unchecked")
	@Test(expected=IllegalStateException.class)
	public void testComputeSamplesDiffWhenGetHasDifferentSizeThanRemove() {
		History.builder()
			.gets(timestamp(5))
			.puts(timestamp(5))
			.removes()
			.build();
	}

	@SuppressWarnings("unchecked")
	@Test(expected=IndexOutOfBoundsException.class)
	public void testComputeSamplesDiffWhenNotEnoughSampleOfOne() {
		History diskStatsHistory = History.builder()
				.gets()
				.puts()
				.removes()
				.build();
		testee.computeSamplesDiff(diskStatsHistory, 1);
	}

	@SuppressWarnings("unchecked")
	@Test(expected=IndexOutOfBoundsException.class)
	public void testComputeSamplesDiffWhenNotEnoughSampleOfFive() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(1), timestamp(2), timestamp(5), timestamp(9))
				.puts(timestamp(1), timestamp(2), timestamp(5), timestamp(9))
				.removes(timestamp(1), timestamp(2), timestamp(5), timestamp(9))
				.build();
		testee.computeSamplesDiff(diskStatsHistory, 5);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenJustEnoughSampleOfOne() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(10))
				.puts(timestamp(5))
				.removes(timestamp(2))
				.build();
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenJustEnoughSampleOf5() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(1), timestamp(2), timestamp(5), timestamp(9), timestamp(13))
				.puts(timestamp(1), timestamp(1), timestamp(1), timestamp(1), timestamp(1))
				.removes(timestamp(1), timestamp(1), timestamp(1), timestamp(1), timestamp(1))
				.build();
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 5)).isEqualTo(2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenMoreSamplesOfOne() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(1), timestamp(2), timestamp(5), timestamp(9))
				.puts(timestamp(1), timestamp(1), timestamp(1), timestamp(1))
				.removes(timestamp(1), timestamp(2), timestamp(3), timestamp(4))
				.build();
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenMoreSamplesOfFour() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(0), timestamp(2), timestamp(5), timestamp(9), timestamp(13))
				.puts(timestamp(1), timestamp(1), timestamp(1), timestamp(2), timestamp(3))
				.removes(timestamp(0), timestamp(0), timestamp(0), timestamp(0), timestamp(3))
				.build();
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 4)).isEqualTo(2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenNoAccessOfOne() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(10), timestamp(10))
				.puts(timestamp(0), timestamp(0))
				.removes(timestamp(0), timestamp(0))
				.build();
		assertThat(testee.computeSamplesDiff(diskStatsHistory, 1)).isEqualTo(0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeSamplesDiffWhenNoAccessOfThree() {
		History diskStatsHistory = History.builder()
				.gets(timestamp(5), timestamp(10), timestamp(10), timestamp(10), timestamp(10))
				.puts(timestamp(0), timestamp(0), timestamp(0), timestamp(0), timestamp(0))
				.removes(timestamp(0), timestamp(0), timestamp(0), timestamp(0), timestamp(0))
				.build();
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
