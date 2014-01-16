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
package org.obm.push.context

import java.math.BigDecimal
import org.obm.push.bean.Device
import org.obm.push.helper.SessionHelper
import com.excilys.ebi.gatling.core.session.Session
import org.apache.james.mime4j.dom.address.Mailbox
import org.obm.push.ProtocolVersion

class User(userNumber: Int, configuration: Configuration) {
	
	val domain = configuration.defaultUserDomain
	val login = "%s%d".format(configuration.defaultUserLoginPrefix, userNumber)
	val password = configuration.defaultUserPassword
	val email = {"%s@%s".format(login, domain)}
	val policyKey = configuration.defaultUserPolicyKey
	val deviceId = configuration.defaultUserDeviceId
	val deviceType = configuration.defaultUserDeviceType
	
	val userProtocol = "%s\\%s".format(domain, login)
	lazy val mailbox = new Mailbox(login, domain)
	lazy val device = new Device.Factory().create(
			null, 
			deviceType,
			"Mozilla/5.0 (X11; Linux x86_64; rv:10.0.7) Gecko/20100101 Firefox/10.0.7 Iceweasel/10.0.7",
			deviceId,
			ActiveSyncConfiguration.activeSyncVersion)
}

class UserKey (val key: String) {
	
	val elUserPolicyKey = key + ":PolicyKey"
	
	def getUser(session: Session) = session.getTypedAttribute[User](key)
	
	lazy val sessionHelper = new SessionHelper(this) 
	lazy val lastFolderSyncSessionKey = buildSessionKey(UserSessionKeys.LAST_FOLDER_SYNC)
	lazy val lastSyncSessionKey = buildSessionKey(UserSessionKeys.LAST_SYNC)
	lazy val lastMeetingResponseSessionKey = buildSessionKey(UserSessionKeys.MEETING_RESPONSE)
	lazy val lastInvitationClientIdSessionKey = buildSessionKey(UserSessionKeys.INVITATION_CLIENT_ID)
	lazy val lastPendingInvitationSessionKey = buildSessionKey(UserSessionKeys.PENDING_INVITATION)
	
	private[this] def buildSessionKey(sessionKey: UserSessionKeys.Keys) = "%s:%s".format(sessionKey, key)
}

object UserSessionKeys extends Enumeration {
	type Keys = Value
	
	val LAST_FOLDER_SYNC = Value("lastFolderSync")
	val LAST_SYNC = Value("lastSync")
	val MEETING_RESPONSE = Value("meetingResponse")
	val INVITATION_CLIENT_ID = Value("invitationClientId")
	val PENDING_INVITATION = Value("pendingInvitation")
	
}