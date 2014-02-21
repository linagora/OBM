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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.push.configuration.LoggerModule;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class HttpErrorResponder {
	
	private final Logger authLogger;

	@Inject
	private HttpErrorResponder(@Named(LoggerModule.AUTH)Logger authLogger) {
		this.authLogger = authLogger;
	}
	
	public void returnHttpUnauthorized(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		returnHttpError(httpServletRequest, response, HttpServletResponse.SC_UNAUTHORIZED);
	}

	public void returnHttpBadRequest(HttpServletRequest httpServletRequest, HttpServletResponse response) {
		returnHttpError(httpServletRequest, response, HttpServletResponse.SC_BAD_REQUEST);
	}

	private void returnHttpError(HttpServletRequest httpServletRequest,
			HttpServletResponse response, int status) {
		authLogger.info("Invalid authorization format, sending http {} ( uri = {}{}{} )", 
					status,
					httpServletRequest.getMethod(), 
					httpServletRequest.getRequestURI(), 
					httpServletRequest.getQueryString());
		
		String s = "Basic realm=\"OBMPushService\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(status);
	}
}