package org.obm.caldav.server.propertyHandler;

import java.util.UUID;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.w3c.dom.Element;


/**
 * Name:      	getetag
 * 
 * Namespace:  	DAV:
 * 
 * Purpose:    	Contains the ETag header returned by a GET without
 * 				accept headers.
 * 
 * Description: The getetag property MUST be defined on any DAV
 * 				compliant resource that returns the Etag header.
 * 
 * Value:      	entity-tag  ; defined in section 3.11 of [RFC2068]
 * 
 * 				<!ELEMENT getetag (#PCDATA) >
 * 
 * @author adrienp
 *
 */
public class DGetETag extends DavPropertyHandler {
	public DGetETag(IProxy proxy) {
		super(proxy);
		// TODO Auto-generated constructor stub
	}

	//TODO implement DGetETag management 
	private String etag = UUID.randomUUID().toString();

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req) {
		prop.setTextContent("\"" + etag + "\"");
	}
}
