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
package org.obm.push.handler;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.LoggerService;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.BadRequestException;
import org.obm.sync.client.login.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthenticatedServlet extends HttpServlet {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Logger authLogger;
	
	private final LoggerService loggerService;
	private final User.Factory userFactory;
	private final LoginService loginService;

	protected AuthenticatedServlet(LoginService loginService, 
			LoggerService loggerService, User.Factory userFactory, Logger authLogger) {
		
		this.loginService = loginService;
		this.loggerService = loggerService;
		this.userFactory = userFactory;
		this.authLogger = authLogger;
	}

	protected void returnHttpUnauthorized(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		returnHttpError(httpServletRequest, response, HttpServletResponse.SC_UNAUTHORIZED);
	}

	protected void returnHttpBadRequest(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		returnHttpError(httpServletRequest, response, HttpServletResponse.SC_BAD_REQUEST);
	}

	private void returnHttpError(HttpServletRequest httpServletRequest,
			HttpServletResponse response, int status) {
		authLogger.info("Invalid authorization format, sending http {} ( uri = {}{}{} )", 
				new Object[] { 
					status,
					httpServletRequest.getMethod(), 
					httpServletRequest.getRequestURI(), 
					httpServletRequest.getQueryString()});
		
		String s = "Basic realm=\"OBMPushService\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(status);
	}

	protected Credentials authentication(HttpServletRequest request) throws AuthFault, BadRequestException {
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
						String userId = userPass.substring(0, p);
						String password = userPass.substring(p + 1);
						return getCredentials(userId, password);
					}
				}
			}
		}
		throw new AuthFault("There is not 'Authorization' field in HttpServletRequest.");
	}

	private Credentials getCredentials(String userId, String password) throws AuthFault, BadRequestException {
		AccessToken accessToken = login(getLoginAtDomain(userId), password);
		User user = createUser(userId, accessToken);
		if (user != null) {
			logger.debug("Login success {} ! ", user.getLoginAtDomain());
			return new Credentials(user, password);
		} else {
			throw new AuthFault("Login {"+ userId + "} failed, bad login or/and password.");
		}
	}
	
	private AccessToken login(String userId, String password) throws AuthFault {
		return loginService.authenticate(userFactory.getLoginAtDomain(userId), password);
	}
	
	private User createUser(String userId, AccessToken accessToken) {
		return userFactory.createUser(userId, accessToken.getUserEmail(), accessToken.getUserDisplayName());
	}

	protected String getLoginAtDomain(String userId) throws BadRequestException {
		try {
			return userFactory.getLoginAtDomain(userId);
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("The userId arg should matchs \"user@domain\" pattern : " + userId, e);
		}
	}

	public LoggerService getLoggerService() {
		return loggerService;
	}
	
}
