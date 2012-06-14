/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.bean.ms;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.utils.type.UnsignedShort;

@RunWith(SlowFilterRunner.class)
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
		return new ASSystemTime.Builder()
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
		new ASTimeZone.Builder()
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsStandardBias() {
		new ASTimeZone.Builder()
			.bias(defaultBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsStandardDate() {
		new ASTimeZone.Builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsDayLightBias() {
		new ASTimeZone.Builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightDate(defaultDayLightDate)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuilderNeedsDayLightDate() {
		new ASTimeZone.Builder()
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
		
		Assertions.assertThat(asTimeZone.getStandardName()).isNull();
	}

	@Test
	public void testBuilderAcceptNullDayLightName() {
		ASTimeZone asTimeZone = requirementsInitializedBuilder()
			.dayLightName(null)
			.build();
		
		Assertions.assertThat(asTimeZone.getDayLightName()).isNull();
	}
	
	public ASTimeZone.Builder requirementsInitializedBuilder() {
		return new ASTimeZone.Builder()
			.bias(defaultBiasInMinutes)
			.standardBias(defaultStandardBiasInMinutes)
			.standardDate(defaultStandardDate)
			.dayLightBias(defaultDayLightBiasInMinutes)
			.dayLightDate(defaultDayLightDate);
	}
}
