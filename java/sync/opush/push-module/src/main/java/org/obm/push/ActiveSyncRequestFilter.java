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
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.protocol.request.Base64QueryString;
import org.obm.push.protocol.request.SimpleQueryString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ActiveSyncRequestFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LoggerService loggerService;
	
	@Inject
	private ActiveSyncRequestFilter(LoggerService loggerService) {
		this.loggerService = loggerService;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (httpRequest.getMethod().equals("POST")) {
			ActiveSyncRequest activeSyncRequest = getActiveSyncRequest(httpRequest);
			loggerService.defineCommand(activeSyncRequest.getCommand());
			httpRequest.setAttribute(RequestProperties.ACTIVE_SYNC_REQUEST, activeSyncRequest);
		}
		chain.doFilter(httpRequest, response);
	}

	private ActiveSyncRequest getActiveSyncRequest(HttpServletRequest request) {
		String qs = request.getQueryString();
		if (qs.contains("Cmd=")) {
			return new SimpleQueryString(request);
		} else {
			InputStream is = null;
			try {
				is = request.getInputStream();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return new Base64QueryString(request, is);
		}
	}
	
	@Override
	public void destroy() {
	}

}
