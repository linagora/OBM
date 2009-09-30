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


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;

/**
 * 
 * https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt
 *  Name:  getctag
 *     
 *     Purpose:  Specifies a "synchronization" token used to indicate when
 *        		 the contents of a calendar or scheduling Inbox or Outbox
 *          	 collection have changed.
 *           
 *	   Conformance:  This property MUST be defined on a calendar or
 *			scheduling Inbox or Outbox collection resource.  It MUST be
 *			protected and SHOULD be returned by a PROPFIND DAV:allprop request
 *			(as defined in Section 12.14.1 of [RFC2518]).
 *
 *	   Description:  The CS:getctag property allows clients to quickly
 *	      determine if the contents of a calendar or scheduling Inbox or
 *	      Outbox collection have changed since the last time a
 *	      "synchronization" operation was done.  The CS:getctag property
 *	      value MUST change each time the contents of the calendar or
 *	      scheduling Inbox or Outbox collection change, and each change MUST
 *	      result in a value that is different from any other used with that
 *	      collection URI.
 *	
 *	   Definition:
 *	
 *		<!ELEMENT getctag #PCDATA>
 *	
 *	   Example:
 *	
 *		<T:getctag xmlns:T="http://calendarserver.org/ns/">
 *			ABCD-GUID-IN-THIS-COLLECTION-20070228T122324010340
 *		</T:getctag>
 * 
 * 
 * @author adrienp
 *
 */
public class GetCTag extends DavPropertyHandler implements PropfindPropertyHandler{

	private static Map<String,Date> lastChangeByUser;
	static{
		lastChangeByUser = new HashMap<String, Date>();
	}
	
	@Override
	public synchronized void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy, String url) {
		Element elem = appendElement(prop, "getctag", NameSpaceConstant.CALENDARSERVER_NAMESPACE_PREFIX); 
		Date lastChange = lastChangeByUser.get(t.getLoginAtDomain());
		
		if(lastChange == null){
			lastChange = new Date();
			
		} else {
			try {
				boolean change = proxy.getCalendarService().getSync(lastChange);
				if(change) {
					lastChange = new Date();
				} else {
					logger.info("Calendar is already synchronized");
				}
			} catch (Exception e) {
				lastChange = new Date();
				
			}
		}
		elem.setTextContent(getCTagValue(t, lastChange));
		
		lastChangeByUser.put(t.getLoginAtDomain(), lastChange);
	}
	
	private String getCTagValue(Token t, Date lastSync){
		return t.getLoginAtDomain()+"-"+lastSync.getTime();
	}

	@Override
	public boolean isUsed() {
		return true;
	}
}
