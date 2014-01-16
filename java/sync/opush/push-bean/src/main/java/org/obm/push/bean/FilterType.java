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
package org.obm.push.bean;

import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum FilterType {

	ALL_ITEMS("0"),
	ONE_DAY_BACK("1"),
	THREE_DAYS_BACK("2"),
	ONE_WEEK_BACK("3"),
	TWO_WEEKS_BACK("4"),
	ONE_MONTHS_BACK("5"),
	THREE_MONTHS_BACK("6"),
	SIX_MONTHS_BACK("7"),
	FILTER_BY_NO_INCOMPLETE_TASKS("8");

	private final String specificationValue;
	
	private FilterType(String specificationValue) {
		this.specificationValue = specificationValue;
	}
	
	public String asSpecificationValue() {
		return specificationValue;
	}
	
	public static FilterType fromSpecificationValue(String specificationValue) {
		if (specValueToEnum.containsKey(specificationValue)) {
			return specValueToEnum.get(specificationValue);
		}
		throw new IllegalArgumentException("No filter type for '" + specificationValue + "'");
	}

	public Date getFilteredDateTodayAtMidnight() {
		return getFilteredDate(DateUtils.getMidnightCalendar().getTime());
	}

	public Date getFilteredDate(Date fromDate) {
		DateTime fromUTCDate = new DateTime(fromDate).withZone(DateTimeZone.UTC);
		switch (this) {
		case ALL_ITEMS:
			return DateUtils.getEpochPlusOneSecondCalendar().getTime();
		case ONE_DAY_BACK:
			return fromUTCDate.minusDays(1).toDate();
		case THREE_DAYS_BACK:
			return fromUTCDate.minusDays(3).toDate();
		case ONE_WEEK_BACK:
			return fromUTCDate.minusWeeks(1).toDate();
		case TWO_WEEKS_BACK:
			return fromUTCDate.minusWeeks(2).toDate();
		case ONE_MONTHS_BACK:
			return fromUTCDate.minusMonths(1).toDate();
		case THREE_MONTHS_BACK:
			return fromUTCDate.minusMonths(3).toDate();
		case SIX_MONTHS_BACK:
			return fromUTCDate.minusMonths(6).toDate();
		case FILTER_BY_NO_INCOMPLETE_TASKS:
			return fromDate;
		}
		throw new IllegalStateException("No filtered date available");
	}

	private static Map<String, FilterType> specValueToEnum;
	
	static {
		Builder<String, FilterType> builder = ImmutableMap.builder();
		for (FilterType filterType : values()) {
			builder.put(filterType.specificationValue, filterType);
		}
		specValueToEnum = builder.build();
	}
}