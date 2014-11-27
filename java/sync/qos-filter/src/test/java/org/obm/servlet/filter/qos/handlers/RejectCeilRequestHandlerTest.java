/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
import org.obm.servlet.filter.qos.handlers.ConcurrentRequestInfoStore.RequestInfoReference;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestHandler.RequestDoneFunction;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestHandler.StartRequestFunction;


public class RejectCeilRequestHandlerTest {
	
	private IMocksControl control;
	private BusinessKeyProvider<String> keyProvider;
	private ConcurrentRequestInfoStore<String> requestInfoStore;
	private ContinuationIdStore continuationIdStore;
	private RejectCeilRequestHandler<String> testee;
	private RequestInfo<String> zeroRequest;
	private RequestInfo<String> oneRequest;
	private RequestInfo<String> twoRequests;
	private RequestInfo<String> threeRequests;
	private String key;

	@Before
	public void setup() {
		control = createStrictControl();
		keyProvider = control.createMock(BusinessKeyProvider.class);
		requestInfoStore = control.createMock(ConcurrentRequestInfoStore.class);
		continuationIdStore = control.createMock(ContinuationIdStore.class);
		key = "myKey";
		zeroRequest = RequestInfo.create(key);
		oneRequest = zeroRequest.oneMoreRequest();
		twoRequests = oneRequest.oneMoreRequest();
		threeRequests = twoRequests.oneMoreRequest();
		
		int suspendCeil = 2;
		int rejectCeil = 3;
		testee = new RejectCeilRequestHandler<String>(keyProvider, requestInfoStore, continuationIdStore, suspendCeil, rejectCeil);
	}
	
	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testRejectCeilEqualsToSuspend() {
		int suspendCeil = 2;
		int rejectCeil = 2;
		new RejectCeilRequestHandler<String>(keyProvider, requestInfoStore, continuationIdStore, suspendCeil, rejectCeil);
	}
	
	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testRejectCeilLessThanSuspend() {
		int suspendCeil = 2;
		int rejectCeil = 1;
		new RejectCeilRequestHandler<String>(keyProvider, requestInfoStore, continuationIdStore, suspendCeil, rejectCeil);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testRejectCeilGreaterThanSuspend() {
		int suspendCeil = 2;
		int rejectCeil = 3;
		new RejectCeilRequestHandler<String>(keyProvider, requestInfoStore, continuationIdStore, suspendCeil, rejectCeil);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void acceptFirstRequest() {
		HttpServletRequest firstRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(zeroRequest);
		expect(keyProvider.provideKey(firstRequest)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(StartRequestFunction.class))).andReturn(QoSAction.ACCEPT);
		
		control.replay();
		QoSAction actual = testee.startRequestImpl(firstRequest, key, ref);
		control.verify();

		assertThat(actual).isEqualTo(QoSAction.ACCEPT);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void acceptSecondRequest() {
		HttpServletRequest firstRequest = control.createMock(HttpServletRequest.class);
		
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(oneRequest);
		expect(keyProvider.provideKey(firstRequest)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(StartRequestFunction.class))).andReturn(QoSAction.ACCEPT);
		
		control.replay();
		QoSAction actual = testee.startRequestImpl(firstRequest, key, ref);
		control.verify();

		assertThat(actual).isEqualTo(QoSAction.ACCEPT);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void suspendThirdRequest() {
		HttpServletRequest thirdRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(twoRequests);
		expect(keyProvider.provideKey(thirdRequest)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(StartRequestFunction.class))).andReturn(QoSAction.SUSPEND);
		
		control.replay();
		QoSAction actual = testee.startRequestImpl(thirdRequest, key, ref);
		control.verify();

		assertThat(actual).isEqualTo(QoSAction.SUSPEND);
	}

	@Test
	public void rejectFourthRequest() {
		HttpServletRequest fourthRequest = control.createMock(HttpServletRequest.class);
		RequestInfoReference<String> ref = control.createMock(RequestInfoReference.class);
		expect(ref.get()).andReturn(threeRequests);
		
		control.replay();
		QoSAction actual = testee.startRequestImpl(fourthRequest, key, ref);
		control.verify();

		assertThat(actual).isEqualTo(QoSAction.REJECT);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void finishRequest() {
		HttpServletRequest request = control.createMock(HttpServletRequest.class);
		expect(keyProvider.provideKey(request)).andReturn(key);
		expect(requestInfoStore.executeInTransaction(eq(key), isA(RequestDoneFunction.class))).andReturn(null);
		
		control.replay();
		testee.finishRequestHandling(request);
		control.verify();
	}
}
