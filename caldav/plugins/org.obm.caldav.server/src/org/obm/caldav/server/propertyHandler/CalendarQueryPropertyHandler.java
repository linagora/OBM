package org.obm.caldav.server.propertyHandler;

import org.obm.caldav.server.IProxy;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Element;

public interface CalendarQueryPropertyHandler {
	public abstract void appendCalendarQueryPropertyValue(Element prop, IProxy proxy, Event event);
}
