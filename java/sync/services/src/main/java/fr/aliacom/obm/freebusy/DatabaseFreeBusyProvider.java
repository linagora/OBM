/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.freebusy;

import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.icalendar.Ical4jHelper;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.exception.ObmUserNotFoundException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;

/**
 * Retrieves freebusy data from the local database.
 */
@Singleton
public class DatabaseFreeBusyProvider implements LocalFreeBusyProvider {
	private Ical4jHelper ical4jHelper;
	private CalendarDao calendarDao;
	private UserService userService;

	@Inject
	/* package */DatabaseFreeBusyProvider(
			Ical4jHelper ical4jHelper, CalendarDao calendarDao, UserService userService) {
		this.ical4jHelper = ical4jHelper;
		this.calendarDao = calendarDao;
		this.userService = userService;
	}

	@Override
	@Transactional(readOnly=true, noRollbackOn = {PrivateFreeBusyException.class})
	public String findFreeBusyIcs(FreeBusyRequest fbr) throws FreeBusyException, ObmUserNotFoundException {
		String ics = null;

		FreeBusy freeBusy = findFreeBusy(fbr);
		if (freeBusy != null) {
			ics = ical4jHelper.parseFreeBusy(freeBusy);
		}
		return ics;
	}

	private FreeBusy findFreeBusy(FreeBusyRequest fbr) throws PrivateFreeBusyException, ObmUserNotFoundException {
		Attendee attendee = fbr.getAttendees().get(0);
		String email = attendee.getEmail();
		ObmUser user = userService.getUserFromEmail(email);
		
		if (user == null) {
			throw new ObmUserNotFoundException("Could not find any ObmUser with email " + email);
		}

		if (!user.isPublicFreeBusy()) {
			throw new PrivateFreeBusyException();
		}

		return getFreeBusy(fbr, user);
	}

	private FreeBusy getFreeBusy(FreeBusyRequest fbr, ObmUser user) {
		List<FreeBusy> freeBusyList = calendarDao.getFreeBusy(user.getDomain(), fbr);
		return !freeBusyList.isEmpty() ? freeBusyList.get(0) : null;
	}
}
