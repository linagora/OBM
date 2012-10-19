package org.obm.push.encoder

import org.obm.push.protocol.data.TimeZoneEncoder
import org.obm.push.protocol.bean.ASTimeZone

object GatlingTimeZoneEncoder extends TimeZoneEncoder {

	override def encode(asTimeZone: ASTimeZone): Array[Byte] = {
		null
	}
}