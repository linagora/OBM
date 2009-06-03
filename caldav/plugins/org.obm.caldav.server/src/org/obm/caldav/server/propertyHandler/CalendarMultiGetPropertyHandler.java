package org.obm.caldav.server.propertyHandler;

import org.obm.caldav.server.IProxy;
import org.w3c.dom.Element;

public interface CalendarMultiGetPropertyHandler {
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			String eventId, String eventICS);
}
