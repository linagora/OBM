/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.authentication;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.imap.archive.exception.AuthenticationException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.client.login.LoginClient.Factory;

import pl.wkr.fluentrule.api.FluentExpectedException;


public class AuthenticationFilterTest {

	private LoginClient.Factory loginClientFactory;
	private LoginClient loginClient;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FilterChain filterChain;
	private AuthenticationFilter authenticationFilter;
	
	private IMocksControl mocks;
	
	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setup() {
		mocks = EasyMock.createControl();
		
		loginClientFactory = mocks.createMock(LoginClient.Factory.class);
		loginClient = mocks.createMock(LoginClient.class);
		request = mocks.createMock(HttpServletRequest.class);
		response = mocks.createMock(HttpServletResponse.class);
		filterChain = mocks.createMock(FilterChain.class);
		
		authenticationFilter = new AuthenticationFilterMockedHttpClient(loginClientFactory);
	}
	
	private class AuthenticationFilterMockedHttpClient extends AuthenticationFilter {
		
		public AuthenticationFilterMockedHttpClient(Factory loginClientFactory) {
			super(loginClientFactory);
		}

		@Override
		protected LoginClient loginClient() {
			return loginClient;
		}
	}
	
	@Test
	public void doFilterWhenBadCredentials() throws Exception {
		String loginAtDomain = "admin@mydomain.org";
		String password = "trust3dToken";
		String header = "Basic YWRtaW5AbXlkb21haW4ub3JnOnRydXN0M2RUb2tlbg==";
		expect(request.getHeader("Authorization")).andReturn(header);

		response.setHeader("WWW-Authenticate", "Basic realm=\"OBMIMAPArchive\"");
		expectLastCall();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		expectLastCall();
		
		expect(loginClient.trustedLogin(loginAtDomain, password)).andThrow(new AuthFault("Bad password"));
		
		mocks.replay();
		authenticationFilter.doFilter(request, response, filterChain);
		mocks.verify();
	}
	
	@Test
	public void doFilterWhenRightCredentials() throws Exception {
		String loginAtDomain = "admin@mydomain.org";
		String password = "trust3dToken";
		String header = "Basic YWRtaW5AbXlkb21haW4ub3JnOnRydXN0M2RUb2tlbg==";
		expect(request.getHeader("Authorization")).andReturn(header);
		
		expect(loginClient.trustedLogin(loginAtDomain, password)).andReturn(new AccessToken(1, "origin"));
		
		filterChain.doFilter(request, response);
		expectLastCall();
		
		mocks.replay();
		authenticationFilter.doFilter(request, response, filterChain);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenNoAuthorization() throws Exception {
		expect(request.getHeader("Authorization")).andReturn(null);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		expectLastCall();
		
		expectedException.expect(AuthenticationException.class)
			.hasMessage("The request has no 'Authorization' header");
		
		mocks.replay();
		authenticationFilter.authenticate(request);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenBadBasicAuthorizationFormat() throws Exception {
		expect(request.getHeader("Authorization")).andReturn("Basic blabla");

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		expectLastCall();
		
		expectedException.expect(AuthenticationException.class)
			.hasMessageEndingWith("Cannot build credentials from the given header");
		
		mocks.replay();
		authenticationFilter.authenticate(request);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenBadAuthorizationFormat() throws Exception {
		expect(request.getHeader("Authorization")).andReturn("givenMethod unsupported");

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		expectLastCall();
		
		expectedException.expect(AuthenticationException.class)
			.hasMessageEndingWith("Only 'Basic' authentication is supported: givenMethod");
		
		mocks.replay();
		authenticationFilter.authenticate(request);
		mocks.verify();
	}
}
