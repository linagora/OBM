package org.obm.caldav.server.resultBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalendarQueryResultBuilder extends ResultBuilder {

	// RESPONSE
	// <?xml version="1.0" encoding="utf-8" ?>
	// <D:multistatus xmlns:D="DAV:"
	// xmlns:C="urn:ietf:params:xml:ns:caldav">
	// <D:response>
	// <D:href>http://cal.example.com/bernard/work/abcd1.ics</D:href>
	// <D:propstat>
	// <D:prop>
	// <D:getetag>"fffff-abcd1"</D:getetag>
	// </D:prop>
	// <D:status>HTTP/1.1 200 OK</D:status>
	// </D:propstat>
	// </D:response>
	// <D:response>
	// <D:href>http://cal.example.com/bernard/work/abcd2.ics</D:href>
	// <D:propstat>
	// <D:prop>
	// <D:getetag>"fffff-abcd2"</D:getetag>
	// </D:prop>
	// <D:status>HTTP/1.1 200 OK</D:status>
	// </D:propstat>
	// </D:response>
	// <D:response>
	// <D:href>http://cal.example.com/bernard/work/abcd3.ics</D:href>
	// <D:propstat>
	// <D:prop>
	// <D:getetag>"fffff-abcd3"</D:getetag>
	// </D:prop>
	// <D:status>HTTP/1.1 200 OK</D:status>
	// </D:propstat>
	// </D:response>
	// </D:multistatus>

	public Document build(DavRequest req, IProxy proxy,
			Set<CalendarQueryPropertyHandler> properties, Map<String, Event> listEvents) {
		Document doc = null;
		try {
			doc = createDocument();
			Element root = doc.getDocumentElement();
			if (listEvents.size() > 0) {
				for (Entry<String, Event> entry : listEvents.entrySet()) {
					Event event = entry.getValue();
					if (!".ics".equals(entry.getKey())) {
						Element response = DOMUtils.createElement(root,
								"D:response");
						DOMUtils.createElementAndText(response, "D:href", entry.getKey());
						Element pStat = DOMUtils.createElement(response,
								"D:propstat");
						Element p = DOMUtils.createElement(pStat, "D:prop");
						for (CalendarQueryPropertyHandler prop : properties) {
							prop.appendCalendarQueryPropertyValue(p, proxy,
									event);
						}
						Element status = DOMUtils.createElement(pStat,
								"D:status");
						status.setTextContent("HTTP/1.1 200 OK");
					} else {
						logger.error("extid of the event " + event.getUid()
								+ " is null");
					}
				}
			} else {

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}
}
