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