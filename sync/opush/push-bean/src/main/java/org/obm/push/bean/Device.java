package org.obm.push.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class Device implements Serializable {

	private final static Logger logger = LoggerFactory.getLogger(Device.class);
	
	public static class Factory {
	
		public Device create(Integer databaseId, String devType, String userAgent, String devId) {
			Properties hints = getHints(userAgent, devType);
			String rewritenDevType = rewriteDevType(userAgent, devType);
			return new Device(databaseId, rewritenDevType, devId, hints);
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
				in = Device.class.getClassLoader()
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
	private final String devId;
	private final Integer databaseId;
	
	public Device(Integer databaseId, String devType, String devId, Properties hints) {
		this.databaseId = databaseId;
		this.devType = devType;
		this.devId = devId;
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

	public String getDevId() {
		return devId;
	}
	
	public Integer getDatabaseId() {
		return databaseId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(devType, hints, devId, databaseId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Device) {
			Device that = (Device) object;
			return Objects.equal(this.devType, that.devType)
				&& Objects.equal(this.hints, that.hints)
				&& Objects.equal(this.devId, that.devId)
				&& Objects.equal(this.databaseId, that.databaseId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("logger", logger)
			.add("devType", devType)
			.add("hints", hints)
			.add("devId", devId)
			.add("databaseId", databaseId)
			.toString();
	}
	
}
