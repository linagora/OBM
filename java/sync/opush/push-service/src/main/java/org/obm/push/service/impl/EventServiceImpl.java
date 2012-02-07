package org.obm.push.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.obm.push.ObmEventToMsEventConverter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.exception.DaoException;
import org.obm.push.service.EventService;
import org.obm.push.store.CalendarDao;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EventServiceImpl implements EventService {

	private final CalendarDao calendarDao;
	private final ObmEventToMsEventConverter obmEventToMsEventConverter;
	
	@Inject
	private EventServiceImpl(CalendarDao calendarDao, ObmEventToMsEventConverter obmEventToMsEventConverter) {
		super();
		this.calendarDao = calendarDao;
		this.obmEventToMsEventConverter = obmEventToMsEventConverter;
	}

	@Override
	public MSEvent convertEventToMSEvent(BackendSession bs, Event event)
			throws DaoException {
		MSEventUid msEventUid = getMsEventUidFor(event, bs.getDevice());
		if (msEventUid == null) {
			msEventUid = createMsEventUidFromEventExtId(event);
		}
		MSEvent msEvent = obmEventToMsEventConverter.convert(event, msEventUid, bs.getCredentials().getUser());
		return msEvent;
	}
	
	private MSEventUid getMsEventUidFor(Event event, Device device) throws DaoException {
		if (event.getObmId() == null || event.getObmId().getIndex() == null) {
			return null;
		}
		MSEventUid msEventUidFromDatabase = calendarDao.getMsEventUidFor(event.getObmId(), device);
		if (msEventUidFromDatabase != null) {
			return msEventUidFromDatabase;
		}
		MSEventUid convertedFromExtId = createMsEventUidFromEventExtId(event);
		calendarDao.insertObmIdMSEventUidMapping(event.getObmId(), convertedFromExtId, device);
		return convertedFromExtId;
	}

	private MSEventUid createMsEventUidFromEventExtId(Event event) {
		return new MSEventUid(convertExtIdAsHex(event.getExtId()));
	}
	
	private String convertExtIdAsHex(EventExtId extId) {
		return Hex.encodeHexString(extId.getExtId().getBytes(Charsets.US_ASCII));
	}

	@Override
	public EventObmId getEventObmIdFor(MSEventUid msEventUid, Device device) throws DaoException {
		return calendarDao.getEventObmIdFor(msEventUid, device);
	}
	
	@Override
	public void trackEventObmIdMSEventUidTranslation(EventObmId eventObmId,
			MSEventUid msEventUid, Device device) throws DaoException {
		calendarDao.insertObmIdMSEventUidMapping(eventObmId, msEventUid, device);
	}
}
