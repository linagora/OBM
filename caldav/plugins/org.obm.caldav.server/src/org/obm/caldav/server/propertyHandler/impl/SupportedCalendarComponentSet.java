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
 * 
 * CALDAV:supported-calendar-component-set Property
 * 
 * Name: supported-calendar-component-set
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies the calendar component types (e.g., VEVENT, VTODO, etc.)
 * that calendar object resources can contain in the calendar collection.
 * 
 * Conformance: This property MAY be defined on any calendar collection. If
 * defined, it MUST be protected and SHOULD NOT be returned by a PROPFIND
 * DAV:allprop request (as defined in Section 12.14.1 of [RFC2518]).
 * 
 * Description: The CALDAV:supported-calendar-component-set property is used to
 * specify restrictions on the calendar component types that calendar object
 * resources may contain in a calendar collection. Any attempt by the client to
 * store calendar object resources with component types not listed in this
 * property, if it exists, MUST result in an error, with the
 * CALDAV:supported-calendar-component precondition (Section 5.3.2.1) being
 * violated. Since this property is protected, it cannot be changed by clients
 * using a PROPPATCH request. However, clients can initialize the value of this
 * property when creating a new calendar collection with MKCALENDAR. The
 * empty-element tag <C:comp name="VTIMEZONE"/> MUST only be specified if
 * support for calendar object resources that only contain VTIMEZONE components
 * is provided or desired. Support for VTIMEZONE components in calendar object
 * resources that contain VEVENT or VTODO components is always assumed. In the
 * absence of this property, the server MUST accept all component types, and the
 * client can assume that all component types are accepted.
 * 
 * Definition:
 * 
 * <!ELEMENT supported-calendar-component-set (comp+)>
 * 
 * Example:
 * 
 * <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
 * <C:comp name="VEVENT"/> <C:comp name="VTODO"/>
 * </C:supported-calendar-component-set>
 * 
 * 
 * @author adrienp
 * 
 */
public class SupportedCalendarComponentSet extends DavPropertyHandler implements PropfindPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req,
			IProxy proxy, String url) {
		Element elem = appendElement(prop, "supported-calendar-component-set", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX);
		appendElement(elem, "comp", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX).setAttribute("name", "VEVENT");
		appendElement(elem, "comp", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX).setAttribute("name", "VTODO");
		appendElement(elem, "comp", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX).setAttribute("name", "VFREEBUSY");
		appendElement(elem, "comp", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX).setAttribute("name", "VTIMEZONE");
	}
	
	@Override
	public boolean isUsed() {
		return true;
	}

}
