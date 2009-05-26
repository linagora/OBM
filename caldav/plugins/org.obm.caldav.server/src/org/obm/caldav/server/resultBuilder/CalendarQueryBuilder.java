package org.obm.caldav.server.resultBuilder;

import java.util.Set;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalendarQueryBuilder extends ResultBuilder {

	public Document build(DavRequest req, IProxy proxy,
			Set<CalendarQueryPropertyHandler> properties, CompFilter compFilter) {
		Document doc = null;
		try {
			doc = createDocument();
			Element root = doc.getDocumentElement();
			Element response = DOMUtils.createElement(root, "D:response");
			DOMUtils.createElementAndText(response, "D:href", req.getHref());
			Element pStat = DOMUtils.createElement(response, "D:propstat");
			Element p = DOMUtils.createElement(pStat, "D:prop");
			if (compFilter != null) {
				for (CalendarQueryPropertyHandler prop : properties) {
					prop.appendCalendarQueryPropertyValue(p, proxy, compFilter.getName());
					/*
					 * if
					 * (CompFilter.VEVENT.equalsIgnoreCase(compFilter.getName(
					 * ))) {
					 * 
					 * } else if (CompFilter.VTODO.equalsIgnoreCase(compFilter
					 * .getName())) {
					 * 
					 * } else { logger.warn("the CompFilter [" +
					 * compFilter.getName() + "] is not implemented"); }
					 */
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}
}
