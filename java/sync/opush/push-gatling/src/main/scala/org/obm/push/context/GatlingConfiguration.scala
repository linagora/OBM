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

import com.google.common.base.Strings
import org.obm.push.bean.DeviceId

object Arguments extends Enumeration {
  
	val TARGET_SERVER_URL_ARG = Value("targetServerUrl")
	val PARALLELS_SCENARIOS_COUNT = Value("parallelsScenariosCount")
	val ASYNCHRONOUS_CHANGE_TIME = Value("asynchronousChangeTime")
	  
	val USER_DOMAIN = Value("userDomain")
	val USER_LOGIN_PREFIX = Value("userLoginPrefix")
	val USER_PASSWORD = Value("userPassword")
	val USER_POLICY_KEY = Value("userPolicyKey")
	val USER_DEVICE_ID = Value("userDeviceId")
	val USER_DEVICE_TYPE = Value("userDeviceType")
}

object GatlingConfiguration {

	def build(): Configuration = build(
			System.getProperty(Arguments.TARGET_SERVER_URL_ARG.toString),
			System.getProperty(Arguments.PARALLELS_SCENARIOS_COUNT.toString),
			System.getProperty(Arguments.ASYNCHRONOUS_CHANGE_TIME.toString),
			System.getProperty(Arguments.USER_DOMAIN.toString),
			System.getProperty(Arguments.USER_LOGIN_PREFIX.toString),
			System.getProperty(Arguments.USER_PASSWORD.toString),
			System.getProperty(Arguments.USER_POLICY_KEY.toString),
			System.getProperty(Arguments.USER_DEVICE_ID.toString),
			System.getProperty(Arguments.USER_DEVICE_TYPE.toString))
	
	def build(
			serverUrl: String, parallelsScenarios: String, asynchronousChange: String,
			domain: String, loginPrefix: String,
			password: String, policyKey: String, deviceId: String, deviceType: String) = {
	  
			require(!Strings.isNullOrEmpty(serverUrl))
			require(!Strings.isNullOrEmpty(parallelsScenarios))
			require(!Strings.isNullOrEmpty(asynchronousChange))
			require(!Strings.isNullOrEmpty(domain))
			require(!Strings.isNullOrEmpty(loginPrefix))
			require(!Strings.isNullOrEmpty(password))
			require(!Strings.isNullOrEmpty(deviceId))
			require(!Strings.isNullOrEmpty(deviceType))
			require(!Strings.isNullOrEmpty(policyKey))
			
			new Configuration() {
			
				override val targetServerUrl = serverUrl
				override val parallelsScenariosCount = parallelsScenarios.toInt
				override val asynchronousChangeTime = asynchronousChange.toInt
				 
				override val defaultUserDomain = domain
				override val defaultUserLoginPrefix = loginPrefix
				override val defaultUserPassword = password
				override val defaultUserPolicyKey = policyKey
				override val defaultUserDeviceId = new DeviceId(deviceId)
				override val defaultUserDeviceType = deviceType
				
		  	}
	}
}