package org.obm.sync.utils;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateHelper {

	private static final Log logger = LogFactory.getLog(DateHelper.class);

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
