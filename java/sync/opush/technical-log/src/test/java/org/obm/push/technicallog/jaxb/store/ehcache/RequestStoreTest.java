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
package org.obm.push.technicallog.jaxb.store.ehcache;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.jaxb.store.ehcache.ObjectStoreManager;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestNotFoundException;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestStore;


public class RequestStoreTest {
	
	public final static String STORE_NAME = "request";
	private CacheManager cacheManager;
	
	@Before
	public void setUp() {
		cacheManager = new CacheManager();
	}
	
	@After
	public void tearDown() {
		cacheManager.shutdown();
	}
	
	@Test
	public void testGetRequest() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		long transactionId = 2;
		Request expectedRequest = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		cache.putIfAbsent(new Element(transactionId, expectedRequest));
		
		replay(objectStoreManager);
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		Request request = requestStore.getRequest(transactionId);
		
		verify(objectStoreManager);
		assertThat(request).isEqualTo(expectedRequest);
	}
	
	@Test (expected=RequestNotFoundException.class)
	public void testGetRequestRequestNotFoundException() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		replay(objectStoreManager);
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		requestStore.getRequest(2);
	}
	
	@Test
	public void testPut() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		long transactionId = 2;
		Request expectedRequest = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		Element expectedElement = new Element(transactionId, expectedRequest);
		cache.putIfAbsent(expectedElement);
		
		replay(objectStoreManager);
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		Element element = requestStore.put(transactionId, expectedRequest);
		
		verify(objectStoreManager);
		assertThat(element).isEqualTo(expectedElement);
		List<Long> keys = cache.getKeys();
		assertThat(keys).containsOnly(transactionId);
	}
	
	@Test
	public void testPutNoCachedElement() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		replay(objectStoreManager);
		
		long transactionId = 2;
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		Element element = requestStore.put(transactionId, request);
		
		verify(objectStoreManager);
		assertThat(element).isNull();
		List<Long> keys = cache.getKeys();
		assertThat(keys).containsOnly(transactionId);
	}
	
	@Test
	public void testDelete() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		long transactionId = 2;
		Request expectedRequest = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		cache.putIfAbsent(new Element(transactionId, expectedRequest));
		
		replay(objectStoreManager);
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		requestStore.delete(transactionId);
		
		verify(objectStoreManager);
		List<Long> keys = cache.getKeys();
		assertThat(keys).isEmpty();
	}
	
	@Test
	public void testDeleteNoCachedElement() throws Exception {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		replay(objectStoreManager);
		
		RequestStore requestStore = new RequestStore(objectStoreManager);
		requestStore.delete(2);
		
		verify(objectStoreManager);
		List<Long> keys = cache.getKeys();
		assertThat(keys).isEmpty();
	}
	
	private ObjectStoreManager mockObjectStoreManager(Cache cache) {
		ObjectStoreManager objectStoreManager = createMock(ObjectStoreManager.class);
		expect(objectStoreManager.getStore(STORE_NAME))
			.andReturn(cache).anyTimes();
		
		return objectStoreManager;
	}

	private Cache buildCache() {
		Cache cache = new Cache(cacheConfigurationForRequest());
		cache.setCacheManager(cacheManager);
		cache.initialise();
		return cache;
	}
	
	private CacheConfiguration cacheConfigurationForRequest() {
		return new CacheConfiguration()
			.maxEntriesLocalHeap(0)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false)
			.name(STORE_NAME);
	}
}
