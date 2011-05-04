package org.obm.sync.calendar;


public enum RecurrenceKind {
	none, daily, weekly, monthlybydate, monthlybyday, yearly;
	
	/**
	 * Same as valueOf but return none when not found
	 */
	public static RecurrenceKind lookup(String kind) {
		try {
			return valueOf(kind);
		} catch (IllegalArgumentException e) {
			return none;
		}
	}
}
