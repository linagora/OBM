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
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
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
		httpServletRequest = mocks.createMock(HttpServletRequest.class);
		httpServletResponse = mocks.createMock(HttpServletResponse.class);
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
	public void doFilterWhenBadParameters() throws Exception {
		String login = "user";
		String domainName = "mydomain.org";
		String password = "password";
		expect(httpServletRequest.getParameter("login")).andReturn(login);
		expect(httpServletRequest.getParameter("domain_name")).andReturn(domainName);
		expect(httpServletRequest.getParameter("password")).andReturn(password);
		
		httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"OBMIMAPArchive\"");
		expectLastCall();
		httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		expectLastCall();
		
		expect(loginClient.trustedLogin(login + "@" + domainName, password)).andThrow(new AuthFault("Bad password"));
		
		mocks.replay();
		authenticationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		mocks.verify();
	}
	
	@Test
	public void doFilterAuthorization() throws Exception {
		String login = "user";
		String domainName = "mydomain.org";
		String password = "password";
		expect(httpServletRequest.getParameter("login")).andReturn(login);
		expect(httpServletRequest.getParameter("domain_name")).andReturn(domainName);
		expect(httpServletRequest.getParameter("password")).andReturn(password);
		
		expect(loginClient.trustedLogin(login + "@" + domainName, password)).andReturn(new AccessToken(1, "origin"));
		
		filterChain.doFilter(httpServletRequest, httpServletResponse);
		expectLastCall();
		
		mocks.replay();
		authenticationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenNoLoginParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn(null);
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'login'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenEmptyLoginParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn("");
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'login'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenNoPasswordParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn("user");
		expect(httpServletRequest.getParameter("password")).andReturn(null);
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'password'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenEmptyPasswordParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn("user");
		expect(httpServletRequest.getParameter("password")).andReturn("");
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'password'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenNoDomainNameParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn("user");
		expect(httpServletRequest.getParameter("password")).andReturn("password");
		expect(httpServletRequest.getParameter("domain_name")).andReturn(null);
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'domain_name'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
	
	@Test
	public void authenticationWhenEmptyDomainNameParameter() throws Exception {
		expect(httpServletRequest.getParameter("login")).andReturn("user");
		expect(httpServletRequest.getParameter("password")).andReturn("password");
		expect(httpServletRequest.getParameter("domain_name")).andReturn("");
		
		expectedException.expect(AuthenticationException.class).hasMessage("A mandatory parameter is missing in HttpServletRequest: 'domain_name'");
		
		mocks.replay();
		authenticationFilter.authentication(httpServletRequest);
		mocks.verify();
	}
}
