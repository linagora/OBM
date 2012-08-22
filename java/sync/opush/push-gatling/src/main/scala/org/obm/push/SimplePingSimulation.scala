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
			.exec(
				http("request_1")
					.post("/Microsoft-Server-ActiveSync")
					.headers(headers_1)
					.queryParam("User", "thilaire.lng.org\\zadmin")
					.queryParam("DeviceId", "Appl5K14358AA4S")
					.queryParam("DeviceType", "iPhone")
					.queryParam("Cmd", "Ping")
					.byteArrayBody(byteArray)
			)

		List(scn.configure.users(1).ramp(10).protocolConfig(httpConf))
	}

	val byteArray = () => {
		Array[Byte](3, 1, 106, 0, 0, 13, 5)
//		Array[Byte](3, 1, 106, 0, 0, 13, 69, 72, 3, 52, 55, 48, 0, 1, 73, 74, 75, 3, 49, 0, 1, 76, 3, 67, 97, 108, 101, 110, 100, 97, 114, 0, 1, 1, 74, 75, 3, 50, 0, 1, 76, 3, 69, 109, 97, 105, 108, 0, 1, 1, 74, 75, 3, 54, 0, 1, 76, 3, 67, 111, 110, 116, 97, 99, 116, 115, 0, 1, 1, 1, 1)
	}
}
