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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.handler.AuthenticatedServlet;
import org.obm.push.handler.AutodiscoverHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.impl.ResponderImpl.Factory;
import org.obm.push.protocol.request.SimpleQueryString;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.BadRequestException;
import org.obm.sync.client.login.LoginService;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class AutodiscoverServlet extends AuthenticatedServlet {

	private final AutodiscoverHandler autodiscoverHandler;
	private final Factory responderFactory;
	private final UserDataRequest.Factory userDataRequestFactory;
	
	@Inject
	protected AutodiscoverServlet(LoginService loginService, AutodiscoverHandler autodiscoverHandler, 
			User.Factory userFactory, LoggerService loggerService, ResponderImpl.Factory responderFactory, 
			@Named(LoggerModule.AUTH)Logger authLogger, UserDataRequest.Factory userDataRequestFactory) {
		
		super(loginService, loggerService, userFactory, authLogger);
		this.autodiscoverHandler = autodiscoverHandler;
		this.responderFactory = responderFactory;
		this.userDataRequestFactory = userDataRequestFactory;
	}

	@Override
	@Transactional
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		UserDataRequest userDataRequest = null;
		try {
			Credentials credentials = authentication(request);
			getLoggerService().initSession(credentials.getUser(), 0, "autodiscover");
			
			userDataRequest = userDataRequestFactory.createUserDataRequest(credentials, "autodiscover", null, null);
			SimpleQueryString queryString = new SimpleQueryString(request);
			Responder responder = responderFactory.createResponder(response);
			
			autodiscoverHandler.process(null, userDataRequest, queryString, responder);
		} catch (AuthFault e) {
			authLogger.info(e.getMessage());
			returnHttpUnauthorized(request, response);
			return;
		} catch (BadRequestException e) {
			logger.warn(e.getMessage());
			returnHttpBadRequest(request, response);
			return;
		} finally {
			if (userDataRequest != null) {
				userDataRequest.closeResources();
			}
		}

	}
	
}
