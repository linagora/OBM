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
package org.obm.push

import org.obm.DateUtils.date
import org.obm.push.bean.AttendeeStatus
import org.obm.push.checks.Check
import org.obm.push.command.InvitationCommand
import org.obm.push.command.InvitationContext
import org.obm.push.command.ModifyInvitationCommand
import org.obm.push.command.SyncCollectionCommand.atLeastOneMeetingRequest
import org.obm.push.command.SyncCollectionCommand.atLeastOneDeleteResponse
import org.obm.push.context.User
import com.excilys.ebi.gatling.http.Predef._

import com.excilys.ebi.gatling.core.Predef.bootstrap.exec

class ModifyInvitationOneAttendeeAcceptOneDeclineSimulation extends InviteTwoUsersOneAcceptOneDeclineSimulation {

	override def buildScenario(users: Iterator[User]) = {
		super.buildScenario(users).exitHereIfFailed.exitBlockOnFail(
			exec(buildModifyInvitationCommand(invitation))
			.exec(s => organizer.sessionHelper.updatePendingInvitation(s))
			.pause(configuration.asynchronousChangeTime)
			.exec(buildSyncCommand(attendee1, usedMailCollection, atLeastOneMeetingRequest)) // Change notification reception
			.exec(buildSyncCommand(attendee2, usedMailCollection, atLeastOneMeetingRequest)) // Change notification reception
			.exec(buildMeetingResponseCommand(attendee1, AttendeeStatus.DECLINE))
			.exec(buildMeetingResponseCommand(attendee2, AttendeeStatus.ACCEPT))
			.exec(buildSyncCommand(attendee1, usedMailCollection, atLeastOneDeleteResponse)) // notification deletion
			.exec(buildSyncCommand(attendee2, usedMailCollection, atLeastOneDeleteResponse)) // notification deletion
			.pause(configuration.asynchronousChangeTime)
			.exec(buildSyncCommand(organizer, usedCalendarCollection, Check.matcher((s, response) 
					=> (organizer.sessionHelper.attendeeRepliesAreReceived(s, response.get), "Each users havn't replied"))))
		)
	}
	
	def buildModifyInvitationCommand(invitation: InvitationContext) = {
		val modifiedInvitation = invitation.modify(
				startTime = date("2014-01-14T09:00:00"),
				endTime = date("2014-01-14T11:00:00"),
				matcher = InvitationCommand.validModifiedInvitation)
		new ModifyInvitationCommand(modifiedInvitation, wbTools).buildCommand
	}
}
