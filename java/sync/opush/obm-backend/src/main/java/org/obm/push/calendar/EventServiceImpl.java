/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.calendar;

import java.io.IOException;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.apache.commons.codec.binary.Hex;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EventNotFoundException;
import org.obm.push.resource.ResourcesUtils;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.EventParsingException;
import org.obm.push.store.CalendarDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EventServiceImpl implements EventService {

	private final CalendarDao calendarDao;
	private final EventConverter eventConverter;
	private final Ical4jHelper ical4jHelper;
	private final Ical4jUser.Factory ical4jUserFactory;

	@Inject
	@VisibleForTesting EventServiceImpl(CalendarDao calendarDao, EventConverter eventConverter, 
			Ical4jHelper ical4jHelper, Ical4jUser.Factory ical4jUserFactory) {
		super();
		this.calendarDao = calendarDao;
		this.eventConverter = eventConverter;
		this.ical4jHelper = ical4jHelper;
		this.ical4jUserFactory = ical4jUserFactory;
	}

	@Override
	public MSEvent convertEventToMSEvent(UserDataRequest udr, Object event) throws DaoException, ConversionException {
		Event obmEvent = (Event) event;
		MSEventUid msEventUid = getMSEventUidFor(obmEvent.getExtId().getExtId(), udr.getDevice());
		MSEvent msEvent = eventConverter.convert(obmEvent, msEventUid, udr.getCredentials().getUser());
		return msEvent;
	}
	
	@Override
	public MSEventUid getMSEventUidFor(String eventExtId, Device device) throws DaoException {
		Preconditions.checkNotNull(eventExtId, "Event must contain an extId");
		MSEventUid msEventUidFromDatabase = retrieveMSEventUidFromDatabase(eventExtId, device);
		if (msEventUidFromDatabase != null) {
			return msEventUidFromDatabase;
		}
		return createMSEventUidInDatabase(eventExtId, device);
	}

	private MSEventUid createMSEventUidInDatabase(String eventExtId, Device device) throws DaoException {
		MSEventUid convertedFromExtId = createMSEventUidFromEventExtId(eventExtId);
		byte[] hashedExtId = hashEventExtId(eventExtId);
		calendarDao.insertExtIdMSEventUidMapping(new EventExtId(eventExtId), convertedFromExtId, device, hashedExtId);
		return convertedFromExtId;
	}

	private byte[] hashEventExtId(String eventExtId) {
		HashCode hashCode = Hashing.sha1().hashString(eventExtId, Charsets.US_ASCII);
		return hashCode.asBytes();
	}

	private MSEventUid retrieveMSEventUidFromDatabase(String eventExtId, Device device)
			throws DaoException {
		MSEventUid msEventUidFromDatabase = calendarDao.getMSEventUidFor(new EventExtId(eventExtId), device);
		return msEventUidFromDatabase;
	}

	private MSEventUid createMSEventUidFromEventExtId(String eventExtId) {
		return new MSEventUid(convertExtIdAsHex(eventExtId));
	}
	
	private String convertExtIdAsHex(String extId) {
		return Hex.encodeHexString(extId.getBytes(Charsets.US_ASCII));
	}

	@Override
	public String getEventExtIdFor(MSEventUid msEventUid, Device device) throws DaoException, EventNotFoundException {
		try {
			return calendarDao.getEventExtIdFor(msEventUid, device).getExtId();
		} catch (org.obm.sync.auth.EventNotFoundException e) {
			throw new EventNotFoundException(e);
		}
	}
	
	@Override
	public void trackEventExtIdMSEventUidTranslation(String eventExtId,
			MSEventUid msEventUid, Device device) throws DaoException {
		byte[] hashedExtId = hashEventExtId(eventExtId);
		calendarDao.insertExtIdMSEventUidMapping(new EventExtId(eventExtId), msEventUid, device, hashedExtId);
	}
	
	@Override
	public MSEvent parseEventFromICalendar(UserDataRequest udr, String ics) throws EventParsingException, ConversionException {
		try {
			AccessToken accessToken = ResourcesUtils.getAccessToken(udr);
			Ical4jUser ical4jUser = ical4jUserFactory.createIcal4jUser(udr.getUser().getEmail(), accessToken.getDomain());
			List<Event> obmEvents = ical4jHelper.parseICSEvent(ics, ical4jUser, accessToken.getObmId());
			
			if (!obmEvents.isEmpty()) {
				final Event icsEvent = obmEvents.get(0);
				return convertEventToMSEvent(udr, icsEvent);
			}
			return null;
		} catch (DaoException e) {
			throw new EventParsingException(e);
		} catch (IOException e) {
			throw new EventParsingException(e);
		} catch (ParserException e) {
			throw new EventParsingException(e);
		}
	}
}
