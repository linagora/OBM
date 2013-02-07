/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.servlet.filter.qos;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * A reusable servlet {@link Filter} able to reject and throttle requests based on some business logic.
 */
@Singleton
public class QoSFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final QoSRequestHandler handler;
	private final QoSContinuationSupport continuationSupport;
	private final CacheManager cacheManager;

	@Inject
	@VisibleForTesting QoSFilter(QoSRequestHandler handler, QoSContinuationSupport continuationSupport, 
			@Named(QoSFilterModule.CONCURRENT_REQUEST_INFO_STORE) CacheManager cacheManager) {
		this.handler = handler;
		this.continuationSupport = continuationSupport;
		this.cacheManager = cacheManager;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * Actually performs the filtering.<br />
	 * This should delegate to our {@link QoSRequestHandler} implementation to:
	 * <ol>
	 * <li>Decide whether or not the request can be processed, based on some business criteria (the user sending the request, etc.).</li>
	 * <li>Eventually throttle the request if necessary, if the server is on heavy load or based on some configuration settings (maxRequestsPerSeconds or similar...)</li>
	 * </ol>
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		QoSAction action = startRequestHandling(httpRequest);
		logger.debug("request action {}", action);
		
		switch (action) {
		case ACCEPT:
			handleRequest(httpRequest, httpResponse, chain);
			break;
		case SUSPEND:
			suspendRequest(httpRequest);
			break;
		case REJECT:
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			break;
		}
	}

	private QoSAction startRequestHandling(HttpServletRequest httpRequest) throws ServletException {
		logger.debug("incoming request");
		return handler.startRequestHandling(httpRequest);
	}

	private void handleRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} finally {
			requestDone(request);
		}
	}

	private void requestDone(HttpServletRequest request) {
		logger.debug("request done");
		handler.finishRequestHandling(request);
	}
	
	private void suspendRequest(HttpServletRequest request) {
		logger.debug("suspend request");
		continuationSupport.suspend(request);
	}
	
	@Override
	public void destroy() {
		cacheManager.shutdown();
	}

}
