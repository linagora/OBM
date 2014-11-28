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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import javax.servlet.http.HttpServletRequest;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.servlet.filter.qos.QoSAction;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSContinuationSupport.QoSContinuation;
import org.obm.servlet.filter.qos.handlers.TransactionalKeyRequestsInfoStore.RequestInfoReference;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestHandler.RequestDoneFunction;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestHandler.StartRequestFunction;

public class NPerClientQoSRequestSuspendHandlerTest {
	
	private IMocksControl control;
	private BusinessKeyProvider<String> keyProvider;
	private TransactionalKeyRequestsInfoStore<String> requestInfoStore;
	private NPerClientQoSRequestSuspendHandler<String> testee;
	private KeyRequestsInfo<String> zeroRequest;
	private KeyRequestsInfo<String> oneRequest;
	private KeyRequestsInfo<String> twoRequests;
	private String key;
	private QoSContinuationSupport continuationSupport;

	@Before
	public void setup() {
		control = createStrictControl();
		keyProvider = control.createMock(BusinessKeyProvider.class);
		requestInfoStore = control.createMock(TransactionalKeyRequestsInfoStore.class);
		continuationSupport = control.createMock(QoSContinuationSupport.class);
		key = "myKey";
		zeroRequest = KeyRequestsInfo.create(key);
		oneRequest = zeroRequest.oneMoreRequest();
		twoRequests = oneRequest.oneMoreRequest();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void startRequest() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		HttpServletRequest firstRequest = control.createMock(HttpServletRequest.class);
		expect(keyProvider.provideKey(firstRequest)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(StartRequestFunction.class))).andReturn(QoSAction.ACCEPT);
		control.replay();
		QoSAction actual = testee.startRequestHandling(firstRequest);
		assertThat(actual).isEqualTo(QoSAction.ACCEPT);
		control.verify();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void requestDone() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		HttpServletRequest firstRequest = control.createMock(HttpServletRequest.class);
		expect(keyProvider.provideKey(firstRequest)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(RequestDoneFunction.class))).andReturn(null);
		control.replay();
		testee.finishRequestHandling(firstRequest);
		control.verify();
	}
	
	@Test
	public void acceptFirstRequest() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		HttpServletRequest firstRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(zeroRequest);
		ref.put(oneRequest);
		expect(ref.get()).andReturn(zeroRequest);
		control.replay();
		QoSAction actual = testee.startRequestImpl(ref, firstRequest);
		assertThat(actual).isEqualTo(QoSAction.ACCEPT);
		control.verify();
	}
	
	@Test
	public void acceptSecondRequest() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		HttpServletRequest secondRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(oneRequest);
		ref.put(twoRequests);
		expect(ref.get()).andReturn(zeroRequest);
		control.replay();
		QoSAction actual = testee.startRequestImpl(ref, secondRequest);
		assertThat(actual).isEqualTo(QoSAction.ACCEPT);
		control.verify();
	}
	
	@Test
	public void tooManyRequestsSuspend() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		HttpServletRequest thirdRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(twoRequests).times(2);
		expect(thirdRequest.getQueryString()).andReturn("");
		QoSContinuation continuation = control.createMock(QoSContinuation.class);
		expect(continuationSupport.getContinuationFor(thirdRequest)).andReturn(continuation);
		KeyRequestsInfo<String> expectedInfo = twoRequests.appendContinuation(continuation);
		ref.put(expectedInfo);
		control.replay();
		QoSAction actual = testee.startRequestImpl(ref, thirdRequest);
		assertThat(actual).isEqualTo(QoSAction.SUSPEND);
		control.verify();
	}
	
	@Test
	public void requestDoneTrackNbRequest() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(twoRequests);
		ref.put(oneRequest);
		expect(ref.get()).andReturn(oneRequest);
		control.replay();
		testee.requestDoneImpl(ref);
		control.verify();
	}
	
	@Test
	public void cleanupEmptyInfo() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(zeroRequest).times(2);
		ref.clear();
		control.replay();
		testee.cleanupImpl(ref);
		control.verify();
	}
	
	@Test
	public void cleanupBusyInfo() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(oneRequest).times(2);
		control.replay();
		testee.cleanupImpl(ref);
		control.verify();
	}
	
	@Test
	public void cleanupBusyInfoSuspendedQueue() {
		testee = new NPerClientQoSRequestSuspendHandler<String>(keyProvider, requestInfoStore, continuationSupport, 2);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		QoSContinuation continuation = control.createMock(QoSContinuation.class);
		expect(ref.get()).andReturn(zeroRequest.appendContinuation(continuation));
		control.replay();
		testee.cleanupImpl(ref);
		control.verify();
	}
	
}
