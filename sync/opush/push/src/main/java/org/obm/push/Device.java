package org.obm.push;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.obm.push.backend.BackendSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Device {

	private final static Logger logger = LoggerFactory.getLogger(Device.class);
	
	public static class Factory {
	
		public Device create(String devType, String userAgent) {
			Properties hints = getHints(userAgent, devType);
			String rewritenDevType = rewriteDevType(userAgent, devType);
			return new Device(rewritenDevType, hints);
		}
				
		private String rewriteDevType(String userAgent, String devType) {
			if (isNokiaE71(userAgent)) {
				return userAgent;
			} else {
				return devType;
			}
		}
		
		private Properties getHints(String userAgent, String devType) {
			if (isNokiaE71(userAgent)) {
				Properties hints = new Properties();
				hints.put("hint.multipleCalendars", false);
				hints.put("hint.loadAttendees", false);
				return hints;
			} else {
				return loadHintsFromDevType(devType);
			}
		}
		
		private boolean isNokiaE71(String userAgent) {
			if (userAgent != null && userAgent.contains("Nokia") && userAgent.contains("MailforExchange")) {
				return true;
			}
			return false;
		}
		
		private Properties loadHintsFromDevType(String devType) {
			Properties hints = new Properties();
			InputStream in = null;
			try {
				in = BackendSession.class.getClassLoader()
						.getResourceAsStream("hints/" + devType + ".hints");
				hints.load(in);
			} catch (Throwable e) {
				logger.warn("could not load hints for device type {} ", devType); 
			} finally {
				IOUtils.closeQuietly(in);
			}
			return hints;
		}

		
	}
	
	private final String devType;
	private final Properties hints;
	
	public Device(String devType, Properties hints) {
		this.devType = devType;
		this.hints = hints;
	}	
	
	public boolean checkHint(String key, boolean defaultValue) {
		if (!hints.containsKey(key)) {
			return defaultValue;
		} else {
			return "true".equals(hints.get(key));
		}
	}

	public String getDevType() {
		return devType;
	}
	
}
