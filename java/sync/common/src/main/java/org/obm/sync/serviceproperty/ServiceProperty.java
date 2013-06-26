/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.sync.serviceproperty;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ServiceProperty {

	private final Integer id;
	private final String service;
	private final String property;
	private final String value;

	public static class Builder {
		
		private Integer id;
		private String service;
		private String property;
		private String value;
		
		private Builder() {
		}

		public Builder from(ServiceProperty serviceProperty) {
			return id(serviceProperty.id)
				.service(serviceProperty.service)
				.property(serviceProperty.property)
				.value(serviceProperty.value);
		}

		
		public Builder id(Integer id) {
			Preconditions.checkNotNull(id);
			this.id = id;
			return this;
		}
		
		public Builder service(String service) {
			Preconditions.checkNotNull(service);
			this.service = service;
			return this;
		}
		
		public Builder property(String property) {
			Preconditions.checkNotNull(property);
			this.property = property;
			return this;
		}
		
		public Builder value(String value) {
			this.value = value;
			return this;
		}
				
		public ServiceProperty build() {
			return new ServiceProperty(id, service, property, value);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}

	private ServiceProperty(Integer id, String service, String property, String value) {
		this.id = id;
		this.service = service;
		this.property = property;
		this.value = value;
	}

	public Integer getId() {
		return id;
	}

	public String getService() {
		return service;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(id, service, property, value);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof ServiceProperty) {
			ServiceProperty that = (ServiceProperty) object;
			
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.service, that.service)
				&& Objects.equal(this.property, that.property)
				&& Objects.equal(this.value, that.value);
			}
		
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("service", service)
			.add("property", property)
			.add("value", value)
			.toString();
	}


}
