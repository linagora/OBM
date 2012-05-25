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
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.handler.AutodiscoverHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;

@RunWith(SlowFilterRunner.class)
public class AutodiscoverServletTest {

	private User user;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private AutodiscoverHandler autodiscoverHandler;
	private Responder responder;
	private UserDataRequest userDataRequest;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		
		request = createMock(HttpServletRequest.class);
		String credentialsString = user.getLogin() + ":test"; 
		expect(request.getHeader("Authorization"))
			.andReturn("Basic " + Base64.encodeBase64String(credentialsString.getBytes())).anyTimes();
		replay(request);
		
		response = createMock(HttpServletResponse.class);
		autodiscoverHandler = createMock(AutodiscoverHandler.class);
	}
	
	@Test
	public void testClosingUserDataRequestResources() throws ServletException, IOException, AuthFault {
		userDataRequest = createMock(UserDataRequest.class);
		userDataRequest.closeResources();
		expectLastCall();
		replay(userDataRequest);
		
		AutodiscoverServlet autodiscoverServlet = createAutodiscoverServlet(autodiscoverHandler);
		
		autodiscoverServlet.service(request, response);
		
		verify(userDataRequest);
	}
	
	private LoginService bindAuthentication() throws AuthFault {
		LoginService loginService = createMock(LoginService.class);
		expect(loginService.authenticate(user.getLogin(), "test")).andReturn(new AccessToken(1, "o-push")).anyTimes();
		return loginService;
	}

	private User.Factory createUser() {
		User.Factory userFactory = createMock(User.Factory.class);
		expect(userFactory.getLoginAtDomain(user.getLogin())).andReturn(user.getLogin()).anyTimes();
		expect(userFactory.createUser(user.getLogin(), null, null)).andReturn(user).anyTimes();
		return userFactory;
	}
	
	private LoggerService bindLoggerService() {
		LoggerService loggerService = createMock(LoggerService.class);
		loggerService.initSession(user, 0, "autodiscover");
		expectLastCall();
		return loggerService;
	}
	
	private ResponderImpl.Factory bindResponderFactory(Responder responder) {
		ResponderImpl.Factory responderFactory = createMock(ResponderImpl.Factory.class);
		expect(responderFactory.createResponder(response)).andReturn(responder).anyTimes();
		return responderFactory;
	}

	private void bindProcess(Responder responder) throws IOException {
		autodiscoverHandler.process(isNull(IContinuation.class), 
									anyObject(UserDataRequest.class), 
									anyObject(ActiveSyncRequest.class), 
									eq(responder));
		expectLastCall();
	}
	
	private AutodiscoverServlet createAutodiscoverServlet(AutodiscoverHandler autodiscoverHandler) 
			throws AuthFault, IOException {
		
		LoginService loginService = bindAuthentication();
		User.Factory userFactory = createUser();
		LoggerService loggerService = bindLoggerService();
		
		responder = createMock(Responder.class);
		ResponderImpl.Factory responderFactory = bindResponderFactory(responder);
		bindProcess(responder);
		replay(loginService, autodiscoverHandler, userFactory, loggerService, responderFactory);

		UserDataRequest.Factory userDataRequestFactory = createMock(UserDataRequest.Factory.class);
		expect(userDataRequestFactory.createUserDataRequest(new Credentials(user, "test"), "autodiscover", null, null))
			.andReturn(userDataRequest).anyTimes();
		replay(userDataRequestFactory);
		
		return new AutodiscoverServlet(
					loginService,
					autodiscoverHandler, 
					userFactory, 
					loggerService, 
					responderFactory, 
					null, 
					userDataRequestFactory);
	}
}
