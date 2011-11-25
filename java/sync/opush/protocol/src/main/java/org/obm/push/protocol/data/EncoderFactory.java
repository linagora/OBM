package org.obm.push.protocol.data;

import org.obm.push.bean.IApplicationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class EncoderFactory {

	private static final Logger logger = LoggerFactory.getLogger(EncoderFactory.class);
	
	private final Provider<CalendarEncoder> calendarProvider;
	private final Provider<ContactEncoder> contactProvider;
	private final Provider<TaskEncoder> taskEncoder;
	private final Provider<EmailEncoder> emailEncoder;

	@Inject
	private EncoderFactory(Provider<CalendarEncoder> calendarProvider,
			Provider<ContactEncoder> contactProvider,
			Provider<TaskEncoder> taskEncoder,
			Provider<EmailEncoder> emailEncoder) {
		super();
		this.calendarProvider = calendarProvider;
		this.contactProvider = contactProvider;
		this.taskEncoder = taskEncoder;
		this.emailEncoder = emailEncoder;
	}
	
	public IDataEncoder getEncoder(IApplicationData data) {
		if (data != null) {
			switch (data.getType()) {

			case CALENDAR:
				return calendarProvider.get();

			case CONTACTS:
				return contactProvider.get();

			case TASKS:
				return taskEncoder.get();

			case EMAIL:
			default:
				return emailEncoder.get();
			}
		} else {
			logger.warn("TRY TO ENCODE NULL OBJECT");
			return null;
		}
	}

}
