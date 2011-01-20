/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.login.LoginBindingImpl;
import org.obm.sync.server.ParametersSource;
import org.obm.sync.server.XmlResponder;

import com.google.inject.Inject;

/**
 * Responds to the following urls :
 * 
 * <code>/login/doLogin?login=xx&password=yy</code>
 */
public class LoginHandler implements ISyncHandler {

	private Log logger = LogFactory.getLog(getClass());
	private LoginBindingImpl binding;

	@Inject
	private LoginHandler(LoginBindingImpl loginBindingImpl) {
		this.binding = loginBindingImpl;
	}

	@Override
	public void handle(String method, ParametersSource params,
			XmlResponder responder) throws Exception {
		logger.info("method: " + method);

		if ("doLogin".equals(method)) {
			doLogin(params, responder);
		} else if ("doLogout".equals(method)) {
			doLogout(params);
		} else {
			responder.sendError("unsupported method: " + method);
			return;
		}

	}

	private void doLogout(ParametersSource params) {
		binding.logout(params.getParameter("sid"));
	}

	private void doLogin(ParametersSource params, XmlResponder responder) {
		String login = params.getParameter("login");
		String pass = params.getParameter("password");
		String origin = params.getParameter("origin");

		if (origin == null) {
			responder.sendError("login refused with null origin");
			return;
		}

		if (logger.isDebugEnabled()) {
			params.dumpHeaders();
		}
		
		AccessToken token = binding.logUserIn(login, pass, origin, 
				params.getClientIP(), params.getRemoteIP(), 
				params.getLemonLdapLogin(), params.getLemonLdapDomain());
		if (token != null) {
			responder.sendToken(token);
		} else {
			responder.sendError("Login failed for user '" + login
					+ "' with password '" + pass + "'");
		}
	}
}
