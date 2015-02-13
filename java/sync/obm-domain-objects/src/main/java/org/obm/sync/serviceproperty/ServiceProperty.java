/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ServiceProperty implements Serializable {

	public static final String IMAP_AUTOSELECT = "imap_autoselect";
	public static final String SAMBA_SERVICE = "samba";
	public static final ServiceProperty IMAP = ServiceProperty
			.builder()
			.service("mail")
			.property("imap")
			.build();
	public static final ServiceProperty SMTP_IN = ServiceProperty
			.builder()
			.service("mail")
			.property("smtp_in")
			.build();
	public static final ServiceProperty OBM_SYNC = ServiceProperty
			.builder()
			.service("sync")
			.property("obm_sync")
			.build();
	public static final ServiceProperty LDAP = ServiceProperty
			.builder()
			.service("ldap")
			.property("ldap")
			.build();

	public static class Builder {

		private String service;
		private String property;

		private Builder() {
		}

		public Builder from(ServiceProperty serviceProperty) {
			return service(serviceProperty.service).property(serviceProperty.property);
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

		public ServiceProperty build() {
			return new ServiceProperty(service, property);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final String service;
	private final String property;

	private ServiceProperty(String service, String property) {
		this.service = service;
		this.property = property;
	}

	public String getService() {
		return service;
	}

	public String getProperty() {
		return property;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(service, property);
	}

	@Override
	public final boolean equals(Object object) {
		if (object instanceof ServiceProperty) {
			ServiceProperty that = (ServiceProperty) object;

			return Objects.equal(this.service, that.service)&& Objects.equal(this.property, that.property);
		}

		return false;
	}

	@Override
	public String toString() {
		return service + '/' + property;
	}

}
