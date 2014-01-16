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

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.bean.ASTimeZone.Builder;
import org.obm.push.utils.type.UnsignedShort;

public class TimeZoneConverterImpl implements TimeZoneConverter {

	private static final int TIMEZONE_DISPLAY_NAME_STYLE = TimeZone.LONG;
	private static final int TIMEZONE_EACH_YEARS_SPEC_VALUE = 0;

	@Override
	public ASTimeZone convert(TimeZone timeZone, Locale locale) {
		if (timeZone == null) {
			return null;
		}
		
		Builder asTimeZoneBuilder = ASTimeZone.builder();
		
		asTimeZoneBuilder
			.bias(biasInMinutes(timeZone))
			.standardBias(standardBiasInMinute(timeZone.getDSTSavings()))
			.dayLightBias(dayLightBiasInMinute(timeZone.getDSTSavings()))
			.standardDate(standardDate(timeZone))
			.dayLightDate(dayLightDate(timeZone))
			.standardName(standardName(timeZone, locale))
			.dayLightName(dayLightName(timeZone, locale));
		
		return asTimeZoneBuilder.build();
	}

	private ASSystemTime standardDate(TimeZone timeZone) {
		DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);

		DateTime dateMidnight = new DateTime(dateTimeZone).withTimeAtStartOfDay();
		
		long firstDSTTransitionInstant = dateTimeZone.nextTransition(dateMidnight.getMillis());
		long secondDSTTransitionInstant = dateTimeZone.nextTransition(firstDSTTransitionInstant);
		
		if (firstDSTTransitionInstant == secondDSTTransitionInstant) {
			return systemTimeFromInstant(0, DateTimeZone.UTC);
		}
		
		if (dateTimeZone.isStandardOffset(firstDSTTransitionInstant)) {
			return systemTimeFromInstant(firstDSTTransitionInstant, dateTimeZone);
		} 
		return systemTimeFromInstant(secondDSTTransitionInstant, dateTimeZone);
	}

	private ASSystemTime dayLightDate(TimeZone timeZone) {
		DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);

		DateTime dateMidnight = new DateTime(dateTimeZone).withTimeAtStartOfDay();
		
		long firstDSTTransitionInstant = dateTimeZone.nextTransition(dateMidnight.getMillis());
		long secondDSTTransitionInstant = dateTimeZone.nextTransition(firstDSTTransitionInstant);
		
		if (firstDSTTransitionInstant == secondDSTTransitionInstant) {
			return systemTimeFromInstant(0, DateTimeZone.UTC);
		}
		
		if (dateTimeZone.isStandardOffset(firstDSTTransitionInstant)) {
			return systemTimeFromInstant(secondDSTTransitionInstant, dateTimeZone);
		} 
		return systemTimeFromInstant(firstDSTTransitionInstant, dateTimeZone);
	}

	private ASSystemTime systemTimeFromInstant(long instant, DateTimeZone dateTimeZone) {
		DateTime dateTime = new DateTime(instant, dateTimeZone);
		return new ASSystemTime.FromDateBuilder()
			.dateTime(dateTime)
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build();
	}

	private String standardName(TimeZone timeZone, Locale locale) {
		return timeZoneDisplayName(timeZone, false, locale);
	}
	
	private String dayLightName(TimeZone timeZone, Locale locale) {
		return timeZoneDisplayName(timeZone, true, locale);
	}

	private String timeZoneDisplayName(TimeZone timezone, boolean useDayLightName, Locale locale) {
		return timezone.getDisplayName(useDayLightName, TIMEZONE_DISPLAY_NAME_STYLE, locale);
	}

	private int standardBiasInMinute(int dstSavings) {
		if (dstSavings > 0) {
			return 0;
		}
		
		int standardBiasInMinute = millisToMinutes(dstSavings);
		return -standardBiasInMinute;
	}
	
	private int dayLightBiasInMinute(int dstSavings) {
		if (dstSavings > 0) {
			int dayLightBiasInMinute = millisToMinutes(dstSavings);
			return -dayLightBiasInMinute;
		}
		
		return 0;
	}
	
	private int biasInMinutes(TimeZone timezone) {
		int offsetFromUtcToLocalInMillis = timezone.getRawOffset();
		int biasInMinute = -millisToMinutes(offsetFromUtcToLocalInMillis);
		return biasInMinute;
	}

	private int millisToMinutes(int millis) {
		return Duration.millis(millis).toStandardMinutes().getMinutes();
	}
}
