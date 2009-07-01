package org.obm.caldav.server.propertyHandler.impl;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.exception.AppendPropertyException;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.Event;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class CalendarData extends DavPropertyHandler implements
		CalendarMultiGetPropertyHandler {

	@Override
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			Event event, String eventIcs) throws AppendPropertyException {
		if (eventIcs == null || "".equals(eventIcs)) {
			throw new AppendPropertyException(StatusCodeConstant.SC_NOT_FOUND);
		}
		
		Element val = DOMUtils.createElement(prop, "calendar-data");
		CDATASection cdata = prop.getOwnerDocument().createCDATASection(
				eventIcs);
		val.appendChild(cdata);
	}

}
