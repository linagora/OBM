package org.obm.caldav.server.propertyHandler.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.exception.AppendPropertyException;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.CalendarQueryPropertyHandler;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
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
public class GetETag implements CalendarQueryPropertyHandler, CalendarMultiGetPropertyHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	public void appendCalendarQueryPropertyValue(Element prop, IProxy proxy,
			EventTimeUpdate event) {
		Element val = DOMUtils.createElement(prop, "D:getetag");
		appendValue(val, event.getExtId() , event.getTimeUpdate());
	}

	@Override
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			Event event, String eventIcs) throws AppendPropertyException{
		Element val = DOMUtils.createElement(prop, "D:getetag");
		appendValue(val, event.getExtId(), event.getTimeUpdate() );
	}
	
	private void appendValue(Element e, String extId, Date dateupdate){
		if(dateupdate == null){
			dateupdate = new Date();
		}
		e.setTextContent("\"" + extId + "-" + dateupdate.getTime() + "\"");
	}
}
