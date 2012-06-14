package org.obm.push.protocol.data;

import org.obm.push.bean.ms.ASTimeZone;

public interface Base64ASTimeZoneDecoder {

	ASTimeZone decode(byte[] base64TimeZone);
}
