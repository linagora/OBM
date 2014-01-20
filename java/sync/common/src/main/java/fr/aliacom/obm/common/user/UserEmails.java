/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.user;

import java.util.List;

import org.obm.sync.host.ObmHost;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

public class UserEmails {

	public static class Builder {

		private ImmutableList.Builder<String> addresses;
		private ObmHost server;
		private Integer quota;
		private ObmDomain domain;

		private Builder() {
			addresses = ImmutableList.builder();
		}

		public Builder from(UserEmails other) {
			this.addresses.addAll(other.addresses);
			this.server = other.server;
			this.quota = other.quota;
			this.domain = other.domain;
			return this;
		}
		
		public Builder addAddress(String address) {
			Preconditions.checkNotNull(address);
			this.addresses.add(address);
			return this;
		}
		
		public Builder addresses(Iterable<String> addresses) {
			this.addresses.addAll(addresses);
			return this;
		}

		public Builder server(ObmHost server) {
			this.server = server;
			return this;
		}

		public Builder quota(Integer quota) {
			this.quota = quota;
			return this;
		}

		public Builder domain(ObmDomain domain) {
			this.domain = domain;
			return this;
		}
		
		public UserEmails build() {
			Preconditions.checkState(domain != null);
			return new UserEmails(addresses.build(), server, quota, domain);
		}

	}

	public static Builder builder() {
		return new Builder();
	}
	

	private final ImmutableList<String> addresses;
	private final ObmHost server;
	private final Integer quota;
	private final ObmDomain domain;

	private UserEmails(ImmutableList<String> addresses, ObmHost server, Integer quota, ObmDomain domain) {
		this.addresses = addresses;
		this.server = server;
		this.quota = quota;
		this.domain = domain;
	}

	public boolean isEmailAvailable() {
		return !addresses.isEmpty();
	}

	public String getFullyQualifiedPrimaryAddress() {
		return appendSuffixToEmailIfRequired(getPrimaryAddress(), domain.getName());
	}

	public Iterable<String> expandAllEmailDomainTuples() {
		return FluentIterable
				.from(Sets.cartesianProduct(
						ImmutableList.of(ImmutableSet.copyOf(addresses), domain.getNames())))
						.transform(new Function<List<String>, String>() {
							@Override
							public String apply(List<String> input) {
								return appendSuffixToEmailIfRequired(input.get(0), input.get(1));
							}
						})
						.toSet();
	}
	
	private String appendSuffixToEmailIfRequired(String emailAddress, String suffix) {
		if (emailAddress != null && !emailAddress.contains("@")) {
			return emailAddress + "@" + suffix;
		}
		return emailAddress;
	}

	
	public ImmutableList<String> getAddresses() {
		return addresses;
	}

	public String getPrimaryAddress() {
		return Iterables.getFirst(addresses, null);
	}
	
	public List<String> getAliases() {
		return addresses.size() > 1 ? addresses.subList(1, addresses.size()) : ImmutableList.<String>of();
	}
	
	public ObmHost getServer() {
		return server;
	}

	public Integer getQuota() {
		return quota;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(addresses, server, quota);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof UserEmails) {
			UserEmails that = (UserEmails) object;
			return Objects.equal(this.addresses, that.addresses)
					&& Objects.equal(this.server, that.server)
					&& Objects.equal(this.quota, that.quota);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("addresses", addresses)
				.add("server", server)
				.add("quota", quota)
				.toString();
	}

}
