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
import org.obm.push.context.UserKey
import org.obm.push.protocol.bean.FolderSyncResponse
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.EasyMockSugar

import com.excilys.ebi.gatling.core.session.Session
import org.obm.push.protocol.bean.FolderSyncResponse
import org.obm.push.bean.SyncKey

@RunWith(classOf[JUnitRunner])
class FolderSyncContextTest extends FunSuite with EasyMockSugar {
	
	val userKey = new UserKey("user")
	
	test("Initial FoldeSync context returns 0 for next synckey") {
		val context = new InitialFolderSyncContext(userKey)
		assert(context.nextSyncKey(null) === new SyncKey("0"))
	}
	
	test("nextSyncKey throw exception if no response in session") {
		val session = strictMock(manifest[Session])
		expecting {
			call(session.getAttributeAsOption("lastFolderSync:user")).andReturn(Option.empty)
		}
		
		whenExecuting(session) {
			intercept[IllegalStateException] {
				new FolderSyncContext(userKey).nextSyncKey(session)
			}
		}
	}
	
	test("nextSyncKey reads in response") {
		val lastResponse = strictMock(manifest[FolderSyncResponse])
		val session = strictMock(manifest[Session])
		expecting {
			call(lastResponse.getNewSyncKey()).andReturn(new SyncKey("1234-5678"))
			call(session.getAttributeAsOption[FolderSyncResponse]("lastFolderSync:user"))
				.andReturn(Option.apply(lastResponse))
		}
		
		whenExecuting(lastResponse, session) {
			assert(new FolderSyncContext(userKey).nextSyncKey(session) === new SyncKey("1234-5678"))
		}
	}
}