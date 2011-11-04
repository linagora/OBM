package org.obm.sync.calendar;

import java.util.Comparator;
import java.util.Date;

import com.google.common.base.Strings;

public class ComparatorUsingEventHasImportantChanges implements Comparator<Event> {

	private final static int SAME = 0;
	private final static int NOT_SAME = -1;
	
	@Override
	public int compare(Event o1, Event o2) {
		int locationCompare = compare(o1.getLocation(), o2.getLocation());
		if (locationCompare != 0) {
			return locationCompare;
		}
		int dateCompare = compare(o1.getDate(), o2.getDate());
		if (dateCompare != 0) {
			return dateCompare;
		}
		int durationCompare = compare(o1.getDuration(), o2.getDuration());
		if (durationCompare != 0) {
			return durationCompare;
		}
		int recurrenceIdCompare = compare(o1.getRecurrenceId(), o2.getRecurrenceId());
		if (recurrenceIdCompare != 0) {
			return recurrenceIdCompare;
		}
		return compare(o1.getType(), o2.getType());
	}
	
	private int compare(String o1, String o2) {
		if (!StringsAreNullOrEmpty(o1, o2)) {
			return o1.compareTo(o2);
		}
		int compare = NOT_SAME;
		if (Strings.isNullOrEmpty(o1) && Strings.isNullOrEmpty(o2)) {
			compare = SAME;
		}
		return compare;
	}
	
	private int compare(Date o1, Date o2) {
		if (objectsAreNotNull(o1, o2)) {
			return o1.compareTo(o2);
		}
		return compareToNull(o1, o2);
	}
	
	private int compare(Enum<?> o1, Enum<?> o2) {
		if (objectsAreNotNull(o1, o2)) {
			return compare(o1.ordinal(), o2.ordinal());
		}
		return compareToNull(o1, o2);
	}

	private int compare(int o1, int o2) {
		return o1 - o2;
	}
	
	private boolean StringsAreNullOrEmpty(String o1, String o2) {
		if (Strings.isNullOrEmpty(o1) || Strings.isNullOrEmpty(o2)) {
			return true;
		}
		return false;
	}

	private boolean objectsAreNotNull(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			return true;
		}
		return false;
	}
	
	private int compareToNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return SAME;
		}
		return NOT_SAME;
	}
	
	public boolean equals(Event o1, Event o2) {
		int compare = compare(o1, o2);
		if (compare == 0) {
			return true;
		}
		return false;
	}

}
