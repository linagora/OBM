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

import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;

import com.google.common.collect.Iterables;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ObmEventToMSEventByICSLoopConverter implements ObmEventToMSEventConverter {


	private final Ical4jHelper ical4j;
	private final ObmEventToMSEventConverter obmEventToMSEventConverter;
	
	public ObmEventToMSEventByICSLoopConverter(Ical4jHelper ical4j) {
		obmEventToMSEventConverter = new ObmEventToMSEventConverterImpl();
		this.ical4j = ical4j;
	}
	
	@Override
	public MSEvent convert(Event eventToConvert, MSEventUid uid, User user) throws ConversionException {
				
		try {
			Ical4jUser ical4jUser = convertIcal4jUser(user);
			String eventAsICS = ical4j.parseEvent(eventToConvert, ical4jUser, new AccessToken(0, "unit testing"));
			List<Event> eventsFromICS = ical4j.parseICSEvent(eventAsICS, ical4jUser, 0);
			Event eventFromICS = Iterables.getOnlyElement(eventsFromICS);
			return obmEventToMSEventConverter.convert(eventFromICS, uid, user);
		} catch (ParserException e) {
			throw new ConversionException(e);
		} catch (IOException e) {
			throw new ConversionException(e);
		}
	}

	private Ical4jUser convertIcal4jUser(User user) {
		ObmDomain obmDomain = ObmDomain
                				.builder()
                				.id(1)
                				.name(user.getDomain())
                				.uuid(ObmDomainUuid.of("83bff451-11b7-8f55-d06b-7013cb8a0531"))
                				.build();
		
		return Ical4jUser.Factory.create().createIcal4jUser(user.getLoginAtDomain(), obmDomain);
	}

}
