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


import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.sync.calendar.EventTimeUpdate;
import org.w3c.dom.Element;

/**
 *  Name:       resourcetype
 *  Purpose:    Specifies the nature of the resource.
 *  Description: The resourcetype property MUST be defined on all DAV
 * 				 compliant resources.  The default value is empty.
 *  
 *  <!ELEMENT resourcetype ANY >
 * 
 * @author adrienp
 *
 */
public class ResourceType extends DavPropertyHandler implements PropfindPropertyHandler, CalendarQueryPropertyHandler{

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req, IBackend proxy, String url) {
		Element elem = appendElement(prop,"resourcetype", NameSpaceConstant.DAV_NAMESPACE_PREFIX);
		appendElement(elem,"collection", NameSpaceConstant.DAV_NAMESPACE_PREFIX);
		appendElement(elem,"calendar", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX);
		appendElement(elem,"schedule-calendar", NameSpaceConstant.CALDAV_NAMESPACE_PREFIX);
	}

	@Override
	public void appendCalendarQueryPropertyValue(Element prop, IBackend proxy,
			EventTimeUpdate event) {
		appendElement(prop,"resourcetype", NameSpaceConstant.DAV_NAMESPACE_PREFIX);
	}

	@Override
	public boolean isUsed() {
		return true;
	}
}
