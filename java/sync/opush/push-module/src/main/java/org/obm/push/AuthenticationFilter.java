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
import org.apache.http.client.HttpClient;
import org.obm.push.backend.IAccessTokenResource;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.resource.HttpClientResource;
import org.obm.push.service.AuthenticationService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationFilter implements Filter {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final AuthenticationService authenticationService;
	private final LoggerService loggerService;
	private final User.Factory userFactory;
	private final HttpErrorResponder httpErrorResponder;
	private final HttpClientService httpClientService;


	@Inject
	private AuthenticationFilter(AuthenticationService authenticationService, 
			LoggerService loggerService, 
			User.Factory userFactory, 
			HttpErrorResponder httpErrorResponder,
			HttpClientService httpClientService) {
		
		this.authenticationService = authenticationService;
		this.loggerService = loggerService;
		this.userFactory = userFactory;
		this.httpErrorResponder = httpErrorResponder;
		this.httpClientService = httpClientService;
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
		} catch (AuthFault e) {
			logger.info(e.getMessage());
			httpErrorResponder.returnHttpUnauthorized(httpRequest, httpResponse);
		}

	}

	private Credentials authentication(HttpServletRequest request) throws AuthFault {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = new String( Base64.decodeBase64(credentials) );
					int p = userPass.indexOf(":");
					if (p != -1) {
						return authenticateValidRequest(request, 
								userPass.substring(0, p), 
								userPass.substring(p + 1));
					}
				}
			}
		}
		throw new AuthFault("There is not 'Authorization' field in HttpServletRequest.");
	}

	private Credentials authenticateValidRequest(HttpServletRequest request, String userId, String password) throws AuthFault {
		HttpClientResource httpClientResource = httpClientService.setHttpClientRequestAttribute(request);
		
		IAccessTokenResource token = setAccessTokenRequestAttribute(request, 
				httpClientResource.getHttpClient(), userId, password);
		return getCredentials(token, userId, password);
	}

	private Credentials getCredentials(IAccessTokenResource token, String userId, String password) throws AuthFault {
		User user = createUser(userId, (AccessToken) token.getAccessToken());
		if (user != null) {
			logger.debug("Login success {} ! ", user.getLoginAtDomain());
			return new Credentials(user, password);
		} else {
			throw new AuthFault("Login {"+ userId + "} failed, bad login or/and password.");
		}
	}

	private IAccessTokenResource setAccessTokenRequestAttribute(HttpServletRequest request, HttpClient httpClient, String userId, String password) throws AuthFault {
		IAccessTokenResource token = login(httpClient, getLoginAtDomain(userId), password);
		request.setAttribute(RequestProperties.ACCESS_TOKEN_RESOURCE, token);
		return token;
	}
	
	private IAccessTokenResource login(HttpClient httpClient, String userId, String password) throws AuthFault {
		try {
			return authenticationService.authenticate(httpClient, userId, password);
		} catch (Exception e) {
			throw new AuthFault(e);
		}
	}
	
	private User createUser(String userId, AccessToken token) {
		return userFactory.createUser(userId, token.getUserEmail(), token.getUserDisplayName());
	}

	protected String getLoginAtDomain(String userId) {
		return userFactory.getLoginAtDomain(userId);
	}
	
	@Override
	public void destroy() {
	}
	
	
}
