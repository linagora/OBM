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
package org.obm.push.technicallog.bean.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@XmlRootElement(name = JAXBConstants.REQUEST_ROOT)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { JAXBConstants.DEVICE_ID, 
			JAXBConstants.DEVICE_TYPE, 
			JAXBConstants.COMMAND,
			JAXBConstants.REQUEST_ID,
			JAXBConstants.TRANSACTION_ID, 
			JAXBConstants.REQUEST_START_TIME, 
			JAXBConstants.REQUEST_END_TIME, 
			JAXBConstants.RESOURCES },
		factoryMethod = JAXBConstants.REQUEST_EMPTY_METHOD_NAME)
public class Request extends JAXBBean {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Request createEmptyRequest() {
		return builder().build();
	}
	
	public static class Builder {
		private String deviceId;
		private String deviceType;
		private String command;
		private Integer requestId;
		private Long transactionId;
		private DateTime requestStartTime;
		private DateTime requestEndTime;
		private List<Resource> resources;
		
		private Builder() {
			resources = Lists.newArrayList();
		}
		
		public Builder deviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public Builder deviceType(String deviceType) {
			this.deviceType = deviceType;
			return this;
		}
		
		public Builder command(String command) {
			this.command = command;
			return this;
		}
		
		public Builder requestId(Integer requestId) {
			this.requestId = requestId;
			return this;
		}
		
		public Builder transactionId(Long transactionId) {
			this.transactionId = transactionId;
			return this;
		}
		
		public Builder requestStartTime(DateTime requestStartTime) {
			this.requestStartTime = requestStartTime;
			return this;
		}
		
		public Builder requestEndTime(DateTime requestEndTime) {
			this.requestEndTime = requestEndTime;
			return this;
		}
		
		public Builder resources(List<Resource> resources) {
			this.resources = resources;
			return this;
		}
		
		public Builder add(Resource resource) {
			this.resources.add(resource);
			return this;
		}
		
		public Request build() {
			return new Request(deviceId, 
					deviceType, 
					command, 
					requestId, 
					transactionId, 
					requestStartTime, 
					requestEndTime, 
					resources);
		}
	}

	@XmlAttribute
	private final String deviceId;
	@XmlAttribute
	private final String deviceType;
	@XmlAttribute
	private final String command;
	@XmlAttribute
	private final Integer requestId;
	@XmlElement
	private final Long transactionId;
	@XmlElement
	private final DateTime requestStartTime;
	@XmlElement
	private final DateTime requestEndTime;
	@XmlElement
	private final List<Resource> resources;
	
	private Request(String deviceId, 
			String deviceType, 
			String command, 
			Integer requestId, 
			Long transactionId, 
			DateTime requestStartTime, 
			DateTime requestEndTime, 
			List<Resource> resources) {
		
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.command = command;
		this.requestId = requestId;
		this.transactionId = transactionId;
		this.requestStartTime = requestStartTime;
		this.requestEndTime = requestEndTime;
		this.resources = resources;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public String getDeviceType() {
		return deviceType;
	}
	
	public String getCommand() {
		return command;
	}
	
	public Integer getRequestId() {
		return requestId;
	}
	
	public Long getTransactionId() {
		return transactionId;
	}
	
	public DateTime getRequestStartTime() {
		return requestStartTime;
	}
	
	public DateTime getRequestEndTime() {
		return requestEndTime;
	}
	
	public List<Resource> getResources() {
		return resources;
	}
	
	public void add(Resource resource) {
		resources.add(resource);
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(deviceId, 
				deviceType, 
				command, 
				requestId, 
				transactionId, 
				requestStartTime, 
				requestEndTime, 
				resources);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Request) {
			Request that = (Request) object;
			return Objects.equal(this.deviceId, that.deviceId)
				&& Objects.equal(this.deviceType, that.deviceType)
				&& Objects.equal(this.command, that.command)
				&& Objects.equal(this.requestId, that.requestId)
				&& Objects.equal(this.transactionId, that.transactionId)
				&& Objects.equal(this.requestStartTime, that.requestStartTime)
				&& Objects.equal(this.requestEndTime, that.requestEndTime)
				&& Objects.equal(this.resources, that.resources);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("deviceId", deviceId)
			.add("deviceType", deviceType)
			.add("command", command)
			.add("requestId", requestId)
			.add("transactionId", transactionId)
			.add("requestStartTime", DateTimeUtils.getInstantMillis(requestStartTime))
			.add("requestEndTime", DateTimeUtils.getInstantMillis(requestEndTime))
			.add("resources", resources)
			.toString();
	}
}
