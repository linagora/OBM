package org.obm.caldav.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class DavMethodHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	public DavMethodHandler() {
		
	}

	public abstract void process(HttpServletRequest req, HttpServletResponse resp);
	
}
