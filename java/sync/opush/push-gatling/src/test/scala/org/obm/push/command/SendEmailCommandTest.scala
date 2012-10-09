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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.obm.push.context.Configuration
import org.obm.push.context.http.HttpContext
import org.obm.push.context.http.ActiveSyncHttpContext
import org.apache.james.mime4j.dom.address.Mailbox
import org.obm.push.context.Configuration
import org.obm.push.context.http.HttpContext
import org.obm.push.context.http.ActiveSyncHttpContext

@RunWith(classOf[JUnitRunner])
class SendEmailCommandTest extends FunSuite {

	val context = new ActiveSyncHttpContext(new Configuration {
		  val targetServerUrl = "192.168.0.1"
		  val userDomain = "domain.org"
		  val userLogin = "login"
		  val userPassword = "pass"
		  val userDeviceId = "deviceId"
		  val userDeviceType = "deviceType"
		  val userPolicyKey = "1234567890"
	})
	
	test("SendEmail command name is SendMail") {
		val sendEmailContext = new SendEmailContext(mailbox("to")) 
		val command = new SendEmailCommand(context, sendEmailContext)
		assert(command.commandName === "SendMail")
	}

	test("SendEmail save in sent is T when true") {
		val sendEmailContext = new SendEmailContext(mailbox("to"), saveInSent = true) 
		assert(new SendEmailCommand(context, sendEmailContext).saveInSent === "T")
	}
	
	test("SendEmail save in sent is F when false") {
		val sendEmailContext = new SendEmailContext(mailbox("to"), saveInSent = false) 
		assert(new SendEmailCommand(context, sendEmailContext).saveInSent === "F")
	}
	
	test("SendEmail message contains recipients") {
		val sendEmailContext = new SendEmailContext(
				mailbox("from"), mailbox("to"), mailbox("cc"), mailbox("bcc"))
		val message = new SendEmailCommand(context, sendEmailContext).buildMail
		assert(message.getTo().contains(mailbox("to")))
		assert(message.getFrom().contains(mailbox("from")))
		assert(message.getCc().contains(mailbox("cc")))
		assert(message.getBcc().contains(mailbox("bcc")))
	}
	
	def mailbox(login: String) = new Mailbox(login, "domain.org")
}