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
package org.obm.push.protocol.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.type.UnsignedShort;


public class ASTimeZoneTest {

	private int defaultBiasInMinutes;
	private int defaultStandardBiasInMinutes;
	private ASSystemTime defaultStandardDate;
	private int defaultDayLightBiasInMinutes;
	private ASSystemTime defaultDayLightDate;

	@Before
	public void setUp() {
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
	
	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsBias() {
		ASTimeZone.builder()
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsStandardBias() {
		ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsStandardDate() {
		ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsDayLightBias() {
		ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsDayLightDate() {
		ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.build();
	}

	@Test
	public void testBuilderAcceptNullStandardName() {
		ASTimeZone asTimeZone = requirementsInitializedBuilder()
				.standardName(null)
				.build();
		
		assertThat(asTimeZone.getStandardName()).isNull();
	}

	@Test
	public void testBuilderAcceptNullDayLightName() {
		ASTimeZone asTimeZone = requirementsInitializedBuilder()
			.dayLightName(null)
			.build();
		
		assertThat(asTimeZone.getDayLightName()).isNull();
	}

	@Test(expected=NullPointerException.class)
	public void testEqualsDiscardingNamesChecksNotNull() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		first.equalsDiscardingNames(null);
	}

	@Test
	public void testEqualsDiscardingNamesWhenSameNames() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isTrue();
	}

	@Test
	public void testEqualsDiscardingNamesWhenEmptyNames() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isTrue();
	}

	@Test
	public void testEqualsDiscardingNamesWhenDifferentBias() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(1000)
			.standardName("")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(4000)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isFalse();
	}

	@Test
	public void testEqualsDiscardingNamesWhenDifferentStandardBias() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("")
			.standardBias(1000)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(4000)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isFalse();
	}

	@Test
	public void testEqualsDiscardingNamesWhenDifferentDayLightBias() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(1000)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(4000)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isFalse();
	}

	@Test
	public void testEqualsDiscardingNamesWhenDifferentStandardDate() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(ASSystemTime.builder()
					.year(UnsignedShort.checkedCast(10))
					.month(UnsignedShort.checkedCast(10))
					.dayOfWeek(UnsignedShort.checkedCast(1))
					.hour(UnsignedShort.checkedCast(10))
					.weekOfMonth(UnsignedShort.checkedCast(1))
					.minute(UnsignedShort.checkedCast(10))
					.second(UnsignedShort.checkedCast(10))
					.milliseconds(UnsignedShort.checkedCast(10))
					.build())
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isFalse();
	}

	@Test
	public void testEqualsDiscardingNamesWhenDifferentDayLightDate() {
		ASTimeZone first = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("")
			.dayLightDate(defaultDayLightDate)
			.build();
		
		ASTimeZone second = ASTimeZone.builder()
			.bias(defaultBiasInMinutes)
			.standardName("Standard name")
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightName("Day light name")
			.dayLightDate(ASSystemTime.builder()
					.year(UnsignedShort.checkedCast(10))
					.month(UnsignedShort.checkedCast(10))
					.dayOfWeek(UnsignedShort.checkedCast(1))
					.hour(UnsignedShort.checkedCast(10))
					.weekOfMonth(UnsignedShort.checkedCast(1))
					.minute(UnsignedShort.checkedCast(10))
					.second(UnsignedShort.checkedCast(10))
					.milliseconds(UnsignedShort.checkedCast(10))
					.build())
			.build();
		
		assertThat(first.equalsDiscardingNames(second)).isFalse();
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
