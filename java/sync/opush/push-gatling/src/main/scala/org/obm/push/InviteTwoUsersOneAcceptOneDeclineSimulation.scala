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
import org.obm.push.bean.FolderType
import org.obm.push.checks.Check
import org.obm.push.command.FolderSyncCommand
import org.obm.push.command.InitialFolderSyncContext
import org.obm.push.command.InitialSyncContext
import org.obm.push.command.InvitationCommand
import org.obm.push.command.InvitationContext
import org.obm.push.command.MeetingResponseCommand
import org.obm.push.command.MeetingResponseContext
import org.obm.push.command.SendInvitationCommand
import org.obm.push.command.SyncCollectionCommand
import org.obm.push.command.SyncCollectionCommand.atLeastOneMeetingRequest
import org.obm.push.command.SyncCollectionCommand.validSync
import org.obm.push.command.SyncContext
import org.obm.push.context.Configuration
import org.obm.push.context.GatlingConfiguration
import org.obm.push.context.User
import org.obm.push.context.UserKey
import org.obm.push.context.feeder.UserFeeder
import org.obm.push.protocol.bean.SyncResponse
import org.obm.push.wbxml.WBXMLTools
import com.excilys.ebi.gatling.core.Predef.Simulation
import com.excilys.ebi.gatling.core.Predef.bootstrap.exec
import com.excilys.ebi.gatling.core.Predef.scenario
import com.excilys.ebi.gatling.core.session.Session
import com.excilys.ebi.gatling.core.check.MatchStrategy
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.request.builder.PostHttpRequestBuilder
import com.excilys.ebi.gatling.core.action.builder.ActionBuilder
import com.excilys.ebi.gatling.core.config.HttpConfiguration

class InviteTwoUsersOneAcceptOneDeclineSimulation extends Simulation {

	val wbTools: WBXMLTools = new WBXMLTools
	val configuration: Configuration = GatlingConfiguration.build

	val usedMailCollection = FolderType.DEFAULT_INBOX_FOLDER 
	val usedCalendarCollection = FolderType.DEFAULT_CALENDAR_FOLDER
	
	val organizer = new UserKey("organizer")
	val attendee1 = new UserKey("attendee1")
	val attendee2 = new UserKey("attendee2")
	val invitation = new InvitationContext(
		organizer = organizer,
		attendees = Set(attendee1, attendee2),
		startTime = date("2014-01-12T09:00:00"),
		endTime = date("2014-01-12T10:00:00"),
		folderType = usedCalendarCollection)
	
		
	val users = for (userNumber <- Iterator.range(1, 100)) yield new User(userNumber, configuration)
	
	val httpConf = httpConfig
		.baseURL(configuration.targetServerUrl)
		.disableFollowRedirect
		.disableCaching
	
	setUp(buildScenario(users)
			.users(configuration.parallelsScenariosCount)
			.protocolConfig(httpConf))

	def buildScenario(users: Iterator[User]) = {

		val feeder = new UserFeeder(users, organizer, attendee1, attendee2)
		
		scenario("Send an invitation at two attendees")
			.exitBlockOnFail(
				exec(s => s.setAttributes(feeder.next()))
				.exec(buildInitialFolderSyncCommand(organizer))
				.exec(buildInitialFolderSyncCommand(attendee1))
				.exec(buildInitialFolderSyncCommand(attendee2))
				.exec(buildInitialSyncCommand(organizer, usedCalendarCollection))
				.exec(s => organizer.sessionHelper.setupNextInvitationClientId(s))
				.exec(buildSendInvitationCommand(invitation))
				.exec(s => organizer.sessionHelper.setupPendingInvitation(s, invitation))
				.pause(configuration.asynchronousChangeTime)
				.exec(buildInitialSyncCommand(attendee1, usedMailCollection))
				.exec(buildInitialSyncCommand(attendee2, usedMailCollection))
				.exec(buildSyncCommand(attendee1, usedMailCollection, atLeastOneMeetingRequest))
				.exec(buildSyncCommand(attendee2, usedMailCollection, atLeastOneMeetingRequest))
				.exec(buildMeetingResponseCommand(attendee1, AttendeeStatus.ACCEPT))
				.exec(buildMeetingResponseCommand(attendee2, AttendeeStatus.DECLINE))
				.pause(configuration.asynchronousChangeTime)
				.exec(buildSyncCommand(organizer, usedCalendarCollection, Check.matcher((s, response) 
						=> (organizer.sessionHelper.attendeeRepliesAreReceived(s, response.get), "Each users havn't replied"))))
			)
	}
	
	def buildInitialFolderSyncCommand(userKey: UserKey): PostHttpRequestBuilder = {
		val context = new InitialFolderSyncContext(userKey, FolderSyncCommand.validInitialFolderSync)
		new FolderSyncCommand(context, wbTools).buildCommand
	}
	
	def buildInitialSyncCommand(userKey: UserKey, folderType: FolderType) = {
		buildSyncCommand(new InitialSyncContext(userKey, folderType, validSync))
	}
	
	def buildSyncCommand(userKey: UserKey, folderType: FolderType, matchers: MatchStrategy[SyncResponse]*): PostHttpRequestBuilder = {
		val matcher = Check.manyToOne(validSync :: matchers.toList)
		buildSyncCommand(new SyncContext(userKey, folderType, matcher))
	}
	
	def buildSyncCommand(syncContext: SyncContext) = {
		new SyncCollectionCommand(syncContext, wbTools).buildCommand
	}
	
	def buildSendInvitationCommand(invitation: InvitationContext) = {
		invitation.matcher = InvitationCommand.validSentInvitation
		new SendInvitationCommand(invitation, wbTools).buildCommand
	}
	
	def buildMeetingResponseCommand(userKey: UserKey, attendeeStatus: AttendeeStatus) = {
		val meetingResponse = new MeetingResponseContext(userKey, attendeeStatus, MeetingResponseCommand.validResponses)
		new MeetingResponseCommand(meetingResponse, wbTools).buildCommand
	}
	
}
