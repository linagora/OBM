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

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.bean.Credentials;
import org.obm.push.exception.AuthenticationException;
import org.obm.push.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationFilter implements Filter {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final LoggerService loggerService;
	private final AuthenticationService authenticationService;
	private final HttpErrorResponder httpErrorResponder;
	
	@Inject
	@VisibleForTesting AuthenticationFilter(AuthenticationService authenticationService, 
			LoggerService loggerService, 
			HttpErrorResponder httpErrorResponder) {
		
		this.authenticationService = authenticationService;
		this.loggerService = loggerService;
		this.httpErrorResponder = httpErrorResponder;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			if ("POST".equals(httpRequest.getMethod())) {
				Credentials credentials = authentication(httpRequest);
				loggerService.defineUser(credentials.getUser());
				httpRequest.setAttribute(RequestProperties.CREDENTIALS, credentials);
			}
			chain.doFilter(request, response);
		} catch (AuthenticationException e) {
			logger.info(e.getMessage());
			httpErrorResponder.returnHttpUnauthorized(httpRequest, httpResponse);
		} finally {
			loggerService.closeSession();
		}
	}

	private Credentials authentication(HttpServletRequest request) throws AuthenticationException {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = new String( Base64.decodeBase64(credentials), Charsets.ISO_8859_1 );
					int p = userPass.indexOf(":");
					if (p != -1) {
						return authenticateValidRequest(request, 
								userPass.substring(0, p), 
								userPass.substring(p + 1));
					}
				}
			}
		}
		throw new AuthenticationException("There is not 'Authorization' field in HttpServletRequest.");
	}

	private Credentials authenticateValidRequest(HttpServletRequest request, String userId, String password) throws AuthenticationException {
		try {
			return authenticationService.authenticateValidRequest(request, userId, password);
		} catch (Exception e) {
			throw new AuthenticationException(e);
		}
	}
	
	@Override
	public void destroy() {
	}
	
	
}
