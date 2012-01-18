package org.obm.push.handler;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.LoggerService;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthenticatedServlet extends HttpServlet {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final LoggerService loggerService;
	private final Factory userFactory;
	private final LoginService loginService;

	protected AuthenticatedServlet(LoginService loginService, 
			LoggerService loggerService, Factory userFactory) {
		
		this.loginService = loginService;
		this.loggerService = loggerService;
		this.userFactory = userFactory;
	}

	protected void returnHttpUnauthorized(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		logger.warn("invalid auth, sending http 401 ( uri = {}{}{} )", 
				new Object[] { 
					httpServletRequest.getMethod(), 
					httpServletRequest.getRequestURI(), 
					httpServletRequest.getQueryString()});
		
		String s = "Basic realm=\"OBMPushService\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	protected Credentials authentication(HttpServletRequest request) throws AuthFault {
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

	private Credentials getCredentials(String userId, String password) throws AuthFault {
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

	protected String getLoginAtDomain(String userId) {
		return userFactory.getLoginAtDomain(userId);
	}

	public LoggerService getLoggerService() {
		return loggerService;
	}
	
}
