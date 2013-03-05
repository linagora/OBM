/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class Device implements Serializable {

	private final static Logger logger = LoggerFactory.getLogger(Device.class);
	
	public static class Factory {
	
		public Device create(Integer databaseId, String devType, String userAgent,
				DeviceId devId, BigDecimal protocolVersion) {
			
			Properties hints = getHints(userAgent, devType);
			String rewritenDevType = rewriteDevType(userAgent, devType);
			return new Device(databaseId, rewritenDevType, devId, hints, protocolVersion);
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
	
	private static final long serialVersionUID = 8923456296693539537L;
	
	private final String devType;
	private final Properties hints;
	private final DeviceId devId;
	private final Integer databaseId;
	private final BigDecimal protocolVersion;
	
	public Device(Integer databaseId, String devType, DeviceId devId, Properties hints, BigDecimal protocolVersion) {
		this.databaseId = databaseId;
		this.devType = devType;
		this.devId = devId;
		this.hints = hints;
		this.protocolVersion = protocolVersion;
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

	public DeviceId getDevId() {
		return devId;
	}
	
	public Integer getDatabaseId() {
		return databaseId;
	}

	public BigDecimal getProtocolVersion() {
		return protocolVersion;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(devType, hints, devId, databaseId, protocolVersion);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Device) {
			Device that = (Device) object;
			return Objects.equal(this.devType, that.devType)
				&& Objects.equal(this.hints, that.hints)
				&& Objects.equal(this.devId, that.devId)
				&& Objects.equal(this.databaseId, that.databaseId)
				&& Objects.equal(this.protocolVersion, that.protocolVersion);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("devType", devType)
			.add("hints", hints)
			.add("devId", devId)
			.add("databaseId", databaseId)
			.add("protocolVersion", protocolVersion)
			.toString();
	}
	
}
