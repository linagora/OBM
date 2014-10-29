/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.provisioning;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MDCFilter extends BasicHttpAuthenticationFilter {

	private static final Logger logger = LoggerFactory.getLogger(MDCFilter.class);

	private final AtomicLong requestIdProvider;
	
	@Inject
	public MDCFilter() {
		requestIdProvider = new AtomicLong();
	}
	
	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		boolean preHandle = super.preHandle(request, response);
		
		try {
			MDC.put("threadId", String.valueOf(Thread.currentThread().getId()));
			MDC.put("requestId", String.valueOf(requestIdProvider.incrementAndGet()));
			Object principal = getSubject(request, response).getPrincipal();
			if (principal != null) {
				MDC.put("user", principal.toString());
			} else {
				logger.warn("Cannot identify the user");
			}
		} catch (Exception e) {
			logger.error("Cannot configure MDC loggers", e);
		}
		return preHandle;
	}
	
	@Override
	public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
		MDC.clear();
	}
}
