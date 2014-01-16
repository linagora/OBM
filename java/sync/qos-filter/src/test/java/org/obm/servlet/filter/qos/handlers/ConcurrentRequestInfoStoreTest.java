/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.servlet.filter.qos.handlers;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.servlet.filter.qos.handlers.ConcurrentRequestInfoStore.StoreFunction;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Cache.class, CacheManager.class})
public class ConcurrentRequestInfoStoreTest {

	
	private ConcurrentRequestInfoStore<String> store;
	private String key;
	private Cache cache;

	@Before
	public void setup() {
		cache = createMock(Cache.class);
		CacheManager cacheManager = createMock(CacheManager.class);
		cacheManager.addCache(anyObject(Cache.class));
		expect(cacheManager.getCache(anyObject(String.class))).andReturn(cache);
		replay(cacheManager);
		store = new ConcurrentRequestInfoStore<String>(cacheManager);
		
		key = "myKey";
	}
	
	@Test
	public void firstGetInfo() {
		expect(cache.get("myKey")).andReturn(null);
		replayAll();
		RequestInfo<String> requestInfo = store.getRequestInfo("myKey");
		verifyAll();
		assertThat(requestInfo).isNotNull();
		assertThat(requestInfo.getKey()).isEqualTo("myKey");
		assertThat(requestInfo.getNumberOfRunningRequests()).isEqualTo(0);
	}
	
	@Test
	public void getStoredInfo() {
		RequestInfo<String> info = createMock(RequestInfo.class);
		expect(cache.get("myKey")).andReturn(element(key, info));
		replayAll();
		RequestInfo<String> actualInfo = store.getRequestInfo("myKey");
		verifyAll();
		assertThat(actualInfo).isNotNull().isSameAs(info);
	}
	
	@Test
	public void storeInfo() {
		RequestInfo<String> info = createMock(RequestInfo.class);
		expect(info.getKey()).andReturn(key).atLeastOnce();
		cache.put(element(key, info));
		expectLastCall().once();
		replayAll();
		RequestInfo<String> updatedInfo = store.storeRequestInfo(info);
		verifyAll();
		assertThat(info).isNotNull().isSameAs(updatedInfo);
	}
	
	@Test
	public void removeInfo() {
		RequestInfo<String> info = createMock(RequestInfo.class);
		expect(info.getKey()).andReturn(key).atLeastOnce();
		expect(cache.remove(element(key, info))).andReturn(true);
		replayAll();
		boolean actual = store.remove(info);
		verifyAll();
		assertThat(actual).isTrue();
	}
	
	@Test
	public void protectedFunction() {
		cache.acquireWriteLockOnKey(key);
		expectLastCall().once();
		StoreFunction<String, Integer> storeFunction = createMock(StoreFunction.class);
		expect(storeFunction.key()).andReturn(key).atLeastOnce();
		expect(storeFunction.execute(store)).andReturn(43).once();
		storeFunction.cleanup(store);
		expectLastCall().once();
		cache.releaseWriteLockOnKey(key);
		expectLastCall().once();
		
		replayAll();
		Integer actual = store.executeInTransaction(storeFunction);
		verifyAll();
		assertThat(actual).isEqualTo(43);
	}
	
	private <K extends Serializable> Element element(K key, RequestInfo<K> info) {
		return new Element(key, info);
	}
}
