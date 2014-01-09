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

import java.util.Locale;
import java.util.TimeZone;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.data.TimeZoneConverterImpl;
import org.obm.push.utils.type.UnsignedShort;


public class TimeZoneConverterImplTest {

	private static final int TIMEZONE_EACH_YEARS_SPEC_VALUE = 0;
	
	private static final int LISBON_GMT = 0;
	private static final int LISBON_STANDARD_OFFSET = 0;
	private static final int LISBON_DST_OFFSET = 1;
	
	private static final int PARIS_GMT = 1;
	private static final int PARIS_STANDARD_OFFSET = 0;
	private static final int PARIS_DST_OFFSET = 1;

	private static final int RIGA_GMT = 2;
	private static final int RIGA_STANDARD_OFFSET = 0;
	private static final int RIGA_DST_OFFSET = 1;

	private static final int SYDNEY_GMT = 10;
	private static final int SYDNEY_STANDARD_OFFSET = 0;
	private static final int SYDNEY_DST_OFFSET = 1;
	
	private static final int AUCKLAND_GMT = 12;
	private static final int AUCKLAND_STANDARD_OFFSET = 0;
	private static final int AUCKLAND_DST_OFFSET = 1;
	
	private static final int HONOLULU_GMT = -10;
	private static final int HONOLULU_STANDARD_OFFSET = 0;
	private static final int HONOLULU_DST_OFFSET = 0;

	
	@Test
	public void testTimezoneIsRequired() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl().convert(null, null);
		Assertions.assertThat(asTimeZone).isNull();
	}
	
	@Test
	public void testLisbonConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Europe/Lisbon"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(LISBON_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(LISBON_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Western European Time");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(LISBON_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Western European Summer Time");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-10-28T02:00:00+00"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-03-25T01:00:00+00"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testParisConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Europe/Paris"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(PARIS_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(PARIS_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Central European Time");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(PARIS_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Central European Summer Time");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-10-28T03:00+01"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-03-25T02:00:00+01"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testParisConversionFrenchLocale() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Europe/Paris"), Locale.FRANCE);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(PARIS_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(PARIS_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Heure d'Europe centrale");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(PARIS_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Heure d'été d'Europe centrale");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-10-28T03:00:00+01"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-03-25T02:00:00+01"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testRigaConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Europe/Riga"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(RIGA_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(RIGA_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Eastern European Time");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(RIGA_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Eastern European Summer Time");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-10-28T04:00:00+02"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-03-25T03:00:00+02"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testSydneyConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Australia/Sydney"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(SYDNEY_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(SYDNEY_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Eastern Standard Time (New South Wales)");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(SYDNEY_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Eastern Summer Time (New South Wales)");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2013-04-07T03:00:00+11"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-10-07T02:00:00+10"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testAucklandConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Pacific/Auckland"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(AUCKLAND_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(AUCKLAND_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("New Zealand Standard Time");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(AUCKLAND_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("New Zealand Daylight Time");
		assertThat(asTimeZone.getStandardDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2013-04-07T03:00:00+13"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
		assertThat(asTimeZone.getDayLightDate()).isEqualTo(new ASSystemTime.FromDateBuilder()
			.dateTime(DateTime.parse("2012-09-30T02:00:00+12"))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build());
	}
	
	@Test
	public void testHonoluluConversion() {
		ASTimeZone asTimeZone = new TimeZoneConverterImpl()
			.convert(TimeZone.getTimeZone("Pacific/Honolulu"), Locale.US);
		
		assertThat(asTimeZone.getBias()).isEqualTo(biasHourOffsetInMinutes(HONOLULU_GMT));
		assertThat(asTimeZone.getStandardBias()).isEqualTo(HONOLULU_STANDARD_OFFSET);
		assertThat(asTimeZone.getStandardName()).isEqualTo("Hawaii Standard Time");
		assertThat(asTimeZone.getDayLightBias()).isEqualTo(biasHourOffsetInMinutes(HONOLULU_DST_OFFSET));
		assertThat(asTimeZone.getDayLightName()).isEqualTo("Hawaii Daylight Time");
		String standardDate = asTimeZone.getStandardDate().toString();
		String dayLightDate = asTimeZone.getDayLightDate().toString();
		assertThat(standardDate).isEqualTo(dayLightDate);
	}
	
	private int biasHourOffsetInMinutes(int hourOffset) {
		return - (hourOffset * 60);
	}
	
}
