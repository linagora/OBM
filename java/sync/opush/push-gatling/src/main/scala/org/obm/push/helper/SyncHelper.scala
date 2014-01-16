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
package org.obm.push.helper

import org.obm.push.protocol.bean.SyncResponse
import scala.collection.JavaConversions._
import org.obm.push.bean.PIMDataType._
import org.obm.push.bean.ms.MSEmail
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest
import scala.collection.mutable.MutableList
import org.obm.push.bean.MSEvent
import org.obm.push.bean.SyncCollectionCommand

object SyncHelper {
	
	def findChangesWithMeetingRequest(syncResponse: SyncResponse) = {
		findChangesWithEmailData(syncResponse)
			.filter(_.getApplicationData().asInstanceOf[MSEmail].getMeetingRequest() != null)
	}
	
	def findChangesWithEmailData(syncResponse: SyncResponse) = {
		findChanges(syncResponse).filter(changeHasEmailData(_))
	}
	
	def findEventChanges(syncResponse: SyncResponse, serverId: String) = {
		findChangesWithServerId(syncResponse, serverId)
			.filter(changeHasCalendarData(_))
			.map(_.getApplicationData().asInstanceOf[MSEvent])
	}
	
	def findChangesWithServerId(syncResponse: SyncResponse, serverId: String) = {
		findChanges(syncResponse).filter(_.getServerId().equals(serverId))
	}
	
	def findChanges(syncResponse: SyncResponse) = {
		syncResponse.getCollectionResponses()
			.flatMap(_.getResponses().getCommands())
	}
	
	def changeHasCalendarData(change: SyncCollectionCommand.Response) = 
		change.getType() == CALENDAR && change.getApplicationData() != null
	
	def changeHasEmailData(change: SyncCollectionCommand.Response) = 
		change.getType() == EMAIL && change.getApplicationData() != null
}