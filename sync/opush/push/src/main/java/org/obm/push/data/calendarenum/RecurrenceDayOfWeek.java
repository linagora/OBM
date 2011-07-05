package org.obm.push.data.calendarenum;

import java.util.HashSet;
import java.util.Set;

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

}
