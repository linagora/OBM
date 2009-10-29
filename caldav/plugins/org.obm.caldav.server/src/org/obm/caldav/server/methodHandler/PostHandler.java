package org.obm.caldav.server.methodHandler;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;

public class PostHandler extends DavMethodHandler {

	@Override
	public void process(Token token, IBackend proxy, DavRequest req,
			HttpServletResponse resp) throws Exception {
		// TODO Auto-generated method stub

	}

}
