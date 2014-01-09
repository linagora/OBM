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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.mutable.MutableInt;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.IntEncoder.Capacity;
import org.obm.push.utils.type.UnsignedShort;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;


public class TimeZoneEncoderImplTest {

	private static final int SPEC_TIMEZONE_LENGHT = 172;
	private static final int SPEC_BIAS_LENGHT = 4;
	private static final int SPEC_NAMES_LENGHT = 64;
	private static final int SPEC_DATE_LENGHT = 16;
	
	private TimeZoneEncoderImpl serializer;

	private int defaultBiasInMinutes;
	private int defaultStandardBiasInMinutes;
	private ASSystemTime defaultStandardDate;
	private int defaultDayLightBiasInMinutes;
	private ASSystemTime defaultDayLightDate;
	
	private IntEncoder intEncoder;
	private SystemTimeEncoder systemTimeEncoder;
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		serializer = new TimeZoneEncoderImpl(
				new IntEncoder(), new WCHAREncoder(), new SystemTimeEncoder());
		intEncoder = new IntEncoder();
		systemTimeEncoder = new SystemTimeEncoder();
		
		defaultBiasInMinutes = 10;
		defaultStandardBiasInMinutes = 100;
		defaultStandardDate = anySystemTime();
		defaultDayLightBiasInMinutes = 100;
		defaultDayLightDate = anySystemTime();
	}
	
	private ASSystemTime anySystemTime() {
		return ASSystemTime.builder()
			.year(UnsignedShort.checkedCast(0))
			.month(UnsignedShort.checkedCast(0))
			.dayOfWeek(UnsignedShort.checkedCast(0))
			.hour(UnsignedShort.checkedCast(0))
			.weekOfMonth(UnsignedShort.checkedCast(0))
			.minute(UnsignedShort.checkedCast(0))
			.second(UnsignedShort.checkedCast(0))
			.milliseconds(UnsignedShort.checkedCast(0))
			.build();
	}
	
	@Test
	public void testEncodeBiasAtZero() {
		byte[] bias = serializer.encodeBias(0);
		
		assertThat(bias).hasSize(SPEC_BIAS_LENGHT).isEqualTo(
				intEncoder.capacity(Capacity.FOUR).toByteArray(0));
	}
	
	@Test
	public void testEncodeBiasAtPositiveValue() {
		byte[] bias = serializer.encodeBias(128);
		
		assertThat(bias).hasSize(SPEC_BIAS_LENGHT).isEqualTo(
				intEncoder.capacity(Capacity.FOUR).toByteArray(128));
	}
	
	@Test
	public void testEncodeBiasAtNegativeValue() {
		byte[] bias = serializer.encodeBias(-128);

		assertThat(bias).hasSize(SPEC_BIAS_LENGHT).isEqualTo(
				intEncoder.capacity(Capacity.FOUR).toByteArray(-128));
	}
	
	@Test
	public void testEncodeBiasAtMAXValue() {
		int maxValue = Integer.MAX_VALUE;
		byte[] bias = serializer.encodeBias(maxValue);
		
		assertThat(bias).hasSize(SPEC_BIAS_LENGHT).isEqualTo(
				intEncoder.capacity(Capacity.FOUR).toByteArray(maxValue));
	}
	
	@Test
	public void testEncodeBiasAtMINValue() {
		int minValue = Integer.MIN_VALUE;
		byte[] bias = serializer.encodeBias(minValue);

		assertThat(bias).hasSize(SPEC_BIAS_LENGHT).isEqualTo(
				intEncoder.capacity(Capacity.FOUR).toByteArray(minValue));
	}
	
	@Test
	public void testEncodeNameEmpty() {
		byte[] standardName = serializer.encodeName("");

		assertThat(standardName).hasSize(SPEC_NAMES_LENGHT).isEqualTo(new byte[SPEC_NAMES_LENGHT]);
	}
	
	@Test
	public void testEncodeNameIsFillWith0x0() {
		byte[] standardName = serializer.encodeName("a");

		byte[] bytesForA = new byte[] {97, 0};
		int spaceToBeFilledByZeroByte = SPEC_NAMES_LENGHT - bytesForA.length;
		byte[] expectedValue = Bytes.concat(bytesForA, new byte[spaceToBeFilledByZeroByte]);
		assertThat(standardName).hasSize(SPEC_NAMES_LENGHT).isEqualTo(expectedValue);
	}
	
	@Test
	public void testEncodeNameAccept32Char() {
		String strOf32Char = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		byte[] encoded32char = strOf32Char.getBytes(Charsets.UTF_16LE);
		
		byte[] standardName = serializer.encodeName(new String(strOf32Char));

		assertThat(standardName).hasSize(SPEC_NAMES_LENGHT).isEqualTo(encoded32char);
	}
	
	@Test
	public void testEncodeNameTruncAt32Char() {
		String strOfMoreThan32Char = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		byte[] encodedMoreThan32Char = strOfMoreThan32Char.getBytes(Charsets.UTF_16LE);
		
		byte[] standardName = serializer.encodeName(new String(strOfMoreThan32Char));
		
		byte[] expectedTruncatedValue = Arrays.copyOf(encodedMoreThan32Char, SPEC_NAMES_LENGHT);
		assertThat(standardName).hasSize(SPEC_NAMES_LENGHT).isEqualTo(expectedTruncatedValue);
	}
	
	@Test
	public void testEncodeDateAtZero() {
		ASSystemTime systemTime = new ASSystemTime.FromDateBuilder().dateTime(new DateTime(0)).build();
		byte[] expectedValue = systemTimeEncoder.toByteArray(systemTime);

		byte[] standardDate = serializer.encodeDate(systemTime);

		assertThat(standardDate).hasSize(SPEC_DATE_LENGHT).isEqualTo(expectedValue);
	}
	
	@Test
	public void testEncodeLastTimeOf2016() {
		ASSystemTime systemTime = new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2016-12-31T23:59:59.999+00")).build();
		byte[] expectedValue = systemTimeEncoder.toByteArray(systemTime);

		byte[] standardDate = serializer.encodeDate(systemTime);
		
		assertThat(standardDate).hasSize(SPEC_DATE_LENGHT).isEqualTo(expectedValue);
	}
	
	@Test
	public void testEncodeEarlyTimeOf2222() {
		ASSystemTime systemTime = new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2222-01-01T00:00:00.000+00")).build();
		byte[] expectedValue = systemTimeEncoder.toByteArray(systemTime);

		byte[] standardDate = serializer.encodeDate(systemTime);

		assertThat(standardDate).hasSize(SPEC_DATE_LENGHT).isEqualTo(expectedValue);
	}
	
	@Test
	public void testEncodeEuropeParisAsExchangeDoes() {
		byte[] exchangeTzBinary = Base64.decodeBase64(
				"xP///ygAVQBUAEMAKwAwADEAOgAwADAAKQAgAEIAcgB1AHgAZQBsAGwAZQ" +
				"BzACwAIABDAG8AcABlAG4AaABhAGcAdQAAAAoAAAAFAAMAAAAAAAAAAAAA" +
				"ACgAVQBUAEMAKwAwADEAOgAwADAAKQAgAEIAcgB1AHgAZQBsAGwAZQBzAC" +
				"wAIABDAG8AcABlAG4AaABhAGcAdQAAAAMAAAAFAAIAAAAAAAAAxP///w==");
		

		byte[] opushTzBinary = serializer.encodeTimeZoneAsBinary(asTimeZoneOf("Europe/Paris"));

		byte[][] exchangeTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(exchangeTzBinary);
		byte[][] opushTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(opushTzBinary);
		
		assertThat(opushTzBinary).hasSize(SPEC_TIMEZONE_LENGHT);
		assertThat(opushTzBinaryFields).isEqualTo(exchangeTzBinaryFields);
	}
	
	@Test
	public void testEncodeEuropeLisbonAsExchangeDoes() {
		byte[] exchangeTzBinary = Base64.decodeBase64(
				"AAAAACgAVQBUAEMAKQAgAEgAZQB1AHIAZQAgAGQAZQAgAEcAcgBlAGUAbg" +
				"B3AGkAYwBoACAAOgAgAEQAdQBiAGwAaQAAAAoAAAAFAAIAAAAAAAAAAAAA" +
				"ACgAVQBUAEMAKQAgAEgAZQB1AHIAZQAgAGQAZQAgAEcAcgBlAGUAbgB3AG" +
				"kAYwBoACAAOgAgAEQAdQBiAGwAaQAAAAMAAAAFAAEAAAAAAAAAxP///w==");
		
		byte[] opushTzBinary = serializer.encodeTimeZoneAsBinary(asTimeZoneOf("Europe/Lisbon"));

		byte[][] exchangeTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(exchangeTzBinary);
		byte[][] opushTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(opushTzBinary);
		
		assertThat(opushTzBinary).hasSize(SPEC_TIMEZONE_LENGHT);
		assertThat(opushTzBinaryFields).isEqualTo(exchangeTzBinaryFields);
	}
	
	@Test
	public void testEncodeAsiaKathmanduAsExchangeDoes() {
		byte[] exchangeTzBinary = Base64.decodeBase64(
				"p/7//ygAVQBUAEMAKwAwADUAOgA0ADUAKQAgAEsAYQB0AG0AYQBuAGQAbw" +
				"B1AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"ACgAVQBUAEMAKwAwADUAOgA0ADUAKQAgAEsAYQB0AG0AYQBuAGQAbwB1AA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==");
		
		byte[] opushTzBinary = serializer.encodeTimeZoneAsBinary(asTimeZoneOf("Asia/Kathmandu"));

		byte[][] exchangeTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(exchangeTzBinary);
		byte[][] opushTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(opushTzBinary);
		
		assertThat(opushTzBinary).hasSize(SPEC_TIMEZONE_LENGHT);
		assertThat(opushTzBinaryFields).isEqualTo(exchangeTzBinaryFields);
	}
	
	@Test
	public void testEncodePacificAucklandAsExchangeDoes() {
		byte[] exchangeTzBinary = Base64.decodeBase64(
				"MP3//ygAVQBUAEMAKwAxADIAOgAwADAAKQAgAEEAdQBjAGsAbABhAG4AZA" +
				"AsACAAVwBlAGwAbABpAG4AZwB0AG8AbgAAAAQAAAABAAMAAAAAAAAAAAAA" +
				"ACgAVQBUAEMAKwAxADIAOgAwADAAKQAgAEEAdQBjAGsAbABhAG4AZAAsAC" +
				"AAVwBlAGwAbABpAG4AZwB0AG8AbgAAAAkAAAAFAAIAAAAAAAAAxP///w==");
		
		byte[] opushTzBinary = serializer.encodeTimeZoneAsBinary(asTimeZoneOf("Pacific/Auckland"));

		byte[][] exchangeTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(exchangeTzBinary);
		byte[][] opushTzBinaryFields = binaryTimeZoneAsArrayOfByteArray(opushTzBinary);
		
		assertThat(opushTzBinary).hasSize(SPEC_TIMEZONE_LENGHT);
		assertThat(opushTzBinaryFields).isEqualTo(exchangeTzBinaryFields);
	}
	
	private ASTimeZone asTimeZoneOf(String timeZoneID) {
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
		ASTimeZone asTimeZone = new TimeZoneConverterImpl().convert(timeZone, Locale.US);
		return asTimeZone;
	}

	private byte[][] binaryTimeZoneAsArrayOfByteArray(byte[] binaryTimeZone) {
		MutableInt rangeStartingIndex = new MutableInt(0);
		byte[] bias = extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_BIAS_LENGHT);
		extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_NAMES_LENGHT);
		byte[] standardDate = extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_DATE_LENGHT);
		byte[] standardBias = extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_BIAS_LENGHT);
		extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_NAMES_LENGHT);
		byte[] dayLightDate = extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_DATE_LENGHT);
		byte[] dayLightBias = extractBytesField(binaryTimeZone, rangeStartingIndex, SPEC_BIAS_LENGHT);

		return new byte[][] {bias,
				standardDate, standardBias,
				dayLightDate, dayLightBias};
	}

	private byte[] extractBytesField(byte[] exchangeTzBinary, MutableInt rangeIndex, int bytesCount) {
		int fromIndex = rangeIndex.intValue();
		rangeIndex.add(bytesCount);
		return Arrays.copyOfRange(exchangeTzBinary, fromIndex, rangeIndex.intValue());
	}

	public ASTimeZone.Builder requirementsInitializedBuilder() {
		return ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate);
	}
}
