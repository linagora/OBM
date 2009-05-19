package org.obm.caldav.obmsync.service;

import java.util.Set;

import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;

public interface ICalendarService {
	Set<CalendarInfo> getListCalendars(Event calendar) throws Exception;
}
