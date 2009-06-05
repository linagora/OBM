package org.obm.caldav.server.propertyHandler.impl;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Element;

/**
 * Name: getetag
 * 
 * Namespace: DAV:
 * 
 * Purpose: Contains the ETag header returned by a GET without accept headers.
 * 
 * Description: The getetag property MUST be defined on any DAV compliant
 * resource that returns the Etag header.
 * 
 * Value: entity-tag ; defined in section 3.11 of [RFC2068]
 * 
 * <!ELEMENT getetag (#PCDATA) >
 * 
 * @author adrienp
 * 
 */
public class DGetETag implements CalendarQueryPropertyHandler, CalendarMultiGetPropertyHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	// FIXME implement DGetETag management
	private String etag = UUID.randomUUID().toString();

	public void appendCalendarQueryPropertyValue(Element prop, IProxy proxy,
			Event event) {
		Element val = DOMUtils.createElement(prop, "D:getetag");
		val.setTextContent("\"" + etag + "-" + event.getUid() + "\"");
	}

	@Override
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			String eventId, String eventIcs) {
		Element val = DOMUtils.createElement(prop, "D:getetag");
		val.setTextContent("\"" + etag + "-" + eventId + "\"");
	}
}
