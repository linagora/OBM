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
package org.obm.push;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.impl.PushContinuation;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.service.DeviceService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;
import org.slf4j.Logger;

@RunWith(SlowFilterRunner.class)
public class ActiveSyncServletTest {

	private User user;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String deviceId;
	private String deviceType;
	private String userAgent;
	private int requestId;
	private String command;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		
		deviceId = "devId";
		deviceType = "devType";
		userAgent = user.getLoginAtDomain();
		requestId = 1;
		command = "cmd";
		
		request = createMock(HttpServletRequest.class);
		String credentialsString = user.getLogin() + ":test"; 
		expect(request.getHeader("Authorization"))
			.andReturn("Basic " + Base64.encodeBase64String(credentialsString.getBytes())).anyTimes();
		expect(request.getQueryString()).andReturn("Cmd=").anyTimes();
		expect(request.getMethod()).andReturn("method").anyTimes();
		expect(request.getParameter("DeviceId")).andReturn(deviceId).anyTimes();
		expect(request.getParameter("DeviceType")).andReturn(deviceType).anyTimes();
		expect(request.getHeader("User-Agent")).andReturn(userAgent).anyTimes();
		expect(request.getParameter("Cmd")).andReturn(command).anyTimes();
		expect(request.getHeader("X-Ms-PolicyKey")).andReturn(null).anyTimes();
		replay(request);
		
		response = createMock(HttpServletResponse.class);
	}
	
	@Test
	public void testEnsureThatProcessActiveSyncMethodCallCloseResources() throws ServletException, IOException, AuthFault, DaoException {
		UserDataRequest userDataRequest = createMock(UserDataRequest.class);
		userDataRequest.closeResources();
		expectLastCall();
		expect(userDataRequest.getCommand()).andReturn(command).anyTimes();
		replay(userDataRequest);
		
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet(userDataRequest);
		
		activeSyncServlet.service(request, response);
		
		verify(userDataRequest);
	}
	
	private LoginService createLoginService() throws AuthFault {
		LoginService loginService = createMock(LoginService.class);
		expect(loginService.authenticate(user.getLogin(), "test")).andReturn(new AccessToken(1, "o-push")).anyTimes();
		return loginService;
	}
	
	private SessionService createSessionService(UserDataRequest userDataRequest) throws DaoException {
		SessionService sessionService = createMock(SessionService.class);
		expect(sessionService.getSession(eq(new Credentials(user, "test")), eq(deviceId), anyObject(ActiveSyncRequest.class)))
			.andReturn(userDataRequest).anyTimes();
		return sessionService;
	}
	
	private LoggerService createLoggerService() {
		LoggerService loggerService = createMock(LoggerService.class);
		loggerService.closeSession();
		expectLastCall();
		loggerService.initSession(user, requestId, command);
		return loggerService;
	}
	
	private IBackend createBackend() {
		IBackend backend = createMock(IBackend.class);
		return backend;
	}

	private PushContinuation.Factory bindContinuation() {
		PushContinuation pushContinuation = createMock(PushContinuation.class);
		expect(pushContinuation.isResumed()).andReturn(false).anyTimes();
		expect(pushContinuation.isInitial()).andReturn(true).anyTimes();
		expect(pushContinuation.getReqId()).andReturn(requestId).anyTimes();
		replay(pushContinuation);
		
		PushContinuation.Factory pushContinuationFactory = createMock(PushContinuation.Factory.class);
		expect(pushContinuationFactory.createContinuation(request)).andReturn(pushContinuation).anyTimes();
		
		return pushContinuationFactory;
	}
	
	private DeviceService createDeviceService() throws DaoException {
		DeviceService deviceService = createMock(DeviceService.class);
		expect(deviceService.initDevice(user, deviceId, deviceType, userAgent)).andReturn(true).anyTimes();
		expect(deviceService.syncAuthorized(user, deviceId)).andReturn(true).anyTimes();
		return deviceService;
	}

	private User.Factory createUserFactory() {
		User.Factory userFactory = createMock(User.Factory.class);
		expect(userFactory.getLoginAtDomain(user.getLogin())).andReturn(user.getLogin()).anyTimes();
		expect(userFactory.createUser(user.getLogin(), null, null)).andReturn(user).anyTimes();
		return userFactory;
	}

	private ResponderImpl.Factory createResponderFactory() {
		ResponderImpl.Factory responderFactory = createMock(ResponderImpl.Factory.class);
		expect(responderFactory.createResponder(response)).andReturn(null).anyTimes();
		return responderFactory;
	}

	private Handlers createHandlers(UserDataRequest userDataRequest) throws IOException {
		IRequestHandler requestHandler = createMock(IRequestHandler.class);
		requestHandler.process(anyObject(IContinuation.class), eq(userDataRequest), anyObject(ActiveSyncRequest.class), (Responder) eq(null));
		expectLastCall();
		
		Handlers handlers = createMock(Handlers.class);
		expect(handlers.getHandler(command)).andReturn(requestHandler).anyTimes();
		return handlers;
	}

	private Logger createLogger() {
		Logger logger = createMock(Logger.class);
		Object[] array = new Object[] { user.getEmail(), deviceType};
		logger.info(EasyMock.anyObject(String.class), EasyMock.aryEq(array));
		expectLastCall();
		logger.debug(anyObject(String.class));
		expectLastCall();
		logger.warn(anyObject(String.class));
		expectLastCall();
		return logger;
	}

	private ActiveSyncServlet createActiveSyncServlet(UserDataRequest userDataRequest) throws AuthFault, DaoException, IOException {
		LoginService loginService = createLoginService();
		SessionService sessionService = createSessionService(userDataRequest);
		LoggerService loggerService = createLoggerService();
		IBackend backend = createBackend();
		PushContinuation.Factory pushContinuationFactory = bindContinuation();
		DeviceService deviceService = createDeviceService();
		User.Factory userFactory = createUserFactory();
		ResponderImpl.Factory responderFactory = createResponderFactory();
		Handlers handlers = createHandlers(userDataRequest);
		
		Logger logger = createLogger();
		
		replay(loginService, 
				sessionService, 
				loggerService, 
				backend, 
				pushContinuationFactory, 
				deviceService, userFactory, 
				responderFactory, 
				handlers, 
				logger);
		
		return new ActiveSyncServlet(loginService, sessionService, loggerService, backend, pushContinuationFactory, deviceService, 
								userFactory, responderFactory, handlers, logger);
	}
}
