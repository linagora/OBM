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

import java.util.HashSet;
import java.util.Set;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum RecurrenceDayOfWeek {
	SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, ;

	public int asInt() {
		switch (this) {
		case SUNDAY:
			return 1;
		case MONDAY:
			return 2;
		case TUESDAY:
			return 4;
		case WEDNESDAY:
			return 8;
		case THURSDAY:
			return 16;
		case FRIDAY:
			return 32;
		case SATURDAY:
			return 64;
		default:
			return 0;
		}
	}

	public static int dayOfWeekToInt(int i) {
		switch (i) {
		case 1:
			return SUNDAY.asInt();
		case 2:
			return MONDAY.asInt();
		case 3:
			return TUESDAY.asInt();
		case 4:
			return WEDNESDAY.asInt();
		case 5:
			return THURSDAY.asInt();
		case 6:
			return FRIDAY.asInt();
		default:
		case 7:
			return SATURDAY.asInt();
		}
	}

	public static int asInt(Set<RecurrenceDayOfWeek> dayOfWeek) {
		int pattern = 0;

		if (dayOfWeek.contains(RecurrenceDayOfWeek.MONDAY)) {
			pattern |= RecurrenceDayOfWeek.MONDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.TUESDAY)) {
			pattern |= RecurrenceDayOfWeek.TUESDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.WEDNESDAY)) {
			pattern |= RecurrenceDayOfWeek.WEDNESDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.THURSDAY)) {
			pattern |= RecurrenceDayOfWeek.THURSDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.FRIDAY)) {
			pattern |= RecurrenceDayOfWeek.FRIDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SATURDAY)) {
			pattern |= RecurrenceDayOfWeek.SATURDAY.asInt();
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SUNDAY)) {
			pattern |= RecurrenceDayOfWeek.SUNDAY.asInt();
		}

		return pattern;
	}

	public static Set<RecurrenceDayOfWeek> fromInt(Integer i) {
		HashSet<RecurrenceDayOfWeek> ret = new HashSet<RecurrenceDayOfWeek>();

		if ((i & MONDAY.asInt()) == MONDAY.asInt()) {
			ret.add(MONDAY);
		}
		if ((i & TUESDAY.asInt()) == TUESDAY.asInt()) {
			ret.add(TUESDAY);
		}
		if ((i & WEDNESDAY.asInt()) == WEDNESDAY.asInt()) {
			ret.add(WEDNESDAY);
		}
		if ((i & THURSDAY.asInt()) == THURSDAY.asInt()) {
			ret.add(THURSDAY);
		}
		if ((i & FRIDAY.asInt()) == FRIDAY.asInt()) {
			ret.add(FRIDAY);
		}
		if ((i & SATURDAY.asInt()) == SATURDAY.asInt()) {
			ret.add(SATURDAY);
		}
		if ((i & SUNDAY.asInt()) == SUNDAY.asInt()) {
			ret.add(SUNDAY);
		}
		return ret;
	}

	public static RecurrenceDayOfWeek getByIndex(int index) {
		return values()[index];
	}
}
