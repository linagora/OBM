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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletRequest;

import org.easymock.IMocksControl;
import org.eclipse.jetty.continuation.Continuation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSContinuationSupportJettyUtils;
import org.obm.servlet.filter.qos.handlers.ContinuationIdStore.ContinuationId;

@RunWith(SlowFilterRunner.class)
public class ContinuationIdStoreTest {

	private ContinuationIdStore store;
	private AtomicLong atomicLong;
	private ConcurrentHashMap<ContinuationId, Continuation> hashmap;
	private QoSContinuationSupport continuationSupport;
	private IMocksControl control;

	@Before
	public void setup() {
		control = createControl();
		hashmap = control.createMock(ConcurrentHashMap.class);
		continuationSupport = control.createMock(QoSContinuationSupportJettyUtils.class);
		atomicLong = new AtomicLong(44l);
		store = new ContinuationIdStore(atomicLong, hashmap, continuationSupport);
	}
	
	@Test
	public void generateIdFor() {
		ContinuationId expectedContinuationId = new ContinuationId(44l);
		ServletRequest servletRequest = control.createMock(ServletRequest.class);
		Continuation continuation = control.createMock(Continuation.class);
		expect(continuationSupport.getContinuationFor(servletRequest)).andReturn(continuation);
		expect(hashmap.put(eq(expectedContinuationId), same(continuation))).andReturn(null).once();
		control.replay();
		ContinuationId actual = store.generateIdFor(servletRequest);
		control.verify();
		assertThat(actual).isEqualTo(expectedContinuationId);
	}

	@Test
	public void generateIdForTwice() {
		ServletRequest firstRequest = control.createMock(ServletRequest.class);
		ServletRequest secondRequest = control.createMock(ServletRequest.class);
		ContinuationId firstContinuationId = new ContinuationId(44l);
		ContinuationId secondContinuationId = new ContinuationId(45l);
		Continuation firstContinuation = control.createMock(Continuation.class);
		Continuation secondContinuation = control.createMock(Continuation.class);
		expect(continuationSupport.getContinuationFor(firstRequest)).andReturn(firstContinuation);
		expect(continuationSupport.getContinuationFor(secondRequest)).andReturn(secondContinuation);
		expect(hashmap.put(eq(firstContinuationId), same(firstContinuation))).andReturn(null).once();
		expect(hashmap.put(eq(secondContinuationId), same(secondContinuation))).andReturn(null).once();
		control.replay();
		ContinuationId first = store.generateIdFor(firstRequest);
		ContinuationId second = store.generateIdFor(secondRequest);
		control.verify();
		assertThat(first).isEqualTo(firstContinuationId);
		assertThat(second).isEqualTo(secondContinuationId);
	}

	@Test
	public void remove() {
		Continuation continuation = control.createMock(Continuation.class);
		ContinuationId continuationId = new ContinuationId(44l);
		expect(hashmap.remove(continuationId)).andReturn(continuation).once();
		control.replay();
		Continuation actual = store.removeContinuation(continuationId);
		control.verify();
		assertThat(actual).isSameAs(continuation);
	}
	
}
