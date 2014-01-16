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
package org.obm.push.impl;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Properties;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.ContinuationTransactionMap;
import org.obm.push.ElementNotFoundException;
import org.obm.push.ProtocolVersion;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;

public class ContinuationServiceImplTest {
	
	private Device device;
	private IMocksControl control;

	@Before
	public void setUp() {
		control = createControl();
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testSuspend() {
		String error = "ERROR";
		UserDataRequest userDataRequest = getFakeUserDataRequest();
		
		IContinuation continuation = mockContinuation();
		continuation.suspend(userDataRequest, 0);
		expectLastCall();
		continuation.error(error);
		expectLastCall();
		
		ContinuationTransactionMap<IContinuation> continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.putContinuationForDevice(device, continuation)).andReturn(false);
		
		control.replay();
		
		continuationService(continuationTransactionMap).suspend(userDataRequest, continuation, 0, error);
		
		control.verify();
	}
	
	@Test
	public void testSuspendOnAlreadyCachedContinuation() {
		String error = "ERROR";
		UserDataRequest userDataRequest = getFakeUserDataRequest();
		
		IContinuation continuation = mockContinuation();
		continuation.suspend(userDataRequest, 0);
		expectLastCall();
		continuation.error(error);
		expectLastCall();
		
		ContinuationTransactionMap<IContinuation> continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.putContinuationForDevice(device, continuation)).andReturn(true);
		
		control.replay();
		
		continuationService(continuationTransactionMap).suspend(userDataRequest, continuation, 0, error);
		
		control.verify();
	}
	
	private UserDataRequest getFakeUserDataRequest() {
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		UserDataRequest udr = new UserDataRequest(new Credentials(user, "test"), "Cmd", device);
		return udr;
	}
	
	@Test
	public void testResume() throws ElementNotFoundException {
		IContinuation continuation = mockContinuation();
		continuation.resume();
		expectLastCall();
		
		ContinuationTransactionMap<IContinuation> continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.getContinuationForDevice(device))
			.andReturn(continuation);
		continuationTransactionMap.delete(device);
		expectLastCall();
		
		control.replay();
		
		continuationService(continuationTransactionMap).resume(device);
		
		control.verify();
	}
	
	@Test
	public void testCancel() throws ElementNotFoundException {
		IContinuation continuation = mockContinuation();
		continuation.resume();
		expectLastCall();
		
		ContinuationTransactionMap<IContinuation> continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.getContinuationForDevice(device))
			.andReturn(continuation);
		continuationTransactionMap.delete(device);
		expectLastCall();
		
		control.replay();
		
		continuationService(continuationTransactionMap).cancel(device);
		
		control.verify();
	}

	private ContinuationTransactionMap<IContinuation> mockContinuationTransactionMap() {
		ContinuationTransactionMap<IContinuation> continuationTransactionMap = control.createMock(ContinuationTransactionMap.class);
		return continuationTransactionMap;
	}

	private IContinuation mockContinuation() {
		IContinuation continuation = control.createMock(IContinuation.class);
		return continuation;
	}

	private ContinuationServiceImpl continuationService(ContinuationTransactionMap<IContinuation> continuationTransactionMap) {
		return new ContinuationServiceImpl(continuationTransactionMap);
	}
}
