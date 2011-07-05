package org.obm.push.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DateUtils {

	public static Calendar getCurrentGMTCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}
}
