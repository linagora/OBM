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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Properties;

import net.sf.ehcache.Element;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.ContinuationTransactionMap;
import org.obm.push.ProtocolVersion;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ResourcesHolder;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ElementNotFoundException;

public class ContinuationServiceImplTest {
	
	private Device device;

	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testSuspend() {
		IContinuation continuation = mockContinuation();
		
		ContinuationTransactionMap continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.putContinuationForDevice(device, continuation))
			.andReturn(null);
		
		replay(continuationTransactionMap);
		
		continuationService(continuationTransactionMap).suspend(getFakeUserDataRequest(), continuation, 0);
		
		verify(continuationTransactionMap);
	}
	
	@Test
	public void testSuspendOnAlreadyCachedContinuation() {
		IContinuation previousContinuation = mockContinuation();
		Element previousElement = new Element(device, previousContinuation);
		IContinuation continuation = mockContinuation();
		
		ContinuationTransactionMap continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.putContinuationForDevice(device, continuation))
			.andReturn(previousElement);
		
		replay(continuationTransactionMap);
		
		continuationService(continuationTransactionMap).suspend(getFakeUserDataRequest(), continuation, 0);
		
		verify(continuationTransactionMap);
	}
	
	private UserDataRequest getFakeUserDataRequest() {
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		UserDataRequest udr = new UserDataRequest(new Credentials(user, "test"), "Cmd", device, new ResourcesHolder());
		return udr;
	}
	
	@Test
	public void testResume() throws ElementNotFoundException {
		IContinuation continuation = mockContinuation();
		continuation.resume();
		expectLastCall();
		
		ContinuationTransactionMap continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.getContinuationForDevice(device))
			.andReturn(continuation);
		continuationTransactionMap.delete(device);
		expectLastCall();
		
		replay(continuationTransactionMap, continuation);
		
		continuationService(continuationTransactionMap).resume(device);
		
		verify(continuationTransactionMap, continuation);
	}
	
	@Test
	public void testCancel() throws ElementNotFoundException {
		String error = "ERROR";
		IContinuation continuation = mockContinuation();
		continuation.resume();
		expectLastCall();
		continuation.error(error);
		expectLastCall();
		
		ContinuationTransactionMap continuationTransactionMap = mockContinuationTransactionMap();
		expect(continuationTransactionMap.getContinuationForDevice(device))
			.andReturn(continuation);
		continuationTransactionMap.delete(device);
		expectLastCall();
		
		replay(continuationTransactionMap, continuation);
		
		continuationService(continuationTransactionMap).cancel(device, error);
		
		verify(continuationTransactionMap, continuation);
	}

	private ContinuationTransactionMap mockContinuationTransactionMap() {
		ContinuationTransactionMap continuationTransactionMap = createMock(ContinuationTransactionMap.class);
		return continuationTransactionMap;
	}

	private IContinuation mockContinuation() {
		IContinuation continuation = createMock(IContinuation.class);
		return continuation;
	}

	private ContinuationServiceImpl continuationService(ContinuationTransactionMap continuationTransactionMap) {
		return new ContinuationServiceImpl(continuationTransactionMap);
	}
}
