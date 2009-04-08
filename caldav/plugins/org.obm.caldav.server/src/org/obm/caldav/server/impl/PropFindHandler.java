package org.obm.caldav.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PropFindHandler extends DavMethodHandler {

	@Override
	public void process(Token t, HttpServletRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");
	}

}
