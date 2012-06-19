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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.obm.push.protocol.bean.ASTimeZone;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ASTimeZoneConverterImpl implements ASTimeZoneConverter {
	
	@VisibleForTesting static final Collection<String> TIME_ZONE_PREFERENCES =
			Lists.newArrayList("Europe/Paris", "Europe/London",
					"America/New_York", "America/Los_Angeles", "Australia/Sydney");
	
	private final TimeZoneConverter timeZoneConverter;
	
	@Inject
	@VisibleForTesting ASTimeZoneConverterImpl(TimeZoneConverter timeZoneConverter) {
		this.timeZoneConverter = timeZoneConverter;
	}
	
	@Override
	public TimeZone convert(ASTimeZone asTimeZone) {
		Collection<String> availableIDs = getAvailableTimeZoneIDs(asTimeZone);
		if (availableIDs != null) {
			if (asTimeZone.useDaylightTime()) {
				return findTimeZone(asTimeZone, availableIDs);
			} else {
				return getFirstTimeZoneWithoutDST(availableIDs);
			}
		}
		return null;
	}
	
	private Collection<String> getAvailableTimeZoneIDs(ASTimeZone asTimeZone) {
		int rawOffset = -1 * toMillis(asTimeZone.getBias());
		
		String[] availableIDs = TimeZone.getAvailableIDs(rawOffset);
		return orderByTimezonePreferences(availableIDs);
	}
	
	private List<String> orderByTimezonePreferences(String[] timeZoneIDs) {
		return Ordering.from(new Comparator<String>() {
			@Override
			public int compare(String left, String right) {
				return TIME_ZONE_PREFERENCES.contains(right) ? 1 : 0;
			}
		}).sortedCopy(Lists.newArrayList(timeZoneIDs));
	}

	private int toMillis(int minutes) {
		return minutes*60*1000;
	}

	private TimeZone findTimeZone(ASTimeZone asTimeZone,
			Collection<String> availableIDs) {
		for (String availableID : availableIDs) {
			TimeZone timeZone = TimeZone.getTimeZone(availableID);
			ASTimeZone timeZoneConverted = timeZoneConverter.convert(timeZone, Locale.US);
			if (timeZoneConverted.equals(asTimeZone)) {
				return timeZone;
			}
		}
		return null;
	}
	
	private TimeZone getFirstTimeZoneWithoutDST(Collection<String> availableIDs) {
		for (String availableID : availableIDs) {
			TimeZone timeZone = TimeZone.getTimeZone(availableID);
			if (!timeZone.useDaylightTime()) {
				return timeZone;
			}
		}
		return null;
	}
}
