package org.obm.caldav.server.resultBuilder;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResultBuilder {
	
	protected Log logger = LogFactory.getLog(ResultBuilder.class);
	
	protected Document createDocument() throws ParserConfigurationException, FactoryConfigurationError{
		Document ret = DOMUtils.createDoc("DAV:", "D:multistatus");
		Element r = ret.getDocumentElement();
		r.setAttribute("xmlns:D", "DAV:");
		r.setAttribute("xmlns:CS", "http://calendarserver.org/ns/");
		r.setAttribute("xmlns:C", "urn:ietf:params:xml:ns:caldav");
		r.setAttribute("xmlns", "urn:ietf:params:xml:ns:caldav");
		return ret;
	}
}
