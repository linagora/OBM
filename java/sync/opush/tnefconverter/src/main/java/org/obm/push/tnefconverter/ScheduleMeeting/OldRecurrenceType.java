package org.obm.push.tnefconverter.ScheduleMeeting;

//MS-XWDCAL
public enum OldRecurrenceType {
	
	NONE, // Not set
	DAILY,// 64 
	WEEKLY, // 48
	MONTHLY,// 12 
	MONTHLY_NDAY, // 56
	YEARLY, // 7
	YEARLY_NDAY; // 51
	
	public static OldRecurrenceType getRecurrenceType(String val) {
		if("64".equals(val)){
			return DAILY;
		} else if("48".equals(val)){
			return WEEKLY;
		} else if("12".equals(val)){
			return MONTHLY;
		} else if("56".equals(val)){
			return MONTHLY_NDAY;
		} else if("7".equals(val)){
			return YEARLY;
		} else if("51".equals(val)){
			return YEARLY_NDAY;
		} else {
			return NONE;
		}
	}
	
}
