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

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.primitives.Ints;

public class TestingEhCacheConfiguration implements EhCacheConfiguration {

	private int maxMemoryInMB;
	private Integer percentageAllowedToCache;
	private long timeToLive;

	public TestingEhCacheConfiguration() {
		this.percentageAllowedToCache = 10;
		this.maxMemoryInMB = Ints.checkedCast(JvmUtils.maxRuntimeJvmMemoryInMB() / 2);
		this.timeToLive = 60;
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
	
	@Override
	public int maxMemoryInMB() {
		return maxMemoryInMB;
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		if (percentageAllowedToCache == null) {
			return Percentage.UNDEFINED;
		}
		return Percentage.of(percentageAllowedToCache);
	}

	@Override
	public long timeToLiveInSeconds() {
		return timeToLive;
	}

	@Override
	public TransactionalMode transactionalMode() {
		return TransactionalMode.XA;
	}
}
