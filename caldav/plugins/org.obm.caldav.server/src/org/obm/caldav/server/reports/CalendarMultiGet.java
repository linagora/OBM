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

package org.obm.caldav.server.reports;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.impl.CalendarData;
import org.obm.caldav.server.propertyHandler.impl.GetETag;
import org.obm.caldav.server.resultBuilder.CalendarMultiGetQueryResultBuilder;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CalendarMultiGet extends ReportProvider {

	private Map<String, CalendarMultiGetPropertyHandler> properties;

	public CalendarMultiGet() {
		properties = new HashMap<String, CalendarMultiGetPropertyHandler>();
		properties.put("D:getetag", new GetETag());
		properties.put("calendar-data", new CalendarData());
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <calendar-multiget xmlns="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
	// <D:prop>
	// <D:getetag/>
	// <calendar-data/>
	// </D:prop>
	// <D:href>/adrien@zz.com/events/979.ics</D:href>
	// </calendar-multiget>

	// <D:response>
	// <D:href>http://cal.example.com/bernard/work/979.ics</D:href>
	// <D:propstat>
	// <D:prop>
	// <D:getetag>"fffff-979"</D:getetag>
	// <C:calendar-data>BEGIN:VCALENDAR
	// VERSION:2.0
	// PRODID:-//Example Corp.//CalDAV Client//EN
	// BEGIN:VTIMEZONE
	// LAST-MODIFIED:20040110T032845Z
	// TZID:US/Eastern
	// BEGIN:DAYLIGHT
	// DTSTART:20000404T020000
	// RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4
	// TZNAME:EDT
	// TZOFFSETFROM:-0500
	// TZOFFSETTO:-0400
	// END:DAYLIGHT
	// BEGIN:STANDARD
	// DTSTART:20001026T020000
	// RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
	// TZNAME:EST
	// TZOFFSETFROM:-0400
	// TZOFFSETTO:-0500
	// END:STANDARD
	// END:VTIMEZONE
	// BEGIN:VEVENT
	// DTSTAMP:20060206T001102Z
	// DTSTART;TZID=US/Eastern:20060102T100000
	// DURATION:PT1H
	// SUMMARY:Event #1
	// Description:Go Steelers!
	// UID:74855313FA803DA593CD579A@example.com
	// END:VEVENT
	// END:VCALENDAR
	// </C:calendar-data>
	// </D:prop>
	// <D:status>HTTP/1.1 200 OK</D:status>
	// </D:propstat>
	// </D:response>
	// <D:response>
	// <D:href>http://cal.example.com/bernard/work/mtg1.ics</D:href>
	// <D:status>HTTP/1.1 404 Not Found</D:status>
	// </D:response>
	// </D:multistatus>

	@Override
	public void process(Token token, IProxy proxy, DavRequest req,
			HttpServletResponse resp, Set<String> requestPropList) {
		logger.info("process(" + token.getLoginAtDomain() + ", req, resp)");

		Set<CalendarMultiGetPropertyHandler> propertiesValues = new HashSet<CalendarMultiGetPropertyHandler>();

		for (String s : requestPropList) {
			CalendarMultiGetPropertyHandler dph = properties.get(s);
			if (dph != null) {
				propertiesValues.add(dph);
			} else {
				logger.warn("the Property [" + s + "] is not implemented");
			}
		}

		try {
			Element root = req.getDocument().getDocumentElement();
			Set<String> listExtIDEvent = getListExtId(root);
			
			Map<Event, String> listICS = proxy.getCalendarService().getICSFromExtId(listExtIDEvent);

			
			
			Document ret = new CalendarMultiGetQueryResultBuilder().build(req, proxy, propertiesValues, listICS);
			DOMUtils.logDom(ret);
			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Set<String> getListExtId(Element root){
		Set<String> listExtIDEvent = new HashSet<String>();
		if(root!= null){
			NodeList dl = root.getElementsByTagNameNS(NameSpaceConstant.DHREF_NAMESPACE, "href");
			for(int i = 0; i<dl.getLength(); i++ ){
				Element dhref = (Element)dl.item(i);
				String hrefContent = dhref.getTextContent();
				try {
					String extid = CalDavUtils.getExtIdFromURL(hrefContent);
					listExtIDEvent.add(extid);
				} catch (MalformedURLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return listExtIDEvent;
	}
}
