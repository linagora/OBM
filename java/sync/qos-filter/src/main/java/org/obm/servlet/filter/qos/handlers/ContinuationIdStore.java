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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.continuation.Continuation;
import org.obm.servlet.filter.qos.QoSContinuationSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContinuationIdStore {

	public static class ContinuationId {

		private final long id;
		
		@VisibleForTesting ContinuationId(long id) {
			this.id = id;
		}

		@Override
		public final int hashCode() {
			return Objects.hashCode(id);
		}
		
		@Override
		public final boolean equals(Object object) {
			if (object instanceof ContinuationId) {
				ContinuationId that = (ContinuationId) object;
				return Objects.equal(this.id, that.id);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("id", id)
				.toString();
		}
		
	}
	
	private final ConcurrentMap<ContinuationId, Continuation> continuationStore;
	private final AtomicLong atomicLong;
	private final QoSContinuationSupport continuationSupport;
	
	@Inject
	@VisibleForTesting ContinuationIdStore(AtomicLong atomicLong, ConcurrentHashMap<ContinuationId, Continuation> continuationStore, QoSContinuationSupport continuationSupport) {
		this.atomicLong = atomicLong;
		this.continuationStore = continuationStore;
		this.continuationSupport = continuationSupport;
	}
	
	public ContinuationId generateIdFor(ServletRequest request) {
		ContinuationId id = new ContinuationId(atomicLong.getAndIncrement());
		continuationStore.put(id, continuationSupport.getContinuationFor(request));
		return id;
	}
	
	public Continuation removeContinuation(ContinuationId id) {
		return continuationStore.remove(id);
	}
	
}
