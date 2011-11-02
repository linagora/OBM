package org.obm.push.service;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;

public interface EventService {

	void trackEventObmIdMSEventUidTranslation(EventObmId eventObmId, MSEventUid msEventUid, Device device) throws DaoException;
	
	EventObmId getEventObmIdFor(MSEventUid msEventUid, Device device) throws DaoException;
	
	MSEvent convertEventToMSEvent(BackendSession bs, Event event)  throws DaoException;
	
}
