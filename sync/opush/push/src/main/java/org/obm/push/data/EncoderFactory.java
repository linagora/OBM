package org.obm.push.data;

import org.obm.push.bean.IApplicationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncoderFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(EncoderFactory.class);

	public IDataEncoder getEncoder(IApplicationData data) {
		if (data != null) {
			switch (data.getType()) {

			case CALENDAR:
				return new CalendarEncoder();

			case CONTACTS:
				return new ContactEncoder();

			case TASKS:
				return new TaskEncoder();

			case EMAIL:
			default:
				return new EmailEncoder();
			}
		} else {
			logger.warn("TRY TO ENCODE NULL OBJECT");
			return null;
		}
	}

}
