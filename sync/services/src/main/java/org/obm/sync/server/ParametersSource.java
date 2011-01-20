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
package org.obm.sync.server;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class ParametersSource {

	private HttpServletRequest req;

	private static final Log logger = LogFactory.getLog(ParametersSource.class);

	public ParametersSource(HttpServletRequest req) {
		this.req = req;
	}

	public String getParameter(String name) {
		return req.getParameter(name);
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

}
