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
package org.obm.push.helper

import scala.collection.JavaConversions._

import org.obm.push.bean.AttendeeStatus.ACCEPT
import org.obm.push.bean.AttendeeStatus.DECLINE
import org.obm.push.bean.FolderType
import org.obm.push.bean.MSAttendee
import org.obm.push.command.InvitationContext
import org.obm.push.command.PendingInvitationContext
import org.obm.push.context.UserKey
import org.obm.push.protocol.bean.FolderSyncResponse
import org.obm.push.protocol.bean.SyncResponse

import com.excilys.ebi.gatling.core.session.Session

class SessionHelper(userKey: UserKey) {
	
	def findLastSync(session: Session): Option[SyncResponse] = {
		session.getAttributeAsOption[SyncResponse](userKey.lastSyncSessionKey)
	}
	
	def findLastFolderSync(session: Session): Option[FolderSyncResponse] = {
		session.getAttributeAsOption[FolderSyncResponse](userKey.lastFolderSyncSessionKey)
	}
	
	def findLastInvitationClientId(session: Session) = 
		session.getTypedAttribute[String](userKey.lastInvitationClientIdSessionKey)
	
	def findPendingInvitation(session: Session): Option[PendingInvitationContext] = {
		session.getAttributeAsOption[PendingInvitationContext](userKey.lastPendingInvitationSessionKey)
	}
	
	
	def collectionId(session: Session, folderType: FolderType): Int = {
		findLastFolderSync(session).get
			.getCollectionsAddedAndUpdated()
			.filter(_.getFolderType() == folderType)
			.head.getCollectionId().toInt
	}
	
	def attendeeRepliesAreNotReceived(s: Session, response: SyncResponse) = !attendeeRepliesAreReceived(s, response)
	def attendeeRepliesAreReceived(s: Session, response: SyncResponse): Boolean = {
		val invitation = findPendingInvitation(s).get
		setAttendeeRepliesFromResponse(response, invitation, userKey.getUser(s).email)
		invitation.hasReplyOfEveryAttendees
	}
	
	def setAttendeeRepliesFromResponse(response: SyncResponse, invitation: PendingInvitationContext, organizerEmail: String) = {
		invitation.attendeeReplies = SyncHelper
			.findEventChanges(response, invitation.serverId)
			.flatMap(_.getAttendees())
			.filter(attendeeHasReplied(organizerEmail, _))
			.map(a => (a.getEmail() -> a.getAttendeeStatus())).toMap
	}
	
	def attendeeHasReplied(organizerEmail: String, attendee: MSAttendee): Boolean = {
		if ((attendee.getAttendeeStatus() == ACCEPT) || (attendee.getAttendeeStatus() == DECLINE)) {
			if (!organizerEmail.equals(attendee.getEmail())) {
				return true
			}
		}
		return false
	}
	
	def invitationIsNotReceived(session: Session) = !invitationIsReceived(session)
	def invitationIsReceived(session: Session): Boolean = {
		!SyncHelper.findChangesWithMeetingRequest(findLastSync(session).get).isEmpty
	}
	
	def setupNextInvitationClientId(session: Session): Session = {
		session.setAttribute(userKey.lastInvitationClientIdSessionKey, InvitationContext.generateClientId)
	}
	
	def setupPendingInvitation(session: Session, invitation: InvitationContext): Session = {
		val clientId = findLastInvitationClientId(session)
		val change = SyncHelper.findChanges(findLastSync(session).get)
								.filter(change => clientId.equals(change.getClientId())).head
		setPendingInvitation(session, new PendingInvitationContext(invitation, change.getServerId()))
	}
	
	def updatePendingInvitation(session: Session): Session = {
		val invitation = findPendingInvitation(session).get
		setAttendeeRepliesFromResponse(findLastSync(session).get, invitation, userKey.getUser(session).email)
		setPendingInvitation(session, invitation)
	}
	
	def setPendingInvitation(session: Session, pendingInvitation: PendingInvitationContext): Session = {
		session.setAttribute(userKey.lastPendingInvitationSessionKey, pendingInvitation)
	}
	
}