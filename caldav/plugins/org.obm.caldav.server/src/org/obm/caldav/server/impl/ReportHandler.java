package org.obm.caldav.server.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.reports.CalendarMultiGet;
import org.obm.caldav.server.reports.CalendarQuery;
import org.obm.caldav.server.reports.PrincipalPropertySearch;
import org.obm.caldav.server.reports.ReportProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportHandler extends DavMethodHandler {

	Map<String, ReportProvider> providers;

	public ReportHandler() {
		providers = new HashMap<String, ReportProvider>();
		providers.put("D:principal-property-search",
				new PrincipalPropertySearch());
		providers.put("calendar-query", new CalendarQuery());
		providers.put("calendar-multiget", new CalendarMultiGet());
	}

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {
		Document d = req.getDocument();
		Element r = d.getDocumentElement();
		String reportKind = r.getNodeName();

		ReportProvider rp = providers.get(reportKind);
		if (rp != null) {
			rp.process(token, req, resp);
		} else {
			logger.error("No report provider for report kind '" + reportKind
					+ "'");
		}
	}

}
