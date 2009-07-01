package org.obm.caldav.server.methodHandler;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.FileUtils;


public class PutHandler extends DavMethodHandler {

	public PutHandler() {
	}

	@Override
	public void process(Token token, IProxy proxy, DavRequest req, HttpServletResponse resp) {
		
		InputStream in;
		try {
			String extId = CalDavUtils.getExtIdFromURL(req.getURI());

			in = req.getInputStream();
			String ics = FileUtils.streamString(in, false);
			logger.info("ics: "+ics);
			proxy.getCalendarService().updateOrCreateEvent(ics, extId);
			
			resp.setStatus(HttpServletResponse.SC_CREATED);
			resp.setContentLength(0);
			resp.setDateHeader("Created", new Date().getTime());
			
			//resp.setHeader("ETag", event.getUid());
		} catch (MalformedURLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentLength(0);
			logger.error(e.getMessage(), e);
		}catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentLength(0);
			logger.error("Unable to create event", e);
		}
	}

}
