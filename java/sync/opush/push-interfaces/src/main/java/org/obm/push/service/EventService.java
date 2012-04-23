package org.obm.push.service;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.service.impl.EventParsingException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;

public interface EventService {

	void trackEventExtIdMSEventUidTranslation(EventExtId eventExtId, MSEventUid msEventUid, Device device) throws DaoException;
	
	EventExtId getEventExtIdFor(MSEventUid msEventUid, Device device) throws DaoException, EventNotFoundException;
	
	MSEvent convertEventToMSEvent(BackendSession bs, Event event)  throws DaoException, ConversionException;

	MSEvent parseEventFromICalendar(BackendSession bs, String ics) throws EventParsingException, ConversionException;
	
}
