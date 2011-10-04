package org.obm.push.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	
	public static Calendar getCurrentGMTCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}
	
	public static Calendar getEpochPlusOneSecondCalendar() {
		Calendar calendar = getCurrentGMTCalendar();
		calendar.setTimeInMillis(0);
		//We don't use zero timestamp to avoid broken handling of MYSQL
		calendar.set(Calendar.SECOND, 1);
		return calendar;
	}
	
	public static Date getCurrentDate() {
		return getCurrentGMTCalendar().getTime();
	}
	
	public static Calendar getMidnightCalendar() {
		Calendar calendar = getCurrentGMTCalendar();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
}
