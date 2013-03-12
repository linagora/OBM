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
package org.obm.sync.server;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class Request {

	private HttpServletRequest req;

	private String handlerName;

	private String method;

	private static final Logger logger = LoggerFactory
			.getLogger(Request.class);

	public Request(HttpServletRequest req) {
		this.req = req;
		parseQuery();
	}

	private void parseQuery() {
		String uri = req.getPathInfo();

		// This is not an error, we want to display the status in this case
		if (uri == null || uri.equals("/"))
			return;

		String uriToSplit;
		if (uri.startsWith("/"))
			uriToSplit = uri.substring(1);
		else
			uriToSplit = uri;

		String[] splitQuery = uriToSplit.split("/");
		if (splitQuery.length != 2) {
			throw new QueryFormatException(
					"Expected query to be like '/obm-sync/services/$handler/$method'");
		}
		this.handlerName = splitQuery[0];

		this.method = splitQuery[1];
	}

	public String getParameter(String name) {
		return req.getParameter(name);
	}
	
	public String getMandatoryParameter(String name) {
		String value = getParameter(name);
		Preconditions.checkArgument(value != null, "'" + name + "' is mandatory");
		return value;
	}

	public String[] getParameterValues(String name) {
		return req.getParameterValues(name);
	}

	public String getHandlerName() {
		return handlerName;
	}

	public String getMethod() {
		return method;
	}

	public String getClientIP() {
		String xForwardedFor = req.getHeader("X-Forwarded-For");
		if (StringUtils.isBlank(xForwardedFor)) {
			return req.getRemoteAddr();
		}
		Iterable<String> ips = Splitter.on(',').trimResults().split(xForwardedFor);
		return Iterables.getFirst(ips, req.getRemoteAddr());
	}

	public String getRemoteIP() {
		return req.getRemoteAddr();
	}

	public String getLemonLdapLogin() {
		String login = req.getHeader("obm_uid");
		if (StringUtils.isEmpty(login)) {
			return null;
		}
		return login;
	}

	public String getLemonLdapDomain() {
		String domain = req.getHeader("obm_domain");
		if (StringUtils.isEmpty(domain)) {
			return null;
		}
		return domain;
	}

	public void dumpHeaders() {
		Enumeration<?> names = req.getHeaderNames();
		while (names.hasMoreElements()) {
			String s = names.nextElement().toString();
			String head = req.getHeader(s);
			logger.debug("head[" + s + "]: " + head);
		}
	}

	public void createSession() {
	    destroySession();
		req.getSession(true);
	}
	
	public void destroySession() {
	   HttpSession session = req.getSession(false);
	   if (session != null) {
	       session.invalidate();
	   }
	}
}
