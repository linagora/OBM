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

import java.util.List;
import java.util.Set;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.utils.DOMUtils;
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

	public Document build(DavRequest req, CalDavToken t, IBackend proxy,
			Set<String> properties, List<DavComponent> comps,
			Set<CalendarQueryPropertyHandler> propertiesValues) {
		Document doc = null;
		try {
			doc = createMultiStatusDocument();
			Element root = doc.getDocumentElement();
			for (DavComponent comp : comps) {
				if (!comp.getURL().endsWith("/.ics")) {
					Element response = DOMUtils.createElement(root,
							"D:response");
					DOMUtils.createElementAndText(response, "D:href", comp
							.getURL());
					Element pStat = DOMUtils.createElement(response,
							"D:propstat");
					Element p = DOMUtils.createElement(pStat, "D:prop");
					for (CalendarQueryPropertyHandler prop : propertiesValues) {
						prop.appendPropertyValue(p, t, req, proxy, comp);
					}
					Element status = DOMUtils.createElement(pStat, "D:status");
					status.setTextContent("HTTP/1.1 200 OK");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}
}
