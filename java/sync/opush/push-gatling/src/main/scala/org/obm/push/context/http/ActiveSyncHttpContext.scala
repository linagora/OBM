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

import org.apache.commons.codec.binary.Base64
import org.obm.push.context.ContextConfiguration

import com.google.common.base.Charsets
import com.google.common.base.Strings

class ActiveSyncHttpContext(context: ContextConfiguration) extends HttpContext {
  
	val headerContentTypeWbXml = new HttpHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-sync.wbxml")
	val headerActiveSyncVersion = new HttpHeader(HttpHeaders.AS_VERSION, "12.1")
	val headerActiveSyncPolicyKey = new HttpHeader(HttpHeaders.AS_POLICY_KEY, context.userPolicyKey)
	val headerAuthorization = {
		val authValue = "%s\\%s:%s".format(context.userDomain, context.userLogin, context.userPassword)
		val authBase64Bytes = Base64.encodeBase64(authValue.getBytes(Charsets.UTF_8))
		val headerValue = "Basic %s".format(new String(authBase64Bytes, Charsets.UTF_8))
		new HttpHeader(HttpHeaders.AUTHORIZATION, headerValue) 
	}
	
	
	val paramDeviceId = new HttpQueryParam(HttpQueryParams.DEVICE_ID, context.userDeviceId)
	val paramDeviceType = new HttpQueryParam(HttpQueryParams.DEVICE_TYPE, context.userDeviceType)
	val paramUser = new HttpQueryParam(HttpQueryParams.USER, context.userDomain + "\\" + context.userLogin)
	
	
	val postUrl = "/Microsoft-Server-ActiveSync"
}