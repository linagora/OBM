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
package fr.aliacom.obm.common.domain;

import java.io.Serializable;
import java.util.Set;

import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ObmDomain implements Serializable {

	public static class Builder {
		
		private Integer id;
		private String name;
		private ObmDomainUuid uuid;
		private ImmutableSet.Builder<String> aliases;
		private String label;
		private boolean global;
		private ImmutableMultimap.Builder<ServiceProperty, ObmHost> hosts;
		
		private Builder() {
			aliases = ImmutableSet.builder();
			hosts = ImmutableMultimap.builder();
		}

		public Builder from(ObmDomain domain) {
			return name(domain.name)
				.label(domain.label)
				.uuid(domain.uuid)
				.aliases(domain.aliases)
				.hosts(domain.hosts)
				.global(domain.global);
		}
		
		public Builder id(int id) {
			this.id = id;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder uuid(ObmDomainUuid uuid) {
			this.uuid = uuid;
			return this;
		}
		
		public Builder aliases(Iterable<String> aliases) {
			Preconditions.checkNotNull(aliases);

			this.aliases.addAll(aliases);
			return this;
		}
		
		public Builder alias(String alias) {
			Preconditions.checkNotNull(alias);
			
			aliases.add(alias);
			return this;
		}
		
		public Builder label(String label) {
			this.label = label;
			return this;
		}

		public Builder host(ServiceProperty serviceProperty, ObmHost host) {
			this.hosts.put(serviceProperty, host);
			return this;
		}

		public Builder hosts(ServiceProperty serviceProperty, Iterable<ObmHost> hosts) {
			this.hosts.putAll(serviceProperty, hosts);
			return this;
		}

		public Builder hosts(Multimap<ServiceProperty, ObmHost> hosts) {
			this.hosts.putAll(hosts);
			return this;
		}
		
		public Builder global(boolean global) {
			this.global = global;
			return this;
		}

		public ObmDomain build() {
			return new ObmDomain(id, name, uuid, label, aliases.build(), hosts.build(), global);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	private final Integer id;
	private final String name;
	private final ObmDomainUuid uuid;
	private final Set<String> aliases;
	private final String label;
	private final Multimap<ServiceProperty, ObmHost> hosts;
	private final boolean global;

	private ObmDomain(Integer id, String name, ObmDomainUuid uuid, String label, Set<String> aliases, Multimap<ServiceProperty, ObmHost> hosts, boolean global) {
		this.id = id;
		this.name = name;
		this.uuid = uuid;
		this.label = label;
		this.aliases = aliases;
		this.hosts = hosts;
		this.global = global;
	}

	public boolean isGlobal() {
		return global;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public ObmDomainUuid getUuid() {
		return uuid;
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public Set<String> getNames() {
		return Sets.union(ImmutableSet.of(name), aliases);
	}
	
	public String getLabel() {
		return label;
	}

	public Multimap<ServiceProperty, ObmHost> getHosts() {
		return hosts;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(id, name, uuid, label, aliases, global);
	}

	@Override
	public final boolean equals(Object object) {
		if (object instanceof ObmDomain) {
			ObmDomain that = (ObmDomain) object;
			
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.label, that.label)
				&& Objects.equal(this.uuid, that.uuid)
				&& Objects.equal(this.aliases, that.aliases)
				&& Objects.equal(this.global, that.global);
		}
		
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("label", label)
			.add("aliases", aliases)
			.add("uuid", uuid)
			.add("hosts", hosts)
			.add("global", global)
			.toString();
	}

}
