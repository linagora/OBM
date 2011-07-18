package org.obm.push;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.sift.SiftingAppenderBase;

@Singleton
public class LoggerService {

	@Inject
	private LoggerService() {
		configureLogger();
	}
	
	public void initLoggerSession(String loginAtDomain) {
		Calendar date = Calendar.getInstance();
		SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy.MM.dd_hh:mm:ss");
		String now = dateformatter.format(date.getTime());

		closePrecedentLogFile();
		String sessionId = loginAtDomain+"-"+now;
		configureLogger("user", loginAtDomain);
		configureLogger("sessionId", sessionId);
	}
	
	private String getLastSessionLogFileName(){
		String sessionId = MDC.get("sessionId");
		if(sessionId == null) {
			return "no-session";
		} else {
			return sessionId;
		}
	}

	private void closePrecedentLogFile(){
		String logFileName = getLastSessionLogFileName();
		if (logFileName != null) {

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			SiftingAppenderBase<?> siftingAppender = (SiftingAppender) loggerContext
														.getLogger(Logger.ROOT_LOGGER_NAME)
														.getAppender("SIFTING");
			if (siftingAppender != null) {
				AppenderTracker<?> appenderTracker = siftingAppender.getAppenderTracker();
				appenderTracker.stopAndRemoveNow(logFileName);
			}
		}
	}

	private void configureLogger(String key, String value) {
		MDC.put(key, value);
		configureLogger();
	}

	private void configureLogger() {
		MDC.put("title", "Opush ActiveSync");
	}

	
}
