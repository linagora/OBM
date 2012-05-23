package org.obm.push.technicallog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.sift.SiftingAppenderBase;

public class TechnicalLoggerService {

	public void closePrecedentLogFile(){
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
	
	private String getLastSessionLogFileName(){
		String sessionId = MDC.get("sessionId");
		if (sessionId == null) {
			return "no-session";
		} else {
			return sessionId;
		}
	}

}
