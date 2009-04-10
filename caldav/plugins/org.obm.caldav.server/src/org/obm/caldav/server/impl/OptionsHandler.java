package org.obm.caldav.server.impl;

import javax.servlet.http.HttpServletResponse;

public class OptionsHandler extends DavMethodHandler {

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.addHeader("DAV", "1, calendar-access, calendar-schedule");
		resp.addHeader("Allow", "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPPATCH, COPY, MOVE, LOCK, UNLOCK");
		resp.addHeader("MS-Author-Via", "DAV");
	}

}
