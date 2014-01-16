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

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._

class SimplePingSimulation extends Simulation {

	def apply = {

		val opushServerTarget = "http://192.168.2.39"

		val httpConf = httpConfig.baseURL(opushServerTarget).disableFollowRedirect

		val headers_1 = Map(
			"Host" -> "192.168.2.67",
			"User-Agent" -> "Apple-iPhone3C1/902.206",
			"Accept" -> "*/*",
			"Content-Type" -> "application/vnd.ms-sync.wbxml",
			"MS-ASProtocolVersion" -> "12.1",
			"X-MS-PolicyKey" -> "1696466519",
			"Authorization" -> "Basic dGhpbGFpcmUubG5nLm9yZ1x6YWRtaW46emFkbWlu=",
			"Accept-Language" -> "fr-fr",
			"Accept-Encoding" -> "gzip,deflate",
			"Connection" -> "keep-alive")

		val scn = scenario("Simple ActiveSync Ping Scenario")
			.exec(http("request_1")
					.post("/Microsoft-Server-ActiveSync")
					.headers(headers_1)
					.queryParam("User", "thilaire.lng.org\\zadmin")
					.queryParam("DeviceId", "Appl5K14358AA4S")
					.queryParam("DeviceType", "iPhone")
					.queryParam("Cmd", "Ping")
					.byteArrayBody(byteArray)
			)

		List(scn.users(1).ramp(10).protocolConfig(httpConf))
	}

	val byteArray = (s: Session) => {
		Array[Byte](3, 1, 106, 0, 0, 13, 5)
//		Array[Byte](3, 1, 106, 0, 0, 13, 69, 72, 3, 52, 55, 48, 0, 1, 73, 74, 75, 3, 49, 0, 1, 76, 3, 67, 97, 108, 101, 110, 100, 97, 114, 0, 1, 1, 74, 75, 3, 50, 0, 1, 76, 3, 69, 109, 97, 105, 108, 0, 1, 1, 74, 75, 3, 54, 0, 1, 76, 3, 67, 111, 110, 116, 97, 99, 116, 115, 0, 1, 1, 1, 1)
	}
}
