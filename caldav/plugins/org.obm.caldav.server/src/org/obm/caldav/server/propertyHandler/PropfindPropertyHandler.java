package org.obm.caldav.server.propertyHandler;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;

public interface PropfindPropertyHandler {
	void appendPropertyValue(Element prop, Token t, DavRequest req, IProxy proxy);
}
