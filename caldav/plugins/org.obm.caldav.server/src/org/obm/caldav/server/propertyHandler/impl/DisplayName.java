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
 * Name: displayname Namespace: DAV: Purpose: Provides a name for the resource
 * that is suitable for presentation to a user. Description: The displayname
 * property should be defined on all DAV compliant resources. If present, the
 * property contains a description of the resource that is suitable for
 * presentation to a user.
 * 
 * <!ELEMENT displayname (#PCDATA) >
 * 
 * @author adrienp
 * 
 */

public class DisplayName extends DavPropertyHandler implements
		PropfindPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req,
			IProxy proxy) {
		appendElement(prop, "displayname",
				NameSpaceConstant.DAV_NAMESPACE_PREFIX).setTextContent(
				"OBM CalDav " + req.getCalendarComponantName());
	}

	@Override
	public boolean isUsed() {
		return true;
	}

}
