/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.calendar;

import java.sql.SQLException;
import java.text.ParseException;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class ParticipationService {

	private static final Logger logger = LoggerFactory.getLogger(ParticipationService.class);

	private final CalendarDao calendarDao;

	@Inject
	protected ParticipationService(CalendarDao calendarDao) {
		this.calendarDao = calendarDao;

	}

	interface ChangeParticipationFunction {
		boolean apply() throws SQLException, ParseException;
	}

	@VisibleForTesting
	boolean changeOnEvent(final AccessToken token, final EventExtId extId,
			final Participation participation, int sequence,
			final ObmUser calendarOwner, Event currentEvent) throws SQLException, ParseException {

		return change(extId, participation, sequence, calendarOwner, currentEvent,
				new ChangeParticipationFunction() {

					@Override
					public boolean apply() throws SQLException, ParseException {
						logger.info(
								"Calendar : event[extId:{}] change participation state for user {} new state : {}",
								new Object[] { extId, calendarOwner.getEmail(), participation });
						return calendarDao.changeParticipation(token, calendarOwner, extId,
								participation);
					}
				});
	}

	@VisibleForTesting
	boolean changeOnOccurrence(final AccessToken token, final EventExtId extId,
			final RecurrenceId recurrenceId, final Participation participation, int sequence,
			final ObmUser calendarOwner, Event currentEvent) throws SQLException, ParseException {

		return change(extId, participation, sequence, calendarOwner, currentEvent,
				new ChangeParticipationFunction() {

					@Override
					public boolean apply() throws SQLException, ParseException {
						logger.info(
								"Calendar : event[extId:{} and recurrenceId:{}] change participation state for user {} new state : {}",
								new Object[] { extId, recurrenceId, calendarOwner.getEmail(),
										participation });
						return calendarDao.changeParticipation(token, calendarOwner, extId,
								recurrenceId, participation);
					}
				});
	}

	private boolean change(EventExtId extId, Participation participation,
			int sequence,
			ObmUser calendarOwner, Event currentEvent, ChangeParticipationFunction changeFunction)
			throws SQLException, ParseException {

		if (currentEvent.getSequence() == sequence) {
			Attendee attendee = currentEvent.findAttendeeFromEmail(calendarOwner.getEmailAtDomain());
			if (attendee.getParticipation().equals(participation)) {
				logger.info(
						"Calendar : event[extId:{}] change participation state for user {} with same state {} ignored",
						new Object[] { extId, calendarOwner.getEmail(), participation });
				return false;
			} else {
				participation.resetComment();
				return changeFunction.apply();
			}
		} else {
			logger.info("Calendar : event[extId:" + extId
					+ "] ignoring new participation state for user " +
					calendarOwner.getEmail()
					+ " as sequence number is different from current event (got " + sequence
					+ ", expected " + currentEvent.getSequence());
			return false;
		}
	}
}
