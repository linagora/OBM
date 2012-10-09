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
package org.obm.push

import scala.collection.mutable.MutableList

import org.obm.push.bean.FolderType
import org.obm.push.command.FolderSyncCommand
import org.obm.push.command.InitialFolderSyncContext
import org.obm.push.command.InitialSyncContext
import org.obm.push.command.SendInvitationCommand
import org.obm.push.command.SendInvitationContext
import org.obm.push.command.SyncCollectionCommand
import org.obm.push.command.SyncContext
import org.obm.push.context.Configuration
import org.obm.push.context.GatlingConfiguration
import org.obm.push.context.UserConfiguration
import org.obm.push.context.http.ActiveSyncHttpContext
import org.obm.push.context.http.HttpContext
import org.obm.push.wbxml.WBXMLTools

import com.excilys.ebi.gatling.core.Predef.Simulation
import com.excilys.ebi.gatling.core.Predef.bootstrap.exec
import com.excilys.ebi.gatling.core.Predef.scenario
import com.excilys.ebi.gatling.core.scenario.configuration.ConfiguredScenarioBuilder
import com.excilys.ebi.gatling.http.Predef.httpConfig
import com.excilys.ebi.gatling.http.Predef.toHttpProtocolConfiguration
import com.excilys.ebi.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder
import com.excilys.ebi.gatling.http.request.builder.PostHttpRequestBuilder

class InviteTwoUsersSimulation extends Simulation {

	val wbTools: WBXMLTools = new WBXMLTools
	val configuration: Configuration = GatlingConfiguration.build

	val usedCalendarCollection = FolderType.DEFAULT_CALENDAR_FOLDER
	
	def apply = {
		
		val httpConf = httpConfig
			.baseURL(configuration.targetServerUrl)
			.disableFollowRedirect
			.disableCaching
		
		var scenarios = MutableList[ConfiguredScenarioBuilder]()
		val userNumber = 1
		for (userNumber <- Iterator.range(1, 100)) {
			val organizerScenario = buildScenarioForOrganizer(userNumber)
			scenarios += organizerScenario.configure.users(1).protocolConfig(httpConf)
		}
		
		scenarios
	}

	def buildScenarioForOrganizer(userNumber: Int) = {
		val userContext = userHttpContext(login(userNumber))
		val invitation = new SendInvitationContext(
				organizerEmail = email(userNumber),
				attendeesEmails = Set(email(userNumber+1), email(userNumber+2)),
				folderType = usedCalendarCollection)

		scenario("Send an invitation from:{%s} attendees:{%s}".format(invitation.organizerEmail, invitation.attendeesEmails))
			.exec(buildInitialFolderSyncCommand(userContext)).pause(2)
			.exec(buildInitialSyncCommand(userContext, usedCalendarCollection)).pause(2)
			.exec(buildSendInvitationCommand(userContext, invitation))
	}
	
	def buildInitialFolderSyncCommand(userContext: HttpContext) = {
		val initialFolderSyncContext = new InitialFolderSyncContext()
		new FolderSyncCommand(userContext, initialFolderSyncContext, wbTools).buildCommand
	}
	
	def buildInitialSyncCommand(userContext: HttpContext, folderType: FolderType) = {
		buildSyncCommand(userContext, new InitialSyncContext(folderType))
	}
	
	def buildSyncCommand(userContext: HttpContext, syncContext: SyncContext) = {
		new SyncCollectionCommand(userContext, syncContext, wbTools).buildCommand
	}
	
	def buildSendInvitationCommand(userContext: HttpContext, invitation: SendInvitationContext) = {
		new SendInvitationCommand(userContext, invitation, wbTools).buildCommand
	}

	def userHttpContext(userLogin: String) = {
		new ActiveSyncHttpContext(
			new UserConfiguration(configuration).cloneForUser(login = userLogin, pwd = "1234"))
	}
	
	def email(userNumber: Int) = "%s@%s".format(login(userNumber), configuration.userDomain)
	def login(userNumber: Int) = "u%d".format(userNumber)
}
