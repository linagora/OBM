/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.service.AuthenticationService;


public class AuthenticationFilterTest {

	private IMocksControl control;
	private LoggerService loggerService;
	private AuthenticationService authService;
	private HttpErrorResponder errorResponder;
	
	private AuthenticationFilter testee;

	@Before
	public void setUp() {
		control = createControl();
		loggerService = control.createMock(LoggerService.class);
		authService = control.createMock(AuthenticationService.class);
		errorResponder = control.createMock(HttpErrorResponder.class);
		
		testee = new AuthenticationFilter(authService, loggerService, errorResponder);
	}
	
	@Test
	public void postCallCloseSession() throws Exception {
		HttpServletRequest request = control.createMock(HttpServletRequest.class);
		HttpServletResponse response = control.createMock(HttpServletResponse.class);
		FilterChain chain = control.createMock(FilterChain.class);
		
		expectAuthentication(request);
		expect(request.getMethod()).andReturn("POST");
		chain.doFilter(request, response);
		expectLastCall();
		loggerService.closeSession();
		expectLastCall();
		
		control.replay();
		testee.doFilter(request, response, chain);
		control.verify();
	}
	
	@Test(expected=RuntimeException.class)
	public void postCallCloseSessionWhenExceptionByChain() throws Exception {
		HttpServletRequest request = control.createMock(HttpServletRequest.class);
		HttpServletResponse response = control.createMock(HttpServletResponse.class);
		FilterChain chain = control.createMock(FilterChain.class);
		
		expectAuthentication(request);
		expect(request.getMethod()).andReturn("POST");
		chain.doFilter(request, response);
		expectLastCall().andThrow(new RuntimeException("Might be exception"));
		loggerService.closeSession();
		expectLastCall();
		
		control.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}

	@Test
	public void postCallCloseSessionWhenExceptionByAuth() throws Exception {
		HttpServletRequest request = control.createMock(HttpServletRequest.class);
		HttpServletResponse response = control.createMock(HttpServletResponse.class);
		FilterChain chain = control.createMock(FilterChain.class);

		expect(request.getHeader("Authorization")).andReturn(null);
		errorResponder.returnHttpUnauthorized(request, response);
		expectLastCall();
		expect(request.getMethod()).andReturn("POST");
		loggerService.closeSession();
		expectLastCall();
		
		control.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}
	
	@Test
	public void getCallCloseSession() throws Exception {
		HttpServletRequest request = control.createMock(HttpServletRequest.class);
		HttpServletResponse response = control.createMock(HttpServletResponse.class);
		FilterChain chain = control.createMock(FilterChain.class);
		
		expect(request.getMethod()).andReturn("GET");
		chain.doFilter(request, response);
		expectLastCall();
		loggerService.closeSession();
		expectLastCall();
		
		control.replay();
		testee.doFilter(request, response, chain);
		control.verify();
	}

	private void expectAuthentication(HttpServletRequest request) throws Exception {
		User user = control.createMock(User.class);
		Credentials credentials = new Credentials(user, "open sesame");
		expect(request.getHeader("Authorization")).andReturn("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
		expect(authService.authenticateValidRequest(request, "Aladdin", "open sesame")).andReturn(credentials);
		loggerService.defineUser(user);
		expectLastCall();
		request.setAttribute("credentials", credentials);
		expectLastCall();
	}
	
}
