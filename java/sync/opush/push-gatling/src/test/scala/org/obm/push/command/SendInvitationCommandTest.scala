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

import org.junit.runner.RunWith
import org.obm.push.bean.AttendeeStatus
import org.obm.push.bean.AttendeeType
import org.obm.push.bean.MSAttendee
import org.obm.push.context.Configuration
import org.obm.push.context.User
import org.obm.push.wbxml.WBXMLTools
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import org.obm.push.context.UserKey
import org.obm.push.bean.DeviceId

@RunWith(classOf[JUnitRunner])
class SendInvitationCommandTest extends FunSuite with BeforeAndAfter {

	val config = new Configuration {
		  val targetServerUrl = "192.168.0.1"
		  val defaultUserDomain = "domain.org"
		  val defaultUserLoginPrefix = "login"
		  val defaultUserPassword = "pass"
		  val defaultUserDeviceId = new DeviceId("deviceId")
		  val defaultUserDeviceType = "deviceType"
		  val defaultUserPolicyKey = "1234567890"
	}
	val user = new User(1, config)
	val userKey = new UserKey("user")
	
	var wbxmlTools: WBXMLTools =_
	
	before {
		wbxmlTools = new WBXMLTools()
	}
	
	test("Context make integer clientId as string") {
		val clientId = InvitationContext.generateClientId
		assert(!Strings.isNullOrEmpty(clientId))
		assert(clientId.toInt > 0)
	}
	
	test("SendInvitation command name is Sync") {
		val invitation = new InvitationContext(userKey)
		val command = new SendInvitationCommand(invitation, wbxmlTools)
		assert(command.commandName === "Sync")
	}
	
	test("SendInvitation contains organizer") {
		val invitation = new InvitationContext(organizer = userKey)
		val event = new SendInvitationCommand(invitation, wbxmlTools)
				.buildEventInvitation(
					organizer = user,
					attendees = Set())
							
		assert(event.getOrganizerEmail() == email("login1"))
	}
	
	test("SendInvitation contains attendees") {
		val invitation = new InvitationContext(
				organizer = userKey,
				attendees = Set(new UserKey("user2"), new UserKey("user3")))
		
		val event = new SendInvitationCommand(invitation, wbxmlTools)
				.buildEventInvitation(
						organizer = user,
						attendees = Set(new User(2, config), new User(3, config)))
						
		assert(event.getAttendeeEmails().containsAll(ImmutableList.of(email("login2"), email("login3"))))
		assert(event.getAttendees().containsAll(ImmutableList.of(
				MSAttendee.builder().withEmail(email("login2"))
									.withType(AttendeeType.REQUIRED)
									.withStatus(AttendeeStatus.NOT_RESPONDED).build(),
				MSAttendee.builder().withEmail(email("login3"))
									.withType(AttendeeType.REQUIRED)
									.withStatus(AttendeeStatus.NOT_RESPONDED).build())))
	}
	
	def email(login: String) = "%s@domain.org".format(login)
}