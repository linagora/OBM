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
package org.obm.push.technicallog.logger;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.ProtocolVersion;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.technicallog.TechnicalLoggerService;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.bean.jaxb.Resource;
import org.obm.push.technicallog.bean.jaxb.Transaction;


public class TechnicalLoggingBinderTest {
	
	@Test
	public void testLogTransaction() {
		DateTime startTime = DateTime.now();
		DateTime endTime = DateTime.now().plusDays(1);
		Transaction transaction = Transaction.builder()
				.id(Thread.currentThread().getId())
				.transactionStartTime(startTime)
				.transactionEndTime(endTime)
				.build();
		TechnicalLoggerService technicalLoggerService = createStrictMock(TechnicalLoggerService.class);
		technicalLoggerService.trace(transaction);
		expectLastCall().once();
		
		replay(technicalLoggerService);
		
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(technicalLoggerService);
		technicalLoggingBinder.logTransaction(startTime, endTime);
		
		verify(technicalLoggerService);
	}
	
	@Test
	public void testGetCorrespondingObjectFromMethodParameters() {
		Integer expectedInteger = new Integer(1);
		
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(null);
		Object obj = technicalLoggingBinder.getCorrespondingObjectFromMethodParameters(Integer.class, new Object[] { expectedInteger });
		
		assertThat(obj).isInstanceOf(Integer.class);
		assertThat(obj).isEqualTo(expectedInteger);
	}
	
	@Test
	public void testGetCorrespondingObjectFromMethodParametersMultipleParameters() {
		String string = new String("string");
		Long lng = new Long(12);
		Integer expectedInteger = new Integer(1);
		Double dble = new Double(14.5);
		
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(null);
		Object obj = technicalLoggingBinder.getCorrespondingObjectFromMethodParameters(Integer.class, new Object[] { string, lng, expectedInteger, dble });
		
		assertThat(obj).isInstanceOf(Integer.class);
		assertThat(obj).isEqualTo(expectedInteger);
	}
	
	private interface MyInterface {
	}
	
	private class MyClass implements MyInterface {
	}
	
	@Test
	public void testFalseCheckInterface() {
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(null);
		boolean value = technicalLoggingBinder.checkInterface(MyClass.class, String.class);
		
		assertThat(value).isFalse();
	}
	
	@Test
	public void testTrueCheckInterface() {
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(null);
		boolean value = technicalLoggingBinder.checkInterface(MyClass.class, MyInterface.class);
		
		assertThat(value).isTrue();
	}
	
	@Test
	public void testLogRequest() {
		UserDataRequest userDataRequest = getFakeUserDataRequest();
		
		int requestId = 1;
		IContinuation continuation = createStrictMock(IContinuation.class);
		expect(continuation.getReqId())
			.andReturn(requestId).times(2);
		
		MethodInvocation methodInvocation = createStrictMock(MethodInvocation.class);
		expect(methodInvocation.getArguments())
			.andReturn(new Object[] { userDataRequest, continuation }).times(2);
			
		DateTime startTime = DateTime.now();
		Request startRequest = Request.builder()
				.deviceId(userDataRequest.getDevId().getDeviceId())
				.deviceType(userDataRequest.getDevType())
				.command(userDataRequest.getCommand())
				.requestId(requestId)
				.transactionId(Thread.currentThread().getId())
				.requestStartTime(startTime)
				.build();
		TechnicalLoggerService technicalLoggerService = createStrictMock(TechnicalLoggerService.class);
		technicalLoggerService.traceStartedRequest(startRequest);
		expectLastCall().once();
		
		DateTime endTime = DateTime.now().plusDays(requestId);
		Request endRequest = Request.builder()
				.deviceId(userDataRequest.getDevId().getDeviceId())
				.deviceType(userDataRequest.getDevType())
				.command(userDataRequest.getCommand())
				.requestId(requestId)
				.transactionId(Thread.currentThread().getId())
				.requestEndTime(endTime)
				.build();
		technicalLoggerService.traceEndedRequest(endRequest);
		expectLastCall().once();
		
		replay(continuation, methodInvocation, technicalLoggerService);
		
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(technicalLoggerService);
		technicalLoggingBinder.logRequest(methodInvocation, startTime, endTime);
		
		verify(continuation, methodInvocation, technicalLoggerService);
	}
	
	private UserDataRequest getFakeUserDataRequest() {
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		UserDataRequest udr = new UserDataRequest(new Credentials(user, "test"),
				"Sync", getFakeDevice());
		return udr;
	}

	private Device getFakeDevice() {
		return new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testLogResource() {
		DateTime startTime = DateTime.now();
		DateTime endTime = DateTime.now().plusDays(1);
		Resource resource = Resource.builder()
				.resourceType(ResourceType.JDBC_CONNECTION)
				.resourceStartTime(startTime)
				.resourceEndTime(endTime)
				.transactionId(Thread.currentThread().getId())
				.build();
		TechnicalLoggerService technicalLoggerService = createStrictMock(TechnicalLoggerService.class);
		technicalLoggerService.traceResource(resource);
		expectLastCall().once();
		
		replay(technicalLoggerService);
		
		TechnicalLoggingBinder technicalLoggingBinder = new TechnicalLoggingBinder(technicalLoggerService);
		technicalLoggingBinder.logResource(ResourceType.JDBC_CONNECTION, startTime, endTime);
		
		verify(technicalLoggerService);
	}
}
