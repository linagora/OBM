package org.obm.caldav.server.methodHandler;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.resultBuilder.ScheduleResponseBuilder;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;

public class PostHandler extends DavMethodHandler {

	@Override
	public void process(Token token, IBackend bakend, DavRequest req,
			HttpServletResponse resp) throws Exception {
		logger.info("process(req, resp)");
		Map<String, String> responseData = bakend.getCalendarService()
				.getFreeBuzy(req.getICS());
		String freeBusyFind = "";
		if (responseData.keySet().size() > 0) {
			for (String mail : responseData.keySet()) {
				freeBusyFind += mail;
			}
		} else {
			logger.info("No word has been found");
		}
		logger.info("FreeBusy find for user " + freeBusyFind);
		Document ret = new ScheduleResponseBuilder().build(token, req, req
				.getRecipients(), responseData);

		try {
			DOMUtils.logDom(ret);
			resp.setStatus(200); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtils.serialise(ret, out);
			resp.setContentLength(out.size());
			resp.getOutputStream().write(out.toByteArray());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
