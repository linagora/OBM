/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2016  Linagora
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
package fr.aliacom.obm.common.mailshare;

import org.obm.sync.host.ObmHost;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomain;

public class SharedMailbox {
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		private Integer id;
		private ObmDomain domain;
		private String name;
		private Boolean archive;
		private Integer quota;
		private ObmHost server;
		private String delegation;
		private String description;
		private String email;

		private Builder() {
		}

		public Builder id(Integer id) {
			Preconditions.checkNotNull(id);
			this.id = id;
			return this;
		}

		public Builder domain(ObmDomain domain) {
			Preconditions.checkNotNull(domain);
			this.domain = domain;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder archive(boolean archive) {
			this.archive = archive;
			return this;
		}

		public Builder quota(int quota) {
			this.quota = quota;
			return this;
		}

		public Builder server(ObmHost server) {
			this.server = server;
			return this;
		}

		public Builder delegation(String delegation) {
			this.delegation = delegation;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public SharedMailbox build() {
			Preconditions.checkState(id != null);
			Preconditions.checkState(domain != null);

			archive = Objects.firstNonNull(archive, false);
			quota = Objects.firstNonNull(quota, 0);
			delegation = Objects.firstNonNull(delegation, "");

			return new SharedMailbox(id, domain, Optional.fromNullable(name), archive, quota,
					Optional.fromNullable(server), delegation, Optional.fromNullable(description), Optional.fromNullable(email));
		}
		
	}
	
	private final int id;
	private final ObmDomain domain;
	private final Optional<String> name;
	private final boolean archive;
	private final int quota;
	private final Optional<ObmHost> server;
	private final String delegation;
	private final Optional<String> description;
	private final Optional<String> email;

	@VisibleForTesting SharedMailbox(int id, ObmDomain domain, Optional<String> name, boolean archive, int quota,
			Optional<ObmHost> server, String delegation, Optional<String> description, Optional<String> email) {
		this.id = id;
		this.domain = domain;
		this.name = name;
		this.archive = archive;
		this.quota = quota;
		this.server = server;
		this.delegation = delegation;
		this.description = description;
		this.email = email;
	}
	
	public int getId() {
		return id;
	}

	public ObmDomain getDomain() {
		return domain;
	}

	public Optional<String> getName() {
		return name;
	}

	public boolean getArchive() {
		return archive;
	}

	public int getQuota() {
		return quota;
	}

	public Optional<ObmHost> getServer() {
		return server;
	}

	public String getDelegation() {
		return delegation;
	}

	public Optional<String> getDescription() {
		return description;
	}

	public Optional<String> getEmail() {
		return email;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(id, domain, name, archive, quota, server, delegation, description, email);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof SharedMailbox) {
			SharedMailbox that = (SharedMailbox) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.archive, that.archive)
				&& Objects.equal(this.quota, that.quota)
				&& Objects.equal(this.server, that.server)
				&& Objects.equal(this.delegation, that.delegation)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.email, that.email);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("description", description)
			.add("domain", domain)
			.add("name", name)
			.add("archive", archive)
			.add("quota", quota)
			.add("server", server)
			.add("delegation", delegation)
			.add("description", description)
			.add("email", email)
			.toString();
	}
}
