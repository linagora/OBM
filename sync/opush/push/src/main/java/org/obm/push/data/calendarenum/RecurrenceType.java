package org.obm.push.data.calendarenum;

public enum RecurrenceType {
	DAILY, // 0
	WEEKLY, // 1
	MONTHLY, // 2
	MONTHLY_NDAY, // 3
	YEARLY, // 5
	YEARLY_NDAY; // 6

	public String asIntString() {
		switch (this) {
		case DAILY:
			return "0";
		case MONTHLY:
			return "2";
		case MONTHLY_NDAY:
			return "3";
		case YEARLY:
			return "5";
		case YEARLY_NDAY:
			return "6";
		default:
		case WEEKLY:
			return "1";

		}
	}
	
}
