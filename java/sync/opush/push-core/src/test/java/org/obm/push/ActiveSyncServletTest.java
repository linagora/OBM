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
package org.obm.push;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.same;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.internal.MocksControl;
import org.easymock.internal.MocksControl.MockType;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.impl.PushContinuation;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.resource.ResourcesService;
import org.obm.push.service.DeviceService;
import org.slf4j.Logger;

import com.google.common.collect.Sets;


public class ActiveSyncServletTest {

	private MocksControl mocksControl;
	private User user;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private DeviceId deviceId;
	private String deviceType;
	private String userAgent;
	private ProtocolVersion protocolVersion;
	private int requestId;
	private String command;
	private Credentials credentials;
	private ActiveSyncRequest activeSyncRequest;
	private PolicyService policyService;
	private ResourcesService resourcesService;
	private Set<ResourcesService> resourcesServices;
	
	@Before
	public void setUp() throws DaoException {
		mocksControl = new MocksControl(MockType.DEFAULT);
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		
		activeSyncRequest = mocksControl.createMock(ActiveSyncRequest.class);
		
		deviceId = new DeviceId("devId");
		deviceType = "devType";
		userAgent = user.getLoginAtDomain();
		protocolVersion = ProtocolVersion.V121;
		requestId = 1;
		command = "cmd";
		
		credentials = mocksControl.createMock(Credentials.class);
		expect(credentials.getUser()).andReturn(user).atLeastOnce();
		
		PushContinuation pushContinuation = mocksControl.createMock(PushContinuation.class);
		expect(pushContinuation.needsContinuationHandling()).andReturn(false).anyTimes();
		expect(pushContinuation.getReqId()).andReturn(requestId).anyTimes();
		
		request = mocksControl.createMock(HttpServletRequest.class);
		expect(request.getQueryString()).andReturn("Cmd=").anyTimes();
		expect(request.getMethod()).andReturn("method").anyTimes();
		expect(activeSyncRequest.getDeviceId()).andReturn(deviceId).anyTimes();
		expect(activeSyncRequest.getDeviceType()).andReturn(deviceType).anyTimes();
		expect(activeSyncRequest.getUserAgent()).andReturn(userAgent).anyTimes();
		expect(activeSyncRequest.getMsPolicyKey()).andReturn(null).anyTimes();
		expect(activeSyncRequest.getHttpServletRequest()).andReturn(request).anyTimes();
		expect(activeSyncRequest.getMSASProtocolVersion()).andReturn(protocolVersion.asSpecificationValue()).anyTimes();
		
		policyService = mocksControl.createMock(PolicyService.class);
		expect(policyService.needProvisionning(activeSyncRequest, user)).andReturn(false);
		
		expect(request.getParameter("Cmd")).andReturn(command).anyTimes();
		expect(request.getAttribute(RequestProperties.CREDENTIALS)).andReturn(credentials);
		expect(request.getAttribute(RequestProperties.CONTINUATION)).andReturn(pushContinuation);
		expect(request.getAttribute(RequestProperties.ACTIVE_SYNC_REQUEST)).andReturn(activeSyncRequest);
		
		response = mocksControl.createMock(HttpServletResponse.class);
		response.setHeader(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();
		
		resourcesService = mocksControl.createMock(ResourcesService.class);
		resourcesServices = Sets.newHashSet(resourcesService);
	}
	
	@Test
	public void testEnsureThatProcessActiveSyncMethodCallCloseResources() throws ServletException, IOException, DaoException {
		UserDataRequest userDataRequest = mocksControl.createMock(UserDataRequest.class);
		expect(userDataRequest.getCommand()).andReturn(command).atLeastOnce();
		
		resourcesService.initRequest(userDataRequest, request);
		expectLastCall().once();
		resourcesService.closeResources(userDataRequest);
		expectLastCall().once();
		
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet(userDataRequest);
		mocksControl.replay();
		
		activeSyncServlet.doPost(request, response);
		mocksControl.verify();
	}
	
	private SessionService createSessionService(UserDataRequest userDataRequest) throws DaoException {
		SessionService sessionService = mocksControl.createMock(SessionService.class);
		expect(sessionService.getSession(same(credentials), eq(deviceId), anyObject(ActiveSyncRequest.class)))
			.andReturn(userDataRequest).anyTimes();
		return sessionService;
	}
	
	private IBackend createBackend() {
		IBackend backend = mocksControl.createMock(IBackend.class);
		return backend;
	}

	private DeviceService createDeviceService() throws DaoException {
		DeviceService deviceService = mocksControl.createMock(DeviceService.class);
		deviceService.initDevice(user, deviceId, deviceType, userAgent, protocolVersion);
		expectLastCall().anyTimes();
		expect(deviceService.syncAuthorized(user, deviceId)).andReturn(true).anyTimes();
		return deviceService;
	}

	private ResponderImpl.Factory createResponderFactory() {
		ResponderImpl.Factory responderFactory = mocksControl.createMock(ResponderImpl.Factory.class);
		expect(responderFactory.createResponder(request, response)).andReturn(null).anyTimes();
		return responderFactory;
	}

	private Handlers createHandlers(UserDataRequest userDataRequest) throws IOException {
		IRequestHandler requestHandler = mocksControl.createMock(IRequestHandler.class);
		requestHandler.process(anyObject(IContinuation.class), eq(userDataRequest), eq(activeSyncRequest), (Responder) eq(null));
		expectLastCall();
		
		Handlers handlers = mocksControl.createMock(Handlers.class);
		expect(handlers.getHandler(command)).andReturn(requestHandler).anyTimes();
		return handlers;
	}

	private Logger createLogger() {
		Logger logger = mocksControl.createMock(Logger.class);
		logger.info(anyObject(String.class), eq(user.getEmail()), eq(deviceType));
		mocksControl.anyTimes();
		logger.debug(anyObject(String.class));
		mocksControl.anyTimes();
		logger.warn(anyObject(String.class));
		mocksControl.anyTimes();
		return logger;
	}

	private ActiveSyncServlet createActiveSyncServlet(UserDataRequest userDataRequest) throws DaoException, IOException {
		SessionService sessionService = createSessionService(userDataRequest);
		LoggerService loggerService = mocksControl.createMock(LoggerService.class);
		IBackend backend = createBackend();
		DeviceService deviceService = createDeviceService();
		ResponderImpl.Factory responderFactory = createResponderFactory();
		Handlers handlers = createHandlers(userDataRequest);
		HttpErrorResponder httpErrorResponder = mocksControl.createMock(HttpErrorResponder.class);
		
		Logger logger = createLogger();
		
		return new ActiveSyncServlet(sessionService, backend, deviceService, policyService, responderFactory, handlers, loggerService, logger, httpErrorResponder, resourcesServices);
	}
}
