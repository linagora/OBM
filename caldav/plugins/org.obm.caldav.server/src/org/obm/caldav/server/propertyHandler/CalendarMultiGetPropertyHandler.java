package org.obm.caldav.server.propertyHandler;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.exception.AppendPropertyException;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Element;

public interface CalendarMultiGetPropertyHandler {
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			Event event, String eventICS) throws AppendPropertyException;
}
