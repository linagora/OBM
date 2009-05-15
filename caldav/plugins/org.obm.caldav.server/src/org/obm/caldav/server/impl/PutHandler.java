package org.obm.caldav.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import fr.aliasource.utils.FileUtils;

public class PutHandler extends DavMethodHandler {

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {
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
