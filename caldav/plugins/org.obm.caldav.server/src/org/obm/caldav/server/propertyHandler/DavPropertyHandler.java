package org.obm.caldav.server.propertyHandler;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;

public abstract class DavPropertyHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	public abstract void appendPropertyValue(Element prop, Token t, DavRequest req);
}
