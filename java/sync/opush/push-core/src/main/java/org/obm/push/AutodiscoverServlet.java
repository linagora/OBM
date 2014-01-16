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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.handler.AutodiscoverHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.impl.ResponderImpl.Factory;
import org.obm.push.protocol.request.SimpleQueryString;
import org.obm.push.resource.ResourcesService;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AutodiscoverServlet extends HttpServlet {

	private final AutodiscoverHandler autodiscoverHandler;
	private final Factory responderFactory;
	private final UserDataRequest.Factory userDataRequestFactory;
	private final LoggerService loggerService;
	private final Set<ResourcesService> resourcesServices;
	
	@Inject
	@VisibleForTesting AutodiscoverServlet(AutodiscoverHandler autodiscoverHandler, 
			ResponderImpl.Factory responderFactory, 
			UserDataRequest.Factory userDataRequestFactory,
			LoggerService loggerService,
			Set<ResourcesService> resourcesServices) {
		
		super();
		this.autodiscoverHandler = autodiscoverHandler;
		this.responderFactory = responderFactory;
		this.userDataRequestFactory = userDataRequestFactory;
		this.loggerService = loggerService;
		this.resourcesServices = resourcesServices;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		UserDataRequest userDataRequest = null;
		try {
			Credentials credentials = getCheckedCredentials(request);
			loggerService.defineCommand("autodiscover");
			
			userDataRequest = userDataRequestFactory.createUserDataRequest(credentials, "autodiscover", null);
			SimpleQueryString queryString = new SimpleQueryString(request);
			Responder responder = responderFactory.createResponder(request, response);
			
			autodiscoverHandler.process(null, userDataRequest, queryString, responder);
		} finally {
			if (userDataRequest != null) {
				for (ResourcesService resourcesService: resourcesServices) {
					resourcesService.closeResources(userDataRequest);
				}
			}
		}

	}

	public Credentials getCheckedCredentials(HttpServletRequest request) {
		Credentials credentials = (Credentials) request.getAttribute(RequestProperties.CREDENTIALS);
		if (credentials == null) {
			throw new IllegalStateException("Credentials must be handled by " + AuthenticationFilter.class.getSimpleName());
		}
		return credentials;
	}
}
