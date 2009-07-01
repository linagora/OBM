package org.obm.caldav.server.propertyHandler.impl;


import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * 
 * This property identifies a particular principal as being the "owner" of the resource.
 * Since the owner of a resource often has special access control capabilities
 * (e.g., the owner frequently has permanent DAV:write-acl privilege), clients might 
 * display the resource owner in their user interface.
 * 
 * Servers MAY implement DAV:owner as protected property 
 * and MAY return an empty DAV:owner element as property value 
 * in case no owner information is available.
 * 
 * <!ELEMENT owner (href?)>
 * 
 * @author adrienp
 *
 */
public class Owner extends DavPropertyHandler implements PropfindPropertyHandler{

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy) {
		DOMUtils.createElementAndText(prop, "D:href", "/"
				+ t.getLoginAtDomain() + "/events");
	}

}
