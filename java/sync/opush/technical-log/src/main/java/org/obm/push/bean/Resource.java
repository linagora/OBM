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

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import com.google.common.base.Objects;

public class Resource {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Resource createEmptyResource() {
		return builder().build();
	}
	
	public static class Builder {
		private long resourceId;
		private ResourceType resourceType;
		private DateTime resourceStartTime;
		private DateTime resourceEndTime;
		
		private Builder() {}
		
		public Builder resourceId(long resourceId) {
			this.resourceId = resourceId;
			return this;
		}
		
		public Builder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
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
			return new Resource(resourceId, resourceType, resourceStartTime, resourceEndTime);
		}
	}
	
	private final long resourceId;
	private final ResourceType resourceType;
	private final DateTime resourceStartTime;
	private final DateTime resourceEndTime;
	
	private Resource(long resourceId, ResourceType resourceType, DateTime resourceStartTime, DateTime resourceEndTime) {
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.resourceStartTime = resourceStartTime;
		this.resourceEndTime = resourceEndTime;
	}
	
	public long getResourceId() {
		return resourceId;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public DateTime getResourceStartTime() {
		return resourceStartTime;
	}
	
	public DateTime getResourceEndTime() {
		return resourceEndTime;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(resourceId, resourceType, resourceStartTime, resourceEndTime);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Resource) {
			Resource that = (Resource) object;
			return Objects.equal(this.resourceId, that.resourceId)
				&& Objects.equal(this.resourceType, that.resourceType)
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
			.add("resourceStartTime", DateTimeUtils.getInstantMillis(resourceStartTime))
			.add("resourceEndTime", DateTimeUtils.getInstantMillis(resourceEndTime))
			.toString();
	}
}
