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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dumps the request for debugging purpose
 */
public class DumpFilter implements Filter {

	Log logger = LogFactory.getLog(getClass());

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
