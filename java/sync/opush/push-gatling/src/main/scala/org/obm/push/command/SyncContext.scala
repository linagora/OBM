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

import scala.collection.JavaConversions.collectionAsScalaIterable
import org.obm.push.bean.FolderType
import org.obm.push.bean.SyncKey
import org.obm.push.helper.SessionHelper
import org.obm.push.context.UserKey
import org.obm.push.protocol.bean.SyncResponse
import com.excilys.ebi.gatling.core.session.Session
import org.obm.push.checks.Check
import com.excilys.ebi.gatling.core.check.MatchStrategy

class InitialSyncContext(
		userKey: UserKey,
		folderType: FolderType,
		matcher: MatchStrategy[SyncResponse] = Check.success)
			extends SyncContext(userKey, folderType) {
	
	val initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY
		
	override def nextSyncKey(session: => Session) = initialSyncKey
	
}

class SyncContext(
		val userKey: UserKey,
		val folderType: FolderType,
		var matcher: MatchStrategy[SyncResponse] = Check.success)
			extends CollectionContext(userKey) {
	
	def nextSyncKey(session: => Session): SyncKey = {
		val lastSync = userKey.sessionHelper.findLastSync(session)
		if (lastSync.isDefined) {
			return nextSyncKeyInResponse(session, lastSync.get)
		}
		throw new IllegalStateException("No last Sync in session")
	}
	
	private[this] def nextSyncKeyInResponse(session: Session, syncResponse: SyncResponse): SyncKey = {
		val collectionId = this.findCollectionId(session, folderType)
		for (collection <- syncResponse.getCollectionResponses()
			if collection.getCollectionId() == collectionId) {
				return collection.getSyncKey()
		}
		throw new NoSuchElementException(
				"Cannot find collection:{%d} in response:{%s}".format(collectionId, syncResponse))
	}
	
	def findCollectionId(session: Session) = super.findCollectionId(session, folderType)
}
