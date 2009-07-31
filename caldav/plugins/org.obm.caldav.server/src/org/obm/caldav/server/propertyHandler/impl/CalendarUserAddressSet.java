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

package org.obm.caldav.server.propertyHandler.impl;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;

/**
 * Name: calendar-user-address-set
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Identify the calendar addresses of the associated principal
 * resource.
 * 
 * Conformance: This property MAY be protected and SHOULD NOT be returned by a
 * PROPFIND allprop request (as defined in Section 14.2 of [RFC4918]). Support
 * for this property is REQUIRED. This property SHOULD be searchable using the
 * DAV:principal-property- search REPORT. The DAV:principal-search-property-set
 * REPORT SHOULD identify this property as such.
 * 
 * Description: This property is needed to map calendar user addresses in
 * iCalendar data to principal resources and their associated scheduling Inbox
 * and Outbox collections. In the event that a user has no well defined
 * identifier for their calendar user address, the URI of their principal
 * resource can be used. Definition: <!ELEMENT calendar-user-address-set
 * (DAV:href*)>
 * 
 * 
 * @author adrienp
 * 
 */
public class CalendarUserAddressSet extends DavPropertyHandler implements
		PropfindPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req,
			IProxy proxy) {
		Element elem = appendElement(prop, "calendar-user-address-set",
				NameSpaceConstant.CALDAV_NAMESPACE_PREFIX);
		appendElement(elem, "href", NameSpaceConstant.DAV_NAMESPACE_PREFIX)
				.setTextContent("/" + t.getLoginAtDomain() + "/events/");
	}

	@Override
	public boolean isUsed() {
		return true;
	}
}
