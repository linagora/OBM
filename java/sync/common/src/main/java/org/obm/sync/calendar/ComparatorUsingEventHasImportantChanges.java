package org.obm.sync.calendar;

import java.util.Comparator;

public class ComparatorUsingEventHasImportantChanges implements Comparator<Event> {

	@Override
	public int compare(Event o1, Event o2) {
		int locationCompare = o1.getLocation().compareTo(o2.getLocation());
		if (locationCompare != 0) {
			return locationCompare;
		}
		int dateCompare = o1.getDate().compareTo(o2.getDate());
		if (dateCompare != 0) {
			return dateCompare;
		}
		int durationCompare = o1.getDuration() - o2.getDuration();
		if (durationCompare != 0) {
			return durationCompare;
		}
		int recurrenceIdCompare = o1.getRecurrenceId().compareTo(o2.getRecurrenceId());
		if (recurrenceIdCompare != 0) {
			return recurrenceIdCompare;
		}
		int type = o1.getType().ordinal() - o2.getType().ordinal();
		return type;
	}
	
	public boolean equals(Event o1, Event o2) {
		int compare = compare(o1, o2);
		if (compare == 0) {
			return true;
		}
		return false;
	}

}
