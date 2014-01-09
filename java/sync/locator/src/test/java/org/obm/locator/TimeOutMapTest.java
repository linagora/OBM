/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.locator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;


public class TimeOutMapTest {

	private final static String APPLY_VALUE = "DEFAULT-VALUE";

	@Test
	public void basicOperation() {
		Cache<String, String> localCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS)
				.build(new CacheLoader<String, String>() {

					@Override
					public String load(String key) throws Exception {
						return APPLY_VALUE;
					}
				});
		Map<String, String> mapCache = localCache.asMap();
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		mapCache.put(key, value);
		Assert.assertEquals(value, mapCache.get(key));
	}
	
	@Test
	public void returnApplyValue() throws ExecutionException {
		LoadingCache<String, String> localCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS)
				.build(new CacheLoader<String, String>() {

					@Override
					public String load(String key) throws Exception {
						return APPLY_VALUE;
					}
				});
		Assert.assertEquals(APPLY_VALUE, localCache.get("KEY-NOT-EXIST") );
	}
	
	@Test
	public void returnApplyValueExpireAfterAccess() throws InterruptedException, ExecutionException {
		LoadingCache<String, String> cache = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS)
				.build(new CacheLoader<String, String>() {

					@Override
					public String load(String key) throws Exception {
						return APPLY_VALUE;
					}
				});
		Map<String, String> mapCache = cache.asMap();
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		mapCache.put(key, value);
		Assert.assertEquals(value, cache.get(key) );
		Thread.sleep(5000);
		Assert.assertEquals(APPLY_VALUE, cache.get(key) );
	}
	
	@Test
	public void testTimeOutMap() throws Exception {
		Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS)
				.removalListener(new RemovalListener<String, Object>() {

					@Override
					public void onRemoval(
							RemovalNotification<String, Object> notification) {
						
					}
				})
				.build(new CacheLoader<String, Object>() {

					@Override
					public Object load(String key) throws Exception {
						return APPLY_VALUE;
					}
				});
		Map<String, Object> mapCache = cache.asMap();
		mapCache.put("a", new Object());
		assertNotNull(mapCache.get("a"));
		for (int i = 0; i < 4; i++) {
			Thread.sleep(1000);
			assertNotNull(mapCache.get("a"));
		}
		Thread.sleep(1000);
		assertNull(mapCache.get("a"));
	}

}
