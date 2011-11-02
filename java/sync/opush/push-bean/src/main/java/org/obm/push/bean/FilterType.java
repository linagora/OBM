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
package org.obm.push.bean;

import java.util.Calendar;

import org.obm.push.utils.DateUtils;

public enum FilterType {

	ALL_ITEMS, // 0
	ONE_DAY_BACK, // 1
	THREE_DAYS_BACK, // 2
	ONE_WEEK_BACK, // 3
	TWO_WEEKS_BACK, // 4
	ONE_MONTHS_BACK, // 5
	THREE_MONTHS_BACK, // 6
	SIX_MONTHS_BACK, // 7
	FILTER_BY_NO_INCOMPLETE_TASKS;// 8

	public static FilterType getFilterType(String number) {
		if (number == null) {
			return ALL_ITEMS;
		}
		
		if ("0".equals(number)) {
			return ALL_ITEMS;
		} else if ("1".equals(number)) {
			return ONE_DAY_BACK;
		} else if ("2".equals(number)) {
			return THREE_DAYS_BACK;
		} else if ("3".equals(number)) {
			return ONE_WEEK_BACK;
		} else if ("4".equals(number)) {
			return TWO_WEEKS_BACK;
		} else if ("5".equals(number)) {
			return ONE_MONTHS_BACK;
		} else if ("6".equals(number)) {
			return THREE_MONTHS_BACK;
		} else if ("7".equals(number)) {
			return SIX_MONTHS_BACK;
		} else if ("8".equals(number)) {
			return FILTER_BY_NO_INCOMPLETE_TASKS;
		}
		
		return ALL_ITEMS;
	}

	public Calendar getFilteredDate() {
		Calendar date = DateUtils.getMidnightCalendar();
		switch (this) {
		case ALL_ITEMS:
			return DateUtils.getEpochPlusOneSecondCalendar();
		case ONE_DAY_BACK:
			date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)-1);
			return date;
		case THREE_DAYS_BACK:
			date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)-3);
			return date;
		case ONE_WEEK_BACK:
			date.set(Calendar.WEEK_OF_MONTH, date.get(Calendar.WEEK_OF_MONTH)-1);
			return date;
		case TWO_WEEKS_BACK:
			date.set(Calendar.WEEK_OF_MONTH, date.get(Calendar.WEEK_OF_MONTH)-2);
			return date;
		case ONE_MONTHS_BACK:
			date.set(Calendar.MONTH, date.get(Calendar.MONTH)-1);
			return date;
		case THREE_MONTHS_BACK:
			date.set(Calendar.MONTH, date.get(Calendar.MONTH)-3);
			return date;
		case SIX_MONTHS_BACK:
			date.set(Calendar.MONTH, date.get(Calendar.MONTH)-6);
			return date;
		default:
			return date;
		}
	}
	
}