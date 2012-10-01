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

import org.obm.push.command.SendEmailContext
import org.obm.push.command.SendEmailCommand
import org.obm.push.context.ContextConfiguration
import org.obm.push.context.GatlingContextConfiguration
import org.obm.push.context.http.ActiveSyncHttpContext
import org.obm.push.context.http.HttpContext
import org.obm.push.wbxml.WBXMLTools
import com.excilys.ebi.gatling.core.Predef.Simulation
import com.excilys.ebi.gatling.core.Predef.scenario
import com.excilys.ebi.gatling.http.Predef.httpConfig
import com.excilys.ebi.gatling.http.Predef.toHttpProtocolConfiguration
import com.excilys.ebi.gatling.http.request.builder.AbstractHttpRequestBuilder.toActionBuilder
import org.apache.james.mime4j.dom.address.Mailbox

import com.excilys.ebi.gatling.core.Predef.Simulation
import com.excilys.ebi.gatling.core.Predef.scenario
import com.excilys.ebi.gatling.http.Predef.httpConfig

class SendEmailToHimselfSimulation extends Simulation {

	val wbTools: WBXMLTools = new WBXMLTools
  
	val contextConfiguration: ContextConfiguration = GatlingContextConfiguration.build
	val httpContext: HttpContext = new ActiveSyncHttpContext(contextConfiguration)

	def apply = {
		val toMailbox = new Mailbox(contextConfiguration.userLogin, contextConfiguration.userDomain)
		val sendEmailContext = new SendEmailContext(to = toMailbox)
		val folderSyncScenario = scenario("Send a simple email to himself")
			.exec(new SendEmailCommand(httpContext, sendEmailContext).buildCommand)
					
		
		val httpConf = httpConfig
			.baseURL(contextConfiguration.targetServerUrl)
			.disableFollowRedirect
			.disableCaching
		List(folderSyncScenario.configure.users(1).ramp(10).protocolConfig(httpConf))
		
	}
	
}
