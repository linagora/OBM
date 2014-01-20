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
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.obm.push.protocol.bean.ASTimeZone;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ASTimeZoneConverterImpl implements ASTimeZoneConverter {
	
	private static final String UTC = "UTC";

	private final class TimeZoneComparator implements Comparator<String> {
		@Override
		public int compare(String left, String right) {
			if (TIME_ZONE_PREFERENCES.contains(right) && !TIME_ZONE_PREFERENCES.contains(left)) {
				return 1;
			} else if (!TIME_ZONE_PREFERENCES.contains(right) && TIME_ZONE_PREFERENCES.contains(left)) {
				return -1;
			} else {
				return left.compareTo(right);
			}
		}
	}

	@VisibleForTesting static final Collection<String> TIME_ZONE_PREFERENCES =
			Lists.newArrayList(UTC, "Europe/Paris", "Europe/London",
					"America/New_York", "America/Los_Angeles", "Australia/Sydney");
	
	private final TimeZoneConverter timeZoneConverter;
	
	@Inject
	@VisibleForTesting ASTimeZoneConverterImpl(TimeZoneConverter timeZoneConverter) {
		this.timeZoneConverter = timeZoneConverter;
	}
	
	@Override
	public TimeZone convert(ASTimeZone asTimeZone) {
		return Iterables.getFirst(matchJavaTimeZones(asTimeZone), TimeZone.getTimeZone(UTC));
	}

	@VisibleForTesting Iterable<TimeZone> matchJavaTimeZones(ASTimeZone asTimeZone) {
		if (asTimeZone.useDaylightTime()) {
			return findTimeZones(asTimeZone);
		}
		return getTimeZonesWithoutDST(asTimeZone);
	}
	
	private Iterable<String> getAvailableTimeZoneIDs(ASTimeZone asTimeZone) {
		int rawOffset = -1 * toMillis(asTimeZone.getBias());
		String[] availableIDs = TimeZone.getAvailableIDs(rawOffset);
		return FluentIterable.from(Arrays.asList(availableIDs))
					.filter(Predicates.in(DateTimeZone.getAvailableIDs()))
					.toSortedSet(new TimeZoneComparator());
	}
	
	private int toMillis(int minutes) {
		return minutes*60*1000;
	}

	private Iterable<TimeZone> findTimeZones(ASTimeZone asTimeZone) {
		Builder<TimeZone> timezones = ImmutableList.builder();
		for (String availableID : getAvailableTimeZoneIDs(asTimeZone)) {
			TimeZone timeZone = TimeZone.getTimeZone(availableID);
			ASTimeZone timeZoneConverted = timeZoneConverter.convert(timeZone, Locale.US);
			if (timeZoneConverted.equalsDiscardingNames(asTimeZone)) {
				timezones.add(timeZone);
			}
		}
		return timezones.build();
	}
	
	private Iterable<TimeZone> getTimeZonesWithoutDST(ASTimeZone asTimeZone) {
		Builder<TimeZone> timezones = ImmutableList.builder();
		for (String availableID : getAvailableTimeZoneIDs(asTimeZone)) {
			TimeZone timeZone = TimeZone.getTimeZone(availableID);
			if (!timeZone.useDaylightTime()) {
				timezones.add(timeZone);
			}
		}
		return timezones.build();
	}
}
