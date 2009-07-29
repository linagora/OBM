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
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Element;


/**
 * CALDAV:calendar-description Property
 * 
 * Name:  calendar-description
 * Namespace:  urn:ietf:params:xml:ns:caldav
 *
 * Purpose:  Provides a human-readable description of the calendar
 *    collection.
 *
 * Conformance:  This property MAY be defined on any calendar
 *    collection.  If defined, it MAY be protected and SHOULD NOT be
 *    returned by a PROPFIND DAV:allprop request (as defined in Section
 *    12.14.1 of [RFC2518]).  An xml:lang attribute indicating the human
 *    language of the description SHOULD be set for this property by
 *    clients or through server provisioning.  Servers MUST return any
 *    xml:lang attribute if set for the property.

 * Description:  If present, the property contains a description of the
 *    calendar collection that is suitable for presentation to a user.
 *    If not present, the client should assume no description for the
 *    calendar collection.
 *
 * @author adrienp
 *
 */
public class CalendarDescription implements PropfindPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req,
			IProxy proxy) {
		DOMUtils.createElementAndText(prop, NameSpaceConstant.DAV_NAMESPACE_PREFIX+"calendar-description", "Calendar from OBM-CalDav");
	}

	@Override
	public boolean isUsed() {
		return true;
	}

}
