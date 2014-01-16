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

import scala.collection.mutable.MutableList
import org.apache.james.mime4j.dom.address.Mailbox
import org.obm.push.command.SendEmailCommand
import org.obm.push.command.SendEmailContext
import org.obm.push.context.Configuration
import org.obm.push.context.GatlingConfiguration
import org.obm.push.context.User
import org.obm.push.context.UserKey
import org.obm.push.wbxml.WBXMLTools
import com.excilys.ebi.gatling.core.Predef.Simulation
import com.excilys.ebi.gatling.core.Predef.scenario
import com.excilys.ebi.gatling.core.feeder.FeederBuiltIns
import com.excilys.ebi.gatling.core.scenario.configuration.ConfiguredScenarioBuilder
import org.obm.push.context.feeder.UserFeeder
import com.excilys.ebi.gatling.http.Predef._

class SendEmailFromManyUsersSimulation extends Simulation {

	val wbTools: WBXMLTools = new WBXMLTools
  
	val configuration: Configuration = GatlingConfiguration.build

	def apply = {
		
		val httpConf = httpConfig
			.baseURL(configuration.targetServerUrl)
			.disableFollowRedirect
			.disableCaching
		
		var scenarios = MutableList[ConfiguredScenarioBuilder]()
		for (userNumber <- Iterator.range(1, 100)) {
			val userSendEmailScenario = buildScenarioForUser(userNumber)
			scenarios += userSendEmailScenario.users(1).protocolConfig(httpConf)
		}
		
		scenarios
	}

	def buildScenarioForUser(userNumber: Int) = {
		val from = new User(1, configuration)
		val to = new User(2, configuration).mailbox
		val cc = new User(3, configuration).mailbox
		val bcc = new User(4, configuration).mailbox

		val fromKey = new UserKey("fromUser")
		val feeder = new UserFeeder(Seq(from).iterator, fromKey)
		
		val sendEmailContext = new SendEmailContext(fromKey, from.mailbox, to, cc, bcc)
		scenario("Send a simple email from:{%s} to:{%s} cc:{%s} bcc:{%s}".format(from.mailbox, to, cc, bcc))
			.exec(s => s.setAttributes(feeder.next))
			.exec(new SendEmailCommand(sendEmailContext).buildCommand)
	}
}
