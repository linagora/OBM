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
package fr.aliacom.obm.common;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dumps the request for debugging purpose
 */
public class DumpFilter implements Filter {

	Logger logger =  LoggerFactory.getLogger(getClass());

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		logger.info("DumpFilter started");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		logger.info("Filtering...");

		if (request != null && request instanceof HttpServletRequest) {
			
			HttpServletRequest req = (HttpServletRequest) request;
			logger.info("uri: " + req.getRequestURI());

			dumpParameters(req);
			dumpAttributes(req);
			dumpHeaders(req);

		} else {
			logger.info("Request is " + request);
		}

		chain.doFilter(request, response);
	}
	
	@Override
	public void destroy() {
		logger.info("DumpFilter stoped");
	}
	
	private void dumpHeaders(HttpServletRequest req) {
		Enumeration<?> enumeration = req.getHeaderNames();
		while (enumeration.hasMoreElements()) {
			String param = (String) enumeration.nextElement();
			logger.info("--- START header '" + param + "'");
			String value = req.getHeader(param);
			logger.info("\n" + value);
			logger.info("--- END header '" + param + "'\n");
		}
	}

	private void dumpAttributes(HttpServletRequest req) {
		Enumeration<?> enumeration = req.getAttributeNames();
		while (enumeration.hasMoreElements()) {
			String param = (String) enumeration.nextElement();
			logger.info("--- START attrib '" + param + "'");
			Object value = req.getAttribute(param);
			logger.info("\n" + value);
			logger.info("--- END attrib '" + param + "'\n");
		}
	}

	private void dumpParameters(HttpServletRequest req) {
		Enumeration<?> enumeration = req.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String param = (String) enumeration.nextElement();
			logger.info("--- START param '" + param + "'");
			String[] values = req.getParameterValues(param);
			for (int i = 0; i < values.length; i++) {
				logger.info("\n" + values[i]);
			}
			logger.info("--- END param '" + param + "'\n");
		}
	}

}
