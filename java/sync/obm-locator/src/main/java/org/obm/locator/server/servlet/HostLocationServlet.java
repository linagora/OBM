/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.locator.server.servlet;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.locator.impl.LocatorDbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Locates OBM host IP addresses with a service, service_property, login@domain.
 * This call url should be /location/host/sync/obm_sync/login@domain
 * 
 */
@Singleton
public class HostLocationServlet extends HttpServlet {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LocatorDbHelper locatorDbHelper;
	
	@Inject
	protected HostLocationServlet(LocatorDbHelper locatorDbHelper) {
		this.locatorDbHelper = locatorDbHelper;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.info("obm locator doGet servlet");

		String uri = getRequestUri(req);
		String[] split;
		try {
			split = splitUri(resp, uri);
		} catch (Exception e) {
			return;
		}

		String service = split[1];
		String property = split[2];
		String loginAtDomain = split[3];

		Set<String> ips = locatorDbHelper.findDomainHost(
				loginAtDomain, service, property);

		if (ips.size() > 0) {
			writeResponse(req, resp, ips);
		} else {
			sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
					"Could not find " + service + "/" + property + " for "
							+ loginAtDomain);
		}

	}

	private void writeResponse(HttpServletRequest req,
			HttpServletResponse resp, Set<String> ips) throws IOException {

		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/plain");

		StringBuilder returnedIps = new StringBuilder();
		returnedIps.append("[");

		StringBuilder sb = new StringBuilder();
		for (String ip : ips) {
			sb.append(ip);
			sb.append('\n');
			returnedIps.append(' ');
			returnedIps.append(ip);
		}
		returnedIps.append(" ]");

		byte[] b = sb.toString().getBytes();
		resp.setContentLength(b.length);

		ServletOutputStream out = resp.getOutputStream();
		out.write(b);
		out.close();

		if (logger.isInfoEnabled()) {
			logger.info("uri : " + req.getRequestURI() + " => returned "
					+ ips.size() + " IP(s) address(es) "
					+ returnedIps.toString());
		}
	}

	private String getRequestUri(HttpServletRequest req) {
		String uri = null;
		if (req != null) {
			uri = req.getPathInfo();
			if (uri.startsWith("/")) {
				uri = uri.substring(1);
			}
		}
		return uri;
	}

	private String[] splitUri(HttpServletResponse resp, String uri)
			throws Exception {

		String[] split = uri.split("/");
		if (split.length != 4) {
			sendErrorResponse(
					resp,
					HttpServletResponse.SC_FORBIDDEN,
					"uri should have 5 parts (got "
							+ split.length
							+ " parts "
							+ "(/location/host/sync/obm_sync/login@domain). The query was: "
							+ uri);
			throw new Exception("Format of Uri is incorrect");
		}
		return split;
	}

	private void sendErrorResponse(HttpServletResponse resp, int sc,
			String message) throws IOException {

		logger.error(message);
		resp.sendError(sc, message);
	}

}
