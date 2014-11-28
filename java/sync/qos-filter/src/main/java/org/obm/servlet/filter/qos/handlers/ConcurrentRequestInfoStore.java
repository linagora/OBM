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

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class ConcurrentRequestInfoStore<K> {

	private static final Logger logger = LoggerFactory.getLogger(ConcurrentRequestInfoStore.class);
	
	interface RequestInfoReference<K> {
		void put(KeyRequestsInfo<K> value);
		KeyRequestsInfo<K> get();
		void clear();
	}
	
	interface StoreFunction<K, V> {
		V execute(RequestInfoReference<K> store);
		void cleanup(RequestInfoReference<K> store);
	}
	

	private static class LockableReference<K> implements Closeable, RequestInfoReference<K> {
		
		private ReentrantLock lock;
		private KeyRequestsInfo<K> info;
		private boolean used;

		public LockableReference(K key) {
			lock = new ReentrantLock(true);
			info = KeyRequestsInfo.create(key);
			used = false;
		}

		private LockableReference<K> lock() {
			lock.lock();
			used = true;
			return this;
		}
		
		@Override
		public void close() {
			lock.unlock();
		}
		
		@Override
		public void clear() {
			Preconditions.checkState(lock.isHeldByCurrentThread());
			logger.debug("mark as unused");
			used = false;
		}
		
		@Override
		public KeyRequestsInfo<K> get() {
			Preconditions.checkState(lock.isHeldByCurrentThread());
			Preconditions.checkState(used == true);
			return info;
		}
		
		@Override
		public void put(KeyRequestsInfo<K> value) {
			Preconditions.checkState(lock.isHeldByCurrentThread());
			Preconditions.checkNotNull(value);
			Preconditions.checkState(used == true);
			logger.debug("put requestInfo {}", value);
			info = value;
		}
		
		private boolean used() {
			Preconditions.checkState(lock.isHeldByCurrentThread());
			return used;
		}
	}
	
	private final ConcurrentMap<K, LockableReference<K>> map;
	private final Set<LockableReference<K>> references;
	private final AtomicInteger count;

	@Inject
	@VisibleForTesting ConcurrentRequestInfoStore() {
		references = Sets.newConcurrentHashSet();
		map = new MapMaker()
			.weakValues()
			.makeMap();
		count = new AtomicInteger();
	}
	
	public <V> V executeInTransaction(K key, StoreFunction<K, V> function) {
		int id = count.incrementAndGet();
		logger.debug("Enters with id {}", id);
		
		try (LockableReference<K> reference = getLock(key).lock()) {
			
			logger.debug("Lock with id {}", id);
			
			V result = executeFunction(function, reference);
			if (!reference.used()) {
				references.remove(reference);
			}
			
			logger.debug("Leaves with id {}", id);
			return result;
		}
	}

	private <V> V executeFunction(StoreFunction<K, V> function, LockableReference<K> reference) {
		try {
			return function.execute(reference);
		} finally {
			function.cleanup(reference);
		}
	}
	
	private LockableReference<K> getLock(K key) {
		LockableReference<K> newReference = new LockableReference<K>(key);
		LockableReference<K> mappedRef = Optional.fromNullable(map.putIfAbsent(key, newReference)).or(newReference);
		references.add(mappedRef);
		return mappedRef;
	}
	
}
