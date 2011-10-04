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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.exception.AppendPropertyException;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.CalDavToken;
import org.w3c.dom.Element;

/**
 * Name: getetag
 * 
 * Namespace: DAV:
 * 
 * Purpose: Contains the ETag header returned by a GET without accept headers.
 * 
 * Description: The getetag property MUST be defined on any DAV compliant
 * resource that returns the Etag header.
 * 
 * Value: entity-tag ; defined in section 3.11 of [RFC2068]
 * 
 * <!ELEMENT getetag (#PCDATA) >
 * 
 * @author adrienp
 * 
 */
public class GetETag extends DavPropertyHandler implements CalendarQueryPropertyHandler, CalendarMultiGetPropertyHandler,PropfindPropertyHandler {

	protected Log logger = LogFactory.getLog(getClass());

	@Override
	public void appendCalendarMultiGetPropertyValue(Element prop, IBackend proxy,
			CalendarResourceICS ics) throws AppendPropertyException{
		Element val = appendElement(prop, "getetag", NameSpaceConstant.DAV_NAMESPACE_PREFIX);
		val.setTextContent(ics.getETag());
	}
	
	@Override
	public void appendPropertyValue(Element prop, CalDavToken t, DavRequest req,
			IBackend proxy, DavComponent comp) {
		Element val = appendElement(prop, "getetag", NameSpaceConstant.DAV_NAMESPACE_PREFIX);
		val.setTextContent(comp.getETag());
	}

	@Override
	public boolean isUsed() {
		return true;
	}

}
