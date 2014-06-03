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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.client.HttpClientBuilder;
import org.obm.imap.archive.exception.AuthenticationException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.client.login.LoginClient.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Factory loginClientFactory;

	@Inject
	@VisibleForTesting AuthenticationFilter(LoginClient.Factory loginClientFactory) {
		this.loginClientFactory = loginClientFactory;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			authentication(httpRequest);
			
			chain.doFilter(request, response);
		} catch (AuthFault e) {
			logger.info(e.getMessage());
			returnHttpAuthenticationError(httpResponse);
		}
	}

	private void returnHttpAuthenticationError(HttpServletResponse response) {
		String s = "Basic realm=\"OBMIMAPArchive\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@VisibleForTesting AccessToken authentication(HttpServletRequest request) throws AuthFault {
		String login = getMandatoryParameter(request, "login");
		String password = getMandatoryParameter(request, "password");
		String domainName = getMandatoryParameter(request, "domain_name");
		
		LoginClient loginClient = loginClient();
		return loginClient.trustedLogin(loginAtDomain(login, domainName), password);
	}

	protected LoginClient loginClient() {
		return loginClientFactory.create(HttpClientBuilder.create().build());
	}
	
	private String loginAtDomain(String login, String domainName) {
		return login + "@" + domainName;
	}

	private String getMandatoryParameter(HttpServletRequest request, String parameterName) {
		String value = request.getParameter(parameterName);
		if (Strings.isNullOrEmpty(value)) {
			throw new AuthenticationException("A mandatory parameter is missing in HttpServletRequest: '" + parameterName + "'");
		}
		return value;
	}

	@Override
	public void destroy() {
	}
}
