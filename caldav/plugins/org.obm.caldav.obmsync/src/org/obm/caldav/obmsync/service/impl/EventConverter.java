package org.obm.caldav.obmsync.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.obm.caldav.server.share.CalendarResource;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.DavComponentType;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;

/**
 * 
 * @author adrienp
 * 
 */
public class EventConverter {

	public static List<DavComponent> convert(List<EventTimeUpdate> etus,
			String compUrl, DavComponentType type) {
		LinkedList<DavComponent> ret = new LinkedList<DavComponent>();
		for (EventTimeUpdate event : etus) {
			ret.add(convert(event, compUrl, type));
		}
		return ret;
	}
	
	/**
	 * OBM to CalendarResource
	 * 
	 * @param etu
	 * @param compUrl
	 *            The URL of the parent component
	 * @param type
	 *            The DavComponentType of the event(VEVENT or VTODO)
	 * @return
	 */
	public static CalendarResource convert(EventTimeUpdate etu, String compUrl,
			DavComponentType type) {
		return new CalendarResource(etu.getExtId(), compUrl, etu
				.getTimeUpdate(), type);
	}
	
	public static CalendarResource convert(Event e, String compUrl) {
		DavComponentType type = getType(e.getType());
		return new CalendarResource(e.getExtId(), compUrl,
				e.getTimeUpdate(), type);
	}

	/**
	 * OBM to CalendarResourceICS
	 * 
	 * @return
	 */
	public static CalendarResourceICS convert(Event e, String compUrl,
			String ics) {
		DavComponentType type = getType(e.getType());
		return new CalendarResourceICS(e.getExtId(), compUrl,
				e.getTimeUpdate(), type, ics);
	}

	private static DavComponentType getType(EventType type) {
		switch (type) {
			case VEVENT:
				return DavComponentType.VEVENT;
			case VTODO:
				return DavComponentType.VTODO;
		}
		return null;
	}

}
