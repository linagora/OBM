/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.obm.servlet.filter.qos.QoSFilterModule;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ConcurrentRequestInfoStore<K extends Serializable> {

	interface StoreFunction<K extends Serializable, V> {
		K key();
		V execute(ConcurrentRequestInfoStore<K> store);
		void cleanup(ConcurrentRequestInfoStore<K> store);
	}
	
	private static final String STORE_NAME = "RequestInfoStore";
	private static final int NO_LIMIT = 0;
	@VisibleForTesting Cache store;

	@Inject
	@VisibleForTesting ConcurrentRequestInfoStore(@Named(QoSFilterModule.CONCURRENT_REQUEST_INFO_STORE) CacheManager cacheManager) {
		cacheManager.addCache(storeDefinition());
		store = cacheManager.getCache(STORE_NAME);
	}
	
	private static Cache storeDefinition() {
		return new Cache(
				new CacheConfiguration()
				.maxElementsInMemory(NO_LIMIT)
				.copyOnRead(false)
				.copyOnWrite(false)
				.overflowToDisk(false)
				.eternal(false)
				.name(STORE_NAME)
				.transactionalMode(TransactionalMode.OFF));
	}
	
	public <V> V executeInTransaction(StoreFunction<K, V> function) {
		K key = function.key();
		store.acquireWriteLockOnKey(key);
		try {
			V result = function.execute(this);
			function.cleanup(this);
			return result;
		} finally {
			store.releaseWriteLockOnKey(key);	
		}
	}

	public RequestInfo<K> getRequestInfo(K key) {
		Element element = store.get(key);

		if (element == null) {
			return RequestInfo.create(key);
		}
		return (RequestInfo<K>) element.getValue();
	}
	
	public RequestInfo<K> storeRequestInfo(RequestInfo<K> value) {
		store.put(element(value));
		return value;
	}
	
	public boolean remove(RequestInfo<K> value) {
		return store.remove(element(value));
	}
	
	private Element element(RequestInfo<K> info) {
		return new Element(info.getKey(), info);
	}
	
}
