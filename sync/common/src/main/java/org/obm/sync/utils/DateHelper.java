package org.obm.sync.utils;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateHelper {

	private static final Logger logger = LoggerFactory.getLogger(DateHelper.class);

	public static final String asString(Date d) {
		return String.valueOf(d.getTime());
	}

	public static final Date asDate(String s) {
		String tmp = s;
		if (tmp == null || tmp.trim().isEmpty() || tmp.equals("null")) {
			tmp = "0";
		}
		try {
			return new Date(Long.valueOf(tmp));
		} catch (Throwable e) {
			logger.error("Error parsing date '" + s + "'", e);
			return null;
		}
	}

}
