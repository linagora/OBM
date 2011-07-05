package org.obm.push.data.calendarenum;

public enum CalendarSensitivity {
	NORMAL, // 0
	PERSONAL, // 1
	PRIVATE, // 2
	CONFIDENTIAL; // 3
	
	public String asIntString() {
		switch (this) {
		case NORMAL:
			return "0";
		case PERSONAL:
			return "1";
		case PRIVATE:
			return "2";
		case CONFIDENTIAL:
			return "3";
		default:
			return null;
		}
	}
}
