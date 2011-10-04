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

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.login.LoginBindingImpl;
import org.obm.sync.server.ParametersSource;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.server.mailer.ErrorMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;

/**
 * Responds to the following urls :
 * 
 * <code>/login/doLogin?login=xx&password=yy</code>
 */
public class LoginHandler implements ISyncHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LoginBindingImpl binding;
	private final ErrorMailer errorMailer;
	private final VersionValidator versionValidator;
	private final SettingsService settingsService;
	private final UserService userService;

	@Inject
	private LoginHandler(LoginBindingImpl loginBindingImpl,
			ErrorMailer errorMailer, SettingsService settingsService,
			VersionValidator versionValidator, UserService userService) {
		this.binding = loginBindingImpl;
		this.errorMailer = errorMailer;
		this.settingsService = settingsService;
		this.versionValidator = versionValidator;
		this.userService = userService;
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
		
		try {
			if (origin == null) {
				responder.sendError("login refused with null origin");
				return;
			}
		
			if (logger.isDebugEnabled()) {
				params.dumpHeaders();
			}
			
			AccessToken token = binding.logUserIn(login, pass, origin, params.getClientIP(), params.getRemoteIP(),
					params.getLemonLdapLogin(), params.getLemonLdapDomain());
			
			if (token != null) {
				versionValidator.checkObmConnectorVersion(token);
				responder.sendToken(token);
			} else {
				responder.sendError("Login failed for user '" + login
						+ "' with password '" + pass + "'");
			}
		} catch (OBMConnectorVersionException e) {
			logger.error(e.getToken().getOrigin() + " isn't longer suppored.");
			ObmUser user = userService.getUserFromAccessToken(e.getToken());
			UserSettings settings = settingsService.getSettings(user);
			errorMailer.notifyConnectorVersionError(e.getToken(), e
					.getConnectorVersion().toString(), settings.locale(),
					settings.timezone());
		} catch (ObmSyncVersionNotFoundException e) {
			responder.sendError("Invalid obm-sync server version");
		}
		
	}
}
