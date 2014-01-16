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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.obm.push.technicallog.bean.ResourceType;

import com.google.common.base.Objects;

@XmlRootElement(name = JAXBConstants.RESOURCE_ROOT)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { JAXBConstants.RESOURCE_ID, 
			JAXBConstants.RESOURCE_TYPE, 
			JAXBConstants.TRANSACTION_ID,
			JAXBConstants.RESOURCE_START_TIME, 
			JAXBConstants.RESOURCE_END_TIME },
		factoryMethod = JAXBConstants.RESOURCE_EMPTY_METHOD_NAME)
public class Resource extends JAXBBean {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Resource createEmptyResource() {
		return builder().build();
	}
	
	public static class Builder {
		private Long resourceId;
		private ResourceType resourceType;
		private Long transactionId;
		private DateTime resourceStartTime;
		private DateTime resourceEndTime;
		
		private Builder() {}
		
		public Builder resourceId(Long resourceId) {
			this.resourceId = resourceId;
			return this;
		}
		
		public Builder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
			return this;
		}
		
		public Builder transactionId(Long transactionId) {
			this.transactionId = transactionId;
			return this;
		}
		
		public Builder resourceStartTime(DateTime resourceStartTime) {
			this.resourceStartTime = resourceStartTime;
			return this;
		}
		
		public Builder resourceEndTime(DateTime resourceEndTime) {
			this.resourceEndTime = resourceEndTime;
			return this;
		}
		
		public Resource build() {
			return new Resource(resourceId, 
					resourceType, 
					transactionId,
					resourceStartTime, 
					resourceEndTime);
		}
	}
	
	@XmlAttribute
	private final Long resourceId;
	@XmlElement
	private final ResourceType resourceType;
	@XmlElement
	private final Long transactionId;
	@XmlElement
	private final DateTime resourceStartTime;
	@XmlElement
	private final DateTime resourceEndTime;
	
	private Resource(Long resourceId, 
			ResourceType resourceType, 
			Long transactionId, 
			DateTime resourceStartTime, 
			DateTime resourceEndTime) {
		
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.transactionId = transactionId;
		this.resourceStartTime = resourceStartTime;
		this.resourceEndTime = resourceEndTime;
	}
	
	public Long getResourceId() {
		return resourceId;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public Long getTransactionId() {
		return transactionId;
	}
	
	public DateTime getResourceStartTime() {
		return resourceStartTime;
	}
	
	public DateTime getResourceEndTime() {
		return resourceEndTime;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(resourceId, 
				resourceType, 
				transactionId,
				resourceStartTime, 
				resourceEndTime);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Resource) {
			Resource that = (Resource) object;
			return Objects.equal(this.resourceId, that.resourceId)
				&& Objects.equal(this.resourceType, that.resourceType)
				&& Objects.equal(this.transactionId, that.transactionId)
				&& Objects.equal(this.resourceStartTime, that.resourceStartTime)
				&& Objects.equal(this.resourceEndTime, that.resourceEndTime);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("resourceId", resourceId)
			.add("resourceType", resourceType)
			.add("transactionId", transactionId)
			.add("resourceStartTime", DateTimeUtils.getInstantMillis(resourceStartTime))
			.add("resourceEndTime", DateTimeUtils.getInstantMillis(resourceEndTime))
			.toString();
	}
}
