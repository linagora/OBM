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
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Element;


/**
 * Name:    	calendar-home-set
 * 
 * Namespace:	urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: 	Identifies the URL of any WebDAV collections 
 * 				that contain calendar collections owned by 
 * 				the associated principal resource.
 * 
 * Conformance: This property SHOULD be defined on a 
 * 				principal resource. If defined, 
 * 				it MAY be protected and SHOULD NOT be returned by 
 * 				a PROPFIND DAV:allprop request 
 * 				(as defined in Section 12.14.1 of [RFC2518]).
 * 
 * Description: The CALDAV:calendar-home-set property is meant to allow 
 * 				users to easily find the calendar collections owned by 
 * 				the principal. Typically, users will group all the calendar 
 * 				collections that they own under a common collection. 
 * 				This property specifies the URL of collections that are either 
 * 				calendar collections or ordinary collections that have child or 
 * 				descendant calendar collections owned by the principal.
 * 
 * Definition:
 *        <!ELEMENT calendar-home-set (DAV:href*)>
 *        
 * Example:
 * 		<C:calendar-home-set xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
 * 			<D:href>http://cal.example.com/home/bernard/calendars/</D:href>
 * 		</C:calendar-home-set>
 * 
 * 
 * @author adrienp
 *
 */
public class CalendarHomeSet extends DavPropertyHandler implements PropfindPropertyHandler{

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy) {
		Element elem = DOMUtils.createElement(prop, NameSpaceConstant.CALDAV_NAMESPACE_PREFIX+"calendar-home-set");
		DOMUtils.createElementAndText(elem, NameSpaceConstant.DAV_NAMESPACE_PREFIX+"href", "/"
				+ t.getLoginAtDomain() + "/events/");
	}

	@Override
	public boolean isUsed() {
		return true;
	}
}
