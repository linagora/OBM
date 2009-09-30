package org.obm.caldav.server.propertyHandler.impl;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;

public class GetContentType extends DavPropertyHandler implements PropfindPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req,
			IProxy proxy, String url) {
		if(url.endsWith(".ics")){
			appendElement(prop, "getcontenttype",
				NameSpaceConstant.DAV_NAMESPACE_PREFIX).setTextContent(
				"text/calendar");
		} else {
			appendElement(prop, "getcontenttype",
			NameSpaceConstant.DAV_NAMESPACE_PREFIX).setTextContent(
				"httpd/unix-directory");
		}
	}

	@Override
	public boolean isUsed() {
		return true;
	}

}
