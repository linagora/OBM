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

import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class EventChangeHandler {

	private final JMSService jmsService;
	private final EventNotificationService eventNotificationService;

	@Inject
	/* package */ EventChangeHandler(JMSService jmsService, EventNotificationService eventNotificationService) {
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
		if (current.hasChangesOnEventAttributesExceptedEventException(previous)) {
			update(notification, token, previous, current);
		} else {
			List<Event> exceptionWithChanges = current.getExceptionsWithImportantChanges(previous);
			for (Event exception: exceptionWithChanges) {
				Event previousException = previous.getEventInstanceWithRecurrenceId(exception.getRecurrenceId());
				update(notification, token, previousException, exception);
			}
		}
	}

	public void updateParticipationState(Event event, ObmUser calendarOwner, 
			ParticipationState state, boolean notification, AccessToken token) {
		
		jmsService.writeIcsInvitationReply(token, event);
		if (notification) {
			eventNotificationService.notifyUpdatedParticipationStateAttendees(event, calendarOwner, state, token);
		}
	}
	
	public void delete(Event event, boolean notification, AccessToken token) throws NotificationException {
		jmsService.writeIcsInvitationCancel(token, event);
		if (notification) {
			eventNotificationService.notifyDeletedEvent(event, token);
		}
	}

	private void update(boolean notification, AccessToken token, Event previousException, Event exception) {
		jmsService.writeIcsInvitationRequest(token, exception);
		if (notification) {
			eventNotificationService.notifyUpdatedEvent(previousException, exception, token);
		}
	}

}
