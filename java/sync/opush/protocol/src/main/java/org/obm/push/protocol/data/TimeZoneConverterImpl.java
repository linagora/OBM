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
package org.obm.push.protocol.data;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.bean.ASTimeZone.Builder;
import org.obm.push.utils.type.UnsignedShort;

import com.google.common.base.Preconditions;

public class TimeZoneConverterImpl implements TimeZoneConverter {

	private static final long INSTANT_USED_FOR_DST_DATES = org.obm.DateUtils.date("2012-01-01T00:00:00.000+00").getTime();
	private static final int TIMEZONE_DISPLAY_NAME_STYLE = TimeZone.LONG;
	private static final int TIMEZONE_EACH_YEARS_SPEC_VALUE = 0;

	@Override
	public ASTimeZone convert(TimeZone timezone) {
		Preconditions.checkNotNull(timezone);
		
		Builder asTimeZoneBuilder = new ASTimeZone.Builder();
		
		appendBias(asTimeZoneBuilder, timezone);
		appendDSTBias(asTimeZoneBuilder, timezone);
		appendDSTTransitionDates(asTimeZoneBuilder, timezone);
		appendTimeZoneNames(asTimeZoneBuilder, timezone);
		
		return asTimeZoneBuilder.build();
	}

	private void appendDSTTransitionDates(Builder asTimeZoneBuilder, TimeZone timezone) {
		DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timezone);
		
//		long firstDSTTransitionInstant = dateTimeZone.convertUTCToLocal(dateTimeZone.nextTransition(INSTANT_USED_FOR_DST_DATES));
//		long secondDSTTransitionInstant = dateTimeZone.convertUTCToLocal(dateTimeZone.nextTransition(firstDSTTransitionInstant));
		long firstDSTTransitionInstant = dateTimeZone.nextTransition(INSTANT_USED_FOR_DST_DATES);
		long secondDSTTransitionInstant = dateTimeZone.nextTransition(firstDSTTransitionInstant);

		if (dateTimeZone.isStandardOffset(INSTANT_USED_FOR_DST_DATES)) {
			asTimeZoneBuilder.standardDate(systemTimeFromInstant(secondDSTTransitionInstant));
			asTimeZoneBuilder.dayLightDate(systemTimeFromInstant(firstDSTTransitionInstant));
		} else {
			asTimeZoneBuilder.standardDate(systemTimeFromInstant(firstDSTTransitionInstant));
			asTimeZoneBuilder.dayLightDate(systemTimeFromInstant(secondDSTTransitionInstant));
		}
	}

	private ASSystemTime systemTimeFromInstant(long instant) {
		return new ASSystemTime.FromDateBuilder()
			.date(new Date(instant))
			.overridingYear(UnsignedShort.checkedCast(TIMEZONE_EACH_YEARS_SPEC_VALUE))
			.build();
	}

	private void appendTimeZoneNames(Builder asTimeZoneBuilder, TimeZone timezone) {
		boolean useDayLightName = true;
		asTimeZoneBuilder.standardName(timeZoneDisplayName(timezone, !useDayLightName));
		asTimeZoneBuilder.dayLightName(timeZoneDisplayName(timezone, useDayLightName));
	}

	private String timeZoneDisplayName(TimeZone timezone, boolean useDayLightName) {
		return timezone.getDisplayName(useDayLightName, TIMEZONE_DISPLAY_NAME_STYLE);
	}

	private void appendDSTBias(Builder asTimeZoneBuilder, TimeZone timezone) {
		int standardBiasInMinutes = 0;
		int dayLightBiasInMinutes = 0;

		int dayLightSavingTimesInMillis = timezone.getDSTSavings();
		if (dayLightSavingTimesInMillis > 0) {
			dayLightBiasInMinutes = millisToMinutes(-dayLightSavingTimesInMillis);
		} else {
			standardBiasInMinutes = millisToMinutes(dayLightSavingTimesInMillis);
		}
		
		asTimeZoneBuilder.standardBias(standardBiasInMinutes);
		asTimeZoneBuilder.dayLightBias(dayLightBiasInMinutes);
	}

	private void appendBias(Builder asTimeZoneBuilder, TimeZone timezone) {
		int biasInMinutes = offsetFromUtcToLocalInMinutes(timezone);
		asTimeZoneBuilder.bias(-biasInMinutes);
	}

	private int offsetFromUtcToLocalInMinutes(TimeZone timezone) {
		int offsetFromUtcToLocalInMillis = timezone.getRawOffset();
		return millisToMinutes(offsetFromUtcToLocalInMillis);
	}

	private int millisToMinutes(int millis) {
		return Duration.millis(millis).toStandardMinutes().getMinutes();
	}
}
