/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.resultBuilder;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.exception.CalDavException;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CalendarMultiGetQueryResultBuilder extends ResultBuilder {

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
			Set<CalendarMultiGetPropertyHandler> properties,
			Map<Event, String> listEvents) {
		Document doc = null;
		try {
			doc = createDocument();
			Element root = doc.getDocumentElement();
			if (listEvents.size() > 0) {
				for (Entry<Event, String> entry : listEvents.entrySet()) {
					Element response = DOMUtils.createElement(root,
							"D:response");
					Event event = entry.getKey();
					String eventICS = entry.getValue();
					String href = req.getHref()
							+ proxy.getCalendarService().getICSName(event);

					try {
						DOMUtils.createElementAndText(response, "D:href", href);
						Element pStat = DOMUtils.createElement(response,
								"D:propstat");
						Element p = DOMUtils.createElement(pStat, "D:prop");
						for (CalendarMultiGetPropertyHandler property : properties) {
							property.appendCalendarMultiGetPropertyValue(p,
									proxy, event, eventICS);
						}
						Element status = DOMUtils.createElement(pStat,
								"D:status");
						status.setTextContent("HTTP/1.1 200 OK");
					} catch (CalDavException e) {
						
						NodeList nl = response.getChildNodes();
						for (int i = 0; i < nl.getLength(); i++) {
							response.removeChild(nl.item(i));
						}
						DOMUtils.createElementAndText(response, "D:href", href);
						
						Element status = DOMUtils.createElement(response,
								"D:status");
						status.setTextContent("HTTP/1.1 "+e.getHttpStatusCode()+" "+e.getMessage());

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
