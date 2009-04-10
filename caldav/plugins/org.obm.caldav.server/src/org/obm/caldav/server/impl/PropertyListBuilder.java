package org.obm.caldav.server.impl;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;

public class PropertyListBuilder {
	
	private Log logger = LogFactory.getLog(PropertyListBuilder.class);
	
	public Document build(Token t, DavRequest req, Set<String> toLoad) {
		try {
			Document ret = DOMUtils.createDoc("DAV:", "D:multistatus");
			Element r = ret.getDocumentElement();
			r.setAttribute("xmlns:D", "DAV:");
			r.setAttribute("xmlns:CS", "http://calendarserver.org/ns/");
			r.setAttribute("xmlns:C", "urn:ietf:params:xml:ns:caldav");
			r.setAttribute("xmlns", "urn:ietf:params:xml:ns:caldav");
			Element response = DOMUtils.createElement(r, "D:response");
			DOMUtils.createElementAndText(response, "D:href", req.getHref());
			Element pStat = DOMUtils.createElement(response, "D:propstat");
			Element p = DOMUtils.createElement(pStat, "D:prop");
			
			DAVStore store = new DAVStore();
			for (String s : toLoad) {
				Element val = DOMUtils.createElement(p, s);
				store.appendPropertyValue(val, t, req);
			}
			DOMUtils.createElementAndText(pStat, "D:status", "HTTP/1.1 200 OK");
			
			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

}
