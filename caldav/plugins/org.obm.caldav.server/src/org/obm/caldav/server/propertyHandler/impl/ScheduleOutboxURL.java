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
 * Name:  			schedule-outbox-URL
 * 
 * Namespace:  		urn:ietf:params:xml:ns:caldav
 * 
 * Purpose:  		Identify the URL of the scheduling Outbox collection owned
 * 					by the associated principal resource.
 * 
 * Conformance:  	This property MAY be protected and SHOULD NOT be
 * 					returned by a PROPFIND allprop request 
 * 					(as defined in Section 14.2 of [RFC4918]).
 * Description:  	This property is needed for a client to determine where
 * 					the scheduling Outbox collection of the current user is located so
 * 					that sending of scheduling messages can occur.
 * 
 * Definition:
 * 		<!ELEMENT schedule-outbox-URL DAV:href>
 * 
 * 
 * @author adrienp
 *
 */
public class ScheduleOutboxURL extends DavPropertyHandler implements PropfindPropertyHandler{

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy) {
		Element elem = appendElement(prop, "schedule-outbox-URL", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX);
		appendElement(elem, "href", NameSpaceConstant.DAV_NAMESPACE_PREFIX).setTextContent("/"
				+ t.getLoginAtDomain() + "/events/outbox");
	}

	@Override
	public boolean isUsed() {
		return true;
	}
}
