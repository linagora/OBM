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
package org.obm.push.helper

import org.obm.push.command.InvitationContext
import org.obm.push.command.PendingInvitationContext
import org.obm.push.protocol.bean.SyncResponse
import com.excilys.ebi.gatling.core.session.Session
import org.obm.push.bean.MSEvent
import org.obm.push.bean.AttendeeStatus
import org.obm.push.bean.AttendeeStatus.{ACCEPT, DECLINE}
import scala.collection.JavaConversions._
import org.obm.push.protocol.bean.FolderSyncResponse
import org.obm.push.bean.MSAttendee

object SessionKeys extends Enumeration {
	type Keys = Value
	
	val LAST_FOLDER_SYNC = Value("lastFolderSync")
	val LAST_SYNC = Value("lastSync")
	val MEETING_RESPONSE = Value("meetingResponse")
	val PENDING_INVITATION = Value("pendingInvitation")
	
}

object SessionHelper {
	
	def findLastSync(session: Session): Option[SyncResponse] = {
		session.getAttributeAsOption[SyncResponse](SessionKeys.LAST_SYNC.toString)
	}
	
	def findLastFolderSync(session: Session): Option[FolderSyncResponse] = {
		session.getAttributeAsOption[FolderSyncResponse](SessionKeys.LAST_FOLDER_SYNC.toString)
	}
	
	def findPendingInvitation(session: Session): Option[PendingInvitationContext] = {
		session.getAttributeAsOption[PendingInvitationContext](SessionKeys.PENDING_INVITATION.toString)
	}
	
	def attendeeRepliesAreNotReceived(session: Session) = !attendeeRepliesAreReceived(session)
	def attendeeRepliesAreReceived(session: Session): Boolean = {
		val pendingInvitationOpt = findPendingInvitation(session)
		if (pendingInvitationOpt.isDefined) {
			val lastSync = findLastSync(session).get
			val pendingInvitation = pendingInvitationOpt.get
			for (event <- SyncHelper.findEventChanges(lastSync, pendingInvitation.serverId);
				attendee <- event.getAttendees();
				if (attendeeHasReplied(pendingInvitation, attendee))) {
				
				pendingInvitation.attendeeReplies += (attendee.getEmail() -> attendee.getAttendeeStatus())
			}
			return pendingInvitation.hasReplyOfEveryAttendees
		}
		throw new IllegalStateException("Check attendee replies but no pending invitation")
	}
	
	def attendeeHasReplied(pendingInvitation: PendingInvitationContext, attendee: MSAttendee): Boolean = {
		if ((attendee.getAttendeeStatus() == ACCEPT) || (attendee.getAttendeeStatus() == DECLINE)) {
			if (!pendingInvitation.invitation.organizerEmail.equals(attendee.getEmail())) {
				return true
			}
		}
		return false
	}
	
	def invitationIsNotReceived(session: Session) = !invitationIsReceived(session)
	def invitationIsReceived(session: Session): Boolean = {
		val lastSync = findLastSync(session)
		if (lastSync.isDefined) {
			val isReceived = !SyncHelper.findChangesWithMeetingRequest(lastSync.get).isEmpty
			return isReceived
		}
		false
	}
	
	def setupPendingInvitation(session: Session, invitation: InvitationContext): Session = {
		var outgoingSession = session
		val lastSync = findLastSync(session)
		if (lastSync.isDefined) {
			for (change <- SyncHelper.findChanges(lastSync.get)
				if invitation.clientId.equals(change.getClientId())) {
				
				outgoingSession = updatePendingInvitation(session, new PendingInvitationContext(invitation, change.getServerId()))
			}
		}
		outgoingSession
	}
	
	def updatePendingInvitation(session: Session, pendingInvitation: PendingInvitationContext): Session = {
		session.setAttribute(SessionKeys.PENDING_INVITATION.toString, pendingInvitation)
	}
	
}