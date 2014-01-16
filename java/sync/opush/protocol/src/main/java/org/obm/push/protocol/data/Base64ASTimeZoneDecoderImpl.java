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

import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.mutable.MutableInt;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.IntEncoder.Capacity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Base64ASTimeZoneDecoderImpl implements Base64ASTimeZoneDecoder {

	private static final int SPEC_BIAS_LENGHT = 4;
	private static final int SPEC_NAMES_LENGHT = 64;
	private static final int SPEC_DATE_LENGHT = 16;

	private final WCHAREncoder wcharEncoder;
	private final IntEncoder intEncoder;
	private final SystemTimeEncoder systemTimeEncoder;
	
	@Inject
	@VisibleForTesting Base64ASTimeZoneDecoderImpl(
			WCHAREncoder wcharEncoder, IntEncoder intEncoder, SystemTimeEncoder systemTimeEncoder) {
		this.wcharEncoder = wcharEncoder;
		this.intEncoder = intEncoder;
		this.systemTimeEncoder = systemTimeEncoder;
	}
	
	@Override
	public ASTimeZone decode(byte[] base64TimeZone) {
		Preconditions.checkNotNull(base64TimeZone, "base64TimeZone can not be null.");
		
		return toASTimeZone(Base64.decodeBase64(base64TimeZone));
	}
	
	@VisibleForTesting ASTimeZone toASTimeZone(byte[] byteASTimeZone) {
		MutableInt rangeStartingIndex = new MutableInt(0);
		
		int bias = byteArrayToInt(byteASTimeZone, rangeStartingIndex);
		String standardName = byteArrayToString(byteASTimeZone, rangeStartingIndex);
		ASSystemTime standardDate = byteArrayToASSystemTime(byteASTimeZone, rangeStartingIndex);
		int standardBias = byteArrayToInt(byteASTimeZone, rangeStartingIndex);
		String dayLightName = byteArrayToString(byteASTimeZone, rangeStartingIndex);
		ASSystemTime dayLightDate = byteArrayToASSystemTime(byteASTimeZone, rangeStartingIndex);
		int dayLightBias = byteArrayToInt(byteASTimeZone, rangeStartingIndex);
	
		return ASTimeZone.builder()
			.bias(bias)
			.dayLightBias(dayLightBias)
			.dayLightDate(dayLightDate)
			.dayLightName(dayLightName)
			.standardBias(standardBias)
			.standardDate(standardDate)
			.standardName(standardName).build();
	}

	private String byteArrayToString(byte[] byteASTimeZone, MutableInt rangeStartingIndex) {
		byte[] bytes = extractBytesField(byteASTimeZone, rangeStartingIndex, SPEC_NAMES_LENGHT);
		return wcharEncoder.byteArrayToEncode(bytes);
	}
	
	private int byteArrayToInt(byte[] byteASTimeZone, MutableInt rangeStartingIndex) {
		byte[] bytes = extractBytesField(byteASTimeZone, rangeStartingIndex, SPEC_BIAS_LENGHT);
		return intEncoder.capacity(Capacity.FOUR).toInt(bytes);
	}
	
	private ASSystemTime byteArrayToASSystemTime(byte[] byteASTimeZone, MutableInt rangeStartingIndex) {
		byte[] bytes = extractBytesField(byteASTimeZone, rangeStartingIndex, SPEC_DATE_LENGHT);
		return systemTimeEncoder.toASSystemTime(bytes);
	}

	private byte[] extractBytesField(byte[] exchangeTzBinary, MutableInt rangeIndex, int bytesCount) {
		int fromIndex = rangeIndex.intValue();
		rangeIndex.add(bytesCount);
		
		ByteArrayInputStream byteArrayInputStream = 
				new ByteArrayInputStream(exchangeTzBinary, fromIndex, rangeIndex.intValue());
		return readBytes(byteArrayInputStream, rangeIndex.intValue());
	}

	private byte[] readBytes(ByteArrayInputStream byteArrayInputStream, int length) {
		byte[] bytes = new byte[length];
		for (int i=0; i<length; i++) {
			bytes[i] = (byte) byteArrayInputStream.read();
		}
		return bytes;
	}
}
