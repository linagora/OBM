package org.obm.caldav.server.impl;

import javax.servlet.http.HttpServletResponse;

public class MoveHandler extends DavMethodHandler {

	@Override
	public void process(Token t, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");
	}

}
