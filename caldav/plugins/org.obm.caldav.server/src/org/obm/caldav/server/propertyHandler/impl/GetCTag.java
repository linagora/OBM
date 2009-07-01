package org.obm.caldav.server.propertyHandler.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.sync.items.EventChanges;
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
	public synchronized void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy) {
		Date lastChange = lastChangeByUser.get(t.getLoginAtDomain());
		
		if(lastChange == null){
			lastChange = new Date();
			
		} else {
			try {
				EventChanges ec = proxy.getCalendarService().getSync(lastChange);
				if(ec.getUpdated().length != 0 || ec.getRemoved().length != 0){
					lastChange = new Date();
				} else {
					logger.info("Calendar is already synchronized");
				}
			} catch (Exception e) {
				lastChange = new Date();
				
			}
		}
		prop.setTextContent(getCTagValue(t, lastChange));
		
		lastChangeByUser.put(t.getLoginAtDomain(), lastChange);
	}
	
	private String getCTagValue(Token t, Date lastSync){
		return t.getLoginAtDomain()+"-"+lastSync.getTime();
	}
}
