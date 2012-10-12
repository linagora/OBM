package fr.aliacom.obm.common.calendar.loader.filter;

import java.util.Map;

import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;

public interface EventFilter {
	Map<EventObmId, Event> filter(Map<EventObmId, Event> events);
}
