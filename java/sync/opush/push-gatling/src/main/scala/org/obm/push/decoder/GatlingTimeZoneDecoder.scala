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
package org.obm.push.decoder

import org.obm.push.protocol.data.Base64ASTimeZoneDecoder
import org.obm.push.protocol.bean.ASTimeZone
import org.obm.push.protocol.bean.ASSystemTime
import org.obm.push.utils.`type`.UnsignedShort

object GatlingTimeZoneDecoder extends Base64ASTimeZoneDecoder {
	
	override def decode(base64TimeZone: Array[Byte]): ASTimeZone  = {
		buildParisTimeZone()
	};
	
	def buildParisTimeZone(): ASTimeZone = {
		ASTimeZone.builder()
			.bias(1)
			.standardName("Central European Summer Time")
			.standardDate(asSummerTime())
			.standardBias(3)
			.dayLightName("Central European Time")
			.dayLightDate(asWinterTime())
			.dayLightBias(2)
			.build()
	}
	
	private[this] def asSummerTime(): ASSystemTime = {
		ASSystemTime.builder()
			.year(UnsignedShort.checkedCast(2012))
			.month(UnsignedShort.checkedCast(10))
			.dayOfWeek(UnsignedShort.checkedCast(0))
			.weekOfMonth(UnsignedShort.checkedCast(5))
			.hour(UnsignedShort.checkedCast(3))
			.minute(UnsignedShort.checkedCast(0))
			.second(UnsignedShort.checkedCast(0))
			.milliseconds(UnsignedShort.checkedCast(0))
			.build();
	}
	
	private[this] def asWinterTime(): ASSystemTime = {
		ASSystemTime.builder()
			.year(UnsignedShort.checkedCast(2012))
			.month(UnsignedShort.checkedCast(03))
			.dayOfWeek(UnsignedShort.checkedCast(0))
			.weekOfMonth(UnsignedShort.checkedCast(5))
			.hour(UnsignedShort.checkedCast(2))
			.minute(UnsignedShort.checkedCast(0))
			.second(UnsignedShort.checkedCast(0))
			.milliseconds(UnsignedShort.checkedCast(0))
			.build();
	}
}