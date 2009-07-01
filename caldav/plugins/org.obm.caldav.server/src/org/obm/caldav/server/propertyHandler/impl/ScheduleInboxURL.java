package org.obm.caldav.server.propertyHandler.impl;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Element;


/**
 * Name:  		schedule-inbox-URL
 * 
 * Namespace:  	urn:ietf:params:xml:ns:caldav
 * 
 * Purpose:  	Identify the URL of the scheduling Inbox collection owned
 * 			 	by the associated principal resource.
 * 
 * Conformance:  This property MAY be protected and SHOULD NOT be
 * 				 returned by a PROPFIND allprop request (as defined in Section 14.2
 * 				 of [RFC4918]).
 * 
 * Description:  This property is needed for a client to determine where
 * 				 the scheduling Inbox collection of the current user is located so
 * 				 that processing of scheduling messages can occur.
 * 
 * Definition:
 * 		<!ELEMENT schedule-inbox-URL (DAV:href)>
 * 
 * 
 * @author adrienp
 *
 */
public class ScheduleInboxURL extends DavPropertyHandler implements PropfindPropertyHandler{

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy) {
		DOMUtils.createElementAndText(prop, "D:href", "/"
				+ t.getLoginAtDomain() + "/events/inbox");
	}
}
