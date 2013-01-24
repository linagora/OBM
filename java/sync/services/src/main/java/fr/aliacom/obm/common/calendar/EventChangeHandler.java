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
package fr.aliacom.obm.common.calendar;

import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.Participation;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class EventChangeHandler {

	private final MessageQueueService jmsService;
	private final EventNotificationService eventNotificationService;

	@Inject
	/* package */ EventChangeHandler(MessageQueueService jmsService, EventNotificationService eventNotificationService) {
		this.jmsService = jmsService;
		this.eventNotificationService = eventNotificationService;
	}

	public void create(Event event, boolean notification, AccessToken token) throws NotificationException {
		jmsService.writeIcsInvitationRequest(token, event);
		if (notification) {
			eventNotificationService.notifyCreatedEvent(event, token);
		}
	}

	public void update(Event previous, Event current, boolean notification, AccessToken token)throws NotificationException {
		if (current.hasChangesExceptedEventException(previous)) {
			updateEvent(notification, token, previous, current);
		}
		updateAddedEventExceptions(previous, current, notification, token);
		updateModifiedEventExceptions(previous, current, notification, token);
		updateDeletedEventExceptions(previous, current, notification, token);
		updateNegativeExceptionsChanges(previous, current, notification, token);
	}

	private void updateAddedEventExceptions(Event previous, Event current,
			boolean notification, AccessToken token) {
		List<Event> addedEventExceptions = current.getAddedEventExceptions(previous);
		for (Event eventException: addedEventExceptions) {
			Event previousException = previous.getOccurrence(eventException.getRecurrenceId());
			updateEvent(notification, token, previousException, eventException);
		}
	}

	private void updateModifiedEventExceptions(Event previous, Event current,
			boolean notification, AccessToken token) {
		List<Event> modifiedEventExceptions = current.getModifiedEventExceptions(previous);
		for (Event eventException: modifiedEventExceptions) {
			Event previousException = previous.getOccurrence(eventException.getRecurrenceId());
			updateEvent(notification, token, previousException, eventException);
		}
	}

	private void updateDeletedEventExceptions(Event previous, Event current,
			boolean notification, AccessToken token) {
		List<Event> deletedEventExceptions = current.getDeletedEventExceptions(previous);
		for (Event eventException: deletedEventExceptions) {
			delete(eventException, notification, token);
		}
	}

	private void updateNegativeExceptionsChanges(Event previous,
			Event current, boolean notification, AccessToken token) {
		for (Date date : current.getNegativeExceptionsChanges(previous)) {
			Event negativeException = current.getOccurrence(date);
			delete(negativeException, notification, token);
		}
	}

	public void updateParticipation(Event event, ObmUser calendarOwner,
			Participation participation, boolean notification, AccessToken token) {
		
		jmsService.writeIcsInvitationReply(token, event, calendarOwner);
		if (notification) {
			eventNotificationService.notifyUpdatedParticipationAttendees(event, calendarOwner, participation, token);
		}
	}
	
	public void delete(Event event, boolean notification, AccessToken token) throws NotificationException {
		jmsService.writeIcsInvitationCancel(token, event);
		if (notification) {
			eventNotificationService.notifyDeletedEvent(event, token);
		}
	}

	private void updateEvent(boolean notification, AccessToken token, Event previousEvent, Event currentEvent) {
		jmsService.writeIcsInvitationRequest(token, currentEvent);
		if (notification) {
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, token);
		}
	}

}
