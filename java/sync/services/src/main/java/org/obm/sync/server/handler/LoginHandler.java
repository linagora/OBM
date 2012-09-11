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
package org.obm.sync.server.handler;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.login.LoginBindingImpl;
import org.obm.sync.server.Request;
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
	public void handle(Request request,
			XmlResponder responder) throws Exception {
		String method = request.getMethod();

		if ("doLogin".equals(method)) {
			doLogin(request, responder);
		} else if ("doLogout".equals(method)) {
			doLogout(request);
		} else {
			responder.sendError("unsupported method: " + method);
			return;
		}

	}

	private void doLogout(Request request) {
		request.destroySession();
		binding.logout(request.getParameter("sid"));
	}

		
	private void doLogin(Request request, XmlResponder responder) {
		request.createSession();
		String login = request.getParameter("login");
		String pass = request.getParameter("password");
		String origin = request.getParameter("origin");
	    
        boolean isPasswordHashed = request.getParameter("isPasswordHashed") != null ? Boolean
				.valueOf(request.getParameter("isPasswordHashed")) : false;
	
		try {
			if (origin == null) {
				responder.sendError("login refused with null origin");
				return;
			}
		
			if (logger.isDebugEnabled()) {
				request.dumpHeaders();
			}
			
			AccessToken token = binding.logUserIn(login, pass, origin, request.getClientIP(), request.getRemoteIP(),
					request.getLemonLdapLogin(), request.getLemonLdapDomain(), isPasswordHashed);
			
			if (token != null) {
				versionValidator.checkObmConnectorVersion(token);
				responder.sendToken(token);
			} else {
				responder.sendError("Login failed for user '" + login + "'");
			}
		} catch (OBMConnectorVersionException e) {
			logger.error(e.getToken().getOrigin() + " is not supported anymore.");
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
