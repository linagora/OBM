package org.obm.caldav.server.reports;

import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.impl.PropertyListBuilder;
import org.obm.caldav.server.impl.Token;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;

public class CalendarQuery extends ReportProvider {

	// <?xml version="1.0" encoding="UTF-8"?>
	// <calendar-query xmlns="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
	// <D:prop>
	// <D:getetag/>
	// </D:prop>
	// <filter>
	// <comp-filter name="VCALENDAR">
	// <comp-filter name="VEVENT"/>
	// </comp-filter>
	// </filter>
	// </calendar-query>

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		logger.info("process(" + token.getLoginAtDomain() + ", req, resp)");
		HashSet<String> toLoad = new HashSet<String>();
		toLoad.add("D:getetag");
		
		Document ret = new PropertyListBuilder().build(token, req, toLoad);

		try {
			DOMUtils.logDom(ret);

			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
