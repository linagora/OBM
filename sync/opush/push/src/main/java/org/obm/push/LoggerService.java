package org.obm.push;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.sift.SiftingAppenderBase;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LoggerService {

	@Inject
	private LoggerService() {
	}
	
	public void initSession(String loginAtDomain, int requestId, String command) {
		Calendar date = Calendar.getInstance();
		SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy.MM.dd_hh:mm:ss");
		String now = dateformatter.format(date.getTime());

		closePrecedentLogFile();
		String sessionId = loginAtDomain+"-"+now;
		
		MDC.put("title", "Opush ActiveSync");
		MDC.put("user", loginAtDomain);
		MDC.put("sessionId", sessionId);
		MDC.put("threadId", String.valueOf(Thread.currentThread().getId()));
		MDC.put("requestId", String.valueOf(requestId));
		MDC.put("command", command);
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

	public void closeSession() {
		MDC.clear();
	}
	
}
