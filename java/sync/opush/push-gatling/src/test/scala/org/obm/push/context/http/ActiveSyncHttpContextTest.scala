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
package org.obm.push.context.http

import org.junit.runner.RunWith
import org.obm.push.context.Configuration
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ActiveSyncHttpContextTest extends FunSuite {
	   
	test("ActiveSyncHttpContext returns expected Authorization header") {

		val context = new Configuration {
			  val targetServerUrl = "192.168.0.1"
			  val userDomain = "domain.org"
			  val userLogin = "login"
			  val userPassword = "pass"
			  val userDeviceId = "deviceId"
			  val userDeviceType = "deviceType"
			  val userPolicyKey = "1234567890"
		}
		val headerAuthorization = new ActiveSyncHttpContext(context).headerAuthorization
		assert(headerAuthorization.name === "Authorization")
		assert(headerAuthorization.value === "Basic ZG9tYWluLm9yZ1xsb2dpbjpwYXNz")
	}
	   
	test("ActiveSyncHttpContext returns expected WBXML Content-Type header") {

		val context = new Configuration {
			  val targetServerUrl = "192.168.0.1"
			  val userDomain = "domain.org"
			  val userLogin = "login"
			  val userPassword = "pass"
			  val userDeviceId = "deviceId"
			  val userDeviceType = "deviceType"
			  val userPolicyKey = "1234567890"
		}
		val headerAuthorization = new ActiveSyncHttpContext(context).headerContentTypeWbXml
		assert(headerAuthorization.name === "Content-Type")
		assert(headerAuthorization.value === "application/vnd.ms-sync.wbxml")
	}
	   
	test("ActiveSyncHttpContext returns ActiveSync version 12.1 header") {

		val context = new Configuration {
			  val targetServerUrl = "192.168.0.1"
			  val userDomain = "domain.org"
			  val userLogin = "login"
			  val userPassword = "pass"
			  val userDeviceId = "deviceId"
			  val userDeviceType = "deviceType"
			  val userPolicyKey = "1234567890"
		}
		val headerAuthorization = new ActiveSyncHttpContext(context).headerActiveSyncVersion
		assert(headerAuthorization.name === "MS-ASProtocolVersion")
		assert(headerAuthorization.value === "12.1")
	}
	   
	test("ActiveSyncHttpContext returns given ActiveSync policy key header") {

		val context = new Configuration {
			  val targetServerUrl = "192.168.0.1"
			  val userDomain = "domain.org"
			  val userLogin = "login"
			  val userPassword = "pass"
			  val userDeviceId = "deviceId"
			  val userDeviceType = "deviceType"
			  val userPolicyKey = "1234567890"
		}
		val headerAuthorization = new ActiveSyncHttpContext(context).headerActiveSyncPolicyKey
		assert(headerAuthorization.name === "X-MS-PolicyKey")
		assert(headerAuthorization.value === "1234567890")
	}
}
