package org.obm.caldav.server.methodHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.FileUtils;


public class PutHandler extends DavMethodHandler {

	public PutHandler() {
	}

	@Override
	public void process(Token token, IProxy proxy, DavRequest req, HttpServletResponse resp) {
		InputStream in;
		try {
			in = req.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			FileUtils.transfer(in, out, true);
			logger.info("received ics:\n" + out.toString());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
