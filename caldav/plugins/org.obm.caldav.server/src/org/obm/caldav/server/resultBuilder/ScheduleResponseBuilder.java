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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-04#section-6
 * 
 * <?xml version="1.0" encoding="utf-8" ?> <C:schedule-response xmlns:D="DAV:"
 * xmlns:C="urn:ietf:params:xml:ns:caldav"> <C:response>
 * <C:recipient>mailto:bernard@example.com</C:recipient>
 * <C:request-status>2.0;Success</C:request-status>
 * <C:calendar-data>BEGIN:VCALENDAR VERSION:2.0 PRODID:-//Example Corp.//CalDAV
 * Server//EN METHOD:REPLY BEGIN:VFREEBUSY DTSTAMP:20040901T200200Z
 * ORGANIZER:mailto:lisa@example.com DTSTART:20040902T000000Z
 * DTEND:20040903T000000Z UID:34222-232@example.com ATTENDEE;CN=Bernard
 * Desruisseaux:mailto:bernard@ example.com
 * FREEBUSY;FBTYPE=BUSY-UNAVAILABLE:20040902T000000Z/
 * 20040902T090000Z,20040902T170000Z/20040903T000000Z END:VFREEBUSY
 * END:VCALENDAR </C:calendar-data> </C:response> <C:response>
 * <C:recipient>mailto:cyrus@example.com</C:recipient>
 * <C:request-status>2.0;Success</C:request-status>
 * <C:calendar-data>BEGIN:VCALENDAR VERSION:2.0 PRODID:-//Example Corp.//CalDAV
 * Server//EN METHOD:REPLY BEGIN:VFREEBUSY DTSTAMP:20040901T200200Z
 * ORGANIZER:mailto:lisa@example.com DTSTART:20040902T000000Z
 * DTEND:20040903T000000Z UID:34222-232@example.com ATTENDEE;CN=Cyrus
 * Daboo:mailto:cyrus@example.com
 * FREEBUSY;FBTYPE=BUSY-UNAVAILABLE:20040902T000000Z/
 * 20040902T090000Z,20040902T170000Z/20040903T000000Z
 * FREEBUSY;FBTYPE=BUSY:20040902T120000Z/20040902T130000Z END:VFREEBUSY
 * END:VCALENDAR </C:calendar-data> </C:response> </C:schedule-response>
 * 
 * @author adrienp
 * 
 */
public class ScheduleResponseBuilder extends ResultBuilder {

	private Log logger = LogFactory.getLog(ScheduleResponseBuilder.class);

	public Document build(Token t, DavRequest req, Set<String> recipients,
			Map<String, String> responseData) {
		try {

			Document ret = createScheduleResponseDocument();
			Element r = ret.getDocumentElement();
			for (String recipient : recipients) {
				Element response = DOMUtils.createElement(r, "C:response");
				String ics = responseData.get(recipient.replaceAll("mailto:",
						""));
				Element er = DOMUtils.createElement(response, "C:recipient");
				DOMUtils.createElementAndText(er, "D:href", recipient);
				if (ics == null || "".equals(ics)) {
					DOMUtils.createElementAndText(response, "C:request-status",
							"3.7;Invalid Calendar User");
				} else {
					DOMUtils.createElementAndText(response, "C:request-status",
							"2.0;Success");

					Element val = DOMUtils.createElement(response,
							"C:calendar-data");

					CDATASection cdata = val.getOwnerDocument()
							.createCDATASection(ics);
					val.appendChild(cdata);
				}
			}
			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

}
