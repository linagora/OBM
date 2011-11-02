package org.obm.push.store;

import org.obm.push.bean.Device;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.sync.calendar.EventObmId;

public interface CalendarDao {
	
	void insertObmIdMSEventUidMapping(EventObmId eventObmId, MSEventUid msEventUid, Device device) throws DaoException;
	
	EventObmId getEventObmIdFor(MSEventUid msEventUid, Device device) throws DaoException;
	
	MSEventUid getMsEventUidFor(EventObmId eventObmId, Device device) throws DaoException;
	
}
