/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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


package org.obm.sync.host;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ObmHost implements Serializable{

	private final Integer id;
	private final Integer domainId;
	private final String name;
	private final String fqdn;
	private final String ip;

	public static class Builder {
		
		private Integer id;
		private Integer domainId;
		private String name;
		private String fqdn;
		private String ip;
		
		private Builder() {
		}

		public Builder from(ObmHost host) {
			return id(host.id)
				.domainId(host.domainId)
				.name(host.name)
				.fqdn(host.fqdn)
				.ip(host.ip);
		}
		
		public Builder id(int id) {
			this.id = id;
			return this;
		}
		
		public Builder domainId(Integer domainId) {
			Preconditions.checkNotNull(domainId);
			this.domainId = domainId;
			return this;
		}
		
		public Builder name(String name) {
			Preconditions.checkNotNull(name);
			this.name = name;
			return this;
		}
		
		public Builder fqdn(String fqdn) {
			this.fqdn = fqdn;
			return this;
		}
		
		public Builder ip(String ip) {
			this.ip = ip;
			return this;
		}
		
		public ObmHost build() {
			return new ObmHost(id, domainId, name, fqdn, ip);
		}
		
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public ObmHost(Integer id, Integer domainId, String name, String fqdn, String ip) {
			this.id = id;
			this.domainId = domainId;
			this.name = name;
			this.fqdn = fqdn;
			this.ip = ip;
	}

	public Integer getId() {
		return id;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public String getName() {
		return name;
	}

	public String getFqdn() {
		return fqdn;
	}

	public String getIp() {
		return ip;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(id, domainId, name, fqdn, ip);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof ObmHost) {
			ObmHost that = (ObmHost) object;
			
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.domainId, that.domainId)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.fqdn, that.fqdn)
				&& Objects.equal(this.ip, that.ip);
			}
		
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("domainId", domainId)
			.add("name", name)
			.add("fqdn", fqdn)
			.add("ip", ip)
			.toString();
	}

}
