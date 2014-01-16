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

import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.IntEncoder.Capacity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;

public class TimeZoneEncoderImpl implements TimeZoneEncoder {

	private static final int SPEC_NAMES_LENGHT = 64;
	
	private final IntEncoder intEncoder;
	private final WCHAREncoder wcharEncoder;
	private final SystemTimeEncoder systemTimeEncoder;

	@Inject
	public TimeZoneEncoderImpl(
			IntEncoder intEncoder, WCHAREncoder wcharEncoder, SystemTimeEncoder systemTimeEncoder) {
		this.intEncoder = intEncoder;
		this.wcharEncoder = wcharEncoder;
		this.systemTimeEncoder = systemTimeEncoder;
	}
	
	@Override
	public byte[] encode(ASTimeZone asTimeZone) {
		if (asTimeZone == null) {
			return null;
		}
		return encodeTimeZoneAsBinary(asTimeZone);
	}
	
	@VisibleForTesting byte[] encodeTimeZoneAsBinary(ASTimeZone asTimeZone) {
		byte[] bias = encodeBias(asTimeZone.getBias());
		byte[] standardName = encodeName(asTimeZone.getStandardName());
		byte[] standardDate = encodeDate(asTimeZone.getStandardDate());
		byte[] standardBias = encodeBias(asTimeZone.getStandardBias());
		byte[] dayLightName = encodeName(asTimeZone.getDayLightName());
		byte[] dayLightDate = encodeDate(asTimeZone.getDayLightDate());
		byte[] dayLightBias = encodeBias(asTimeZone.getDayLightBias());

		byte[] binaryTimeZone = Bytes.concat(bias,
				standardName, standardDate, standardBias,
				dayLightName, dayLightDate, dayLightBias);
		return binaryTimeZone;
	}

	@VisibleForTesting byte[] encodeBias(int biasToEncode) {
		return intEncoder.capacity(Capacity.FOUR).toByteArray(biasToEncode);
	}

	@VisibleForTesting byte[] encodeName(String nameToEncode) {
		byte[] standardName = wcharEncoder.toByteArray(nameToEncode);
		return Arrays.copyOf(standardName, SPEC_NAMES_LENGHT);
	}

	@VisibleForTesting byte[] encodeDate(ASSystemTime dateToEncode) {
		return systemTimeEncoder.toByteArray(dateToEncode);
	}

}
