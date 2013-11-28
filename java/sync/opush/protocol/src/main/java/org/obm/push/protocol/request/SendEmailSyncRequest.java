/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.protocol.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.obm.push.bean.DeviceId;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;


public class SendEmailSyncRequest implements ActiveSyncRequest {

	public static class Builder {
		private Map<String, String> parameters;
		private InputStream inputStream;
		private Map<String, String> headers;
		private DeviceId deviceId;
		private String deviceType;
		private String userAgent;
		private String command;
		private String msPolicyKey;
		private String mSASProtocolVersion;
		
		public Builder() {
			this.parameters = Maps.newHashMap();
			this.headers = Maps.newHashMap();
		}
		
		public Builder parameters(Map<String, String> parameters) {
			if (parameters != null) {
				this.parameters = parameters;
			}
			return this;
		}
		
		public Builder inputStream(InputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}
		
		public Builder headers(Map<String, String> headers) {
			if (headers != null) {
				this.headers = headers;
			}
			return this;
		}
		
		public Builder deviceId(DeviceId deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public Builder deviceType(String deviceType) {
			this.deviceType = deviceType;
			return this;
		}
		
		public Builder userAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}
		
		public Builder command(String command) {
			this.command = command;
			return this;
		}
		
		public Builder msPolicyKey(String msPolicyKey) {
			this.msPolicyKey = msPolicyKey;
			return this;
		}
		
		public Builder mSASProtocolVersion(String mSASProtocolVersion) {
			this.mSASProtocolVersion = mSASProtocolVersion;
			return this;
		}
		
		public SendEmailSyncRequest build() {
			return new SendEmailSyncRequest(parameters, 
					inputStream, 
					headers, 
					deviceId, 
					deviceType, 
					userAgent, 
					command, 
					msPolicyKey, 
					mSASProtocolVersion);
		}
	}
	
	private final Map<String, String> parameters;
	private final InputStream inputStream;
	private final Map<String, String> headers;
	private final DeviceId deviceId;
	private final String deviceType;
	private final String userAgent;
	private final String command;
	private final String msPolicyKey;
	private final String mSASProtocolVersion;
	
	private SendEmailSyncRequest(Map<String, String> parameters, 
		InputStream inputStream, 
		Map<String, String> headers, 
		DeviceId deviceId, 
		String deviceType, 
		String userAgent, 
		String command, 
		String msPolicyKey, 
		String mSASProtocolVersion) {
		
		this.parameters = parameters;
		this.inputStream = inputStream;
		this.headers = headers;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.userAgent = userAgent;
		this.command = command;
		this.msPolicyKey = msPolicyKey;
		this.mSASProtocolVersion = mSASProtocolVersion;
	}

	@Override
	public String getParameter(String key) {
		return parameters.get(key);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	@Override
	public HttpServletRequest getHttpServletRequest() {
		return null;
	}

	@Override
	public DeviceId getDeviceId() {
		return deviceId;
	}

	@Override
	public String getDeviceType() {
		return deviceType;
	}
	
	@Override
	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public String getCommand() {
		return command;
	}
	
	@Override
	public String getMsPolicyKey() {
		return msPolicyKey;
	}
	
	@Override
	public String getMSASProtocolVersion() {
		return mSASProtocolVersion;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(headers, deviceId, deviceType, userAgent, command, msPolicyKey, mSASProtocolVersion);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SendEmailSyncRequest) {
			SendEmailSyncRequest that = (SendEmailSyncRequest) object;
			return Objects.equal(this.headers, that.headers)
					&& Objects.equal(this.deviceId, that.deviceId)
					&& Objects.equal(this.deviceType, that.deviceType)
					&& Objects.equal(this.userAgent, that.userAgent)
					&& Objects.equal(this.command, that.command)
					&& Objects.equal(this.msPolicyKey, that.msPolicyKey)
					&& Objects.equal(this.mSASProtocolVersion, that.mSASProtocolVersion);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("headers", headers)
				.add("deviceId", deviceId)
				.add("deviceType", deviceType)
				.add("userAgent", userAgent)
				.add("command", command)
				.add("msPolicyKey", msPolicyKey)
				.add("mSASProtocolVersion", mSASProtocolVersion)
				.toString();
	}
}
