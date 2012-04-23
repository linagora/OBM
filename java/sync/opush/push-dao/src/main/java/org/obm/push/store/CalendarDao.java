package org.obm.push.store;

import org.obm.push.bean.Device;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.calendar.EventExtId;

public interface CalendarDao {
	
	void insertExtIdMSEventUidMapping(EventExtId eventExtId, MSEventUid msEventUid, Device device) throws DaoException;
	
	EventExtId getEventExtIdFor(MSEventUid msEventUid, Device device) throws DaoException, EventNotFoundException;
	
	MSEventUid getMSEventUidFor(EventExtId eventExtId, Device device) throws DaoException;
	
}
