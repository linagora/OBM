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
package org.obm.push.command

import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.collectionAsScalaIterable

import org.obm.push.bean.MSEvent
import org.obm.push.bean.SyncCollectionCommands
import org.obm.push.bean.SyncCollectionRequest
import org.obm.push.checks.Check
import org.obm.push.context.User
import org.obm.push.encoder.GatlingEncoders.calendarEncoder
import org.obm.push.protocol.bean.SyncResponse
import org.obm.push.utils.DOMUtils
import org.obm.push.wbxml.WBXMLTools

import com.excilys.ebi.gatling.core.Predef.Session

abstract class AbstractInvitationCommand(invitation: InvitationContext, wbTools: WBXMLTools)
		extends AbstractSyncCommand(invitation, wbTools) {

	override def buildSyncCollectionRequest(session: Session) = {
		SyncCollectionRequest.builder()
				.collectionId(invitation.findCollectionId(session))
				.syncKey(invitation.nextSyncKey(session))
				.commands(SyncCollectionCommands.Request.builder()
					.addCommand(org.obm.push.bean.SyncCollectionCommand.Request.builder()
							.name(collectionCommandName)
							.clientId(clientId(session))
							.serverId(serverId(session))
							.applicationData(buildInvitationData(session))
							.build())
					.build())
				.build()
	}
	
	def buildInvitationData(session: Session) = {
		val organizer = invitation.userKey.getUser(session)
		val attendees = invitation.attendees.map(_.getUser(session))
		
		val event = buildEventInvitation(session, organizer, attendees)
		val parent = DOMUtils.createDoc(null, "ApplicationData").getDocumentElement()
		calendarEncoder.encode(organizer.device, parent, event, true)
		event
	}
	
	val collectionCommandName: String
	def clientId(session: Session): String
	def serverId(session: Session): String
	
	def buildEventInvitation(session: Session, organizer: User, attendees: Iterable[User]): MSEvent
}


object InvitationCommand {
	val validSentInvitation = Check.manyToOne(Seq(SyncCollectionCommand.validSync, SyncCollectionCommand.atLeastOneAddResponse))
	val validModifiedInvitation = Check.manyToOne(Seq(SyncCollectionCommand.validSync, SyncCollectionCommand.atLeastOneModifyResponse))
	val validDeleteInvitation = Check.manyToOne(Seq(SyncCollectionCommand.validSync, SyncCollectionCommand.atLeastOneDeleteResponse))
}