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
package org.obm.push.command

import java.util.Date
import java.util.UUID

import org.obm.DateUtils.date
import org.obm.push.bean.AttendeeStatus
import org.obm.push.bean.AttendeeType
import org.obm.push.bean.CalendarBusyStatus
import org.obm.push.bean.CalendarMeetingStatus
import org.obm.push.bean.CalendarSensitivity
import org.obm.push.bean.MSAttendee
import org.obm.push.bean.MSEvent
import org.obm.push.bean.MSEventUid
import org.obm.push.context.User
import org.obm.push.encoder.GatlingEncoders.calendarEncoder
import org.obm.push.protocol.bean.SyncRequestCollection
import org.obm.push.protocol.bean.SyncRequestCollectionCommand
import org.obm.push.protocol.bean.SyncRequestCollectionCommands
import org.obm.push.utils.DOMUtils
import org.obm.push.wbxml.WBXMLTools

import com.excilys.ebi.gatling.core.Predef.Session
import com.google.common.collect.ImmutableList

class SendInvitationCommand(invitation: InvitationContext, wbTools: WBXMLTools)
		extends AbstractSyncCommand(invitation, wbTools) {

	override def buildSyncRequestCollections(session: Session) = {
		List(SyncRequestCollection.builder()
				.id(invitation.findCollectionId(session))
				.syncKey(invitation.nextSyncKey(session))
				.commands(SyncRequestCollectionCommands.builder()
					.commands(ImmutableList.of(
						SyncRequestCollectionCommand.builder()
							.name("Add")
							.clientId(invitation.userKey.sessionHelper.findLastInvitationClientId(session))
							.applicationData(buildInvitationData(session))
							.build()))
					.build())
				.build())
	}
	
	def buildInvitationData(session: Session) = {
		val organizer = invitation.userKey.getUser(session)
		val attendees = for (attendee <- invitation.attendees) yield attendee.getUser(session)
		
		val parent = DOMUtils.createDoc(null, "ApplicationData").getDocumentElement()
		calendarEncoder.encode(organizer.device, parent, buildEventInvitation(organizer, attendees), true)
		parent
	}
	
	def buildEventInvitation(organizer: User, attendees: Iterable[User]) = {
		val event = new MSEvent()
		event.setDtStamp(new Date())
		event.setUid(new MSEventUid(UUID.randomUUID().toString()))
		event.setSubject("Invitation from %s".format(organizer.email))
		event.setMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
		event.setBusyStatus(CalendarBusyStatus.BUSY)
		event.setSensitivity(CalendarSensitivity.NORMAL)
		
		event.setOrganizerEmail(organizer.email)
		for (attendee <- attendees) {
			event.addAttendee(MSAttendee.builder()
					.withEmail(attendee.email)
					.withType(AttendeeType.REQUIRED)
					.withStatus(AttendeeStatus.NOT_RESPONDED)
					.build())
		}
		
		event.setAllDayEvent(false)
		event.setStartTime(date("2014-12-01T09:00:00"))
		event.setEndTime(date("2014-12-01T10:00:00"))
		event
	}
}
