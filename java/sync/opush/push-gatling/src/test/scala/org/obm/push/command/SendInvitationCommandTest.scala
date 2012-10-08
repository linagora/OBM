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

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.obm.push.context.Configuration
import org.obm.push.context.http.HttpContext
import org.obm.push.context.http.ActiveSyncHttpContext
import org.apache.james.mime4j.dom.address.Mailbox
import org.obm.push.context.Configuration
import org.obm.push.context.http.HttpContext
import org.obm.push.context.http.ActiveSyncHttpContext
import org.obm.push.wbxml.WBXMLTools
import com.google.common.collect.ImmutableList
import org.obm.push.bean.MSAttendee
import org.obm.push.bean.AttendeeType
import org.obm.push.bean.AttendeeStatus
import com.google.common.base.Strings

@RunWith(classOf[JUnitRunner])
class SendInvitationCommandTest extends FunSuite with BeforeAndAfter {

	val context = new ActiveSyncHttpContext(new Configuration {
		  val targetServerUrl = "192.168.0.1"
		  val userDomain = "domain.org"
		  val userLogin = "login"
		  val userPassword = "pass"
		  val userDeviceId = "deviceId"
		  val userDeviceType = "deviceType"
		  val userPolicyKey = "1234567890"
	})
	
	var wbxmlTools: WBXMLTools =_
	
	before {
		wbxmlTools = new WBXMLTools()
	}
	
	test("Context make integer clientId as string") {
		val invitation = new InvitationContext(email("user"))
		assert(!Strings.isNullOrEmpty(invitation.clientId))
		assert(invitation.clientId.toInt > 0)
	}
	
	test("SendInvitation command name is Sync") {
		val invitation = new InvitationContext(email("user"))
		val command = new SendInvitationCommand(context, invitation, wbxmlTools)
		assert(command.commandName === "Sync")
	}
	
	test("SendInvitation contains organizer") {
		val invitation = new InvitationContext(email("user"))
		val event = new SendInvitationCommand(context, invitation, wbxmlTools).buildEventInvitation
		assert(event.getOrganizerEmail() == email("user"))
	}
	
	test("SendInvitation contains attendees") {
		val invitation = new InvitationContext(email("user"), attendeesEmails = Set(email("inv1"), email("inv2")))
		val event = new SendInvitationCommand(context, invitation, wbxmlTools).buildEventInvitation
		assert(event.getAttendeeEmails().containsAll(ImmutableList.of(email("inv1"), email("inv2"))))
		assert(event.getAttendees().containsAll(ImmutableList.of(
				MSAttendee.builder().withEmail(email("inv1"))
									.withType(AttendeeType.REQUIRED)
									.withStatus(AttendeeStatus.NOT_RESPONDED).build(),
				MSAttendee.builder().withEmail(email("inv2"))
									.withType(AttendeeType.REQUIRED)
									.withStatus(AttendeeStatus.NOT_RESPONDED).build())))
	}
	
	def email(login: String) = "%s@domain.org".format(login)
}