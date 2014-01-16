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

import scala.collection.JavaConversions._
import org.obm.push.bean.MeetingResponse
import org.obm.push.checks.{WholeBodyExtractorCheckBuilder => bodyExtractor}
import org.obm.push.encoder.GatlingEncoders.meetingProtocol
import org.obm.push.protocol.bean.MeetingHandlerRequest
import org.obm.push.protocol.bean.MeetingHandlerResponse
import org.obm.push.wbxml.WBXMLTools
import com.excilys.ebi.gatling.core.check._
import com.excilys.ebi.gatling.core.Predef.Session
import com.excilys.ebi.gatling.core.Predef.checkBuilderToCheck
import com.excilys.ebi.gatling.core.Predef.matcherCheckBuilderToCheckBuilder
import org.obm.push.bean.MeetingResponseStatus
import org.obm.push.checks.Check
import com.google.common.base.Strings

class MeetingResponseCommand(response: MeetingResponseContext, wbTools: WBXMLTools)
	extends AbstractActiveSyncCommand(response.userKey) {

	val namespace = "MeetingResponse"
	
	override val commandTitle = "MeetingResponse command"
	override val commandName = "MeetingResponse"
	  
	override def buildCommand() = {
		super.buildCommand()
			.byteArrayBody((session: Session) => buildMeetingResponse(session))
			.check(bodyExtractor
			    .find
			    .transform((response: Array[Byte]) => toMeetingResponseReply(response))
			    .matchWith(response.matcher)
			    .saveAs(response.userKey.lastMeetingResponseSessionKey))
	}

	def buildMeetingResponse(session: Session): Array[Byte] = {
		val request = MeetingHandlerRequest.builder()
				.meetingResponses(buildResponseForPendingRequest(session))
				.build()
		val requestDoc = meetingProtocol.encodeRequest(request)
		wbTools.toWbxml(namespace, requestDoc)
	}
	
	def buildResponseForPendingRequest(session: Session) = {
		for (serverId <- response.findServerIds(session))
			yield MeetingResponse.builder()
					.reqId(serverId)
					.collectionId(response.collectionIdFromServerId(serverId))
					.userResponse(response.attendeeStatus)
					.build()
	}
	
	def toMeetingResponseReply(response: Array[Byte]): MeetingHandlerResponse = {
		val responseDoc = wbTools.toXml(response)
		meetingProtocol.decodeResponse(responseDoc)
	}
}

object MeetingResponseCommand {
	
	val statusOk = new MatchStrategy[MeetingHandlerResponse] {
		def apply(value: Option[MeetingHandlerResponse], session: Session) = {
			val everyOkStatus = value.get
					.getItemChanges()
					.find(_.getStatus() != MeetingResponseStatus.SUCCESS)
					.isEmpty
			if (everyOkStatus) Success(value)
			else Failure("Status isn't ok for a meeting response")
		}
	}
	
	val atLeastOneResponse = new MatchStrategy[MeetingHandlerResponse] {
		def apply(value: Option[MeetingHandlerResponse], session: Session) = {
			if (!value.get.getItemChanges().isEmpty) Success(value) 
			else Failure("No meeting response in reply")
		}
	}
	
	val validResponses = Check.manyToOne[MeetingHandlerResponse](Seq(statusOk, atLeastOneResponse))
}
