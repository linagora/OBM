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
package org.obm.push.protocol.data;

import java.util.Arrays;

import org.apache.commons.lang.mutable.MutableInt;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.utils.type.UnsignedShort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SystemTimeEncoder {

	private static final int SHORT_SIZE = 2;

	@Inject
	@VisibleForTesting SystemTimeEncoder() {}
	
	public byte[] toByteArray(ASSystemTime toEncode) {

		byte[] year = toEncode.getYear().toByteArray();
		byte[] month = toEncode.getMonth().toByteArray();
		byte[] dayOfWeek = toEncode.getDayOfWeek().toByteArray();
		byte[] day = toEncode.getWeekOfMonth().toByteArray();
		byte[] hour = toEncode.getHour().toByteArray();
		byte[] minute = toEncode.getMinute().toByteArray();
		byte[] second = toEncode.getSecond().toByteArray();
		byte[] millis = toEncode.getMilliseconds().toByteArray();
		
		return Bytes.concat(year, month, dayOfWeek, day, hour, minute, second, millis);
	}
	
	public ASSystemTime toASSystemTime(byte[] toEncode) {
		MutableInt index = new MutableInt(0);
		return ASSystemTime.builder()
				.year(readBytes(index, toEncode))
				.month(readBytes(index, toEncode))
				.dayOfWeek(readBytes(index, toEncode))
				.weekOfMonth(readBytes(index, toEncode))
				.hour(readBytes(index, toEncode))
				.minute(readBytes(index, toEncode))
				.second(readBytes(index, toEncode))
				.milliseconds(readBytes(index, toEncode)).build();
	}

	private UnsignedShort readBytes(MutableInt index, byte[] toEncode) {
		int fromIndex = index.intValue();
		index.add(SHORT_SIZE);
		return new UnsignedShort(
				Arrays.copyOfRange(toEncode, fromIndex, index.intValue()));
	}
}
