/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

public class TimeOutMapTest {

	private final static String APPLY_VALUE = "DEFAULT-VALUE";
	private Map<String, String> cache;

	@Before
	public void init() {
		cache = new MapMaker()
	    .expireAfterAccess(2, TimeUnit.SECONDS)
	    .makeComputingMap(new Function<String, String>() {
	        @Override
	        public String apply(String input) {
	            return APPLY_VALUE;
	        }
	    });
	}
	
	@After
	public void flush() {
		cache.clear();
	}
	
	@Test
	public void basicOperation() {
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		cache.put(key, value);
		Assert.assertEquals(value, cache.get(key) );
	}
	
	@Test
	public void returnApplyValue() {
		Assert.assertEquals(APPLY_VALUE, cache.get("KEY-NOT-EXIST") );
	}
	
	@Test
	public void returnApplyValueExpireAfterAccess() throws InterruptedException {
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		cache.put(key, value);
		
		Thread.sleep(5000);
		Assert.assertEquals(APPLY_VALUE, cache.get(key) );
	}
	
	@Test
	public void testTimeOutMap() throws Exception {
		ConcurrentMap<String, Object> t = new MapMaker().
			expireAfterWrite(5, TimeUnit.SECONDS).
			evictionListener(new MapEvictionListener<String, Object>() {
				@Override
				public void onEviction(String key, Object value) {
				}
			}).makeMap();
		t.put("a", new Object());
		assertNotNull(t.get("a"));
		for (int i = 0; i < 4; i++) {
			Thread.sleep(1000);
			assertNotNull(t.get("a"));
		}
		Thread.sleep(1000);
		assertNull(t.get("a"));
	}

}
